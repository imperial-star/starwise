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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.gua.imper.AiModel
import org.gua.imper.R
import org.gua.imper.SettingsManager
import org.gua.imper.ui.theme.ImperTheme

class AppSettings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImperTheme {
                SettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var useCustomKey by remember { mutableStateOf(SettingsManager.shouldUseCustomApiKey(context)) }
    var apiKey by remember { mutableStateOf(SettingsManager.getApiKey(context) ?: "") }
    var selectedModel by remember { mutableStateOf(SettingsManager.getSelectedAiModel(context)) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(stringResource(id = R.string.choose_ai_model))
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedModel.name,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    AiModel.entries.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model.name) },
                            onClick = {
                                selectedModel = model
                                SettingsManager.setSelectedAiModel(context, model)
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(id = R.string.use_custom_api_key))
                Switch(
                    checked = useCustomKey,
                    onCheckedChange = {
                        useCustomKey = it
                        SettingsManager.setUseCustomApiKey(context, it)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text(stringResource(id = R.string.your_gemini_api_key)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = useCustomKey
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (useCustomKey) {
                        SettingsManager.saveApiKey(context, apiKey)
                        Toast.makeText(context, context.getString(R.string.key_saved), Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = useCustomKey
            ) {
                Text(stringResource(id = R.string.save))
            }
        }
    }
}
