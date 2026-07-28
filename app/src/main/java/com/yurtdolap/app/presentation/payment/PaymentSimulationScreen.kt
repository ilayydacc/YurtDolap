package com.yurtdolap.app.presentation.payment

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.Product
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
fun PaymentSimulationScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentSimulationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.paymentSuccessEvent.collect {
            onNavigateBack()
        }
    }

    UIStateWrapper(
        state = uiState,
        onRetry = viewModel::loadProduct
    ) { product ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PaymentHeader(onNavigateBack = onNavigateBack)
            DemoNotice()
            ProductPaymentSummary(product = product)
            PaymentForm(
                formState = formState,
                onCardHolderChange = viewModel::updateCardHolder,
                onCardNumberChange = viewModel::updateCardNumber,
                onExpiryChange = viewModel::updateExpiry,
                onCvvChange = viewModel::updateCvv,
                onSubmit = viewModel::submitPayment
            )
        }
    }
}

@Composable
private fun PaymentHeader(onNavigateBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        YurtSecondaryButton(
            text = "Geri Don",
            onClick = onNavigateBack
        )
        Text(
            text = "Test Odeme",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TextDarkPurple
        )
    }
}

@Composable
private fun DemoNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PrimaryLilac.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Bu ekran sadece tanitim icindir.",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Text(
                text = "Gercek para cekilmez, kart bilgileri kaydedilmez. Basarili test icin 4242 4242 4242 4242, basarisiz test icin 4000 0000 0000 0002 kullan.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDarkPurple.copy(alpha = 0.76f)
            )
        }
    }
}

@Composable
private fun ProductPaymentSummary(product: Product) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Text(
                text = product.sellerName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextDarkPurple.copy(alpha = 0.7f)
            )
            Text(
                text = formatPrice(product.price),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = CtaGreen
            )
        }
    }
}

@Composable
private fun PaymentForm(
    formState: PaymentSimulationFormState,
    onCardHolderChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = formState.cardHolder,
                onValueChange = onCardHolderChange,
                label = { Text("Kart sahibi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.cardNumber,
                onValueChange = onCardNumberChange,
                label = { Text("Kart numarasi") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CardNumberVisualTransformation,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = formState.expiry,
                    onValueChange = onExpiryChange,
                    label = { Text("AA/YY") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ExpiryVisualTransformation,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = formState.cvv,
                    onValueChange = onCvvChange,
                    label = { Text("CVV") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.weight(1f)
                )
            }

            formState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            YurtPrimaryButton(
                text = if (formState.isSubmitting) "Test odeme isleniyor..." else "Test Odemeyi Tamamla",
                onClick = onSubmit,
                enabled = !formState.isSubmitting
            )
        }
    }
}

private object CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = text.text.chunked(4).joinToString(" ")
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return (offset + offset / 4).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return (offset - offset / 5).coerceIn(0, text.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

private object ExpiryVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = if (text.length > 2) {
            "${text.text.take(2)}/${text.text.drop(2)}"
        } else {
            text.text
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return if (offset > 2) {
                    (offset + 1).coerceAtMost(formatted.length)
                } else {
                    offset.coerceAtMost(formatted.length)
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset > 2) {
                    (offset - 1).coerceIn(0, text.length)
                } else {
                    offset.coerceIn(0, text.length)
                }
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
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
