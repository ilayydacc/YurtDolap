package com.yurtdolap.app.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.R
import com.yurtdolap.app.presentation.designsystem.components.UIState
import com.yurtdolap.app.presentation.designsystem.components.YurtPrimaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.theme.*

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is UIState.Success) {
            viewModel.resetState()
            onLoginSuccess()
        } else if (uiState is UIState.Error) {
            Toast.makeText(context, (uiState as UIState.Error).message, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Logo / App Name Appears here
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(PrimaryLilac.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home), // Placeholder for logo
                contentDescription = "Logo",
                modifier = Modifier.size(64.dp),
                tint = PrimaryLilac
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "YurtDolap'a Hoşgeldin!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextDarkPurple,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Kampüsün ikinci el pazarına giriş yap.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextDarkPurple.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        YurtTextField(
            value = formState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            placeholder = "Örn: ogrenci@edu.tr",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        YurtTextField(
            value = formState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            placeholder = "Şifre",
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        YurtPrimaryButton(
            text = if (uiState is UIState.Loading) "Giriş Yapılıyor..." else "Giriş Yap",
            onClick = { viewModel.login() },
            enabled = uiState !is UIState.Loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hesabın yok mu? ",
                color = TextDarkPurple.copy(alpha = 0.6f)
            )
            Text(
                text = "Kayıt Ol",
                color = PrimaryLilac,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// Temporary for compilation before Logo is correctly imported
@Composable
fun Icon(painter: androidx.compose.ui.graphics.painter.Painter, contentDescription: String, modifier: Modifier, tint: androidx.compose.ui.graphics.Color) {
    androidx.compose.material3.Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
