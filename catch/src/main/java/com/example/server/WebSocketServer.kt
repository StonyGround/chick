package com.example.server

// An highlighted block
import android.util.Log
import com.blankj.utilcode.util.NetworkUtils
import org.greenrobot.eventbus.EventBus
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import java.net.InetSocketAddress


/**
 * @Author HBY
 * @Date 2021/5/28 15:20
 * 概述：
 */
class WebSocketServer internal constructor(host: InetSocketAddress?) : org.java_websocket.server.WebSocketServer(host) {
    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Log.d("WebSocketServer", "onOpen()：连接到: " + getRemoteSocketAddress(conn))
        EventBus.getDefault().post(MessageEvent( "onOpen：" + getRemoteSocketAddress(conn)))
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        Log.d("WebSocketServer", "onClose")
        EventBus.getDefault().post(MessageEvent( "onClose：$reason"))
    }

    override fun onMessage(conn: WebSocket, message: String) {
        Log.d("WebSocketServer", "onMessage$message")
        EventBus.getDefault().post(MessageEvent( getRemoteSocketAddress(conn).toString() + message))
    }

    override fun onError(conn: WebSocket, ex: Exception) {
        Log.d("WebSocketServer", "onError$ex")
        EventBus.getDefault().post(MessageEvent("onError：$ex"))
    }

    override fun onStart() {
        Log.d("WebSocketServer", "onStart:" + websocketServer!!.address)
        EventBus.getDefault().post(MessageEvent("onStart：服务器已就绪"))
    }

    companion object {
        private var websocketServer: WebSocketServer? = null
        fun ready(ip:String) {
            val myHost = InetSocketAddress(ip, 15211)
            val websocketServer = WebSocketServer(myHost)
            try {
                websocketServer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Companion.websocketServer = websocketServer
        }

        fun Stop() {
            try {
                websocketServer?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun Send(string: String?) {
            try {
                websocketServer?.broadcast(string)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

