package com.example.genie.executor;

import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class GPTActionExecutor {

    private static final String TAG = "GPTActionExecutor";

    public enum ActionType {
        TAP,
        ENTER_TEXT,
        SCROLL,
        OPEN_APP,
        UNKNOWN
    }

    /*
    public void executeAction(ActionType actionType, String target, AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Log.e(TAG, "Root node is null, cannot perform action.");
            return;
        }

        switch (actionType) {
            case TAP:
                clickNodeByText(rootNode, target);
                break;

            case ENTER_TEXT:
                enterText(rootNode, target);
                break;

            case SCROLL:
                // In actual usage, scrolling is done via gestures in AccessibilityService
                Log.d(TAG, "Scroll action requested: " + target);
                break;

            case OPEN_APP:
                Log.d(TAG, "Open app action requested: " + target);
                // Launch intent logic would be in a service or activity, not here
                break;

            default:
                Log.w(TAG, "Unknown action type. Skipping.");
        }
    }
     */

    public void executeAction(ActionType actionType, String target, AccessibilityNodeInfo rootNode) {
        Log.d(TAG, "===========ExecuteAction=========");
        if (rootNode == null) {
            Log.e(TAG, "Root node is null, cannot perform action.");
            return;
        }

        switch (actionType) {
            case TAP:
                // Find the node by matching text
                AccessibilityNodeInfo targetNode = findNodeByText(rootNode, target);
                if (targetNode != null) {
                    String targetInfo = "Text: " + (targetNode.getText() != null ? targetNode.getText().toString() : "null")
                            + ", ContentDesc: " + (targetNode.getContentDescription() != null ? targetNode.getContentDescription().toString() : "null")
                            + ", ViewId: " + (targetNode.getViewIdResourceName() != null ? targetNode.getViewIdResourceName() : "null");
                    Log.d(TAG, "TAP action: Found target node: " + targetInfo);
                    // Attempt to click the node
                    boolean result = targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (result) {
                        Log.d(TAG, "TAP action performed successfully.");
                    } else {
                        Log.e(TAG, "TAP action failed to perform on target node.");
                    }
                } else {
                    Log.e(TAG, "TAP action: Target node with text '" + target + "' not found.");
                }
                break;

            case ENTER_TEXT:
                enterText(rootNode, target);
                break;

            case SCROLL:
                Log.d(TAG, "Scroll action requested: " + target);
                break;

            case OPEN_APP:
                Log.d(TAG, "Open app action requested: " + target);
                break;

            default:
                Log.w(TAG, "Unknown action type. Skipping.");
        }
    }

    /**
     * Recursively searches the accessibility node tree for a node whose text equals (ignoring case)
     * the specified target text.
     */
    private AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo node, String targetText) {
        if (node == null) {
            return null;
        }

        if (node.getText() != null && node.getText().toString().equalsIgnoreCase(targetText)) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo result = findNodeByText(child, targetText);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static void clickNodeByText(AccessibilityNodeInfo rootNode, String text) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo node : nodes) {
            if (node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "Clicked on: " + text);
                return;
            }
        }
        Log.w(TAG, "Clickable node not found for: " + text);
    }

    private static void enterText(AccessibilityNodeInfo rootNode, String text) {
        traverseAndSetText(rootNode, text);
    }

    private static void traverseAndSetText(AccessibilityNodeInfo node, String text) {
        if (node == null) return;

        if (node.isEditable()) {
            Bundle args = new Bundle();
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
            Log.d(TAG, "Set text: " + text);
            return;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            traverseAndSetText(node.getChild(i), text);
        }
    }
}
