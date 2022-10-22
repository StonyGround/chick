package com.example.server

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.blankj.utilcode.util.NetworkUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)


        val ip: String = NetworkUtils.getIpAddressByWifi()
        textView.text = buildString {
            append(getString(R.string.local_ip))
            append(ip)
            append("\n")
        }
        WebSocketServer.ready(ip)

        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MessageEvent) {
        textView.append(event.msg + "\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketServer.Stop()
    }
}