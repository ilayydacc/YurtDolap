package com.yurtdolap.app.presentation.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.ProductTags
import coil.compose.AsyncImage
import com.yurtdolap.app.presentation.designsystem.components.UIState
import com.yurtdolap.app.presentation.designsystem.components.YurtPrimaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current
    val isNeedRequest = formState.selectedTag == ProductTags.NEED_REQUEST

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.onImageSelect(uri)
    }

    LaunchedEffect(uiState) {
        if (uiState is UIState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNeedRequest) "Talebi Düzenle" else "İlanı Düzenle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDarkPurple
                )
            )
        }
    ) { innerPadding ->
        if (uiState is UIState.Loading && formState.title.isEmpty() && formState.price.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryLilac)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundWhite)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Image Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceLight)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (formState.imageUri != null) {
                        AsyncImage(
                            model = formState.imageUri,
                            contentDescription = "Seçilen Fotoğraf",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (formState.existingImageUrl != null) {
                        AsyncImage(
                            model = formState.existingImageUrl,
                            contentDescription = "Mevcut Fotoğraf",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Fotoğraf Seç / Değiştir",
                                tint = PrimaryLilac,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (isNeedRequest) "Referans Görseli Değiştirmek İçin Dokun" else "Fotoğrafı Değiştirmek İçin Dokun",
                                color = PrimaryLilac,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = if (isNeedRequest) "Talep Başlığı" else "İlan Başlığı", fontWeight = FontWeight.Bold, color = TextDarkPurple)
                Spacer(modifier = Modifier.height(8.dp))
                YurtTextField(
                    value = formState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    placeholder = if (isNeedRequest) "Örn: Aynı yurttan ikinci el kettle arıyorum" else "Örn: masa lambası"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Açıklama (opsiyonel)", fontWeight = FontWeight.Bold, color = TextDarkPurple)
                Spacer(modifier = Modifier.height(8.dp))
                YurtTextField(
                    value = formState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    placeholder = if (isNeedRequest) {
                        "Örn: Aynı yurttan alabilirim, hafta sonu lazım."
                    } else {
                        "Örn: Temiz kullanıldı, kutusu duruyor."
                    },
                    singleLine = false,
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = if (isNeedRequest) "Bütçe / Süre (opsiyonel)" else "Fiyat (₺)", fontWeight = FontWeight.Bold, color = TextDarkPurple)
                Spacer(modifier = Modifier.height(8.dp))
                YurtTextField(
                    value = formState.price,
                    onValueChange = { viewModel.onPriceChange(it) },
                    placeholder = if (isNeedRequest) "Örn: 250 TL altı veya 3 günlüğüne" else "Örn: 500"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Teslim Uygunlugu (opsiyonel)", fontWeight = FontWeight.Bold, color = TextDarkPurple)
                Spacer(modifier = Modifier.height(8.dp))
                YurtTextField(
                    value = formState.deliveryPreference,
                    onValueChange = { viewModel.onDeliveryPreferenceChange(it) },
                    placeholder = "Orn: aksam teslim, gece uygun, hafta sonu uygun"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Kategori", fontWeight = FontWeight.Bold, color = TextDarkPurple)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    formState.categories.take(3).forEach { category ->
                        CategoryChip(
                            text = category.name,
                            isSelected = formState.selectedCategoryId == category.id,
                            onClick = { viewModel.onCategorySelect(category.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Paylaşım Türü", fontWeight = FontWeight.Bold, color = TextDarkPurple)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = formState.selectedTag == ProductTags.FOR_SALE,
                            onClick = { viewModel.onTagSelect(ProductTags.FOR_SALE) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryLilac)
                        )
                        Text(ProductTags.FOR_SALE, color = TextDarkPurple)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = formState.selectedTag == ProductTags.FOR_RENT,
                            onClick = { viewModel.onTagSelect(ProductTags.FOR_RENT) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryLilac)
                        )
                        Text(ProductTags.FOR_RENT, color = TextDarkPurple)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = formState.selectedTag == ProductTags.NEED_REQUEST,
                            onClick = { viewModel.onTagSelect(ProductTags.NEED_REQUEST) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryLilac)
                        )
                        Text(ProductTags.NEED_REQUEST, color = TextDarkPurple)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                YurtPrimaryButton(
                    text = if (uiState is UIState.Loading) "Güncelleniyor..." else "Değişiklikleri Kaydet",
                    onClick = { viewModel.updateProduct(context) },
                    enabled = uiState !is UIState.Loading
                )
            }
        }
    }
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PrimaryLilac else OutlineSoft)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) SurfaceLight else TextDarkPurple,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
