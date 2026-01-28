/*
 * This is the source code of Starwise for Android v. 10.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Gleb Obitocjkiy, 2026.
 */

package org.gua.imper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.gua.imper.settings.AppSettings
import org.gua.imper.settings.MapSettings
import org.gua.imper.ui.theme.ImperTheme

class MainActivity : ComponentActivity() {
    private var roadmaps by mutableStateOf<List<Roadmap>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            ImperTheme {
                RoadmapListScreen(roadmaps = roadmaps, onAddClick = {
                    startActivity(Intent(this, MapSettings::class.java))
                }, onRoadmapClick = { roadmap ->
                    val intent = Intent(this, WebViewActivity::class.java).apply {
                        putExtra("url", "file://${roadmap.filePath}")
                        putExtra("title", roadmap.name)
                    }
                    startActivity(intent)
                }, onDeleteRequest = { roadmap ->
                    RoadmapManager.deleteRoadmap(this, roadmap)
                    refreshRoadmaps()
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshRoadmaps()
    }

    private fun refreshRoadmaps() {
        roadmaps = RoadmapManager.getRoadmaps(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapListScreen(
    roadmaps: List<Roadmap>,
    onAddClick: () -> Unit,
    onRoadmapClick: (Roadmap) -> Unit,
    onDeleteRequest: (Roadmap) -> Unit
) {
    val context = LocalContext.current
    var roadmapToDelete by remember { mutableStateOf<Roadmap?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.my_roadmaps)) }, actions = {
                IconButton(onClick = {
                    val url = "https://imperial-star.github.io/docs/"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Filled.Info, contentDescription = stringResource(id = R.string.info))
                }
                IconButton(onClick = {
                    context.startActivity(Intent(context, AppSettings::class.java))
                }) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.settings))
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.create_roadmap))
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            if (roadmaps.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.no_roadmaps_prompt),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(roadmaps) { roadmap ->
                    RoadmapCard(roadmap = roadmap, onClick = {
                        onRoadmapClick(roadmap)
                    }, onDelete = {
                        roadmapToDelete = roadmap
                    })
                }
            }
        }
    }

    roadmapToDelete?.let {
        DeleteConfirmationDialog(roadmapName = it.name, onConfirm = {
            onDeleteRequest(it)
            roadmapToDelete = null
        }, onDismiss = {
            roadmapToDelete = null
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapCard(roadmap: Roadmap, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                Text(text = roadmap.name, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = roadmap.description, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete_roadmap))
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(roadmapName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.delete_confirmation_title)) },
        text = { Text(stringResource(id = R.string.delete_confirmation_text, roadmapName)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(id = R.string.delete))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}
