package com.example.todolistapp.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolistapp.ui.theme.*
import com.example.todolistapp.viewmodel.AuthViewModel
import com.example.todolistapp.viewmodel.ProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val userData     by authViewModel.userData.collectAsState()
    val profileState by authViewModel.profileState.collectAsState()
    val context      = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Profile", "Password")

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { authViewModel.loadUserData() }

    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.Success -> {
                snackbarHostState.showSnackbar("✓ Changes saved successfully!")
                authViewModel.resetProfileState()
            }
            is ProfileState.Error -> {
                snackbarHostState.showSnackbar("⚠ ${(profileState as ProfileState.Error).message}")
                authViewModel.resetProfileState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BgDark,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceLight,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Account Settings",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Avatar banner ──
            AvatarSection(
                avatarUrl   = userData?.avatarUrl ?: "",
                displayName = userData?.name ?: authViewModel.currentUser?.displayName ?: "User",
                email       = userData?.email ?: authViewModel.currentUser?.email ?: "",
                isLoading   = profileState is ProfileState.Loading,
                context     = context,
                onPickImage = { uri -> authViewModel.uploadAvatar(context, uri) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Tabs ──
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = SurfaceDark,
                contentColor     = Purple,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Purple
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text = {
                            Text(
                                tab,
                                color      = if (selectedTab == index) Purple else TextSecondary,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize   = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (selectedTab) {
                0 -> ProfileTab(
                    currentName  = userData?.name  ?: authViewModel.currentUser?.displayName ?: "",
                    currentEmail = userData?.email ?: authViewModel.currentUser?.email ?: "",
                    isLoading    = profileState is ProfileState.Loading,
                    onUpdateName  = { authViewModel.updateName(it) },
                    onUpdateEmail = { pwd, email -> authViewModel.updateEmail(pwd, email) }
                )
                1 -> PasswordTab(
                    authViewModel    = authViewModel,
                    isLoading        = profileState is ProfileState.Loading,
                    onChangePassword = { cur, new, confirm ->
                        authViewModel.changePassword(cur, new, confirm)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Logout ──
            Button(
                onClick = { authViewModel.logout(); onLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red.copy(alpha = 0.12f),
                    contentColor   = Red
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Avatar Section ──────────────────────────────────────────────────────────
@Composable
fun AvatarSection(
    avatarUrl   : String,
    displayName : String,
    email       : String,
    isLoading   : Boolean,
    context     : Context,
    onPickImage : (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onPickImage(it) } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {

            // Avatar image or initial
            if (avatarUrl.startsWith("data:image")) {
                // Decode base64
                val base64 = avatarUrl.substringAfter("base64,")
                val bytes  = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap             = bitmap.asImageBitmap(),
                        contentDescription = "Avatar",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(3.dp, Purple, CircleShape)
                    )
                } else {
                    AvatarPlaceholder(displayName)
                }
            } else {
                AvatarPlaceholder(displayName)
            }

            // Camera icon overlay
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Purple, CircleShape)
                    .border(2.5.dp, BgDark, CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Change photo",
                        tint     = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(email, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
fun AvatarPlaceholder(displayName: String) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(
                Brush.linearGradient(listOf(Purple, PurpleLight)),
                CircleShape
            )
            .border(3.dp, Purple, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
            fontSize   = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = Color.White
        )
    }
}

// ── Profile Tab ─────────────────────────────────────────────────────────────
@Composable
fun ProfileTab(
    currentName  : String,
    currentEmail : String,
    isLoading    : Boolean,
    onUpdateName  : (String) -> Unit,
    onUpdateEmail : (currentPwd: String, newEmail: String) -> Unit
) {
    var name              by remember(currentName) { mutableStateOf(currentName) }
    var newEmail          by remember { mutableStateOf("") }
    var emailPassword     by remember { mutableStateOf("") }
    var showEmailSection  by remember { mutableStateOf(false) }
    var emailPwdVisible   by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Name card
        ProfileCard(title = "Display Name", icon = Icons.Default.Person) {
            ProfileFieldLabel("CURRENT NAME")
            ProfileOutlinedField(
                value         = name,
                onValueChange = { name = it },
                placeholder   = "Your display name"
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileSaveButton(
                text      = "Update Name",
                enabled   = !isLoading && name.isNotBlank() && name != currentName,
                isLoading = isLoading,
                onClick   = { onUpdateName(name) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Email card
        ProfileCard(title = "Email Address", icon = Icons.Default.Email) {
            ProfileFieldLabel("CURRENT EMAIL")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Text(currentEmail, color = TextSecondary, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Email address cannot be changed for security reasons.",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// ── Password Tab ─────────────────────────────────────────────────────────────
@Composable
fun PasswordTab(
    authViewModel: AuthViewModel, // Added to allow sending reset email
    isLoading: Boolean,
    onChangePassword: (current: String, new: String, confirm: String) -> Unit
) {
    var currentPwd  by remember { mutableStateOf("") }
    var newPwd      by remember { mutableStateOf("") }
    var confirmPwd  by remember { mutableStateOf("") }
    var curVisible  by remember { mutableStateOf(false) }
    var newVisible  by remember { mutableStateOf(false) }
    var conVisible  by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var showResetMsg by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        ProfileCard(title = "Change Password", icon = Icons.Default.Lock) {
            ProfileFieldLabel("CURRENT PASSWORD")
            ProfileOutlinedField(
                value            = currentPwd,
                onValueChange    = { currentPwd = it },
                placeholder      = "Enter current password",
                isPassword       = true,
                passwordVisible  = curVisible,
                onTogglePassword = { curVisible = !curVisible }
            )

            TextButton(
                onClick = {
                    val email = authViewModel.currentUser?.email
                    if (email != null) {
                        authViewModel.sendPasswordReset(email) { success, msg ->
                            showResetMsg = true
                        }
                    }
                },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Forgot current password?", color = Purple, fontSize = 12.sp)
            }

            if (showResetMsg) {
                Text(
                    "Reset link sent to your email!",
                    color = Green,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileFieldLabel("NEW PASSWORD")
            ProfileOutlinedField(
                value            = newPwd,
                onValueChange    = { newPwd = it },
                placeholder      = "At least 6 characters",
                isPassword       = true,
                passwordVisible  = newVisible,
                onTogglePassword = { newVisible = !newVisible }
            )

            // Strength indicator
            if (newPwd.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthBar(password = newPwd)
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileFieldLabel("CONFIRM NEW PASSWORD")
            ProfileOutlinedField(
                value            = confirmPwd,
                onValueChange    = { confirmPwd = it },
                placeholder      = "Re-enter new password",
                isPassword       = true,
                passwordVisible  = conVisible,
                onTogglePassword = { conVisible = !conVisible }
            )

            if (confirmPwd.isNotEmpty() && newPwd != confirmPwd) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("⚠ Passwords do not match", color = Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSaveButton(
                text      = "Change Password",
                enabled   = !isLoading && currentPwd.isNotBlank() && newPwd.isNotBlank() && confirmPwd.isNotBlank(),
                isLoading = isLoading,
                onClick   = {
                    onChangePassword(currentPwd, newPwd, confirmPwd)
                    currentPwd = ""; newPwd = ""; confirmPwd = ""
                }
            )
        }
    }
}

// ── Shared UI helpers ────────────────────────────────────────────────────────

@Composable
fun ProfileCard(
    title   : String,
    icon    : ImageVector,
    content : @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Purple.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Purple, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

@Composable
fun ProfileFieldLabel(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.labelMedium,
        color    = TextSecondary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileOutlinedField(
    value            : String,
    onValueChange    : (String) -> Unit,
    placeholder      : String,
    isPassword       : Boolean = false,
    passwordVisible  : Boolean = false,
    onTogglePassword : (() -> Unit)? = null
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(10.dp),
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon  = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Purple,
            unfocusedBorderColor = BorderColor,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = Purple,
            focusedContainerColor   = BgDark,
            unfocusedContainerColor = BgDark
        )
    )
}

@Composable
fun ProfileSaveButton(
    text      : String,
    enabled   : Boolean,
    isLoading : Boolean,
    onClick   : () -> Unit
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape    = RoundedCornerShape(10.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = Purple)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color       = Color.White,
                modifier    = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun PasswordStrengthBar(password: String) {
    val strength = when {
        password.length >= 10 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() } -> 3 // Strong
        password.length >= 8 &&
                (password.any { it.isUpperCase() } || password.any { it.isDigit() }) -> 2 // Medium
        else -> 1 // Weak
    }
    val (label, color, fraction) = when (strength) {
        3    -> Triple("Strong",  Green,  1.0f)
        2    -> Triple("Medium",  Yellow, 0.6f)
        else -> Triple("Weak",    Red,    0.3f)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Password strength", color = TextMuted, fontSize = 11.sp)
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(BorderColor, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}