package com.yurtdolap.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.isNeedRequest
import com.yurtdolap.app.presentation.designsystem.components.ProductCard
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun SearchScreen(
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "İlan ve Talep Ara",
                style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(24.dp))
            YurtTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = "Kiralık ütü, 250 TL altı lamba, kettle..."
            )
        }

        UIStateWrapper(
            state = uiState,
            onRetry = { viewModel.updateSearchQuery(searchQuery) }
        ) { products ->
            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(text = "Sonuç bulunamadı.", color = TextDarkPurple.copy(alpha = 0.5f))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            title = product.title,
                            price = product.price,
                            imageUrl = product.imageUrl,
                            tag = product.tag,
                            location = product.dormitory,
                            deliveryLabel = deliveryLabelFor(product),
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
