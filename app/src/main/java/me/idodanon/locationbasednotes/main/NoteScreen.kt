package me.idodanon.locationbasednotes.main

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import me.idodanon.locationbasednotes.data.Note
import me.idodanon.locationbasednotes.utils.ShowImageOptionsDialog
import me.idodanon.locationbasednotes.utils.getCurrentCity
import me.idodanon.locationbasednotes.utils.saveImageToCache
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditNoteScreen(
    noteViewModel: NoteViewModel,
    userEmail: String,
    context: Context,
    existingNote: Note? = null,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var body by remember { mutableStateOf(existingNote?.body ?: "") }
    var imageUri by remember { mutableStateOf(existingNote?.imageUri ?: "") }
    val date = existingNote?.date ?: System.currentTimeMillis()
    var location by remember { mutableStateOf(existingNote?.location ?: "Fetching location...") }
    var showImageDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) scope.launch {
                location = getCurrentCity(context)
            } else location = "Location permission not granted"
        })

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val uri = saveImageToCache(context, it)
                showImageDialog = false
                imageUri = uri.toString()
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { imageUri = it.toString() }
            showImageDialog = false
        }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Edit Note",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(Date(date))
                    Text("Date: $formattedDate", style = MaterialTheme.typography.bodyLarge)

                    Text("Title:", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Body:", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 8
                    )

                    Text("Image:", style = MaterialTheme.typography.titleMedium)

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .border(2.dp, Color.Gray, CircleShape)
                            .clickable {
                                if (imageUri.isNotEmpty()) showImageDialog = !showImageDialog
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri.isNotEmpty()) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Note Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = "Placeholder photo",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    if (showImageDialog) {
                        ShowImageOptionsDialog(
                            onPickGallery = { galleryLauncher.launch("image/*") },
                            onTakePhoto = { cameraLauncher.launch(null) },
                            onRemove = {
                                imageUri = ""
                                showImageDialog = false
                            },
                            onCancel = { showImageDialog = false }
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Gallery") }

                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Camera") }
                    }

                    Text("Location: $location", style = MaterialTheme.typography.bodySmall)
                    Text("Author: $userEmail", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.95f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (existingNote != null) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            scope.launch {
                                noteViewModel.deleteNote(existingNote)
                                Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                                onDelete()
                            }
                        },
                        icon = { Icon(Icons.Default.Delete, contentDescription = "Delete") },
                        text = { Text("Delete") },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        if (title.isBlank() || body.isBlank()) {
                            Toast.makeText(context, "Title and body cannot be empty", Toast.LENGTH_SHORT).show()
                            return@ExtendedFloatingActionButton
                        }

                        val note = Note(
                            id = existingNote?.id ?: UUID.randomUUID().toString(),
                            date = System.currentTimeMillis(),
                            title = title,
                            body = body,
                            location = location,
                            userEmail = userEmail,
                            imageUri = imageUri
                        )

                        scope.launch {
                            noteViewModel.saveNote(note)
                            Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show()
                            onSave()
                        }
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Save") },
                    text = { Text("Save") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
        }
    }
}


@Composable
fun ViewNoteScreen(
    note: Note,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = note.title,
                style = MaterialTheme.typography.displayMedium,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 24.dp),
                maxLines = 2
            )

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(2.dp, Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!note.imageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = note.imageUri,
                        contentDescription = "Note Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Placeholder photo",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(Date(note.date))

                    Text(
                        "Date:",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formattedDate,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Body:",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(note.body, fontSize = 20.sp, style = MaterialTheme.typography.bodyLarge)

                    Text(
                        "Location:",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        note.location,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.95f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = onEdit,
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                    text = { Text("Edit") },
                    modifier = Modifier.weight(1f)
                )

                ExtendedFloatingActionButton(
                    onClick = onDelete,
                    icon = { Icon(Icons.Default.Delete, contentDescription = "Delete") },
                    text = { Text("Delete") },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
        }
    }
}

