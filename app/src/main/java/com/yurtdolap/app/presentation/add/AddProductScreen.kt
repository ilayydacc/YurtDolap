package com.yurtdolap.app.presentation.add

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.ProductTags
import coil.compose.AsyncImage
import com.yurtdolap.app.presentation.designsystem.components.UIState
import com.yurtdolap.app.presentation.designsystem.components.YurtPrimaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddProductViewModel = hiltViewModel()
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
            Toast.makeText(context, if (isNeedRequest) "Talep başarıyla eklendi!" else "İlan başarıyla eklendi!", Toast.LENGTH_SHORT).show()
            onNavigateBack() // Go back or home after adding successfully
        } else if (uiState is UIState.Error) {
            Toast.makeText(context, (uiState as UIState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNeedRequest) "Yeni Talep Ekle" else "Yeni İlan Ekle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDarkPurple,
                    navigationIconContentColor = TextDarkPurple
                )
            )
        },
        containerColor = BackgroundWhite,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                YurtPrimaryButton(
                    text = if (uiState is UIState.Loading) "Paylaşılıyor..." else if (isNeedRequest) "Talebi Paylaş" else "İlanı Paylaş",
                    onClick = { viewModel.addProduct(context) },
                    enabled = uiState !is UIState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Text(
                text = "Paylaşım Türü",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(12.dp))
            SegmentedTagControl(
                selectedTag = formState.selectedTag,
                onTagSelected = { viewModel.onTagSelect(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Kategori Seçimi
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                item { Spacer(modifier = Modifier.width(24.dp)) }
                items(formState.categories, key = { it.id }) { category ->
                    val isSelected = formState.selectedCategoryId == category.id
                    val bgColor = if (isSelected) PrimaryLilac else SurfaceLight
                    val textColor = if (isSelected) BackgroundWhite else PrimaryLilac

                    Box(
                        modifier = Modifier
                            .background(bgColor, RoundedCornerShape(12.dp))
                            .clickable { viewModel.onCategorySelect(category.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isNeedRequest) "Talep Başlığı" else "İlan Başlığı",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            YurtTextField(
                value = formState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                placeholder = if (isNeedRequest) "Örn: 3 günlüğüne hesap makinesi lazım" else "Örn: Az kullanılmış çalışma masası"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Açıklama (opsiyonel)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            YurtTextField(
                value = formState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                placeholder = if (isNeedRequest) {
                    "Örn: Final haftası için 3 günlüğüne lazım, aynı yurttan teslim alabilirim."
                } else {
                    "Örn: Az kullanıldı, çizik yok, akşam teslim edebilirim."
                },
                singleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isNeedRequest) "Bütçe / Süre (opsiyonel)" else "Fiyat (₺)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            YurtTextField(
                value = formState.price,
                onValueChange = { viewModel.onPriceChange(it) },
                placeholder = if (isNeedRequest) "Örn: 250 TL altı veya 3 günlüğüne" else "Örn: 250"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Teslim Uygunlugu (opsiyonel)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            YurtTextField(
                value = formState.deliveryPreference,
                onValueChange = { viewModel.onDeliveryPreferenceChange(it) },
                placeholder = "Orn: aksam teslim, gece uygun, hafta sonu uygun"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isNeedRequest) "Referans Fotoğrafı (opsiyonel)" else "Ürün Fotoğrafı",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Resim iconu veya "+" işareti eklenebilir
                        Text(
                            if (isNeedRequest) "İstersen Referans Görsel Seç" else "Galeriden Resim Seç",
                            color = PrimaryLilac,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SegmentedTagControl(
    selectedTag: String,
    onTagSelected: (String) -> Unit
) {
    val options = listOf(ProductTags.FOR_SALE, ProductTags.FOR_RENT, ProductTags.NEED_REQUEST)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(SurfaceLight, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEach { option ->
            val isSelected = selectedTag == option
            val bgColor = if (isSelected) BackgroundWhite else Color.Transparent
            val textColor = if (isSelected) PrimaryLilac else PrimaryLilac.copy(alpha = 0.5f)
            val elevation = if (isSelected) 2.dp else 0.dp

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(20.dp),
                color = bgColor,
                shadowElevation = elevation
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onTagSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
