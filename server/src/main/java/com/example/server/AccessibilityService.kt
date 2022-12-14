package com.example.server

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.GsonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class AccessibilityService : AccessibilityService() {


    private val TAG: String = "AccessibilityService"

    private var name: String? = null
    private var direction: String? = null
    private var price: String? = null
    private var num: String? = null

    private var currentSize: Int = 0

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected: ")
        EventBus.getDefault().post(MessageEvent("无障碍开启成功"))
    }

    @SuppressLint("SwitchIntDef")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "onAccessibilityEvent $windows")
        if (event == null) return
        val nodeInfo: AccessibilityNodeInfo? = when {
            event.source != null -> event.source
            rootInActiveWindow != null -> rootInActiveWindow
            !windows.isNullOrEmpty() -> windows[0].root
            else -> null
        }

        if (nodeInfo == null) {
            Log.e(TAG, "nodeInfo is null")
            return
        }

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val nodeItemList = nodeInfo.findAccessibilityNodeInfosByViewId("com.shenhuaqihuo.pbmobile:id/rl_qh_cj_tab")
                if (!nodeItemList.isNullOrEmpty() && nodeItemList.size > currentSize) {
                    currentSize = nodeItemList.size
                    name = getLastText(nodeInfo, "com.shenhuaqihuo.pbmobile:id/pb_tv_qh_cjname")
                    direction = getLastText(nodeInfo, "com.shenhuaqihuo.pbmobile:id/pb_tv_qh_cj_fx")
                    price = getLastText(nodeInfo, "com.shenhuaqihuo.pbmobile:id/tv_qh_cj_price")
                    num = getLastText(nodeInfo, "com.shenhuaqihuo.pbmobile:id/pb_tv_qh_cj_shuliang")
                    Log.d(TAG, "onAccessibilityEvent: $name---$direction--$price--$num")
                    if (name?.isNotEmpty() == true && direction?.isNotEmpty() == true || price?.isNotEmpty() == true || num?.isNotEmpty() == true) {
                        val targetDirection = when (direction) {
                            "买开" -> {
                                1
                            }
                            "卖开" -> {
                                2
                            }
                            "买平" -> {
                                3
                            }
                            "卖平" -> {
                                4
                            }
                            else -> {
                                0
                            }
                        }
                        val toJson =
                            GsonUtils.toJson(MsgBean(name!!, targetDirection, price!!, num!!))
                        WebSocketServer.Send(toJson)
                    }
                }
                nodeInfo.recycle()
            }
        }
    }


}

private fun clickAction(nodeInfo: AccessibilityNodeInfo, s: String) {
    val nodeList =
        nodeInfo.findAccessibilityNodeInfosByViewId(s)
    if (!nodeList.isNullOrEmpty()) {
        nodeList[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
        nodeInfo.recycle()
    }
}

private fun getLastText(nodeInfo: AccessibilityNodeInfo, s: String): String? {
    val nodeList =
        nodeInfo.findAccessibilityNodeInfosByViewId(s)
    if (!nodeList.isNullOrEmpty()) {
        return nodeList[0].text.toString()
    }
    return null
}