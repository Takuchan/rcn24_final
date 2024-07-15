package com.takuchan.helpme
import org.tensorflow.lite.support.label.Category
interface AudioClassificationListener {
    fun onError(e: String)
    fun onResult(result: List<Category>,interfaceTime: Long)
}