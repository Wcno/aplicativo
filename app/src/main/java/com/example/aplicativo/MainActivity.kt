package com.example.aplicativo

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ModernTTSUI()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTTSUI() {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("Español") }
    var selectedAccent by remember { mutableStateOf("") }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var pitch by remember { mutableFloatStateOf(1.0f) }
    var volume by remember { mutableFloatStateOf(1.0f) }

    val allVoices = remember { mutableStateListOf<Voice>() }
    var accentOptions by remember { mutableStateOf(listOf<String>()) }
    var ttsReady by remember { mutableStateOf(false) }

    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Text to Speech", style = MaterialTheme.typography.headlineSmall)

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ingresa tu texto:", fontSize = 14.sp)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 4
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        if (!ttsReady || text.isBlank()) return@IconButton

                        val selectedVoice = allVoices.firstOrNull {
                            "${it.locale.displayCountry} - ${it.name}" == selectedAccent
                        }

                        selectedVoice?.let { tts.voice = it }
                        tts.setSpeechRate(speed)
                        tts.setPitch(pitch)
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    }, modifier = Modifier
                        .background(Color(0xFFFFC107), CircleShape)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    }

                    IconButton(onClick = { text = "" }, modifier = Modifier
                        .background(Color(0xFFFF4081), CircleShape)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val languageOptions = listOf("Español", "Inglés", "Francés", "Mandarín")
                var langExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(expanded = langExpanded, onExpandedChange = { langExpanded = !langExpanded }) {
                    OutlinedTextField(
                        value = selectedLanguage,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Idioma") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = langExpanded, onDismissRequest = { langExpanded = false }) {
                        languageOptions.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = {
                                selectedLanguage = it
                                val locale = when (it) {
                                    "Español" -> Locale("es", "ES")
                                    "Inglés" -> Locale("en", "US")
                                    "Francés" -> Locale("fr", "FR")
                                    "Mandarín" -> Locale.SIMPLIFIED_CHINESE
                                    else -> Locale.getDefault()
                                }

                                val result = tts.setLanguage(locale)
                                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                    accentOptions = emptyList()
                                    return@DropdownMenuItem
                                }

                                val voices = tts.voices?.filter { it.locale.language == locale.language } ?: emptyList()
                                allVoices.clear()
                                allVoices.addAll(voices)

                                accentOptions = voices.map { "${it.locale.displayCountry} - ${it.name}" }.distinct()
                                if (accentOptions.isNotEmpty()) {
                                    selectedAccent = accentOptions.first()
                                }
                                langExpanded = false
                            })
                        }
                    }
                }

                val accentEnabled = accentOptions.isNotEmpty()
                var accentExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = accentExpanded,
                    onExpandedChange = { if (accentEnabled) accentExpanded = !accentExpanded }) {
                    OutlinedTextField(
                        value = selectedAccent,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Acento") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        enabled = accentEnabled
                    )
                    if (accentEnabled) {
                        ExposedDropdownMenu(expanded = accentExpanded, onDismissRequest = { accentExpanded = false }) {
                            accentOptions.forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = {
                                    selectedAccent = it
                                    accentExpanded = false
                                })
                            }
                        }
                    }
                }

                Text("Velocidad")
                Slider(value = speed, onValueChange = { speed = it }, valueRange = 0.5f..2f)
                Text("Tono")
                Slider(value = pitch, onValueChange = { pitch = it }, valueRange = 0.5f..2f)
            }
        }

    }
}