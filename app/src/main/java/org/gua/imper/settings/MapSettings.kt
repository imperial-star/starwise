/*
 * This is the source code of Starwise for Android v. 10.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Gleb Obitocjkiy, 2026.
 */

package org.gua.imper.settings

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.gua.imper.AiModel
import org.gua.imper.ImpExtra
import org.gua.imper.R
import org.gua.imper.Roadmap
import org.gua.imper.RoadmapManager
import org.gua.imper.SettingsManager
import org.gua.imper.ui.theme.ImperTheme
import java.io.File

class MapSettings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImperTheme {
                MapSettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSettingsScreen() {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var refinement by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.create_roadmap)) }, navigationIcon = {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                }
            })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(id = R.string.creating_please_wait))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(stringResource(id = R.string.roadmap_name)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text(stringResource(id = R.string.short_description)) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = topic,
                                onValueChange = { topic = it },
                                label = { Text(stringResource(id = R.string.gemini_topic)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = refinement,
                                onValueChange = { refinement = it },
                                label = { Text(stringResource(id = R.string.gemini_refinement)) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = {
                        if (name.isBlank() || description.isBlank() || topic.isBlank()) return@Button

                        coroutineScope.launch {
                            val llmModel = SettingsManager.getSelectedAiModel(context)
                            val apiKeyToUse = if (SettingsManager.shouldUseCustomApiKey(context)) {
                                SettingsManager.getApiKey(context)
                            } else {
                                if (llmModel == AiModel.GIGACHAT) ImpExtra.API_GIGACHAT else ImpExtra.API_GEMINI
                            }

                            if (apiKeyToUse.isNullOrBlank()) {
                                Toast.makeText(context, context.getString(R.string.api_key_not_found), Toast.LENGTH_LONG).show()
                                return@launch
                            }

                            isLoading = true
                            val userPrompt = "Создай подробную дорожную карту в формате HTML для изучения темы: '$topic'. Уточнение: '$refinement'. Верни только HTML без своего текста , только HTML . все должно выгледеть в современном ui стиле , должно быть множество ссылок на существующие ресурсы , используй курсы и тд , свои обьяснение темы должны быть подробные и полные на примерах и тд."
                            val finalPrompt = """$userPrompt

                        ВАЖНОЕ ТРЕБОВАНИЕ:
                        Результат должен быть ОДНИМ HTML-файлом.
                        Структура файла должна быть следующей:
                        1. Вверху: Визуальная дорожная карта в стиле сайта roadmap.sh. Она должна состоять из кликабельных названий тем. Каждая тема должна быть ссылкой-якорем, ведущей к соответствующему разделу с контентом внизу страницы (например, <a href=\"#topic1\">).
                        2. Внизу: Разделы с контентом для каждой темы. Каждый раздел должен иметь уникальный id, на который ссылается якорь из верхней дорожной карты (например, <div id=\"topic1\">). Каждый раздел должен содержать краткое объяснение темы и множество полезных ссылок на ресурсы.
                        3. Используй плавную прокрутку через CSS (html { scroll-behavior: smooth; }).
                        4. Весь контент и UI должны быть в современном, чистом стиле и на русском языке.
                        5. Сейчас 2026 год
                        6. Пиши все без ошибок
                        7. RoadMap должен быть в виде древа и снизу этого древа обьяснения и ресурсы для рочтения , просмотра и тд
                        8. В каждом пояснение должено быть большое количество информативного текста по этой микро теме
                        Верни только HTML-код, без ```html и прочего обрамления.
                        """

                            val htmlContent = if (llmModel == AiModel.GIGACHAT) {
                                withContext(Dispatchers.IO) {
                                    try {
                                        if (!Python.isStarted()) {
                                            Python.start(AndroidPlatform(context))
                                        }
                                        val python = Python.getInstance()
                                        val gigaChatModule = python.getModule("GigaChat")
                                        gigaChatModule.callAttr("generate_roadmap", finalPrompt , apiKeyToUse).toString()
                                    } catch (e: Exception) {
                                        "<html><body><h1>Ошибка</h1><p>Не удалось получить ответ от GigaChat: ${e.message}</p></body></html>"
                                    }
                                }
                            } else {
                                try {
                                    val generativeModel = GenerativeModel(
                                        modelName = "gemini-2.5-flash",
                                        apiKey = apiKeyToUse
                                    )
                                    val responseStream = generativeModel.generateContentStream(finalPrompt)
                                    buildString {
                                        responseStream.collect { chunk ->
                                            append(chunk.text)
                                        }
                                    }
                                } catch (e: Exception) {
                                    "<html><body><h1>Ошибка</h1><p>Не удалось получить ответ от нейросети: ${e.message}</p></body></html>"
                                }
                            }

                            val fileName = "${name.replace(" ", "_")}.html"
                            val file = File(context.filesDir, fileName)
                            file.writeText(htmlContent)

                            val newRoadmap = Roadmap(name, description, file.absolutePath)
                            RoadmapManager.addRoadmap(context, newRoadmap)

                            isLoading = false
                            (context as? Activity)?.finish()
                        }
                    }) {
                        Text(stringResource(id = R.string.create_and_save))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
