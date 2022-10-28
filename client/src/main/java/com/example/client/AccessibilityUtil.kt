package com.example.client

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityUtil {
    private const val TAG = "MyAccessibilityService"


    /**
     * 创建点击事件
     */
    fun AccessibilityService.createClick(
        node: AccessibilityNodeInfo,
        callback: AccessibilityService.GestureResultCallback? = null
    ) {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        Log.d(TAG, "createClick: $rect")
        createClick(rect.left + 10f, rect.top + 10f, callback)
    }

    fun AccessibilityService.createClick(
        x: Float,
        y: Float,
        callback: AccessibilityService.GestureResultCallback? = null
    ) {
        // for a single tap a duration of 1 ms is enough
        val clickPath = Path()
        clickPath.moveTo(x, y)
        val clickStroke = GestureDescription.StrokeDescription(clickPath, 0, 1L);
        val clickBuilder = GestureDescription.Builder()
        clickBuilder.addStroke(clickStroke)

        dispatchGesture(
            clickBuilder.build(),
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    Log.d(TAG, "click completed:($x,$y)")
                    callback?.onCompleted(gestureDescription)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    Log.d(TAG, "onCancelled: ")
                }
            },
            null
        )
    }

    /**
     * 创建滑动手势
     * [fromX] [fromY] 起点
     * [toX] [toY] 终点
     * [duration] 滑动时长 单位ms
     */
    fun AccessibilityService.createSlide(
        accessibilityService: AccessibilityService,
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        duration: Long
    ) {
        val path = Path()
        path.moveTo(fromX, fromY)
        path.lineTo(toX, toY)
        //手势操作300ms最佳，低于此值，拖动过程中手势会脱离按钮
        accessibilityService.dispatchGesture(
            GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration)).build(),
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    Log.d(TAG, "onCompleted:($fromX,$fromY)--($toX,$toY)")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    Log.d(TAG, "onCancelled: ")
                }
            },
            null
        )
    }
}