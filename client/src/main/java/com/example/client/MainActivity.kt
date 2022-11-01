package com.example.client

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val EXAMPLE_COUNTER = stringPreferencesKey("example_counter")


class MainActivity : AppCompatActivity() {

    // At the top level of your kotlin file:

    lateinit var input: AppCompatEditText
    lateinit var btn: AppCompatButton
    lateinit var textView: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input = findViewById(R.id.input)
        btn = findViewById(R.id.btn)
        textView = findViewById(R.id.textView)

        runBlocking {
            val exampleCounterFlow = dataStore.data
                .map { preferences ->
                    preferences[EXAMPLE_COUNTER]
                }.first()
            exampleCounterFlow?.let {
                input.append(it)
            }
        }

        btn.setOnClickListener {
            val ip = input.text.toString()
            WebSocketClient.connect(ip)
            CoroutineScope(Dispatchers.IO).launch {
                dataStore.edit { settings ->
                    settings[EXAMPLE_COUNTER] = ip
                }
            }
        }

        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DataEvent) {
        textView.append(event.msg + "\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketClient.Release()
    }
}