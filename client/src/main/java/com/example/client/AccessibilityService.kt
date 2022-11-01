package com.example.client

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ScreenUtils
import com.example.client.AccessibilityUtil.createClick
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class AccessibilityService : AccessibilityService() {


    private var nameNode: MutableList<AccessibilityNodeInfo>? = null
    private val TAG: String = "AccessibilityService"

    private var targetDirection: Int? = 0
    private var price: String? = null
    private var num: String? = null
    private var nodeInfo: AccessibilityNodeInfo? = null

    private var currentSize: Int = 0

    private val map = mutableMapOf<String, Coordinate>()
    private val numLine = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    private val q_p = arrayOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
    private val a_l = arrayOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
    private val z_m = arrayOf("Z", "X", "C", "V", "B", "N", "M")

    //按键间距
    private val btnMarginY = ConvertUtils.dp2px(10f)
    private val btnMarginX = ConvertUtils.dp2px(6f)

    // 按键高
    private val btnHeight = ConvertUtils.dp2px(40f)

    // 按键宽
    private val btnWidth = ConvertUtils.dp2px(31f)
    private val btnNumWidth = ConvertUtils.dp2px(30f)

    // shift按键宽
    private val btnShiftWidth = ConvertUtils.dp2px(40f)

    // 键盘底部空白高
    private val keyboardBottomHeight = ConvertUtils.dp2px(20f)

    // 当前高度
    private var currentY = ScreenUtils.getScreenHeight().toFloat()
    private var currentX = 0f

    private var msg: MsgBean? = null
    private var isFindResult: Boolean = false

    private var isRunning: Boolean = false

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "onServiceConnected: ")
        EventBus.getDefault().post(MessageEvent("脚本开启成功"))
        EventBus.getDefault().register(this)
//        Handler(Looper.myLooper()!!).postDelayed(kotlinx.coroutines.Runnable {
//            EventBus.getDefault()
//                .post(DataEvent("{\"direction\":1,\"name\":\"豆粕2301\",\"num\":\"5\",\"price\":\"4033\"}"))
//        }, 10000)
        initCoordinate()
    }


    /**
     *  按键高120px 宽99px x间距15px y间距30px
     */
    private fun initCoordinate() {

        // 定位到z键
        // - 键盘底部 - 快搜行 - 1个y边距 - 一半高
        currentY =
            currentY - keyboardBottomHeight - btnHeight - btnMarginY - btnHeight / 2
        currentX += btnMarginX / 2 + btnShiftWidth + btnMarginX + btnWidth / 2
        // 最底部
        for (value in z_m) {
            map[value] = Coordinate(currentX, currentY)
            currentX += btnWidth + btnMarginX
        }

        // 重新定位到a按键
        currentX = 0f
        currentX += btnMarginX * 2 + btnWidth / 2
        currentY = currentY - btnMarginY - btnHeight
        for (value in a_l) {
            map[value] = Coordinate(currentX, currentY)
            currentX += btnWidth + btnMarginX
        }

        // 重新定位到q按键
        currentX = 0f
        currentX += btnMarginX / 2 + btnNumWidth / 2
        currentY = currentY - btnMarginY - btnHeight
        for (value in q_p) {
            map[value] = Coordinate(currentX, currentY)
            currentX += btnNumWidth + btnMarginX
        }

        // 重新定位到1按键
        currentX = 0f
        currentX += btnMarginX / 2 + btnNumWidth / 2
        currentY = currentY - btnMarginY - btnHeight
        for (value in numLine) {
            map[value] = Coordinate(currentX, currentY)
            currentX += btnNumWidth + btnMarginX
        }
        Log.d(TAG, "initCoordinate: $map")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DataEvent) {
        isRunning = true
        isFindResult = false
        try {
            msg = GsonUtils.fromJson(event.msg, MsgBean::class.java)
            Log.d(TAG, "onEvent: $msg---$this")
            if (!nameNode.isNullOrEmpty()) {

                CoroutineScope(Dispatchers.IO).launch {
                    // 点击输入框出现键盘
                    createClick(nameNode!![0], object : GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                        }
                    })
                    delay(300)

                    // 点击拼音
                    val split = Pinyin.toPinyin(msg!!.name, "").flatMap { listOf(it.toString()) }
                    for (item in split) {
                        if (!isFindResult) {
                            map[item]?.let { createClick(it.x, map[item]!!.y) }
                            delay(200)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            msg = null
            isRunning = false
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
//        Log.d(TAG, "onAccessibilityEvent: $nameNode")

        // 合约代码输入框
        if (this.nameNode.isNullOrEmpty()) {
            this.nameNode =
                nodeInfo!!.findAccessibilityNodeInfosByViewId("com.shenhuaqihuo.pbmobile:id/tv_contract_name")
        }
        // 搜索结果
        if (msg != null) {
            val searchNameNode =
                nodeInfo!!.findAccessibilityNodeInfosByViewId("com.shenhuaqihuo.pbmobile:id/pb_qh_contract_name_search_name")
            if (!searchNameNode.isNullOrEmpty()) {
                for (node in searchNameNode) {
                    if (node.text.contains(msg!!.name)) {
                        Log.d(TAG, "onAccessibilityEvent: ${node.text}")
                        isFindResult = true

                        //点击搜索结果
                        createClick(node)

                        Thread.sleep(200)
                        // 输入价格
                        Log.d(TAG, "onAccessibilityEvent msg: $msg")
                        for (i in 1 until msg!!.num.toInt()) {
                            clickAction(
                                nodeInfo!!,
                                "com.shenhuaqihuo.pbmobile:id/iv_add_quantity",
                            )
                            Thread.sleep(100)
                        }

                        when (msg!!.direction) {
                            1 -> {
                                clickAction(
                                    nodeInfo!!,
                                    "com.shenhuaqihuo.pbmobile:id/rb_open_pos"
                                )
                                Thread.sleep(200)
                                clickAction(
                                    nodeInfo!!,
                                    "com.shenhuaqihuo.pbmobile:id/rl_btn_sell"
                                )
                            }
                            2 -> {
                                clickAction(
                                    nodeInfo!!,
                                    "com.shenhuaqihuo.pbmobile:id/rb_open_pos"
                                )
                                Thread.sleep(200)
                                clickAction(
                                    nodeInfo!!,
                                    "com.shenhuaqihuo.pbmobile:id/rl_btn_buy"
                                )
                            }
                            3 -> {
                                clickAction(
                                    nodeInfo!!,
                                    "com.shenhuaqihuo.pbmobile:id/rb_close_pos"
                                )
                                Thread.sleep(200)
                                clickAction(
                                    nodeInfo!!,
                                    "com.shenhuaqihuo.pbmobile:id/rl_btn_sell"
                                )
                            }
                            4 -> {
                                clickAction(
                                    nodeInfo!!,
                                    "com.shenhuaqihuo.pbmobile:id/rb_close_pos"
                                )
                                Thread.sleep(200)
                                clickAction(
                                    nodeInfo!!,
                                    "com.shenhuaqihuo.pbmobile:id/rl_btn_buy"
                                )
                            }
                        }
                        Thread.sleep(200)
//                        clickAction(nodeInfo!!, "com.shenhuaqihuo.pbmobile:id/btn_pos")
                        break
                    }
                }
            }
        }
    }

    // com.shenhuaqihuo.pbmobile:id/iv_reduce_quantity
    // com.shenhuaqihuo.pbmobile:id/iv_add_quantity
    // com.shenhuaqihuo.pbmobile:id/rb_open_pos
    // com.shenhuaqihuo.pbmobile:id/rb_close_pos
    //com.shenhuaqihuo.pbmobile:id/pb_qh_contract_name_search_name
    //                clickAction(nodeInfo, "com.shenhuaqihuo.pbmobile:id/rl_btn_buy")
//                clickAction(nodeInfo, "com.shenhuaqihuo.pbmobile:id/rl_btn_sell")
//                clickAction(nodeInfo, "com.shenhuaqihuo.pbmobile:id/btn_pos")
    // com.shenhuaqihuo.pbmobile:id/edit_price
    // com.shenhuaqihuo.pbmobile:id/edit_quantity
    // com.shenhuaqihuo.pbmobile:id/tv_contract_name

    private fun clickAction(nodeInfo: AccessibilityNodeInfo, s: String) {
        val nodeList =
            nodeInfo.findAccessibilityNodeInfosByViewId(s)
        if (!nodeList.isNullOrEmpty()) {
            nodeList[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
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