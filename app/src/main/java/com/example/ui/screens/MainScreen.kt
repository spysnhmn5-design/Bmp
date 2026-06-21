package com.example.ui.screens

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.data.model.CommunityMessage
import com.example.data.model.WebProject
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

// Style Color Palette - Frosted Glass Inspired Aesthetics
val DeepDarkBg = Color(0xFF0A0C10)       // Main backdrop oscuro
val SurfaceCardBg = Color(0x14FFFFFF)    // Frosted glass panel (White 8% with transparency)
val BorderHighlight = Color(0x1CFFFFFF)  // Glare border highlight (White 11%)
val NeonCyan = Color(0xFF818CF8)         // Elegant Indigo 400 accent
val NeonIndigo = Color(0xFFA5B4FC)       // Indigo 300 accent
val SecureOrange = Color(0xFFFB923C)     // Orange 400 status alert
val ErrorRed = Color(0xFFF87171)         // Red 400 error accent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val screen by viewModel.currentScreen.collectAsState()
    val isCrawling by viewModel.isCrawling.collectAsState()
    val crawlProgress by viewModel.crawlProgress.collectAsState()
    val targetUrlInput by viewModel.targetUrlInput.collectAsState()
    val showCrawlDialog by viewModel.showCrawlDialog.collectAsState()
    val projectLockedForVerification by viewModel.projectLockedForVerification.collectAsState()

    // Base Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    ) {
                        Text(
                            text = "SiteVault",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ארכיון חכם",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Offline Vault",
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (screen != Screen.DASHBOARD) {
                        IconButton(
                            onClick = { viewModel.currentScreen.value = Screen.DASHBOARD },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "חזרה למסך הבית",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigation(
                currentScreen = screen,
                onNavigate = { viewModel.currentScreen.value = it }
            )
        },
        containerColor = DeepDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepDarkBg)
        ) {
            // Elegant Background Aura Blur Circle - Top Left (Indigo-600/20 glow)
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-100).dp, y = (-100).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x334F46E5), Color.Transparent)
                        )
                    )
            )

            // Elegant Background Aura Blur Circle - Bottom Right (Purple-600/20 glow)
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 100.dp, y = 100.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x2AA855F7), Color.Transparent)
                        )
                    )
            )
            // Screen router with clean slide animations
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.DASHBOARD -> DashboardView(viewModel)
                    Screen.PROJECT_VIEWER -> ProjectDetailsView(viewModel)
                    Screen.COMMUNITY_HUB -> CommunityHubView(viewModel)
                    Screen.EXPERIMENTAL_LAB -> ExperimentalLabView(viewModel)
                }
            }

            // Real-Time Crawl Activity Dialog Overlay
            if (showCrawlDialog) {
                CrawlDialog(
                    urlInput = targetUrlInput,
                    onUrlChange = { viewModel.targetUrlInput.value = it },
                    isCrawling = isCrawling,
                    progress = crawlProgress,
                    onStartCrawl = { viewModel.startCrawl(targetUrlInput) },
                    onDismiss = { viewModel.showCrawlDialog.value = false }
                )
            }

            // Secure PIN Decryption keypad overlay
            if (projectLockedForVerification != null) {
                PinDecryptionBarrier(
                    project = projectLockedForVerification!!,
                    pinInput = viewModel.pinInput.collectAsState().value,
                    onPinChange = { viewModel.pinInput.value = it },
                    isError = viewModel.pinError.collectAsState().value,
                    onSubmit = { viewModel.submitPin(it) },
                    onCancel = { viewModel.cancelPinVerification() }
                )
            }
        }
    }
}

@Composable
fun BottomNavigation(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.04f),
        tonalElevation = 0.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .border(
                width = 1.dp,
                color = BorderHighlight,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.DASHBOARD || currentScreen == Screen.PROJECT_VIEWER,
            onClick = { onNavigate(Screen.DASHBOARD) },
            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "הכספת שלי") },
            label = { Text("הכספת שלי", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonCyan,
                selectedTextColor = NeonCyan,
                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                unselectedTextColor = Color.White.copy(alpha = 0.4f),
                indicatorColor = Color.White.copy(alpha = 0.08f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.COMMUNITY_HUB,
            onClick = { onNavigate(Screen.COMMUNITY_HUB) },
            icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "קהילת דיון") },
            label = { Text("קהילה", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonCyan,
                selectedTextColor = NeonCyan,
                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                unselectedTextColor = Color.White.copy(alpha = 0.4f),
                indicatorColor = Color.White.copy(alpha = 0.08f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.EXPERIMENTAL_LAB,
            onClick = { onNavigate(Screen.EXPERIMENTAL_LAB) },
            icon = { Icon(imageVector = Icons.Default.Build, contentDescription = "ניסיוני") },
            label = { Text("מעבדה", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonCyan,
                selectedTextColor = NeonCyan,
                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                unselectedTextColor = Color.White.copy(alpha = 0.4f),
                indicatorColor = Color.White.copy(alpha = 0.08f)
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardView(viewModel: MainViewModel) {
    val projects by viewModel.projects.collectAsState()

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (projects.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "סורק ריק",
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(82.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "אין עדיין אתרים סרוקים בכספת",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "לחץ על כפתור הפלוס (+) למטה, הזן קישור URL וסרוק אתר שלם לצפייה מוחלטת באופליין!",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "פרויקטים סרוקים באופליין (${projects.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    textAlign = TextAlign.End
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectItemCard(
                            project = project,
                            onSelect = { viewModel.selectProject(project) },
                            onDelete = { viewModel.deleteProject(project) }
                        )
                    }
                }
            }
        }

        // Floating Action Scan Button
        FloatingActionButton(
            onClick = {
                viewModel.targetUrlInput.value = ""
                viewModel.crawlProgress.value = ""
                viewModel.showCrawlDialog.value = true
            },
            containerColor = NeonCyan,
            contentColor = DeepDarkBg,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "הוסף אתר חדש",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ProjectItemCard(
    project: WebProject,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(width = 1.dp, color = BorderHighlight, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showDeleteConfirm = true },
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Gray.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "מחק פרויקט",
                    tint = ErrorRed
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (project.passcode != null) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "מוגן בPIN",
                            tint = SecureOrange,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = project.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.url,
                    fontSize = 13.sp,
                    color = NeonIndigo,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "נסרק בתאריך: " + formatDate(project.scannedAt),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(width = 1.dp, color = BorderHighlight, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (project.passcode != null) Icons.Default.Lock else Icons.Default.Home,
                    contentDescription = "סוג",
                    tint = if (project.passcode != null) SecureOrange else NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("מחק לצמיתות", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("ביטול", color = Color.Gray)
                }
            },
            title = {
                Text(
                    text = "מחיקת אתר סרוק?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            text = {
                Text(
                    text = "האם אתה בטוח שברצונך למחוק את '${project.title}' מתוך זיכרון האפליקציה? כל התמונות והתוכן יימחקו לחלוטין באופליין.",
                    color = Color.LightGray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            },
            containerColor = SurfaceCardBg
        )
    }
}

@Composable
fun CrawlDialog(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    isCrawling: Boolean,
    progress: String,
    onStartCrawl: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isCrawling) onDismiss() },
        confirmButton = {
            if (!isCrawling) {
                Button(
                    onClick = onStartCrawl,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepDarkBg)
                ) {
                    Text("התחל סריקה אופליין", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isCrawling) {
                TextButton(onClick = onDismiss) {
                    Text("ביטול", color = Color.Gray)
                }
            }
        },
        title = {
            Text(
                text = "סריקת אתר אינטרנט חדש",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "הזן כתובת אתר מלאה. המערכת תוריד את קוד ה-HTML, תחלץ את כל התמונות והמדיה ותבצע המרה מלאה לשימוש אופליין עצמאי.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    placeholder = { Text("https://example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, textAlign = TextAlign.Left),
                    enabled = !isCrawling,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderHighlight,
                        disabledBorderColor = BorderHighlight,
                        focusedLabelColor = NeonCyan
                    )
                )

                if (isCrawling || progress.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = progress,
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        containerColor = SurfaceCardBg
    )
}

@Composable
fun PinDecryptionBarrier(
    project: WebProject,
    pinInput: String,
    onPinChange: (String) -> Unit,
    isError: Boolean,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .border(width = 2.dp, color = SecureOrange, shape = RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "אבטחה",
                    tint = SecureOrange,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "קובץ אתר נעול ומאובטח",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "פרויקט '${project.title}' פוצל ומאובטח עם קוד גישה מקומי. הזן את קוד ה-PIN כדי לגשת למידע:",
                    fontSize = 14.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = onPinChange,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    placeholder = { Text("קוד PIN") },
                    modifier = Modifier.fillMaxWidth(0.6f),
                    textStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center, fontSize = 20.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecureOrange,
                        unfocusedBorderColor = BorderHighlight
                    )
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "קוד PIN שגוי! נא לנסות שנית.",
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onSubmit(pinInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = SecureOrange, contentColor = Color.White)
                    ) {
                        Text("פענוח ופתיחה", fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = onCancel) {
                        Text("ביטול", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectDetailsView(viewModel: MainViewModel) {
    val project by viewModel.selectedProject.collectAsState()
    if (project == null) return

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("אבטחה והגדרות", "🤖 עוזר AI", "דפדפן אופליין")

    Column(modifier = Modifier.fillMaxSize()) {
        // Project Title Header
        Surface(
            color = SurfaceCardBg,
            modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = BorderHighlight)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = project!!.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = project!!.url,
                    fontSize = 12.sp,
                    color = NeonIndigo,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }

        // Custom segmented tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DeepDarkBg,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyan
                )
            }
        ) {
            // Note: We render tabs from index 2 to 0 to align with Hebrew Reading Direction RTL
            tabTitles.reversed().forEachIndexed { index, title ->
                // Actual selected tab maps appropriately back
                val actualIndex = tabTitles.size - 1 - index
                Tab(
                    selected = selectedTab == actualIndex,
                    onClick = { selectedTab = actualIndex },
                    text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> SecureSettingsSection(project!!, viewModel)
                1 -> AiAssistantSection(project!!, viewModel)
                2 -> OfflineBrowserSection(project!!, viewModel)
            }
        }
    }
}

@Composable
fun OfflineBrowserSection(project: WebProject, viewModel: MainViewModel) {
    val context = LocalContext.current
    var viewingModeText by remember { mutableStateOf(false) } // true for static text reader fallback file representation, false for raw WebView rendering

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle view types bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("מצב קריאה טקסטואלי", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = viewingModeText,
                    onCheckedChange = { viewingModeText = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                )
            }

            Text("מצב תצוגת אינטרנט", fontSize = 12.sp, color = Color.Gray)
        }

        if (viewingModeText) {
            // Clean Text representation with gorgeous margins, readable in airplane mode
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                item {
                    Text(
                        text = project.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = project.textContent.ifBlank { "אין תוכן לקריאה זמין." },
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // Raw HTML offline browser
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return false // prevent loading outside our vault browser
                            }
                        }
                        
                        settings.apply {
                            javaScriptEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            domStorageEnabled = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }
                    }
                },
                update = { webView ->
                    // Load the in-memory modified offline HTML (referencing absolute file links)
                    webView.loadDataWithBaseURL(
                        "file:///android_asset/", 
                        project.htmlContent, 
                        "text/html", 
                        "UTF-8", 
                        null
                    )
                },
                modifier = Modifier.fillMaxSize().background(Color.White)
            )
        }
    }
}

@Composable
fun AiAssistantSection(project: WebProject, viewModel: MainViewModel) {
    val aiPromptInput by viewModel.aiPromptInput.collectAsState()
    val aiIsGenerating by viewModel.aiIsGenerating.collectAsState()
    val aiChats by viewModel.projectAiChats.collectAsState()
    
    val listState = rememberLazyListState()
    val currentHistory = aiChats[project.id] ?: emptyList()

    // Smooth scroll chat down to latest questions
    LaunchedEffect(currentHistory.size) {
        if (currentHistory.isNotEmpty()) {
            listState.animateScrollToItem(currentHistory.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Chat list area
        if (currentHistory.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "רובוט",
                        tint = NeonCyan,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "עוזר ה-AI של WebVault מוכן לגמרי!",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "אנא שאל כל שאלה לגבי האתר, ואנתח אותו אופליין עבורך.",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentHistory) { chatTurn ->
                    // User Question bubble (Right aligned)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            modifier = Modifier
                                .background(NeonIndigo, shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp))
                                .padding(12.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Text(text = chatTurn.first, color = Color.White, fontSize = 14.sp)
                        }
                        Text(text = "אתה", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp, end = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // AI response bubble (Left aligned)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(SurfaceCardBg, shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                                .border(width = 1.dp, color = BorderHighlight, shape = RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                                .padding(12.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Text(text = chatTurn.second, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Left)
                        }
                        Text(text = "עוזר AI של Gemini", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp, start = 4.dp))
                    }
                }
            }
        }

        // Input bottom panel
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.askAiAssistant(aiPromptInput) },
                enabled = aiPromptInput.isNotBlank() && !aiIsGenerating,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (aiPromptInput.isNotBlank() && !aiIsGenerating) NeonCyan else Color.DarkGray)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "שלח שאלה",
                    tint = DeepDarkBg
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = aiPromptInput,
                onValueChange = { viewModel.aiPromptInput.value = it },
                placeholder = { Text("שאל את ה-AI משהו על האתר...") },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = Color.White, textAlign = TextAlign.End),
                singleLine = true,
                isError = aiIsGenerating,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = BorderHighlight
                )
            )
        }
    }
}

@Composable
fun SecureSettingsSection(project: WebProject, viewModel: MainViewModel) {
    var passcodeField by remember { mutableStateOf(project.passcode ?: "") }
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.End
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = BorderHighlight, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardBg)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("אבטחת המידע של הפרויקט", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SecureOrange)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "באפשרותך להגדיר קוד PIN/passcode ספציפי עבור פרויקט זה. רק לאחר הקלדת הקוד הנכון תתאפשר קריאה וגישה לעוזר ה-AI.",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = passcodeField,
                        onValueChange = { passcodeField = it },
                        placeholder = { Text("הזן קוד (למשל 1234)") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White, textAlign = TextAlign.Center),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecureOrange,
                            unfocusedBorderColor = BorderHighlight
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Button(
                            onClick = {
                                viewModel.updateProjectPasscode(project, passcodeField)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SecureOrange)
                        ) {
                            Text("שמור והחל הגנה")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { isEditing = false }) {
                            Text("ביטול", color = Color.Gray)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if (project.passcode != null) Color.DarkGray else SecureOrange)
                        ) {
                            Text(if (project.passcode != null) "שנה קוד גישה" else "הפעל נעילת PIN")
                        }

                        Text(
                            text = if (project.passcode != null) "סטטוס: נעול מקומית" else "סטטוס: לא מוגן בקוד",
                            color = if (project.passcode != null) SecureOrange else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (project.passcode != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            viewModel.updateProjectPasscode(project, null)
                            passcodeField = ""
                        }) {
                            Text("בטל הגנת קוד PIN לחלוטין", color = ErrorRed)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Extra details statistics card
        Card(
            modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = BorderHighlight, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardBg)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("מצב חבילת המדיה הסרוקה", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("מאובטח/מוצפן", color = Color.LightGray)
                    Text(if (project.passcode != null) "כן" else "לא", color = NeonCyan)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderHighlight)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("קוד מקור HTML סרוק", color = Color.LightGray)
                    Text("${project.htmlContent.length} תווים", color = NeonIndigo)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderHighlight)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("קוד מזהה פנימי", color = Color.LightGray)
                    Text("#${project.id}", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun CommunityHubView(viewModel: MainViewModel) {
    val messages by viewModel.communityMessages.collectAsState()
    val nickname by viewModel.nicknameInput.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val isSending by viewModel.isSubmittingMessage.collectAsState()

    val chatListState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            chatListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Community Top Header with refresh button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.refreshCommunity() },
                colors = IconButtonDefaults.iconButtonColors(contentColor = NeonCyan)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "רענן")
            }

            Text(
                text = "קהילת התייעצות ופיתוח",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Profile details setup
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .border(width = 1.dp, color = BorderHighlight, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("הכינוי שלך בפורום הקהילה:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.62f))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { viewModel.nicknameInput.value = it },
                    singleLine = true,
                    textStyle = TextStyle(color = NeonCyan, textAlign = TextAlign.End, fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderHighlight,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
        }

        // Message Feed
        LazyColumn(
            state = chatListState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                CommunityMessageBubble(msg)
            }
        }

        // Message bottom send input
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.submitPost(messageInput) },
                enabled = messageInput.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (messageInput.isNotBlank() && !isSending) NeonCyan else Color.DarkGray)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "שלח פוסט",
                    tint = DeepDarkBg
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = messageInput,
                onValueChange = { viewModel.messageInput.value = it },
                placeholder = { Text("כתוב שאלה או התייעצות לפורום הכללי...") },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = Color.White, textAlign = TextAlign.End),
                singleLine = true,
                isError = isSending,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = BorderHighlight,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun CommunityMessageBubble(msg: CommunityMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderHighlight, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(msg.timestamp),
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Text(
                    text = msg.authorName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = msg.content,
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ExperimentalLabView(viewModel: MainViewModel) {
    val jsSandbox by viewModel.enableJavascriptSandbox.collectAsState()
    val autoCompress by viewModel.autoCompressImages.collectAsState()
    val deepCrawling by viewModel.deepCrawlingDepth.collectAsState()
    val darkThemeInjected by viewModel.darkThemeInjected.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "מעבדת תכונות ניסיוניות (BETA)",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = "שלוט ונהל הגדרות פיתוח וסריקה ניסיוניות. תכונות אלו עשויות להשתנות בעדכונים הבאים.",
            fontSize = 13.sp,
            color = Color.LightGray,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.End
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = BorderHighlight, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardBg),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Feature 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = jsSandbox,
                        onCheckedChange = { viewModel.enableJavascriptSandbox.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("הפעל ארגז חול Javascript", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("מריץ דפים עם תמיכה מלאה ב-JS דינמי (ניסיוני)", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.End)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderHighlight)

                // Feature 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = autoCompress,
                        onCheckedChange = { viewModel.autoCompressImages.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("כיווץ תמונות אוטומטי", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("חוסך שטח אחסון על ידי המרת תמונות ל-WebP", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.End)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderHighlight)

                // Feature 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = deepCrawling,
                        onCheckedChange = { viewModel.deepCrawlingDepth.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("סריקה רב-שכבתית (Deep Crawl)", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("סורק ומוריד דפי משנה מקושרים נוספים", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.End)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderHighlight)

                // Feature 4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = darkThemeInjected,
                        onCheckedChange = { viewModel.darkThemeInjected.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("הזרקת עיצוב כהה (Dark Injection)", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("מאלץ צבעי רקע כהה על HTMLים סרוקים", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(date)
}
