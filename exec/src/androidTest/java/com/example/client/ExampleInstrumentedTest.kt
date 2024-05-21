package com.example.client

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.promeg.pinyinhelper.Pinyin
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val toPinyin = Pinyin.toPinyin("豆粕2391", "").flatMap {
            listOf(it)
        }
        Log.d("TAG", "useAppContext: $toPinyin")
    }
}