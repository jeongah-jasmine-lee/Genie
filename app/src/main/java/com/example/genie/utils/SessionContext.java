package com.example.genie.utils;

import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class SessionContext {

    private static SessionContext instance;

    private String userCommand;
    private String uiSnapshot; // Holds the rich UI snapshot as a string (e.g., XML or text tree)
    private String executionHistory;
    private String appName;
    private String packageName;
    private String activityName;
    private String screenResolution;
    private List<String> allowedActions;
    private AccessibilityNodeInfo rootNode; // Stores the raw accessibility node representing the current UI

    private static final String TAG = "SessionContext";

    // Private constructor (singleton pattern)
    private SessionContext() {
        this.userCommand = "";
        this.uiSnapshot = "";
        this.executionHistory = "";
        this.appName = "";
        this.packageName = "";
        this.activityName = "";
        this.screenResolution = "";
        this.allowedActions = new ArrayList<>();
    }

    /**
     * Returns the singleton instance of SessionContext.
     */
    public static synchronized SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
            Log.d(TAG, "SessionContext instance created");
        }
        return instance;
    }

    // Setters and getters for all session fields.

    public void setUserCommand(String userCommand) { this.userCommand = userCommand; }
    public String getUserCommand() { return userCommand; }

    public void setUiSnapshot(String uiSnapshot) { this.uiSnapshot = uiSnapshot; }
    public String getUiSnapshot() { return uiSnapshot; }

    public void setExecutionHistory(String executionHistory) { this.executionHistory = executionHistory; }
    public String getExecutionHistory() { return executionHistory; }

    public void setAppName(String appName) { this.appName = appName; }
    public String getAppName() { return appName; }

    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getPackageName() { return packageName; }

    public void setActivityName(String activityName) { this.activityName = activityName; }
    public String getActivityName() { return activityName; }

    public void setScreenResolution(String screenResolution) { this.screenResolution = screenResolution; }
    public String getScreenResolution() { return screenResolution; }

    public void setAllowedActions(List<String> allowedActions) { this.allowedActions = allowedActions; }
    public List<String> getAllowedActions() { return allowedActions; }

    /**
     * Updates the current raw accessibility root node.
     */
    public synchronized void setRootNode(AccessibilityNodeInfo rootNode) {
        this.rootNode = rootNode;
        Log.d(TAG, "Root node updated in SessionContext");
    }

    /**
     * Retrieves the current raw accessibility root node.
     */
    public synchronized AccessibilityNodeInfo getRootNode() {
        return this.rootNode;
    }
}
