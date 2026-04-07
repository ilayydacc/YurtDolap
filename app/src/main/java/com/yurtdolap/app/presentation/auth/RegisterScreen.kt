package com.yurtdolap.app.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.presentation.designsystem.components.UIState
import com.yurtdolap.app.presentation.designsystem.components.YurtDropdown
import com.yurtdolap.app.presentation.designsystem.components.YurtPrimaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is UIState.Success) {
            viewModel.resetState()
            Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
            onRegisterSuccess()
        } else if (uiState is UIState.Error) {
            Toast.makeText(context, (uiState as UIState.Error).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBackToLogin) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    navigationIconContentColor = TextDarkPurple
                )
            )
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hesap Oluştur",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            Text(
                text = "Bilgilerini girerek hemen kullanmaya başla.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextDarkPurple.copy(alpha = 0.6f),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )

            // Name
            YurtTextField(
                value = formState.name,
                onValueChange = { viewModel.onNameChange(it) },
                placeholder = "Adınız ve Soyadınız",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Email
            YurtTextField(
                value = formState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                placeholder = "E-posta Adresi",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password
            YurtTextField(
                value = formState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                placeholder = "Şifre (En az 6 karakter)",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val cities by viewModel.cities.collectAsState()
            val dormitories by viewModel.dormitories.collectAsState()

            // City
            YurtDropdown(
                selectedValue = formState.city,
                onValueChange = { viewModel.onCityChange(it) },
                options = cities,
                placeholder = "Şehir Seçiniz",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Dormitory
            YurtDropdown(
                selectedValue = formState.dormitory,
                onValueChange = { viewModel.onDormitoryChange(it) },
                options = dormitories,
                placeholder = "Kaldığınız Yurt",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            YurtPrimaryButton(
                text = if (uiState is UIState.Loading) "Kaydediliyor..." else "Kayıt Ol",
                onClick = { viewModel.register() },
                enabled = uiState !is UIState.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Zaten hesabın var mı? ",
                    color = TextDarkPurple.copy(alpha = 0.6f)
                )
                Text(
                    text = "Giriş Yap",
                    color = PrimaryLilac,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateBackToLogin() }
                )
            }
        }
    }
}
