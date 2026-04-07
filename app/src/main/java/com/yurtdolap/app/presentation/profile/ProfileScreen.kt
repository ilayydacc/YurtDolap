package com.yurtdolap.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.isNeedRequest
import com.yurtdolap.app.presentation.designsystem.components.ProductCard
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.components.YurtSecondaryButton
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.CtaGreen
import com.yurtdolap.app.presentation.designsystem.theme.ErrorRed
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SecondaryLavender
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun ProfileScreen(
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var productToDelete by remember { mutableStateOf<String?>(null) }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(text = "Silmeyi Onayla", fontWeight = FontWeight.Bold) },
            text = { Text("Bu paylasimi gercekten kalici olarak silmek istiyor musun?") },
            confirmButton = {
                TextButton(onClick = {
                    productToDelete?.let { viewModel.deleteProduct(it) }
                    productToDelete = null
                }) {
                    Text("Sil", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Iptal", color = PrimaryLilac)
                }
            }
        )
    }

    UIStateWrapper(
        state = uiState,
        onRetry = { viewModel.loadProfile() }
    ) { profile ->
        val listingCount = profile.activeListings.size
        val requestCount = profile.activeListings.count { it.isNeedRequest() }
        val productCount = profile.activeListings.count { !it.isNeedRequest() }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProfileHeaderBlock(
                    name = profile.name,
                    dormitory = profile.dormitory,
                    listingCount = listingCount,
                    productCount = productCount,
                    requestCount = requestCount,
                    onEditProfileClick = onNavigateToEditProfile,
                    onSignOutClick = {
                        viewModel.signOut()
                        onSignOut()
                    }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                ProfileSectionHeader(totalCount = listingCount)
            }

            if (profile.activeListings.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyProfileListings()
                }
            } else {
                items(profile.activeListings, key = { it.id }) { product ->
                    ProductCard(
                        title = product.title,
                        price = product.price,
                        imageUrl = product.imageUrl,
                        tag = product.tag,
                        location = product.dormitory,
                        deliveryLabel = deliveryLabelFor(product),
                        onClick = { onNavigateToEdit(product.id) },
                        onDeleteClick = { productToDelete = product.id },
                        onEditClick = { onNavigateToEdit(product.id) }
                    )
                }
            }
        }
    }
}

private fun deliveryLabelFor(product: Product): String? {
    return product.deliveryPreference
        .takeIf { it.isNotBlank() }
        ?.let { "Teslim: $it" }
}

@Composable
fun ProfileHeaderBlock(
    name: String,
    dormitory: String,
    listingCount: Int,
    productCount: Int,
    requestCount: Int,
    onEditProfileClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineSoft.copy(alpha = 0.65f), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = SurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryLilac, SecondaryLavender)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(SurfaceLight.copy(alpha = 0.95f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = PrimaryLilac,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = SurfaceLight,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dormitory,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SurfaceLight.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileStatCard(label = "Toplam", value = listingCount.toString(), modifier = Modifier.weight(1f))
                ProfileStatCard(label = "Ilan", value = productCount.toString(), modifier = Modifier.weight(1f))
                ProfileStatCard(label = "Talep", value = requestCount.toString(), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                YurtSecondaryButton(
                    text = "Profili Duzenle",
                    onClick = onEditProfileClick,
                    modifier = Modifier.weight(1f)
                )
                YurtSecondaryButton(
                    text = "Cikis",
                    onClick = onSignOutClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceLight.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, SurfaceLight.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = SurfaceLight,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SurfaceLight.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun ProfileSectionHeader(totalCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Paylasimlarim",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Text(
                text = "Ilanlarini ve ihtiyac taleplerini buradan yonet.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDarkPurple.copy(alpha = 0.68f)
            )
        }
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = CtaGreen.copy(alpha = 0.14f)
        ) {
            Text(
                text = "$totalCount aktif",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = CtaGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyProfileListings() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(PrimaryLilac.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryLilac,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Henuz paylasimin yok",
                style = MaterialTheme.typography.titleMedium,
                color = TextDarkPurple,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Urun ilani veya ihtiyac talebi eklediginde burada gorunecek.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDarkPurple.copy(alpha = 0.68f),
                textAlign = TextAlign.Center
            )
        }
    }
}
