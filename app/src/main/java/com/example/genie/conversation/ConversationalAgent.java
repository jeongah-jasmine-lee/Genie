package com.example.genie.conversation;

import android.util.Log;
import com.example.genie.utils.GPTApiClient;
import com.example.genie.utils.SessionContext;
import android.view.accessibility.AccessibilityNodeInfo;
import com.example.genie.executor.GPTActionExecutor;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityNodeInfo;
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

    // Parses the action type from GPT response and converts it to an enum.
    private GPTActionExecutor.ActionType parseActionTypeEnum(String actionTypeStr) {
        try {
            return GPTActionExecutor.ActionType.valueOf(actionTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Invalid action type string: " + actionTypeStr);
            return GPTActionExecutor.ActionType.UNKNOWN;
        }
    }

    // Processes the user command using GPT API (two-step process).
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
                        AccessibilityNodeInfo rootNode = context.getRootNode(); // Retrieved from SessionContext.
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

    // Builds the prompt for step 1 using enriched UI snapshot data.
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

    // Builds the prompt for step 2 using the selected action type.
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

    // Helper method to parse action type from GPT response.
    private String parseActionType(String gptResponse) {
        if (gptResponse.contains("Action:")) {
            return gptResponse.split("Action:")[1].split("\n")[0].trim();
        }
        return "unknown";
    }

    // Helper method to parse target element from GPT response.
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

    /**
     * Logs all clickable nodes in the UI recursively.
     * This is useful for debugging which UI elements can be interacted with.
     */
    private void logAllClickableNodes(AccessibilityNodeInfo node) {
        if (node == null) {
            return;
        }
        if (node.isClickable()) {
            String text = node.getText() != null ? node.getText().toString() : "null";
            String contentDesc = node.getContentDescription() != null ? node.getContentDescription().toString() : "null";
            String viewId = node.getViewIdResourceName() != null ? node.getViewIdResourceName() : "null";
            Log.d(TAG, "⭐Clickable Node: text=" + text
                    + ", contentDesc=" + contentDesc
                    + ", viewId=" + viewId);
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            logAllClickableNodes(child);
        }
    }

    /**
     * Recursively logs the entire UI node tree (including non-clickable nodes).
     * This method helps you see the complete UI structure.
     */
    private void logNodeTree(AccessibilityNodeInfo node, String indent) {
        if (node == null) return;
        String details = String.format("⭐Class: %s, Text: %s, ContentDesc: %s, ViewId: %s",
                node.getClassName(),
                node.getText(),
                node.getContentDescription(),
                node.getViewIdResourceName());
        Log.d(TAG, indent + details);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            logNodeTree(child, indent + "  ");
        }
    }

    private void logRootNodeDetails(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return;
        String packageName = (rootNode.getPackageName() != null) ? rootNode.getPackageName().toString() : "null";
        // getWindowId() is available from API 21 onward.
        int windowId = rootNode.getWindowId();
        Log.d(TAG, "Root Node Details: Package = " + packageName + ", Window ID = " + windowId);
    }

    /**
     * Demo method that opens YouTube and then logs the enhanced UI snapshot.
     * This demonstrates how the enriched UI data (raw node and snapshot) can be used.

     public void runDemo(SessionContext context, Context activityContext) {
     // Pause to allow any UI changes to take effect
     sleep();
     Log.d(TAG, "Demo Step 1: Open YouTube");

     // Try to launch the YouTube app; if not available, open in a browser.
     Intent launchIntent = activityContext.getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
     if (launchIntent != null) {
     launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     activityContext.startActivity(launchIntent);
     Log.d(TAG, "YouTube app launched.");
     } else {
     Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"));
     webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     activityContext.startActivity(webIntent);
     Log.d(TAG, "YouTube website opened in browser.");
     }

     // Retrieve the raw root node from SessionContext.
     AccessibilityNodeInfo rootNode = context.getRootNode();
     if (rootNode == null) {
     Log.e(TAG, "Root node is null, cannot simulate further actions.");
     return;
     }

     // Log the enriched UI snapshot (full hierarchy) stored in SessionContext.
     Log.d(TAG, "⭐ UI Snapshot from SessionContext:");
     Log.d(TAG, context.getUiSnapshot());

     // For additional debugging, log clickable nodes and the full node tree.
     Log.d(TAG, "⭐ Logging all clickable nodes in the UI:");
     logAllClickableNodes(rootNode);
     logNodeTree(rootNode, "");
     }
     */

    private AccessibilityNodeInfo findNodeByTextPartial(AccessibilityNodeInfo node, String targetText) {
        if (node == null) return null;

        if (node.getText() != null &&
                node.getText().toString().toLowerCase().contains(targetText.toLowerCase())) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo result = findNodeByTextPartial(child, targetText);
            if (result != null) return result;
        }
        return null;
    }

    private AccessibilityNodeInfo findClickableParent(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = node;
        while (parent != null) {
            if (parent.isClickable()) return parent;
            parent = parent.getParent();
        }
        return null;
    }

    public void runDemo(SessionContext context, Context activityContext) {
        // Step 1: Open device Settings
        Log.d(TAG, "Demo Step 1: Open Device Settings");
        Intent settingsIntent = new Intent(Settings.ACTION_SETTINGS);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activityContext.startActivity(settingsIntent);

        // Wait for the Settings window to become active.
        waitForSettingsWindow(new OnWindowReadyListener() {
            @Override
            public void onWindowReady(AccessibilityNodeInfo rootNode) {
                if (rootNode == null) {
                    Log.e(TAG, "Failed to detect Settings UI");
                    return;
                }

                // Log UI details
                Log.d(TAG, "Settings UI is active. Logging UI nodes:");
                logAllClickableNodes(rootNode);
                logNodeTree(rootNode, "");
                logRootNodeDetails(rootNode);

                // Try to tap using fallback logic
                Log.d(TAG, "Demo Step 2: Tap Display");

                AccessibilityNodeInfo displayNode = findNodeByTextPartial(rootNode, "Display");
                if (displayNode != null) {
                    AccessibilityNodeInfo targetToClick = findClickableParent(displayNode);
                    if (targetToClick != null) {
                        boolean clicked = targetToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        if (clicked) {
                            Log.d(TAG, "Successfully tapped on Display.");
                        } else {
                            Log.e(TAG, "Failed to click on Display node.");
                        }
                    } else {
                        Log.e(TAG, "No clickable parent found for Display.");
                    }
                } else {
                    Log.e(TAG, "Could not find any node containing 'Display'.");
                }

                Log.d(TAG, "Demo Step 3: Tap Font size");
                executor.executeAction(GPTActionExecutor.ActionType.TAP, "Font size", rootNode);

                Log.d(TAG, "Demo Step 4: Tap '+' to increase font size");
                executor.executeAction(GPTActionExecutor.ActionType.TAP, "+", rootNode);
            }
        });
    }


    /*private void sleep() {
        try {
            Log.d(TAG, "Sleeping start");
            Thread.sleep(5000); // 1 second pause between steps
            Log.d(TAG, "Sleeping end");
        } catch (InterruptedException e) {
            Log.d(TAG, "Error sleeping");
            Thread.currentThread().interrupt();
        }
    }*/
    // Define a callback interface for when the desired window is detected.
    public interface OnWindowReadyListener {
        void onWindowReady(AccessibilityNodeInfo rootNode);
    }

    // Method to wait until the Settings UI is active or timeout after 10 seconds.
    public void waitForSettingsWindow(final OnWindowReadyListener listener) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final long startTime = System.currentTimeMillis();
        final long timeout = 10000; // 10 seconds timeout

        Runnable checkRunnable = new Runnable() {
            @Override
            public void run() {
                AccessibilityNodeInfo rootNode = SessionContext.getInstance().getRootNode();
                if (rootNode != null && rootNode.getPackageName() != null &&
                        rootNode.getPackageName().toString().equals("com.android.settings")) {
                    Log.d(TAG, "Settings UI detected with package: " + rootNode.getPackageName());
                    listener.onWindowReady(rootNode);
                } else if (System.currentTimeMillis() - startTime < timeout) {
                    // Re-check after a delay (e.g., 500ms)
                    handler.postDelayed(this, 500);
                } else {
                    Log.e(TAG, "Timeout waiting for Settings UI");
                    listener.onWindowReady(null);
                }
            }
        };
        handler.post(checkRunnable);
    }
}
