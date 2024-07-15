package com.takuchan

import androidx.compose.runtime.MutableState
import com.takuchan.helpme.AudioClassificationHelper
import com.takuchan.helpme.AudioClassificationListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.label.Category
class AudioDetect {

    private val _result = MutableStateFlow(emptyList<Category>())
    val result = _result.asStateFlow()

    @OptIn(DelicateCoroutinesApi::class)
    val audioClassificationListener = object : AudioClassificationListener {
        override fun onError(e: String) {
            GlobalScope.launch(Dispatchers.Main) {

            }
        }
        override fun onResult(result: List<Category>, interfaceTime: Long) {
            GlobalScope.launch(Dispatchers.Main) {
                _result.value = result
            }
        }

    }

}