package com.example.aicompanion

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip

import androidx.compose.runtime.saveable.rememberSaveable
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share

import androidx.compose.material3.*

import androidx.compose.runtime.*


import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.lifecycleScope

import coil.compose.AsyncImage

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import java.util.Locale



import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState



data class Conversation(
    val id: String = "",
    val title: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val pinned: Boolean = false
)
class MainActivity : ComponentActivity() {

    private lateinit var textToSpeech: TextToSpeech


    override fun onDestroy() {

        textToSpeech.stop()

        textToSpeech.shutdown()

        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        textToSpeech = TextToSpeech(this) { status ->

            if (status != TextToSpeech.ERROR) {

                textToSpeech.language = Locale.US

            }
        }
        setContent {

            val darkTheme =
                ThemeManager.isDarkTheme.value

            MaterialTheme(
                colorScheme =
                    if (darkTheme)
                        darkColorScheme()
                    else
                        lightColorScheme()
            ) {

                val auth = FirebaseAuth.getInstance()

                var isLoggedIn by remember {
                    mutableStateOf(auth.currentUser != null)
                }

                var showSplash by remember {
                    mutableStateOf(true)
                }

                val prefs = getSharedPreferences("model_usage", MODE_PRIVATE)

                var gptCount by remember {
                    mutableIntStateOf(
                        prefs.getInt("gptCount", 0)
                    )
                }

                var claudeCount by remember {
                    mutableIntStateOf(
                        prefs.getInt("claudeCount", 0)
                    )
                }

                var deepseekCount by remember {
                    mutableIntStateOf(
                        prefs.getInt("deepseekCount", 0)
                    )
                }

                var llamaCount by remember {
                    mutableIntStateOf(
                        prefs.getInt("llamaCount", 0)
                    )
                }


                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                if (showSplash) {

                    SplashScreen()

                } else {

                    if (isLoggedIn) {

                        MainScreen(
                            onLogout = {
                                auth.signOut()
                                isLoggedIn = false
                            }
                        )

                    } else {

                        AuthScreen(
                            onLoginSuccess = {
                                isLoggedIn = true
                            }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(
        onLogout: () -> Unit
    ) {

        val prefs = getSharedPreferences("model_usage", MODE_PRIVATE)

        var gptCount by rememberSaveable {
            mutableIntStateOf(
                prefs.getInt("gptCount", 0)
            )
        }

        var claudeCount by rememberSaveable {
            mutableIntStateOf(
                prefs.getInt("claudeCount", 0)
            )
        }

        var deepseekCount by rememberSaveable {
            mutableIntStateOf(
                prefs.getInt("deepseekCount", 0)
            )
        }

        var llamaCount by rememberSaveable {
            mutableIntStateOf(
                prefs.getInt("llamaCount", 0)
            )
        }

        val isDark = ThemeManager.isDarkTheme.value

        val backgroundColor =
            if (isDark) DarkBackground
            else LightBackground

        val cardColor =
            if (isDark) DarkCard
            else LightCard

        val textColor =
            if (isDark) Color.White
            else Color.Black

        val drawerState =
            rememberDrawerState(
                initialValue = DrawerValue.Closed
            )

        val scope = rememberCoroutineScope()

        val context = LocalContext.current

        var selectedModel by remember {
            mutableStateOf("openai/gpt-4o-mini")
        }

        val models = listOf(
            "openai/gpt-4o-mini",
            "anthropic/claude-3-haiku",
            "deepseek/deepseek-chat",
            "meta-llama/llama-3.3-70b-instruct"
        )
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid ?: ""

        var currentConversationId by remember {
            mutableStateOf("")
        }

        val conversations = remember {
            mutableStateListOf<Conversation>()
        }

        var showChatMenu by remember {
            mutableStateOf<String?>(null)
        }

        var showRenameDialog by remember {
            mutableStateOf(false)
        }

        var chatToRename by remember {
            mutableStateOf<Conversation?>(null)
        }

        var newChatTitle by remember {
            mutableStateOf("")
        }

        var searchQuery by remember {
            mutableStateOf("")
        }

        var showSearch by remember {
            mutableStateOf(false)
        }

        // LOAD CONVERSATIONS
        LaunchedEffect(Unit) {

            db.collection("users")
                .document(userId)
                .collection("conversations")

                .orderBy("timestamp")
                .get()
                .addOnSuccessListener { result ->

                    conversations.clear()

                    for (document in result) {

                        try {

                            val conversation =
                                document.toObject(
                                    Conversation::class.java
                                )

                            conversations.add(conversation)

                        } catch (e: Exception) {

                            Log.e(
                                "CHAT_LOAD",
                                e.toString()
                            )
                        }
                    }
                    conversations.sortByDescending {
                        it.pinned
                    }

                }
        }

        ModalNavigationDrawer(

            drawerState = drawerState,
            drawerContent = {

                ModalDrawerSheet(

                    modifier = Modifier.width(320.dp),

                    drawerContainerColor = Color.Transparent

                ) {

                    Column(

                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (ThemeManager.isDarkTheme.value) {

                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF081229),
                                            Color(0xFF0D1B3D),
                                            BackgroundColor
                                        )
                                    )

                                } else {

                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF),
                                            Color(0xFFF5F5F5),
                                            Color(0xFFEDEDED)
                                        )
                                    )
                                }
                            )
                            .padding(20.dp)
                    ) {

                        Spacer(modifier = Modifier.height(20.dp))

                        // LOGO
                        Text(
                            text = "🤖 AICompanion",
                            fontSize = 34.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        // NEW CHAT BUTTON
                        Card(

                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    currentConversationId = ""
                                    showChatMenu = null

                                    scope.launch {
                                        drawerState.close()
                                    }
                                },

                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF8B5CF6)
                            ),

                            shape = RoundedCornerShape(18.dp)

                        ) {

                            Row(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),

                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "New Chat",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // RECENT CHAT TITLE
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Recent Chats",
                                color = textColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    showSearch = !showSearch
                                }
                            ) {
                                Text(
                                    text = "🔍",
                                    fontSize = 20.sp
                                )
                            }
                        }

                        if (showSearch) {

                            TextField(

                                value = searchQuery,

                                onValueChange = {
                                    searchQuery = it
                                },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),

                                placeholder = {
                                    Text("Search chats...")
                                },

                                singleLine = true
                            )
                        }


                        Spacer(modifier = Modifier.height(14.dp))

                        // SCROLLABLE CHAT AREA
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {

                            items(

                                conversations.filter {

                                    it.title.contains(
                                        searchQuery,
                                        ignoreCase = true
                                    )
                                }

                            ) { conversation ->

                                Card(

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clickable {

                                            currentConversationId = conversation.id

                                            scope.launch {
                                                drawerState.close()
                                            }
                                        },

                                    colors = CardDefaults.cardColors(

                                        containerColor =

                                            if (currentConversationId == conversation.id)
                                                Color(0xFF8B5CF6)
                                            else
                                                cardColor,
                                    ),

                                    shape = RoundedCornerShape(18.dp),

                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 6.dp
                                    )

                                ) {
                                    Row(

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),

                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            text =
                                                if (conversation.pinned)
                                                    "📌"
                                                else
                                                    "💬",

                                            fontSize = 22.sp
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(

                                            text = conversation.title,

                                            color = textColor,

                                            fontSize = 17.sp,

                                            maxLines = 1,

                                            modifier = Modifier.weight(1f)
                                        )
                                        // SHARE BUTTON
                                        IconButton(

                                            onClick = {

                                                db.collection("users")
                                                    .document(userId)
                                                    .collection("conversations")
                                                    .document(conversation.id)
                                                    .collection("messages")
                                                    .orderBy("timestamp")
                                                    .get()
                                                    .addOnSuccessListener { result ->
                                                        val messagesList =
                                                            mutableListOf<Map<String, Any>>()

                                                        result.documents.forEach { doc ->

                                                            messagesList.add(
                                                                mapOf(
                                                                    "message" to (doc.getString("message")
                                                                        ?: ""),
                                                                    "isUser" to (doc.getBoolean("user")
                                                                        ?: false)
                                                                )
                                                            )
                                                        }

                                                        val sharedData = hashMapOf(

                                                            "title" to conversation.title,

                                                            "messages" to messagesList,

                                                            "timestamp" to System.currentTimeMillis()
                                                        )

                                                        db.collection("sharedChats")
                                                            .document(conversation.id)
                                                            .set(sharedData)
                                                            .addOnSuccessListener {

                                                                val shareLink =
                                                                    "https://aicompanion-ee1e6.web.app/chat/${conversation.id}"

                                                                val shareIntent =
                                                                    Intent().apply {

                                                                        action =
                                                                            Intent.ACTION_SEND

                                                                        putExtra(
                                                                            Intent.EXTRA_TEXT,
                                                                            shareLink
                                                                        )

                                                                        type = "text/plain"
                                                                    }

                                                                context.startActivity(
                                                                    Intent.createChooser(
                                                                        shareIntent,
                                                                        "Share Chat"
                                                                    )
                                                                )
                                                            }
                                                    }
                                            }
                                        ) {

                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }

                                        // DELETE BUTTON
                                        IconButton(
                                            onClick = {
                                                showChatMenu = conversation.id
                                            }
                                        ) {

                                            Text(
                                                text = "⋮",
                                                color = Color.White,
                                                fontSize = 20.sp
                                            )
                                        }
                                        DropdownMenu(

                                            expanded =
                                                showChatMenu == conversation.id,

                                            onDismissRequest = {
                                                showChatMenu = null
                                            }

                                        ) {

                                            DropdownMenuItem(

                                                text = {

                                                    Text(

                                                        if (conversation.pinned)
                                                            "📍 Unpin Chat"
                                                        else
                                                            "📌 Pin Chat"

                                                    )
                                                },

                                                onClick = {

                                                    db.collection("users")
                                                        .document(userId)
                                                        .collection("conversations")
                                                        .document(conversation.id)
                                                        .update(
                                                            "pinned",
                                                            !conversation.pinned
                                                        )

                                                    val index =
                                                        conversations.indexOf(conversation)

                                                    if (index != -1) {

                                                        conversations[index] =
                                                            conversation.copy(
                                                                pinned = !conversation.pinned
                                                            )
                                                        conversations.sortByDescending {
                                                            it.pinned
                                                        }
                                                    }

                                                    showChatMenu = null
                                                }
                                            )

                                            DropdownMenuItem(

                                                text = {
                                                    Text("✏ Rename Chat")
                                                },

                                                onClick = {

                                                    chatToRename = conversation

                                                    newChatTitle =
                                                        conversation.title

                                                    showRenameDialog = true

                                                    showChatMenu = null
                                                }
                                            )

                                            DropdownMenuItem(

                                                text = {
                                                    Text("🗑 Delete Chat")
                                                },

                                                onClick = {

                                                    showChatMenu = null

                                                    db.collection("users")
                                                        .document(userId)
                                                        .collection("conversations")
                                                        .document(conversation.id)
                                                        .collection("messages")
                                                        .get()
                                                        .addOnSuccessListener { result ->

                                                            for (document in result) {
                                                                document.reference.delete()
                                                            }


                                                            db.collection("users")
                                                                .document(userId)
                                                                .collection("conversations")
                                                                .document(conversation.id)
                                                                .delete()

                                                            conversations.remove(conversation)

                                                            if (
                                                                currentConversationId ==
                                                                conversation.id
                                                            ) {
                                                                currentConversationId = ""
                                                            }
                                                        }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "📊 Model Usage",
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = cardColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {

                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {

                                Text(
                                    text = "🚀 GPT-4o Mini : $gptCount",
                                    color = textColor,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = "🧠 Claude Haiku : $claudeCount",
                                    color = textColor,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = "💻 DeepSeek : $deepseekCount",
                                    color = textColor,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = "🦙 Llama 3.3 : $llamaCount",
                                    color = textColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // EMAIL

                        Card(
                            modifier = Modifier.fillMaxWidth(),

                            colors = CardDefaults.cardColors(
                                containerColor = cardColor
                            ),

                            shape = RoundedCornerShape(18.dp)

                        ) {

                            Row(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),

                                verticalAlignment = Alignment.CenterVertically

                            ) {

                                Text(
                                    text =
                                        if (ThemeManager.isDarkTheme.value)
                                            "🌙 Dark Mode"
                                        else
                                            "☀ Light Mode",

                                    color = textColor,

                                    fontSize = 16.sp
                                )

                                Spacer(
                                    modifier = Modifier.weight(1f)
                                )

                                Switch(

                                    checked =
                                        ThemeManager.isDarkTheme.value,

                                    onCheckedChange = {

                                        ThemeManager.isDarkTheme.value = it
                                    }
                                )
                            }
                        }

                        Text(
                            text = auth.currentUser?.email ?: "No Email",
                            color = textColor,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // PREMIUM LOGOUT BUTTON
                        Card(

                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLogout()
                                },

                            colors = CardDefaults.cardColors(
                                containerColor = cardColor
                            ),

                            shape = RoundedCornerShape(18.dp)

                        ) {

                            Row(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),

                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "🚪",
                                    fontSize = 22.sp
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "Logout",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        ) {
            if (showRenameDialog) {

                AlertDialog(

                    onDismissRequest = {
                        showRenameDialog = false
                    },

                    title = {
                        Text("Rename Chat")
                    },

                    text = {

                        TextField(

                            value = newChatTitle,

                            onValueChange = {
                                newChatTitle = it
                            }
                        )
                    },

                    confirmButton = {

                        Button(

                            onClick = {

                                chatToRename?.let { chat ->

                                    db.collection("users")
                                        .document(userId)
                                        .collection("conversations")
                                        .document(chat.id)
                                        .update(
                                            "title",
                                            newChatTitle
                                        )

                                    val index =
                                        conversations.indexOf(chat)

                                    if (index != -1) {

                                        conversations[index] =
                                            chat.copy(
                                                title = newChatTitle
                                            )
                                    }
                                }

                                showRenameDialog = false
                            }
                        ) {

                            Text("Save")
                        }
                    },

                    dismissButton = {

                        Button(
                            onClick = {
                                showRenameDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            ChatScreen(

                gptCount = gptCount,
                claudeCount = claudeCount,
                deepseekCount = deepseekCount,
                llamaCount = llamaCount,
                onGptUsed = {
                    gptCount++

                    prefs.edit()
                        .putInt("gptCount", gptCount)
                        .apply()
                },

                        onClaudeUsed = {
                    claudeCount++

                    prefs.edit()
                        .putInt("claudeCount", claudeCount)
                        .apply()
                },

                        onDeepSeekUsed = {
                    deepseekCount++

                    prefs.edit()
                        .putInt("deepseekCount", deepseekCount)
                        .apply()
                },

                        onLlamaUsed = {
                    llamaCount++

                    prefs.edit()
                        .putInt("llamaCount", llamaCount)
                        .apply()
                },

                conversationId = currentConversationId,

                selectedModel = selectedModel,

                models = models,

                onModelSelected = {
                    selectedModel = it
                },

                onConversationCreated = {

                    conversations.add(it)

                    currentConversationId = it.id
                },

                onConversationDeleted = { deletedId ->

                    conversations.removeAll {
                        it.id == deletedId
                    }

                    if (currentConversationId == deletedId) {
                        currentConversationId = ""
                    }
                },

                onMenuClick = {

                    scope.launch {
                        drawerState.open()
                    }
                }
            )

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen(
        conversationId: String,
        selectedModel: String,
        models: List<String>,
        onModelSelected: (String) -> Unit,
        onConversationCreated: (Conversation) -> Unit,
        onConversationDeleted: (String) -> Unit,
        onMenuClick: () -> Unit,


         gptCount: Int,
         claudeCount: Int,
         deepseekCount: Int,
         llamaCount: Int,

        onGptUsed: () -> Unit,
        onClaudeUsed: () -> Unit,
        onDeepSeekUsed: () -> Unit,
        onLlamaUsed: () -> Unit,

    ) {

        val context = LocalContext.current

        val isDark = ThemeManager.isDarkTheme.value

        val backgroundColor =
            if (isDark) DarkBackground
            else LightBackground

        val cardColor =
            if (isDark) DarkCard
            else LightCard

        val textColor =
            if (isDark) Color.White
            else Color.Black

        val auth = FirebaseAuth.getInstance()

        val db = FirebaseFirestore.getInstance()


        val userId = auth.currentUser?.uid ?: ""
        var userMemory by remember {
            mutableStateOf(UserMemory())
        }

        var messageText by remember {
            mutableStateOf("")
        }

        var lastPrompt by remember {
            mutableStateOf("")
        }

        var lastUserMessage by remember {
            mutableStateOf("")
        }

        var localConversationId by remember {
            mutableStateOf(conversationId)
        }



        LaunchedEffect(conversationId) {
            localConversationId = conversationId
        }

        LaunchedEffect(Unit) {

            db.collection("users")
                .document(userId)
                .collection("memory")
                .document("user_profile")
                .get()
                .addOnSuccessListener {

                    userMemory =
                        it.toObject(UserMemory::class.java)
                            ?: UserMemory()
                }
        }

        val messages = remember {
            mutableStateListOf<ChatMessage>()
        }

        var isLoading by remember {
            mutableStateOf(false)
        }


        var showModelInfo by remember {
            mutableStateOf(false)
        }

        var isTemporaryChat by remember {
            mutableStateOf(false)
        }


        val speechLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->

                if (result.resultCode == android.app.Activity.RESULT_OK) {

                    val spokenText =
                        result.data
                            ?.getStringArrayListExtra(
                                RecognizerIntent.EXTRA_RESULTS
                            )
                            ?.getOrNull(0)

                    if (!spokenText.isNullOrEmpty()) {
                        messageText = spokenText
                    }
                }
            }

        val listState = rememberLazyListState()

        // LOAD CHAT
        LaunchedEffect(conversationId) {
            messages.clear()

            if (conversationId.isNotEmpty()) {

                db.collection("users")
                    .document(userId)
                    .collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .orderBy("timestamp")
                    .get()
                    .addOnSuccessListener { result ->

                        result.documents.forEach { document ->

                            val chat = ChatMessage(
                                id = document.id,
                                message = document.getString("message") ?: "",
                                imageUrl = document.getString("imageUrl") ?: "",
                                isUser = document.getBoolean("user") ?: false,
                                timestamp = document.getLong("timestamp") ?: 0L
                            )

                            messages.add(chat)
                        }
                    }
            }
        }


        // AUTO SCROLL
        LaunchedEffect(messages.size) {

            if (messages.isNotEmpty()) {

                listState.animateScrollToItem(
                    messages.size - 1
                )
            }
        }

        if (showModelInfo) {

            AlertDialog(

                onDismissRequest = {
                    showModelInfo = false
                },

                title = {
                    Text("🤖 Model Guide")
                },

                text = {

                    Column {

                        Text(
                            "🚀 GPT-4o Mini\n" +
                                    "• General Chat\n" +
                                    "• Fast Responses\n" +
                                    "• Android Development\n" +
                                    "• Everyday Questions"
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            "🧠 Claude Haiku\n" +
                                    "• Writing\n" +
                                    "• Documents\n" +
                                    "• Summaries\n" +
                                    "• Long Explanations"
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            "💻 DeepSeek\n" +
                                    "• Coding\n" +
                                    "• DSA\n" +
                                    "• Debugging\n" +
                                    "• Competitive Programming"
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            "🦙 Llama 3.3\n" +
                                    "• Reasoning\n" +
                                    "• General Knowledge\n" +
                                    "• Brainstorming"
                        )
                    }
                },

                confirmButton = {

                    Button(
                        onClick = {
                            showModelInfo = false
                        }
                    ) {
                        Text("Got it")
                    }
                }
            )
        }

        Scaffold(

            topBar = {

                TopAppBar(

                    title = {
                        Column {

                            Text(
                                text = "🤖 AICompanion",
                                color =
                                    if (ThemeManager.isDarkTheme.value)
                                        textColor
                                    else
                                        Color.Black,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = selectedModel.substringAfterLast("/"),
                                color = Color(0xFFCDA4FF),
                                fontSize = 12.sp
                            )


                        }
                    },

                    navigationIcon = {


                        IconButton(
                            onClick = onMenuClick
                        ) {

                            Icon(
                                Icons.Default.Menu,
                                contentDescription = null,
                                tint = textColor
                            )
                        }
                    },


                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor
                    )
                )
            },

            containerColor = backgroundColor

        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                LazyRow(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),

                    contentPadding = PaddingValues(horizontal = 12.dp),

                    horizontalArrangement = Arrangement.spacedBy(8.dp)

                ) {

                    items(models) { model ->

                        FilterChip(

                            selected = model == selectedModel,

                            onClick = {
                                onModelSelected(model)
                            },
                            shape = RoundedCornerShape(20.dp),
                            label = {

                                Text(
                                    text = when (model) {

                                        "openai/gpt-4o-mini" ->
                                            "GPT-4o Mini"


                                        "anthropic/claude-3-haiku" ->
                                            "Claude Haiku"

                                        "deepseek/deepseek-chat" ->
                                            "DeepSeek"

                                        "meta-llama/llama-3.3-70b-instruct" ->
                                            "Llama 3.3"

                                        else ->
                                            model
                                    }
                                )
                            },

                            colors = FilterChipDefaults.filterChipColors(

                                selectedContainerColor =
                                    Color(0xFF7C4DFF),
                                selectedLabelColor =
                                    Color.White,

                                containerColor =
                                    cardColor,

                                labelColor = textColor
                            )
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )
                    }
                    item {

                        IconButton(
                            onClick = {
                                showModelInfo = true
                            }
                        ) {

                            Text(
                                text = "ⓘ",
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "🕶 Temporary Chat",
                        color = textColor
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Switch(

                        checked = isTemporaryChat,

                        onCheckedChange = {

                            isTemporaryChat = it
                        }
                    )
                }
                LazyColumn(

                    state = listState,

                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {

                    items(messages) { chat ->

                        Row(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),

                            horizontalArrangement =
                                if (chat.isUser)
                                    Arrangement.End
                                else
                                    Arrangement.Start
                        ) {
                            Card(

                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        if (chat.isUser)
                                            Color(0xFF8B5CF6)
                                        else
                                            if (ThemeManager.isDarkTheme.value)
                                                Color(0xFF16213E)
                                            else
                                                Color(0xFFE3F2FD)  // AI bubble
                                ),

                                shape = RoundedCornerShape(22.dp),

                                modifier = Modifier
                                    .wrapContentWidth()
                                    .widthIn(max = 300.dp)

                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                ) {

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            text =
                                                if (chat.isUser)
                                                    "👤 You"
                                                else
                                                    "🤖 AICompanion",

                                            fontSize = 13.sp,

                                            color =
                                                if (chat.isUser)
                                                    Color(0xFFE6D7FF)
                                                else
                                                    Color(0xFFC7D6FF),

                                            fontWeight = FontWeight.SemiBold,

                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = {

                                                val clipboard =
                                                    context.getSystemService(
                                                        Context.CLIPBOARD_SERVICE
                                                    ) as ClipboardManager

                                                clipboard.setPrimaryClip(
                                                    ClipData.newPlainText(
                                                        if (chat.isUser)
                                                            "User Message"
                                                        else
                                                            "AI Response",

                                                        chat.message
                                                    )
                                                )
                                            }
                                        ) {

                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        if (!chat.isUser) {


                                            IconButton(
                                                onClick = {
                                                    textToSpeech.speak(
                                                        chat.message,
                                                        TextToSpeech.QUEUE_FLUSH,
                                                        null,
                                                        null
                                                    )
                                                }
                                            ) {
                                                Text(
                                                    text = "🔊",
                                                    fontSize = 18.sp
                                                )
                                            }

                                        }
                                        IconButton(
                                            onClick = {

                                                messages.remove(chat)

                                                if (messages.isEmpty()) {

                                                    db.collection("users")
                                                        .document(userId)
                                                        .collection("conversations")
                                                        .document(localConversationId)
                                                        .delete()

                                                    onConversationDeleted(localConversationId)

                                                    localConversationId = ""
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = "🗑",
                                                fontSize = 18.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (!chat.isUser) {

                                        TextButton(

                                            onClick = {

                                                if (
                                                    !isLoading &&
                                                    lastUserMessage.isNotEmpty()
                                                ) {

                                                    lifecycleScope.launch {

                                                        isLoading = true

                                                        val newReply =
                                                            getAIResponse(
                                                                lastUserMessage,
                                                                selectedModel
                                                            )

                                                        messages.add(
                                                            ChatMessage(
                                                                message = newReply,
                                                                isUser = false
                                                            )
                                                        )

                                                        isLoading = false
                                                    }
                                                }

                                            }

                                        ) {

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                Text(
                                                    text = "🔄",
                                                    fontSize = 14.sp
                                                )

                                                Spacer(
                                                    modifier = Modifier.width(4.dp)
                                                )

                                                Text(
                                                    text = "Regenerate",
                                                    color = Color(0xFFCDA4FF),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }


                                    if (chat.message.contains("```")) {

                                        val language =
                                            Regex("```([a-zA-Z0-9+#-]*)")
                                                .find(chat.message)
                                                ?.groupValues
                                                ?.getOrNull(1)
                                                ?.uppercase()
                                                ?: "CODE"

                                        val codeText =
                                            chat.message

                                                .replace(
                                                    Regex("```[a-zA-Z0-9+#-]*"),
                                                    ""
                                                )

                                                .replace("```", "")
                                                .trim()

                                        Card(
                                            border = BorderStroke(
                                                1.dp,
                                                Color(0xFF7C4DFF)
                                            ),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFF0A0A0A)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {

                                            Column {

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),

                                                    verticalAlignment =
                                                        Alignment.CenterVertically
                                                ) {

                                                    Text(
                                                        text = language,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    Spacer(
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    IconButton(
                                                        onClick = {

                                                            val clipboard =
                                                                context.getSystemService(
                                                                    Context.CLIPBOARD_SERVICE
                                                                ) as ClipboardManager

                                                            clipboard.setPrimaryClip(
                                                                ClipData.newPlainText(
                                                                    language,
                                                                    codeText
                                                                )
                                                            )

                                                            Toast.makeText(
                                                                context,
                                                                "Code Copied",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    ) {

                                                        Icon(
                                                            imageVector =
                                                                Icons.Default.ContentCopy,

                                                            contentDescription = "Copy",

                                                            tint = Color.White
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = codeText,

                                                    color = Color.White,

                                                    modifier = Modifier
                                                        .horizontalScroll(
                                                            rememberScrollState()
                                                        )
                                                        .padding(12.dp)
                                                )
                                            }
                                        }

                                    } else {

                                        FormattedMessage(chat.message)
                                    }
                                }
                            }
                        }
                    }

                }

                if (isLoading) {

                    Text(
                        text = "AICompanion is thinking...",
                        color = Color(0xFFCDA4FF),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {

                            val intent =
                                Intent(
                                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                                )

                            intent.putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )

                            speechLauncher.launch(intent)
                        }
                    ) {

                        Text(
                            text = "🎤",
                            fontSize = 24.sp
                        )
                    }

                    TextField(

                        value = messageText,

                        onValueChange = {
                            messageText = it
                        },

                        modifier = Modifier.weight(1f),

                        placeholder = {
                            Text(
                                "Ask anything...",
                                color = Color(0xFFB0B0B0)
                            )
                        },

                        shape = RoundedCornerShape(30.dp),

                        colors = TextFieldDefaults.colors(

                            focusedContainerColor =
                                Color(0xFF181818),

                            unfocusedContainerColor =
                                Color(0xFF181818),

                            focusedTextColor = Color.White,

                            unfocusedTextColor = Color.White,

                            cursorColor = Color.White,

                            focusedIndicatorColor =
                                Color.Transparent,

                            unfocusedIndicatorColor =
                                Color.Transparent
                        )
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Button(
                        onClick = {

                            if (messageText.isNotBlank() && !isLoading) {

                                val userMessage = messageText

                                lastPrompt = userMessage
                                lastUserMessage = userMessage

                                if (localConversationId.isEmpty()) {

                                    val conversationId =
                                        db.collection("users")
                                            .document(userId)
                                            .collection("conversations")
                                            .document()
                                            .id

                                    localConversationId = conversationId

                                    val newConversation =
                                        Conversation(
                                            id = conversationId,
                                            title = userMessage.take(30),
                                            timestamp = System.currentTimeMillis()
                                        )

                                    db.collection("users")
                                        .document(userId)
                                        .collection("conversations")
                                        .document(conversationId)
                                        .set(newConversation)

                                    onConversationCreated(newConversation)
                                }


                                messages.add(
                                    ChatMessage(
                                        message = userMessage,
                                        isUser = true
                                    )
                                )

                                db.collection("users")
                                    .document(userId)
                                    .collection("conversations")
                                    .document(localConversationId)
                                    .collection("messages")
                                    .add(
                                        hashMapOf(
                                            "message" to userMessage,
                                            "user" to true,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                    )

                                messageText = ""

                                Log.d("MODEL_TEST", "Selected Model = $selectedModel")

                                when (selectedModel) {

                                    "openai/gpt-4o-mini" -> onGptUsed()

                                    "anthropic/claude-3-haiku" -> onClaudeUsed()

                                    "deepseek/deepseek-chat" -> onDeepSeekUsed()

                                    "meta-llama/llama-3.3-70b-instruct" -> onLlamaUsed()
                                }

                                lifecycleScope.launch {

                                    isLoading = true

                                    val aiReply =
                                        getAIResponse(
                                            userMessage,
                                            selectedModel
                                        )

                                    messages.add(
                                        ChatMessage(
                                            message = aiReply,
                                            isUser = false
                                        )
                                    )

                                    db.collection("users")
                                        .document(userId)
                                        .collection("conversations")
                                        .document(localConversationId)
                                        .collection("messages")
                                        .add(
                                            hashMapOf(
                                                "message" to aiReply,
                                                "user" to false,
                                                "timestamp" to System.currentTimeMillis()
                                            )
                                        )

                                    isLoading = false
                                }
                            }
                        },

                        enabled = !isLoading,

                        shape = CircleShape,

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C4DFF)
                        )
                    ) {
                        Text(
                            text = "➤",
                            fontSize = 22.sp
                        )
                    }
                }


            }
        }
    }


    @Composable
    fun FormattedMessage(text: String) {

        val lines = text.split("\n")

        Column {

            lines.forEach { line ->

                when {

                    line.startsWith("##") -> {

                        Text(
                            text = line.removePrefix("##").trim(),
                            color = Color(0xFFBB86FC),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    line.startsWith("#") -> {

                        Text(
                            text = line.removePrefix("#").trim(),
                            color = Color(0xFFCDA4FF),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    line.startsWith("- ") -> {

                        Text(
                            text = "▸ " + line.removePrefix("- "),
                            color = Color(0xFF81D4FA),
                            fontSize = 17.sp
                        )
                    }

                    else -> {

                        Text(
                            text = line
                                .replace("**", ""),
                            color = TextColor,
                            fontSize = 18.sp,
                            fontWeight =
                                if (line.contains("**"))
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(2.dp)
                )
            }
        }
    }

    private suspend fun getAIResponse(
        prompt: String,
        model: String
    ): String {

        return try {
            Log.d("MODEL_TEST", "Using model = $model")
            val request = ChatRequest(

                model = model,

                messages = listOf(

                    MessageData(
                        role = "user",
                        content = prompt
                    )
                )
            )


            val response =
                RetrofitClient.api.getChatResponse(

                    auth = "Bearer ${BuildConfig.OPENROUTER_API_KEY}",
                    request = request
                )

            val aiMessage =
                response.choices
                    .firstOrNull()
                    ?.message
                    ?.content

            if (aiMessage.isNullOrBlank()) {

                "Sorry, I couldn't understand that."

            } else {

                aiMessage
            }

        } catch (e: Exception) {

            e.printStackTrace()

            Log.e("API_ERROR", e.toString())
            "Error: ${e.message}"
        }
    }
}



