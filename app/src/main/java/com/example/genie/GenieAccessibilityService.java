package com.example.genie;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.genie.utils.SessionContext;

public class GenieAccessibilityService extends AccessibilityService {

    private static final String TAG = "GenieAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Obtain the current active window's root node
        AccessibilityNodeInfo currentRoot = getRootInActiveWindow();
        if (currentRoot != null) {
            // Update the singleton SessionContext with the new root node
            SessionContext.getInstance().setRootNode(currentRoot);
            Log.d("AccessibilityService", "Updated root node in SessionContext");
        } else {
            Log.w("AccessibilityService", "Root node is null on event: " + event.getEventType());
        }
    }

    @Override
    public void onInterrupt() {
        // Required override; triggered when system interrupts accessibility feedback
        Log.w(TAG, "Accessibility service was interrupted.");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Genie Accessibility Service connected and running.");
    }
}
