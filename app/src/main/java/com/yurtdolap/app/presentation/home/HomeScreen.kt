package com.yurtdolap.app.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.Category
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.ProductTags
import com.yurtdolap.app.domain.model.isNeedRequest
import com.yurtdolap.app.presentation.designsystem.components.ProductCard
import com.yurtdolap.app.presentation.designsystem.components.ProductCardSkeleton
import com.yurtdolap.app.presentation.designsystem.components.rentalSummaryLabel
import com.yurtdolap.app.presentation.designsystem.components.sellerRatingLabel
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.components.YurtSecondaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SecondaryLavender
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

enum class HomeListingTypeFilter(val label: String) {
    ALL("Tum Ilanlar"),
    FOR_SALE("Satilik"),
    FOR_RENT("Kiralik"),
    NEEDS("Talepler")
}

@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedListingType by rememberSaveable { mutableStateOf(HomeListingTypeFilter.ALL.name) }
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    UIStateWrapper(
        state = uiState,
        loadingContent = { HomeLoadingSkeleton() },
        onRetry = { viewModel.loadHomeData() }
    ) { state ->
        val listingType = HomeListingTypeFilter.valueOf(selectedListingType)

        val filteredProducts = remember(state.featuredProducts, searchQuery, listingType) {
            val searched = if (searchQuery.isBlank()) {
                state.featuredProducts
            } else {
                state.featuredProducts.filter {
                    val query = searchQuery.trim()
                    it.title.contains(query, ignoreCase = true) ||
                        it.tag.contains(query, ignoreCase = true) ||
                        it.dormitory.contains(query, ignoreCase = true) ||
                        it.price.contains(query, ignoreCase = true)
                }
            }

            when (listingType) {
                HomeListingTypeFilter.ALL -> searched
                HomeListingTypeFilter.FOR_SALE -> searched.filter { it.tag == ProductTags.FOR_SALE }
                HomeListingTypeFilter.FOR_RENT -> searched.filter { it.tag == ProductTags.FOR_RENT }
                HomeListingTypeFilter.NEEDS -> searched.filter { it.isNeedRequest() }
            }
        }

        val highlightedProducts = remember(filteredProducts) { filteredProducts.take(6) }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {
            items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
                HomeHeroSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    totalCount = state.featuredProducts.size,
                    filteredCount = filteredProducts.size,
                    categories = state.categories,
                    selectedCategoryId = state.selectedCategoryId,
                    selectedListingType = listingType,
                    filtersExpanded = filtersExpanded,
                    onFiltersExpandedChange = { filtersExpanded = it },
                    onListingTypeSelected = { selectedListingType = it.name },
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onClearFilters = {
                        searchQuery = ""
                        selectedListingType = HomeListingTypeFilter.ALL.name
                        if (state.selectedCategoryId != null) {
                            viewModel.selectCategory(null)
                        }
                    }
                )
            }

            if (highlightedProducts.isNotEmpty()) {
                items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
                    FeaturedCarouselSection(
                        products = highlightedProducts,
                        favoriteIds = state.favoriteIds,
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        onProductClick = onNavigateToDetail
                    )
                }
            }

            items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
                FeaturedProductsHeader(productCount = filteredProducts.size)
            }

            if (filteredProducts.isEmpty()) {
                items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
                    EmptyProductsState(
                        onClearFilters = {
                            searchQuery = ""
                            selectedListingType = HomeListingTypeFilter.ALL.name
                            if (state.selectedCategoryId != null) {
                                viewModel.selectCategory(null)
                            }
                        }
                    )
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        title = product.title,
                        price = product.price,
                        imageUrl = product.imageUrl,
                        tag = product.tag,
                        isFavorite = state.favoriteIds.contains(product.id),
                        location = product.dormitory,
                        timeLabel = "Bugun",
                        deliveryLabel = deliveryLabelFor(product),
                        sellerRatingLabel = product.sellerRatingLabel(),
                        rentalSummaryLabel = product.rentalSummaryLabel(),
                        onFavoriteClick = { viewModel.toggleFavorite(product.id) },
                        onClick = { onNavigateToDetail(product.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeroSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    totalCount: Int,
    filteredCount: Int,
    categories: List<Category>,
    selectedCategoryId: String?,
    selectedListingType: HomeListingTypeFilter,
    filtersExpanded: Boolean,
    onFiltersExpandedChange: (Boolean) -> Unit,
    onListingTypeSelected: (HomeListingTypeFilter) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryLilac, SecondaryLavender)
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceLight.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SurfaceLight
                        )
                    }
                    Text(
                        text = "Yurtta ihtiyacin olan her sey burada.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceLight
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "İlanları ve ihtiyaç taleplerini aynı yerden keşfet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SurfaceLight.copy(alpha = 0.95f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                YurtTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = "Örn: kitap, kettle veya hesap makinesi"
                )

                Spacer(modifier = Modifier.height(12.dp))

                FilterToggleRow(
                    selectedListingType = selectedListingType,
                    selectedCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Tum kategoriler",
                    filtersExpanded = filtersExpanded,
                    onToggleClick = { onFiltersExpandedChange(!filtersExpanded) }
                )

                if (filtersExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HomeFilterPanel(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        selectedListingType = selectedListingType,
                        onListingTypeSelected = onListingTypeSelected,
                        onCategorySelected = onCategorySelected,
                        onClearFilters = onClearFilters
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoPill(label = "Toplam", value = totalCount.toString())
                    InfoPill(label = "Gosterilen", value = filteredCount.toString())
                }
            }
        }
    }
}

@Composable
private fun FilterToggleRow(
    selectedListingType: HomeListingTypeFilter,
    selectedCategoryName: String,
    filtersExpanded: Boolean,
    onToggleClick: () -> Unit
) {
    Surface(
        onClick = onToggleClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceLight.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, SurfaceLight.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Filtrele",
                    style = MaterialTheme.typography.labelLarge,
                    color = SurfaceLight,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${selectedListingType.label} / $selectedCategoryName",
                    style = MaterialTheme.typography.labelMedium,
                    color = SurfaceLight.copy(alpha = 0.85f)
                )
            }
            Text(
                text = if (filtersExpanded) "Kapat" else "Ac",
                style = MaterialTheme.typography.labelLarge,
                color = SurfaceLight,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeFilterPanel(
    categories: List<Category>,
    selectedCategoryId: String?,
    selectedListingType: HomeListingTypeFilter,
    onListingTypeSelected: (HomeListingTypeFilter) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceLight.copy(alpha = 0.96f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Paylasim turu",
                style = MaterialTheme.typography.labelLarge,
                color = TextDarkPurple,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(HomeListingTypeFilter.values().toList()) { filter ->
                    CategoryChip(
                        label = filter.label,
                        isSelected = filter == selectedListingType,
                        onClick = { onListingTypeSelected(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Kategori",
                style = MaterialTheme.typography.labelLarge,
                color = TextDarkPurple,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CategoryChip(
                        label = "Tum",
                        isSelected = selectedCategoryId == null,
                        onClick = { onCategorySelected(null) }
                    )
                }
                items(categories, key = { it.id }) { category ->
                    CategoryChip(
                        label = category.name,
                        isSelected = category.id == selectedCategoryId,
                        onClick = { onCategorySelected(category.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            YurtSecondaryButton(
                text = "Filtreleri Temizle",
                onClick = onClearFilters
            )
        }
    }
}

@Composable
private fun FeaturedCarouselSection(
    products: List<Product>,
    favoriteIds: List<String>,
    onFavoriteClick: (String) -> Unit,
    onProductClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Bugun One Cikanlar",
            style = MaterialTheme.typography.titleMedium,
            color = TextDarkPurple,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    modifier = Modifier.width(240.dp),
                    title = product.title,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    tag = product.tag,
                    isFavorite = favoriteIds.contains(product.id),
                    location = product.dormitory,
                    timeLabel = "Bugun",
                    deliveryLabel = deliveryLabelFor(product),
                    sellerRatingLabel = product.sellerRatingLabel(),
                    rentalSummaryLabel = product.rentalSummaryLabel(),
                    onFavoriteClick = { onFavoriteClick(product.id) },
                    onClick = { onProductClick(product.id) }
                )
            }
        }
    }
}

@Composable
private fun InfoPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = SurfaceLight.copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SurfaceLight.copy(alpha = 0.9f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = SurfaceLight,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryLilac else SurfaceLight
    val textColor = if (isSelected) SurfaceLight else TextDarkPurple
    val border = if (isSelected) null else BorderStroke(1.dp, OutlineSoft)

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp),
        border = border
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FeaturedProductsHeader(productCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "One Cikan Ilan ve Talepler",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDarkPurple
        )

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = PrimaryLilac.copy(alpha = 0.12f)
        ) {
            Text(
                text = "$productCount ilan",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryLilac,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyProductsState(onClearFilters: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sonuc bulunamadi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Filtrelerini temizleyip tekrar deneyebilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDarkPurple.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            YurtSecondaryButton(
                text = "Filtreleri Temizle",
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
    }
}

@Composable
private fun HomeLoadingSkeleton() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
            Surface(shape = RoundedCornerShape(24.dp), color = OutlineSoft.copy(alpha = 0.5f)) {
                Box(modifier = Modifier.fillMaxWidth().height(196.dp))
            }
        }

        items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(4) {
                    Surface(shape = RoundedCornerShape(14.dp), color = OutlineSoft.copy(alpha = 0.5f)) {
                        Box(modifier = Modifier.width(88.dp).height(36.dp))
                    }
                }
            }
        }

        items(6) {
            ProductCardSkeleton()
        }
    }
}

private fun deliveryLabelFor(product: Product): String? {
    return product.deliveryPreference
        .takeIf { it.isNotBlank() }
        ?.let { "Teslim: $it" }
}
