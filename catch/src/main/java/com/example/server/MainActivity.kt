package com.example.server

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.blankj.utilcode.util.GsonUtils
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
        findViewById<Button>(R.id.btn).setOnClickListener {
            val toJson =
                GsonUtils.toJson(MsgBean("豆粕2407", 1, "10000", "5"))
            WebSocketServer.Send(toJson)
        }


        val ip: String = NetworkUtils.getIpAddressByWifi()
        textView.text = buildString {
            append(getString(R.string.local_ip))
            append(ip)
            append("\n")
        }
        WebSocketServer.ready(ip)

        findViewById<AppCompatButton>(R.id.setting).setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

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