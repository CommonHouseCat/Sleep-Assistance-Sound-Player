package com.example.nlcn

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nlcn.ui.theme.NLCNTheme

class PlaylistActivity : ComponentActivity() {

    private lateinit var pickAudioFileLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playlistId = intent.getIntExtra("PLAYLIST_ID", -1)
        val playlistTitle = intent.getStringExtra("PLAYLIST_TITLE") ?: "Playlist"

        pickAudioFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val displayName = getAudioFileName(this, uri)
            }
        }

        setContent {
            NLCNTheme {
                PlaylistScreen(context = this, playlistId, playlistTitle, pickAudioFileLauncher = pickAudioFileLauncher)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(context: Context, playlistId: Int, playlistTitle: String, pickAudioFileLauncher: ActivityResultLauncher<String>) {

    var showAddDialog by remember { mutableStateOf(false)}
    var displayName by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column {
            TopAppBar (
                title = { Text(playlistTitle , color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( containerColor = Color.Black ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Playlist",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )

            Text (
                "Playlist Content (ID: $playlistId)",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Dialog box for adding a playlist
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    displayName = ""
                },
                title = { Text("Add audio from Device", color = Color.White) },
                text = {
                    Column {
                        TextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display name", color = Color.LightGray) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        IconButton(onClick = { pickAudioFileLauncher.launch("audio/*") }, modifier = Modifier.padding(top = 4.dp)){
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Add from local device",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        if (displayName.isNotEmpty()) {
                            Text(text = "Selected file: $displayName", color = Color.White)
                        }
                    }
                },

                confirmButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        displayName = ""
                    }) {
                        Text("Confirm", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        displayName = ""

                    }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color.DarkGray
            )
        }
    }
}
// Helper function to get the file name from the Uri
private fun getAudioFileName(context: Context, uri: Uri): String {
    var name = "Unknown"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }
    return name
}
