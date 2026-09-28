package com.ainotes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ainotes.app.data.NoteRepository
import com.ainotes.app.data.local.NoteEntity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AiNotesApp() }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = query
    val notes: StateFlow<List<NoteEntity>> = query
        .flatMapLatest(repository::observe)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }

    fun save(id: Long?, title: String, content: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = id?.let { repository.get(it) }
            repository.save(
                NoteEntity(
                    id = existing?.id ?: 0,
                    title = title,
                    content = content,
                    isPinned = existing?.isPinned ?: false,
                    isFavorite = existing?.isFavorite ?: false,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
        }
    }

    fun togglePinned(note: NoteEntity) = viewModelScope.launch {
        repository.save(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    fun toggleFavorite(note: NoteEntity) = viewModelScope.launch {
        repository.save(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
    }
}

@Composable
private fun AiNotesApp(viewModel: NotesViewModel = hiltViewModel()) {
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF5B4FCF),
            secondary = Color(0xFF1E8E88),
            background = Color(0xFFF8F7FC),
            surface = Color.White,
        ),
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (showEditor) {
                val note = viewModel.notes.value.firstOrNull { it.id == editingId }
                EditorScreen(
                    note = note,
                    onBack = { showEditor = false },
                    onSave = { title, content ->
                        viewModel.save(editingId, title, content)
                        showEditor = false
                    },
                )
            } else {
                HomeScreen(
                    notes = viewModel.notes.value,
                    query = viewModel.searchQuery.value,
                    onQueryChange = viewModel::setQuery,
                    onCreate = { editingId = null; showEditor = true },
                    onOpen = { editingId = it.id; showEditor = true },
                    onTogglePinned = viewModel::togglePinned,
                    onToggleFavorite = viewModel::toggleFavorite,
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    notes: List<NoteEntity>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCreate: () -> Unit,
    onOpen: (NoteEntity) -> Unit,
    onTogglePinned: (NoteEntity) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
) {
    Scaffold(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
        topBar = {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(18.dp))
                Row {
                    Column(Modifier.weight(1f)) {
                        Text("Good morning", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Your notes", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.AutoAwesome, "AI assistant", Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search your notes") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                )
                Spacer(Modifier.height(18.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, "New note", tint = Color.White)
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (notes.isEmpty()) {
                item {
                    Text(
                        if (query.isBlank()) "No notes yet. Tap + to capture your first idea."
                        else "No matching notes.",
                        Modifier.padding(top = 48.dp),
                    )
                }
            } else {
                items(notes, key = { it.id }) { note ->
                    NoteCard(note, { onOpen(note) }, { onTogglePinned(note) }, { onToggleFavorite(note) })
                }
            }
        }
    }
}

@Composable
private fun NoteCard(note: NoteEntity, onOpen: () -> Unit, onPin: () -> Unit, onFavorite: () -> Unit) {
    Card(
        onClick = onOpen,
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row {
                Text(note.title.ifBlank { "Untitled" }, Modifier.weight(1f), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = onPin, Modifier.size(34.dp)) {
                    Icon(Icons.Default.PushPin, "Pin note", tint = if (note.isPinned) MaterialTheme.colorScheme.primary else Color.LightGray)
                }
                IconButton(onClick = onFavorite, Modifier.size(34.dp)) {
                    Icon(if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, "Favorite note", tint = if (note.isFavorite) Color(0xFFF2A900) else Color.LightGray)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(note.content, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen(note: NoteEntity?, onBack: () -> Unit, onSave: (String, String) -> Unit) {
    var title by rememberSaveable(note?.id) { mutableStateOf(note?.title.orEmpty()) }
    var content by rememberSaveable(note?.id) { mutableStateOf(note?.content.orEmpty()) }
    Scaffold(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "New note" else "Edit note") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    TextButton(onClick = { onSave(title.trim(), content.trim()) }) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.size(6.dp))
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), placeholder = { Text("Title") }, singleLine = true, shape = RoundedCornerShape(16.dp))
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(content, { content = it }, Modifier.fillMaxWidth().weight(1f), placeholder = { Text("Start writing...") }, shape = RoundedCornerShape(16.dp))
        }
    }
}
