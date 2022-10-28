package com.example.client

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.client.AccessibilityUtil.createClick
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class AccessibilityService : AccessibilityService() {


    private var nameNode: MutableList<AccessibilityNodeInfo>? = null
    private val TAG: String = "AccessibilityService"

    private var name: String? = null
    private var direction: String? = null
    private var price: String? = null
    private var num: String? = null
    private var nodeInfo: AccessibilityNodeInfo? = null

    private var currentSize: Int = 0

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected: ")
        EventBus.getDefault().post(MessageEvent("无障碍开启成功"))
        EventBus.getDefault().register(this)
        Handler(Looper.myLooper()!!).postDelayed(kotlinx.coroutines.Runnable {
            EventBus.getDefault().post(DataEvent("无障碍开启成功"))
        }, 10000)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DataEvent) {
        try {
//            val msg = GsonUtils.fromJson(event.msg, MsgBean::class.java)
            val msg = MsgBean("豆粕2301", "买开", "4033", "1")
            Log.d(TAG, "onEvent: $msg")
            if (!nameNode.isNullOrEmpty()) {
//                for (node in nameNode!!) {
//                    if (node.text.equals(msg.name)) {
//                        Log.d(TAG, "z============: ")
//                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                    }
//                }
//                setTextArgument(nameNode!![0], msg.name)
                // 点击输入框出现键盘
                createClick(nameNode!![0], object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)

                    }
                })
            }

//            val priceNode = nodeInfo!!.findAccessibilityNodeInfosByViewId("com.shenhuaqihuo.pbmobile:id/edit_price")
//            if (!priceNode.isNullOrEmpty()) {
//                setTextArgument(priceNode[0], msg.price)
//            }
//
//            val numNode = nodeInfo!!.findAccessibilityNodeInfosByViewId("com.shenhuaqihuo.pbmobile:id/edit_quantity")
//            if (!numNode.isNullOrEmpty()) {
//                setTextArgument(numNode[0], msg.num)
//            }
        } catch (e: Exception) {
            Log.e(TAG, "fromJson: " + e.message)
            EventBus.getDefault().post(MessageEvent("错误：" + e.message))
        }
    }


    @SuppressLint("SwitchIntDef")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        if (event == null) return
        nodeInfo = when {
            event.source != null -> event.source
            rootInActiveWindow != null -> rootInActiveWindow
            !windows.isNullOrEmpty() -> windows[0].root
            else -> null
        }

        if (nodeInfo == null) {
            Log.e(TAG, "nodeInfo is null")
            return
        }
//        if (this.nameNode.isNullOrEmpty()) {
        val nameNode =
            nodeInfo!!.findAccessibilityNodeInfosByViewId("com.shenhuaqihuo.pbmobile:id/tv_contract_name")
//                nodeInfo!!.findAccessibilityNodeInfosByViewId("com.hexin.android.futures:id/tv_type")
//            if (!nameNode.isNullOrEmpty()) {
        Log.d(TAG, "onAccessibilityEvent: $nameNode")
        this.nameNode = nameNode
//            }
//        }
        //                clickAction(nodeInfo, "com.shenhuaqihuo.pbmobile:id/rl_btn_buy")
//                clickAction(nodeInfo, "com.shenhuaqihuo.pbmobile:id/rl_btn_sell")
//                clickAction(nodeInfo, "com.shenhuaqihuo.pbmobile:id/btn_pos")
        // com.shenhuaqihuo.pbmobile:id/edit_price
        // com.shenhuaqihuo.pbmobile:id/edit_quantity
        // com.shenhuaqihuo.pbmobile:id/tv_contract_name


    }

    private fun clickAction(nodeInfo: AccessibilityNodeInfo, s: String) {
        val nodeList =
            nodeInfo.findAccessibilityNodeInfosByViewId(s)
        if (!nodeList.isNullOrEmpty()) {
            nodeList[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            nodeInfo.recycle()
        }
    }

    // 设置文本输入
    private fun setTextArgument(node: AccessibilityNodeInfo, text: String?) {
        val arguments = Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }
}