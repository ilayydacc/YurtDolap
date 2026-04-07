package com.yurtdolap.app.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.presentation.designsystem.components.YurtPrimaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtSecondaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.components.YurtDropdown
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.ErrorRed
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SecondaryLavender
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profili Duzenle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !state.isSaving) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDarkPurple,
                    navigationIconContentColor = TextDarkPurple
                )
            )
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundWhite),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryLilac)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditProfileHero(
                name = state.name,
                dormitory = state.dormitory
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceLight,
                border = BorderStroke(1.dp, OutlineSoft)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Isim",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextDarkPurple,
                            fontWeight = FontWeight.Bold
                        )
                        YurtTextField(
                            value = state.name,
                            onValueChange = viewModel::onNameChange,
                            placeholder = "Ad Soyad",
                            isError = state.errorMessage != null && state.name.isBlank()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Sehir",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextDarkPurple,
                            fontWeight = FontWeight.Bold
                        )
                        YurtDropdown(
                            selectedValue = state.city,
                            onValueChange = viewModel::onCityChange,
                            options = state.cities,
                            placeholder = "Sehir sec",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Sehri degistirirsen yurt listesi de o sehre gore yenilenir.",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextDarkPurple.copy(alpha = 0.62f)
                        )
                    }

                    if (state.cities.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = ErrorRed.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.18f))
                        ) {
                            Text(
                                text = "Firestore'da sehir listesi bulunamadi.",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = ErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Yurt",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextDarkPurple,
                            fontWeight = FontWeight.Bold
                        )
                        YurtDropdown(
                            selectedValue = state.dormitory,
                            onValueChange = viewModel::onDormitoryChange,
                            options = state.dormitories,
                            placeholder = if (state.isDormitoriesLoading) "Yurtlar yukleniyor..." else "Yurt sec",
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!state.isDormitoriesLoading && state.city.isNotBlank() && state.dormitories.isEmpty()) {
                            Text(
                                text = "Bu sehir icin Firestore'da yurt kaydi bulunamadi.",
                                style = MaterialTheme.typography.labelMedium,
                                color = ErrorRed
                            )
                        }
                    }

                    if (state.errorMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = ErrorRed.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = state.errorMessage.orEmpty(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = ErrorRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = PrimaryLilac.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, PrimaryLilac.copy(alpha = 0.18f))
            ) {
                Text(
                    text = "Yurt bilgisini sadece kayit olurken sectigin sehirdeki yurtlardan biriyle degistirebilirsin.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDarkPurple.copy(alpha = 0.74f),
                    textAlign = TextAlign.Start
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                YurtSecondaryButton(
                    text = "Vazgec",
                    onClick = onNavigateBack,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
                YurtPrimaryButton(
                    text = if (state.isSaving) "Kaydediliyor..." else "Kaydet",
                    onClick = { viewModel.saveProfile() },
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EditProfileHero(
    name: String,
    dormitory: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineSoft.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryLilac, SecondaryLavender)
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SurfaceLight.copy(alpha = 0.96f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.trim().take(1).ifBlank { "?" }.uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    color = PrimaryLilac,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { "Ad Soyad" },
                    style = MaterialTheme.typography.titleLarge,
                    color = SurfaceLight,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dormitory.ifBlank { "Yurt adi" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SurfaceLight.copy(alpha = 0.9f)
                )
            }
        }
    }
}
