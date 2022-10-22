package com.example.client

import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.handshake.ServerHandshake
import java.net.URI


class WebSocketClient(serverUri: URI?) : org.java_websocket.client.WebSocketClient(serverUri) {

    override fun onOpen(handshakedata: ServerHandshake) {
        Log.d("WebSocketClient", "onOpen成功连接到：$remoteSocketAddress")
        EventBus.getDefault().post(MessageEvent(1, "onOpen：$remoteSocketAddress"))
    }

    override fun onMessage(message: String) {
        Log.d("WebSocketClient", "onMessage$message")
        EventBus.getDefault().post(MessageEvent(2, "$remoteSocketAddress：$message"))
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        Log.d("WebSocketClient", "onClose")
        EventBus.getDefault().post(MessageEvent(1, "onClose：$reason"))
    }

    override fun onError(ex: Exception) {
        Log.d("WebSocketClient", "onError")
        EventBus.getDefault().post(MessageEvent(1, "onError：$ex"))
    }

    companion object {
        private var webSocketClient: WebSocketClient? = null
        fun connect(ip: String): Boolean {
            if (webSocketClient != null) {
                Release()
            }
            if (webSocketClient == null) {
                val uri: URI = URI.create("ws://$ip:15211")
                webSocketClient = WebSocketClient(uri)
            }
            return try {
                webSocketClient!!.connectBlocking()
                true
            } catch (e: InterruptedException) {
                e.printStackTrace()
                false
            }
        }

        fun Release() {
            Close()
            webSocketClient = null
        }

        fun Close() {
            if (webSocketClient == null) return
            if (!webSocketClient!!.isOpen) return
            try {
                webSocketClient!!.closeBlocking()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        fun Send(string: String?) {
            if (webSocketClient == null) return
            if (!webSocketClient!!.isOpen) Reconnect()
            try {
                webSocketClient!!.send(string)
            } catch (e: WebsocketNotConnectedException) {
                e.printStackTrace()
            }
        }

        fun Reconnect(): Boolean {
            if (webSocketClient == null) return false
            return if (webSocketClient!!.isOpen) true else try {
                webSocketClient!!.reconnectBlocking()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}

