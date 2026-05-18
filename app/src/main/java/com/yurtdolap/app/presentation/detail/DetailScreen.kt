package com.yurtdolap.app.presentation.detail

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yurtdolap.app.R
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.model.ProductReview
import com.yurtdolap.app.domain.model.ProductTransaction
import com.yurtdolap.app.domain.model.ProductTransactionStatus
import com.yurtdolap.app.domain.model.hasRentalRating
import com.yurtdolap.app.domain.model.hasSellerRating
import com.yurtdolap.app.domain.model.isForRent
import com.yurtdolap.app.domain.model.isNeedRequest
import com.yurtdolap.app.presentation.designsystem.components.UIState
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.components.YurtPrimaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtSecondaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.components.formatRatingValue
import com.yurtdolap.app.presentation.designsystem.components.formatRelativeRentalTime
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.CtaGreen
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TurkishLira = "\u20BA"

@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()
    val transactionsState by viewModel.transactionsState.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isSubmittingReview by viewModel.isSubmittingReview.collectAsState()
    val isSubmittingTransaction by viewModel.isSubmittingTransaction.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigateToChatEvent.collect(onNavigateToChat)
    }

    LaunchedEffect(Unit) {
        viewModel.productDeletedEvent.collect { onNavigateBack() }
    }

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    UIStateWrapper(
        state = uiState,
        onRetry = {
            viewModel.loadProduct()
            viewModel.loadReviews()
            viewModel.loadTransactions()
        }
    ) { product ->
        val reviews = (reviewsState as? UIState.Success)?.data.orEmpty()
        val transactions = (transactionsState as? UIState.Success)?.data.orEmpty()
        val reviewEligibility = remember(product, reviews, transactions, currentUserId) {
            viewModel.reviewEligibility(product, currentUserId, reviews, transactions)
        }

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
                if (product.isForRent() && product.hasRentalRating()) {
                    RentalTrustBlock(product = product)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                SellerTrustBlock(product = product)
                Spacer(modifier = Modifier.height(16.dp))
                TransactionSection(
                    product = product,
                    transactionsState = transactionsState,
                    currentUserId = currentUserId,
                    isSubmittingTransaction = isSubmittingTransaction,
                    onRequestTransaction = viewModel::requestTransaction,
                    onCompleteTransaction = viewModel::completeTransaction
                )
                Spacer(modifier = Modifier.height(16.dp))
                ReviewEntrySection(
                    product = product,
                    canReview = reviewEligibility.canReview,
                    hasReviewed = reviewEligibility.hasReviewed,
                    blockedReason = reviewEligibility.blockedReason,
                    isSubmittingReview = isSubmittingReview,
                    onSubmit = viewModel::submitReview
                )
                Spacer(modifier = Modifier.height(16.dp))
                ReviewListSection(
                    product = product,
                    reviewsState = reviewsState,
                    currentUserId = currentUserId
                )
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
                            product.isNeedRequest() -> "Talep kapali"
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
                    product.isNeedRequest() -> "Talep kapali"
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
                text = "Aciklama",
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
    if (Regex("[A-Za-z]").containsMatchIn(trimmed)) return trimmed
    return "$TurkishLira $trimmed"
}

@Composable
private fun SellerTrustBlock(product: Product) {
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
                    if (product.hasSellerRating()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatRatingValue(product.sellerRatingAverage)} satici puani · ${product.sellerRatingCount} yorum",
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryLilac,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
private fun RentalTrustBlock(product: Product) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Kiralik Urun Degerlendirmesi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrustStatPill(
                    value = formatRatingValue(product.rentalRatingAverage),
                    label = "urun puani",
                    modifier = Modifier.weight(1f)
                )
                TrustStatPill(
                    value = product.rentalCount.toString(),
                    label = "kiralama",
                    modifier = Modifier.weight(1f)
                )
            }

            if (product.rentalRatingCount > 0 || product.lastRentedAt != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val metaParts = buildList {
                    if (product.rentalRatingCount > 0) {
                        add("${product.rentalRatingCount} degerlendirme")
                    }
                    formatRelativeRentalTime(product.lastRentedAt)?.let(::add)
                }
                Text(
                    text = metaParts.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDarkPurple.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun TransactionSection(
    product: Product,
    transactionsState: UIState<List<ProductTransaction>>,
    currentUserId: String?,
    isSubmittingTransaction: Boolean,
    onRequestTransaction: () -> Unit,
    onCompleteTransaction: (String) -> Unit
) {
    if (product.isNeedRequest()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Dogrulanmis Islem",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (transactionsState) {
                is UIState.Loading -> {
                    Text(
                        text = "Islem durumu yukleniyor...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDarkPurple.copy(alpha = 0.72f)
                    )
                }
                is UIState.Error -> InfoNotice(text = transactionsState.message)
                is UIState.Success -> {
                    val transactions = transactionsState.data
                    if (currentUserId == null) {
                        InfoNotice(text = "Islem talebi acmak ve yorum birakmak icin giris yapman gerekir.")
                    } else if (currentUserId == product.sellerId) {
                        SellerTransactionManager(
                            transactions = transactions,
                            isSubmittingTransaction = isSubmittingTransaction,
                            onCompleteTransaction = onCompleteTransaction
                        )
                    } else {
                        BuyerTransactionStatus(
                            transaction = transactions.firstOrNull { it.buyerId == currentUserId },
                            isSubmittingTransaction = isSubmittingTransaction,
                            onRequestTransaction = onRequestTransaction
                        )
                    }
                }
                UIState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun BuyerTransactionStatus(
    transaction: ProductTransaction?,
    isSubmittingTransaction: Boolean,
    onRequestTransaction: () -> Unit
) {
    when {
        transaction == null -> {
            Text(
                text = "Saticiya once islem talebi gonder. Satici talebi tamamlandi olarak isaretledikten sonra yorum birakabilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDarkPurple.copy(alpha = 0.74f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            YurtPrimaryButton(
                text = if (isSubmittingTransaction) "Gonderiliyor..." else "Islem Talebi Gonder",
                onClick = onRequestTransaction,
                enabled = !isSubmittingTransaction
            )
        }
        transaction.status == ProductTransactionStatus.COMPLETED -> {
            InfoNotice(text = "Islemin satici tarafindan tamamlandi olarak onaylandi. Artik degerlendirme birakabilirsin.")
        }
        else -> {
            InfoNotice(text = "Islem talebin saticiya iletildi. Satici islemi tamamlandi diye onayladiginda yorum acilacak.")
        }
    }
}

@Composable
private fun SellerTransactionManager(
    transactions: List<ProductTransaction>,
    isSubmittingTransaction: Boolean,
    onCompleteTransaction: (String) -> Unit
) {
    if (transactions.isEmpty()) {
        InfoNotice(text = "Bu ilan icin henuz bir islem talebi gelmedi.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        transactions.forEach { transaction ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = BackgroundWhite,
                border = BorderStroke(1.dp, OutlineSoft.copy(alpha = 0.8f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transaction.buyerName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkPurple
                            )
                            Text(
                                text = when (transaction.status) {
                                    ProductTransactionStatus.COMPLETED -> "Islem tamamlandi"
                                    else -> "Onay bekliyor"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (transaction.status == ProductTransactionStatus.COMPLETED) CtaGreen else PrimaryLilac
                            )
                        }
                        if (transaction.status == ProductTransactionStatus.COMPLETED) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = CtaGreen.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Tamamlandi",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CtaGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Talep tarihi: ${formatReviewDate(transaction.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkPurple.copy(alpha = 0.68f)
                    )

                    if (transaction.status != ProductTransactionStatus.COMPLETED) {
                        Spacer(modifier = Modifier.height(12.dp))
                        YurtSecondaryButton(
                            text = if (isSubmittingTransaction) "Isaretleniyor..." else "Islemi Tamamlandi Olarak Isaretle",
                            onClick = { onCompleteTransaction(transaction.buyerId) },
                            enabled = !isSubmittingTransaction
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewEntrySection(
    product: Product,
    canReview: Boolean,
    hasReviewed: Boolean,
    blockedReason: String?,
    isSubmittingReview: Boolean,
    onSubmit: (Int, Int?, String) -> Unit
) {
    if (product.isNeedRequest()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Degerlendirme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(8.dp))

            when {
                canReview -> ReviewComposer(
                    product = product,
                    isSubmittingReview = isSubmittingReview,
                    onSubmit = onSubmit
                )
                hasReviewed -> InfoNotice(
                    text = "Bu ilan icin degerlendirmeni zaten biraktin."
                )
                else -> InfoNotice(
                    text = blockedReason ?: "Bu ilan icin henuz degerlendirme birakamazsin."
                )
            }
        }
    }
}

@Composable
private fun ReviewComposer(
    product: Product,
    isSubmittingReview: Boolean,
    onSubmit: (Int, Int?, String) -> Unit
) {
    var sellerRating by rememberSaveable(product.id) { mutableIntStateOf(0) }
    var rentalRating by rememberSaveable(product.id) { mutableIntStateOf(0) }
    var comment by rememberSaveable(product.id) { mutableStateOf("") }
    val isRentProduct = product.isForRent()
    val canSubmit = sellerRating in 1..5 && (!isRentProduct || rentalRating in 1..5) && !isSubmittingReview

    Text(
        text = if (isRentProduct) {
            "Islem tamamlandiysa saticiyi ve urunun tekrar kiralanabilir durumunu puanlayabilirsin."
        } else {
            "Islem tamamlandiysa satici deneyimini puanlayabilirsin."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = TextDarkPurple.copy(alpha = 0.74f)
    )
    Spacer(modifier = Modifier.height(12.dp))

    RatingSelector(
        label = "Satici puani",
        selectedRating = sellerRating,
        onSelect = { sellerRating = it }
    )

    if (isRentProduct) {
        Spacer(modifier = Modifier.height(12.dp))
        RatingSelector(
            label = "Kiralik urun puani",
            selectedRating = rentalRating,
            onSelect = { rentalRating = it }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    YurtTextField(
        value = comment,
        onValueChange = { comment = it },
        placeholder = "Istersen kisa bir yorum ekle",
        singleLine = false,
        minLines = 3
    )

    Spacer(modifier = Modifier.height(12.dp))

    YurtPrimaryButton(
        text = if (isSubmittingReview) "Kaydediliyor..." else "Degerlendirmeyi Gonder",
        onClick = {
            onSubmit(
                sellerRating,
                rentalRating.takeIf { isRentProduct },
                comment
            )
        },
        enabled = canSubmit
    )
}

@Composable
private fun RatingSelector(
    label: String,
    selectedRating: Int,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = TextDarkPurple,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { rating ->
                val isSelected = selectedRating == rating
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) PrimaryLilac else SurfaceLight,
                    border = BorderStroke(1.dp, if (isSelected) PrimaryLilac else OutlineSoft),
                    modifier = Modifier.clickable { onSelect(rating) }
                ) {
                    Text(
                        text = "$rating",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) SurfaceLight else TextDarkPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewListSection(
    product: Product,
    reviewsState: UIState<List<ProductReview>>,
    currentUserId: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Yorumlar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(10.dp))

            when (reviewsState) {
                is UIState.Loading -> {
                    Text(
                        text = "Degerlendirmeler yukleniyor...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDarkPurple.copy(alpha = 0.7f)
                    )
                }
                is UIState.Error -> InfoNotice(text = reviewsState.message)
                is UIState.Success -> {
                    val reviews = reviewsState.data
                    if (reviews.isEmpty()) {
                        InfoNotice(text = "Henuz yorum birakilmamis.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            reviews.forEach { review ->
                                ReviewCard(
                                    review = review,
                                    isRentProduct = product.isForRent(),
                                    isMine = review.reviewerId == currentUserId
                                )
                            }
                        }
                    }
                }
                UIState.Idle -> Unit
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: ProductReview,
    isRentProduct: Boolean,
    isMine: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BackgroundWhite,
        border = BorderStroke(1.dp, OutlineSoft.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildString {
                            append(review.reviewerName)
                            if (isMine) append(" (Sen)")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPurple
                    )
                    Text(
                        text = formatReviewDate(review.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextDarkPurple.copy(alpha = 0.65f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = PrimaryLilac.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${review.sellerRating}/5 satici",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryLilac
                    )
                }
            }

            if (isRentProduct && review.rentalRating != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = CtaGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${review.rentalRating}/5 urun",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CtaGreen
                    )
                }
            }

            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDarkPurple.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun InfoNotice(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = PrimaryLilac.copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = TextDarkPurple.copy(alpha = 0.76f)
        )
    }
}

private fun formatReviewDate(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("tr-TR"))
    return formatter.format(Date(timestamp))
}

@Composable
private fun TrustStatPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = CtaGreen.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CtaGreen
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextDarkPurple.copy(alpha = 0.7f)
            )
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
