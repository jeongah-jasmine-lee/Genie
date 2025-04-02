package com.example.genie.conversation;

import android.util.Log;
import com.example.genie.utils.GPTApiClient;
import com.example.genie.utils.SessionContext;
import java.util.StringJoiner;
import android.view.accessibility.AccessibilityNodeInfo;
import com.example.genie.executor.GPTActionExecutor;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class ConversationalAgent {

    private static final String TAG = "ConversationalAgent";
    private final GPTApiClient gptApiClient;
    private final GPTActionExecutor executor = new GPTActionExecutor();

    public ConversationalAgent(GPTApiClient apiClient) {
        this.gptApiClient = apiClient;
    }

    public interface AgentCallback {
        void onDecision(ActionDecision decision);
        void onError(String error);
    }

    private GPTActionExecutor.ActionType parseActionTypeEnum(String actionTypeStr) {
        try {
            return GPTActionExecutor.ActionType.valueOf(actionTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid action type string: " + actionTypeStr);
            return GPTActionExecutor.ActionType.UNKNOWN;
        }
    }

    public void processUserCommand(SessionContext context, AgentCallback callback) {
        String step1Prompt = buildStep1Prompt(context);
        Log.d(TAG, "Step 1 Prompt:\n" + step1Prompt);

        gptApiClient.callGPT4API(step1Prompt, new GPTApiClient.GPTCallback() {
            @Override
            public void onSuccess(String step1Response) {
                String actionTypeStr = parseActionType(step1Response);
                GPTActionExecutor.ActionType parsedActionType = parseActionTypeEnum(actionTypeStr);

                String step2Prompt = buildStep2Prompt(context, parsedActionType.name());
                Log.d(TAG, "Step 2 Prompt:\n" + step2Prompt);

                gptApiClient.callGPT4API(step2Prompt, new GPTApiClient.GPTCallback() {
                    @Override
                    public void onSuccess(String step2Response) {
                        String targetElement = parseTargetElement(step2Response);
                        AccessibilityNodeInfo rootNode = context.getRootNode(); // You’ll need to add this getter
                        GPTActionExecutor executor = new GPTActionExecutor();
                        executor.executeAction(parsedActionType, targetElement, rootNode);
                        Log.d(TAG, "parsedActionType:\n" + parsedActionType);
                        callback.onDecision(new ActionDecision(parsedActionType.name(), targetElement));

                    }

                    @Override
                    public void onError(String error) {
                        callback.onError("Step 2 error: " + error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                callback.onError("Step 1 error: " + error);
            }
        });
    }

    // prompt builders & parsers (변경 없음)
    private String buildStep1Prompt(SessionContext ctx) {
        return String.format(
                "Task: Determine the appropriate action type.\n" +
                        "User said: \"%s\"\n" +
                        "UI Snapshot: %s\n" +
                        "Execution History: %s\n" +
                        "App Info: %s (%s - %s)\n" +
                        "Allowed Actions: %s\n" +
                        "Screen Resolution: %s\n\n" +
                        "Choose one action type from: [tap, scroll, enter_text, open_app]\n" +
                        "Explain your reasoning. Respond with: Action: <type>\nReason: <your logic>",
                ctx.getUserCommand(),
                ctx.getUiSnapshot(),
                ctx.getExecutionHistory(),
                ctx.getAppName(), ctx.getPackageName(), ctx.getActivityName(),
                String.join(", ", ctx.getAllowedActions()),
                ctx.getScreenResolution()
        );
    }

    private String buildStep2Prompt(SessionContext ctx, String actionType) {
        return String.format(
                "Task: Find the target element for action.\n" +
                        "Action Type: %s\n" +
                        "User said: \"%s\"\n" +
                        "UI Snapshot: %s\n" +
                        "Execution History: %s\n\n" +
                        "Respond with: Target: <element description>",
                actionType,
                ctx.getUserCommand(),
                ctx.getUiSnapshot(),
                ctx.getExecutionHistory()
        );
    }

    private String parseActionType(String gptResponse) {
        if (gptResponse.contains("Action:")) {
            return gptResponse.split("Action:")[1].split("\n")[0].trim();
        }
        return "unknown";
    }

    private String parseTargetElement(String gptResponse) {
        if (gptResponse.contains("Target:")) {
            return gptResponse.split("Target:")[1].split("\n")[0].trim();
        }
        return "unknown_element";
    }

    public static class ActionDecision {
        public final String actionType;
        public final String targetElement;

        public ActionDecision(String actionType, String targetElement) {
            this.actionType = actionType;
            this.targetElement = targetElement;
        }
    }

    // 🔧 Static run-through demo — no GPT required
    private void logAllClickableNodes(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }

        // If the node is clickable, log its details
        if (node.isClickable()) {
            String text = node.getText() != null ? node.getText().toString() : "null";
            String contentDesc = node.getContentDescription() != null ? node.getContentDescription().toString() : "null";
            String viewId = node.getViewIdResourceName() != null ? node.getViewIdResourceName() : "null";
            Log.d(TAG, "⭐Clickable Node: text=" + text
                    + ", contentDesc=" + contentDesc
                    + ", viewId=" + viewId);
        }

        // Recursively process each child node
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            logAllClickableNodes(child);
            // Note: Do not recycle the node here if it's managed by the framework.
        }
    }

    private void logNodeTree(AccessibilityNodeInfo node, String indent) {
        if (node == null) return;

        // Log details of the current node
        String details = String.format("⭐Class: %s, Text: %s, ContentDesc: %s, ViewId: %s",
                node.getClassName(),
                node.getText(),
                node.getContentDescription(),
                node.getViewIdResourceName());
        Log.d(TAG, indent + details);

        // Recursively log all children nodes
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            logNodeTree(child, indent + "  ");
        }
    }

    public void runDemo(SessionContext context, Context activityContext) {
        // Step 1: Open device Settings
        sleep();

        Log.d(TAG, "Demo Step 1: Open Device Settings");
        Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityContext.startActivity(settingsIntent);
        sleep();

        // ⭐ [TODO] Step 2: Get the accessibility root node (ensure your AccessibilityService is updating this)
        AccessibilityNodeInfo rootNode = context.getRootNode();
        if (rootNode == null) {
            Log.e(TAG, "Root node is null, cannot simulate further actions.");
            // Optionally, notify the caller that the root node is not available.
            return;
        }
        // Log all clickable UI targets for debugging
        Log.d(TAG, "Logging all clickable nodes in the UI:");
        logAllClickableNodes(rootNode);
        logNodeTree(rootNode, "");

        // Step 3: Simulate tapping "Display"
        Log.d(TAG, "Demo Step 2: Tap Display");
        executor.executeAction(GPTActionExecutor.ActionType.TAP, "Display & touch", rootNode);
        sleep();

        // Step 4: Simulate tapping "Font size"
        Log.d(TAG, "Demo Step 3: Tap Font size");
        executor.executeAction(GPTActionExecutor.ActionType.TAP, "Font size", rootNode);
        sleep();

        // Step 5: Simulate tapping the "+" on the slider
        Log.d(TAG, "Demo Step 4: Tap '+' to increase font size");
        executor.executeAction(GPTActionExecutor.ActionType.TAP, "+", rootNode);
        sleep();
    }


    private void sleep() {
        try {
            Thread.sleep(1000); // 1 second pause between steps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
