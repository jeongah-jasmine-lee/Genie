package com.example.genie.utils;

import java.util.List;

public class SessionContext {
    private final String userCommand;
    private final String uiSnapshot;
    private final String executionHistory;
    private final String appName;
    private final String packageName;
    private final String activityName;
    private final String screenResolution;
    private final List<String> allowedActions;

    public SessionContext(String userCommand, String uiSnapshot, String executionHistory,
                          String appName, String packageName, String activityName,
                          String screenResolution, List<String> allowedActions) {
        this.userCommand = userCommand;
        this.uiSnapshot = uiSnapshot;
        this.executionHistory = executionHistory;
        this.appName = appName;
        this.packageName = packageName;
        this.activityName = activityName;
        this.screenResolution = screenResolution;
        this.allowedActions = allowedActions;
    }

    public String getUserCommand() { return userCommand; }
    public String getUiSnapshot() { return uiSnapshot; }
    public String getExecutionHistory() { return executionHistory; }
    public String getAppName() { return appName; }
    public String getPackageName() { return packageName; }
    public String getActivityName() { return activityName; }
    public String getScreenResolution() { return screenResolution; }
    public List<String> getAllowedActions() { return allowedActions; }
}
