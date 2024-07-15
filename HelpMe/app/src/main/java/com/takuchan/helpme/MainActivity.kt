package com.takuchan.helpme

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.takuchan.AudioDetect
import com.takuchan.helpme.ui.theme.HelpMeTheme
import kotlinx.coroutines.DelicateCoroutinesApi

class MainActivity : ComponentActivity() {
    private fun requestAudioPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO),
            200
        )
    }

    private lateinit var audioHelper: AudioClassificationHelper
    @OptIn(DelicateCoroutinesApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestAudioPermissions()
        setContent {
            val audioDetect = remember { AudioDetect() }
            val audioDetectListener by audioDetect.result.collectAsState()
            audioHelper = AudioClassificationHelper(
                context = this,
                listener = audioDetect.audioClassificationListener
            )
            HelpMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text(title.toString()) })
                    }
                    ) { innerPadding ->
                    // Title bar
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedObject { it ->
                            LogoImage(y = it)
                            Text("あなたの安全を見守ります。", modifier = Modifier
                                .padding(16.dp)
                                .offset(y = it.dp))
                        }
                        Button(onClick = {
                            audioHelper.startAudioClassification()
                            audioHelper.currentModel = AudioClassificationHelper.YAMNET_MODEL
                            audioHelper.initClassifier()
                        }) {
                            Text("音声検知を開始")
                        }
                        LazyColumn {
                            audioDetectListener.forEach() {
                                item {
                                    Text(text = it.label)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    override fun onResume(){
        super.onResume()

        if(::audioHelper.isInitialized){
            audioHelper.startAudioClassification()
        }
    }

    override fun onPause(){
        super.onPause()
        if (::audioHelper.isInitialized ) {
            audioHelper.stopAudioClassification()
        }
    }
}

@Composable
fun AnimatedObject(
    //unitの戻り
    yNumber: @Composable (Float) -> Unit
){
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = ""
    )
    yNumber(animatedValue)
}

@Composable
fun LogoImage(
    y:Float = 0f
) {
    val image = painterResource(id = R.drawable.logo) // Load the image
    Image(
        modifier = Modifier
            .size(width = 200.dp, height = 200.dp)
            .offset(y = y.dp),painter = image, contentDescription = "Logo")
}

