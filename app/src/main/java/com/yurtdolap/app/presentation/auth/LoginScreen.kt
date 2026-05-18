package com.yurtdolap.app.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

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
            Toast.makeText(
                context,
                (uiState as UIState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_brand_logo),
                contentDescription = "YurtDolap logosu",
                modifier = Modifier.size(108.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "YurtDolap",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )

            Text(
                text = "Yurttaki ikinci el alışveriş için giriş yap.",
                modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = TextDarkPurple.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )

            YurtTextField(
                value = formState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                placeholder = "Örn: ogrenci@edu.tr",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            YurtTextField(
                value = formState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                placeholder = "Şifre",
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            YurtPrimaryButton(
                text = if (uiState is UIState.Loading) "Giriş yapılıyor..." else "Giriş Yap",
                onClick = { viewModel.login() },
                enabled = uiState !is UIState.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

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
        }
    }
}
