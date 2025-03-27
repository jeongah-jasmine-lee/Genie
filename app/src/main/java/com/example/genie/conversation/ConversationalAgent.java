package com.example.genie.conversation;

import android.util.Log;
import com.example.genie.utils.GPTApiClient;
import com.example.genie.utils.SessionContext;
import java.util.StringJoiner;

public class ConversationalAgent {

    private static final String TAG = "ConversationalAgent";
    private final GPTApiClient gptApiClient;

    public ConversationalAgent(GPTApiClient apiClient) {
        this.gptApiClient = apiClient;
    }

    public interface AgentCallback {
        void onDecision(ActionDecision decision);
        void onError(String error);
    }

    public void processUserCommand(SessionContext context, AgentCallback callback) {
        String step1Prompt = buildStep1Prompt(context);
        Log.d(TAG, "Step 1 Prompt:\n" + step1Prompt);

        gptApiClient.callGPT4API(step1Prompt, new GPTApiClient.GPTCallback() {
            @Override
            public void onSuccess(String step1Response) {
                String actionType = parseActionType(step1Response);

                String step2Prompt = buildStep2Prompt(context, actionType);
                Log.d(TAG, "Step 2 Prompt:\n" + step2Prompt);

                gptApiClient.callGPT4API(step2Prompt, new GPTApiClient.GPTCallback() {
                    @Override
                    public void onSuccess(String step2Response) {
                        String targetElement = parseTargetElement(step2Response);
                        callback.onDecision(new ActionDecision(actionType, targetElement));
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
}
