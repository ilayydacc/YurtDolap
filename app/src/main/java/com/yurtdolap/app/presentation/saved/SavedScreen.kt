package com.yurtdolap.app.presentation.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.isNeedRequest
import com.yurtdolap.app.presentation.designsystem.components.ProductCard
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun SavedScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: SavedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    UIStateWrapper(
        state = uiState,
        onRetry = { viewModel.loadSavedProducts() }
    ) { state ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(top = 48.dp)
        ) {
            Text(
                text = "Favorilerim",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            if (state.savedProducts.isEmpty()) {
                EmptySavedState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.savedProducts, key = { it.id }) { product ->
                        ProductCard(
                            title = product.title,
                            price = product.price,
                            imageUrl = product.imageUrl,
                            tag = product.tag,
                            isFavorite = state.favoriteIds.contains(product.id),
                            location = product.dormitory,
                            deliveryLabel = deliveryLabelFor(product),
                            onFavoriteClick = { viewModel.toggleFavorite(product.id) },
                            onClick = { onNavigateToDetail(product.id) }
                        )
                    }
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
fun EmptySavedState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = "Boş Favoriler",
            modifier = Modifier.size(80.dp),
            tint = PrimaryLilac.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Henüz favori ilanınız yok.",
            style = MaterialTheme.typography.titleLarge,
            color = TextDarkPurple,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Beğendiğiniz ilanların üzerindeki kalp ikonuna tıklayarak buraya kaydedebilirsiniz.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextDarkPurple.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
