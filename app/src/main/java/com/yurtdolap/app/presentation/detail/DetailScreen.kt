package com.yurtdolap.app.presentation.detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yurtdolap.app.R
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.isNeedRequest
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.components.YurtPrimaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtSecondaryButton
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.CtaGreen
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

private const val TurkishLira = "\u20BA"

@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToChatEvent.collect { chatId ->
            onNavigateToChat(chatId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.productDeletedEvent.collect {
            onNavigateBack()
        }
    }

    UIStateWrapper<Product>(
        state = uiState,
        onRetry = { viewModel.loadProduct() }
    ) { product ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .verticalScroll(rememberScrollState())
        ) {
            ProductImageHeader(
                imageUrl = product.imageUrl,
                title = product.title,
                tag = product.tag,
                onBackClick = onNavigateBack
            )

            Column(modifier = Modifier.padding(20.dp)) {
                ProductInfoCard(product = product)
                if (product.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ProductDescriptionCard(description = product.description)
                }
                Spacer(modifier = Modifier.height(16.dp))
                SellerTrustBlock(product = product)
                Spacer(modifier = Modifier.height(20.dp))
                ActionBlock(
                    isAdmin = isAdmin,
                    isNeedRequest = product.isNeedRequest(),
                    onMessageClick = { viewModel.onMessageSellerClicked() },
                    onAdminDelete = { viewModel.deleteProductAsAdmin() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProductImageHeader(
    imageUrl: String?,
    title: String,
    tag: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(316.dp)
            .background(SurfaceLight)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Product image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.38f)
                        )
                    )
                )
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .background(SurfaceLight.copy(alpha = 0.9f), CircleShape)
                .align(Alignment.TopStart)
        ) {
            Text(text = "<", fontWeight = FontWeight.Bold, color = TextDarkPurple)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = PrimaryLilac.copy(alpha = 0.95f)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = SurfaceLight,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = SurfaceLight,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ProductInfoCard(product: Product) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = CtaGreen.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = when {
                            product.isNeedRequest() && product.isAvailable -> "Talep aktif"
                            product.isNeedRequest() -> "Talep kapalı"
                            product.isAvailable -> "Stokta"
                            else -> "Tukendi"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = CtaGreen,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = formatPrice(product.price),
                    style = MaterialTheme.typography.headlineSmall,
                    color = CtaGreen,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = OutlineSoft)
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Kategori", value = categoryLabel(product.categoryId))
            InfoRow(label = "Yurt", value = product.dormitory)
            if (product.deliveryPreference.isNotBlank()) {
                InfoRow(label = "Teslim", value = product.deliveryPreference)
            }
            InfoRow(
                label = "Durum",
                value = when {
                    product.isNeedRequest() && product.isAvailable -> "Talep aktif"
                    product.isNeedRequest() -> "Talep kapalı"
                    product.isAvailable -> "Urun aktif"
                    else -> "Urun pasif"
                }
            )
        }
    }
}

@Composable
private fun ProductDescriptionCard(description: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Açıklama",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextDarkPurple.copy(alpha = 0.78f)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextDarkPurple.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextDarkPurple,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

private fun categoryLabel(categoryId: String?): String {
    return when (categoryId) {
        "1" -> "Elektronik"
        "2" -> "Kitap"
        "3" -> "Mutfak"
        "4" -> "Kirtasiye"
        "5" -> "Giyim"
        else -> "Diger"
    }
}

private fun formatPrice(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return trimmed
    if (trimmed.contains(TurkishLira)) return trimmed
    if (Regex("\\bTL\\b", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
        return trimmed.replace(Regex("\\bTL\\b", RegexOption.IGNORE_CASE), TurkishLira)
    }
    if (Regex("[A-Za-zÇĞİÖŞÜçğıöşü]").containsMatchIn(trimmed)) return trimmed
    return "$TurkishLira $trimmed"
}

@Composable
fun SellerTrustBlock(product: Product) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (product.isNeedRequest()) "Talep Sahibi" else "Satici Bilgileri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryLilac.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = null,
                        tint = PrimaryLilac
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.sellerName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPurple
                    )
                    Text(
                        text = product.dormitory,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDarkPurple.copy(alpha = 0.75f)
                    )
                }

                Surface(
                    color = CtaGreen.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Onayli",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        color = CtaGreen,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionBlock(
    isAdmin: Boolean,
    isNeedRequest: Boolean,
    onMessageClick: () -> Unit,
    onAdminDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            YurtPrimaryButton(
                text = if (isNeedRequest) "Talep Sahibine Mesaj At" else "Saticiya Mesaj At",
                onClick = onMessageClick
            )

            if (isAdmin) {
                Spacer(modifier = Modifier.height(10.dp))
                YurtSecondaryButton(
                    text = "Admin: Ilani Kaldir",
                    onClick = onAdminDelete
                )
            }
        }
    }
}
