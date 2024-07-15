package com.takuchan.helpme

import android.content.Context
import android.media.AudioRecord
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.core.BaseOptions
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AudioClassificationHelper(
    val context: Context,
    val listener: AudioClassificationListener,
    var currentModel: String = YAMNET_MODEL,
    val classificationThreshold: Float = DISPLAY_THRESHOLD,
    val overlap: Float = DEFAULT_OVERLAP_VALUE,
    val numOfResults: Int = DEFAULT_NUM_OF_RESULTS,
    val currentDelegate: Int = 0,
    var numThreads: Int = 2
) {
    private lateinit var classifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio
    private lateinit var recorder: AudioRecord
    private lateinit var executor: ScheduledThreadPoolExecutor

    private val classifyRunnable = Runnable {
        classifyAudio()
    }
    init {
        initClassifier()
    }

    fun initClassifier(){
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(numThreads)
        when(currentDelegate){
            DELEGATE_CPU -> {
                // Default
            }
            DELEGATE_NNAPI -> {
                baseOptionsBuilder.useNnapi()
            }
        }

        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setScoreThreshold(classificationThreshold)
            .setMaxResults(numOfResults)
            .setBaseOptions(baseOptionsBuilder.build())
            .build()

        try {
            classifier = AudioClassifier.createFromFileAndOptions(
                context,
                currentModel,
                options
            )
            tensorAudio = classifier.createInputTensorAudio()
            recorder = classifier.createAudioRecord()
            startAudioClassification()
        } catch (e: Exception) {
            Log.e("AudioClassificationHelper", "Error creating audio classifier", e)
        }
    }
    fun startAudioClassification() {
        if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            return
        }
        recorder.startRecording()
        executor = ScheduledThreadPoolExecutor(1)

        val lengthInMilliSeconds =
            ((classifier.requiredInputBufferSize * 1.0f) / classifier.requiredTensorAudioFormat.sampleRate) * 1000

        executor.scheduleAtFixedRate(
            classifyRunnable,
            0,
            lengthInMilliSeconds.toLong(),
            TimeUnit.MILLISECONDS
        )
    }

        fun classifyAudio(){
            tensorAudio.load(recorder)
            var interfaceTime = SystemClock.uptimeMillis()
            val output = classifier.classify(tensorAudio)
            interfaceTime = SystemClock.uptimeMillis() - interfaceTime
            listener.onResult(output[0].categories, interfaceTime)
        }

        fun stopAudioClassification(){
            recorder.stop()
            executor.shutdownNow()
        }
    companion object{
        const val DELEGATE_CPU = 0
        const val DELEGATE_NNAPI = 1
        const val YAMNET_MODEL = "yamnet.tflite"
        const val DISPLAY_THRESHOLD = 0.3f
        const val DEFAULT_OVERLAP_VALUE = 0.5f
        const val DEFAULT_NUM_OF_RESULTS = 2
    }
}
