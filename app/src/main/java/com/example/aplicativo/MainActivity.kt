package com.example.aplicativo

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.aplicativo.ui.theme.AplicativoTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AplicativoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        TextToSpeechScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
/*@Composable
fun GreetingPreview() {
    TalkingappTheme {
        Greeting("Android")
    }
}*/

@Composable
fun TextToSpeechScreen() {
    var isSpeaking by remember { mutableStateOf(false) }

    val tts1 = rememberTextToSpeech("US")
    val tts2 = rememberTextToSpeech("ES")

    val oracion = listOf(
        "Hola, ¿cómo estás?",
        "Bienvenido a nuestra aplicación.",
        "Espero que tengas un buen día.",
        "Esta es una prueba de texto a voz en español."
    )

    val sentences = listOf(
        "Hello, how are you?",
        "Welcome to our application.",
        "I hope you have a great day.",
        "This is a text to speech test in English."
    )

    Spacer(modifier = Modifier.height(45.dp))

    Column {
        Row {
            Column(modifier = Modifier.padding(70.dp)) {
                isSpeaking = false
                for (r in oracion) {
                    Button (onClick = {
                        if (tts2.value?.isSpeaking == true) {
                            tts2.value?.stop()
                            isSpeaking = false

                        } else {
                            tts2.value?.speak(
                                r, TextToSpeech.QUEUE_FLUSH, null, ""
                            )
                            isSpeaking = true
                        }
                    }
                    ) {
                        Text(r)
                    }
                }

            }
        }

        Row {
            //TTS EN INGLES
            Column(modifier = Modifier.padding(54.dp)) {
                isSpeaking = false
                for (sentence in sentences) {
                    Button(onClick = {
                        if (tts1.value?.isSpeaking == true) {
                            tts1.value?.stop()
                            isSpeaking = false

                        } else {
                            tts1.value?.speak(
                                sentence, TextToSpeech.QUEUE_FLUSH, null, ""
                            )
                            isSpeaking = true
                        }
                    }) {
                        Text(sentence)
                    } // End Button
                } // End for

            }
        }

    }

}

@Composable
fun rememberTextToSpeech(idioma: String): MutableState<TextToSpeech?> {
    val context = LocalContext.current
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect (context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                if (idioma != "ES")
                    tts.value?.language = Locale.US
                else
                    tts.value?.language = Locale.forLanguageTag(idioma)
            }
        }
        tts.value = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
    return tts
}