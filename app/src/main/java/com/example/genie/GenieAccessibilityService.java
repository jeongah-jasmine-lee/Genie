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
        // Obtain the current active window's root node from the system
        AccessibilityNodeInfo currentRoot = getRootInActiveWindow();
        if (currentRoot != null) {
            // Generate a detailed snapshot of the entire UI tree.
            // This recursive method traverses the node hierarchy and builds a string representation.
            String uiSnapshot = dumpAccessibilityTree(currentRoot, "");

            // Update the SessionContext with both the raw root node and the UI snapshot.
            SessionContext.getInstance().setRootNode(currentRoot);
            SessionContext.getInstance().setUiSnapshot(uiSnapshot);
            Log.d("AccessibilityService", "\uD83D\uDD25Root node updated from package: " +
                    (currentRoot.getPackageName() != null ? currentRoot.getPackageName().toString() : "null"));
        } else {
            Log.w("AccessibilityService", "\uD83D\uDD25Root node is null on event: " + event.getEventType());
        }
    }

    /**
     * Recursively traverses the accessibility node tree to build a string snapshot.
     * @param node The current AccessibilityNodeInfo.
     * @param indent The indentation string for formatting.
     * @return A string representing the UI hierarchy from the given node.
     */
    private String dumpAccessibilityTree(AccessibilityNodeInfo node, String indent) {
        if (node == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // Append basic information for the current node.
        sb.append(indent)
                .append("Class: ").append(node.getClassName())
                .append(", Text: ").append(node.getText())
                .append(", ContentDesc: ").append(node.getContentDescription())
                .append(", ViewId: ").append(node.getViewIdResourceName())
                .append("\n");
        // Recursively process each child node.
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            sb.append(dumpAccessibilityTree(child, indent + "  "));
        }
        return sb.toString();
    }

    @Override
    public void onInterrupt() {
        // Called when the system interrupts the accessibility feedback.
        Log.w(TAG, "Accessibility service was interrupted.");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Log service connection to verify that the accessibility service is active.
        Log.d(TAG, "Genie Accessibility Service connected and running.");
    }
}
