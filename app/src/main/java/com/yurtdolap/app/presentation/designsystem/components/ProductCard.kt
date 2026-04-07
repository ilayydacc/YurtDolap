package com.yurtdolap.app.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yurtdolap.app.presentation.designsystem.theme.CtaGreen
import com.yurtdolap.app.presentation.designsystem.theme.ErrorRed
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun ProductCard(
    title: String,
    price: String,
    imageUrl: String?,
    tag: String,
    isFavorite: Boolean = false,
    location: String? = null,
    timeLabel: String? = null,
    deliveryLabel: String? = null,
    onFavoriteClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = cardShape)
            .border(width = 1.dp, color = OutlineSoft.copy(alpha = 0.7f), shape = cardShape)
            .clickable { onClick() },
        color = SurfaceLight,
        shape = cardShape
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .background(OutlineSoft.copy(alpha = 0.45f))
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Product image: $title",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(152.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Text(
                        text = "Gorsel yok",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextDarkPurple.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusChip(
                        text = tag,
                        backgroundColor = PrimaryLilac,
                        textColor = SurfaceLight
                    )

                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onEditClick != null) {
                        RoundedActionButton(
                            onClick = onEditClick,
                            icon = Icons.Default.Edit,
                            contentDescription = "Duzenle",
                            tint = PrimaryLilac
                        )
                    }

                    if (onDeleteClick != null) {
                        RoundedActionButton(
                            onClick = onDeleteClick,
                            icon = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = ErrorRed
                        )
                    } else if (onFavoriteClick != null) {
                        RoundedActionButton(
                            onClick = onFavoriteClick,
                            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) ErrorRed else TextDarkPurple.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDarkPurple,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!location.isNullOrBlank() || !timeLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!location.isNullOrBlank()) {
                            Text(
                                text = location,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextDarkPurple.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                        if (!timeLabel.isNullOrBlank()) {
                            Text(
                                text = timeLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextDarkPurple.copy(alpha = 0.62f)
                            )
                        }
                    }
                }

                if (!deliveryLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = deliveryLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryLilac,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = OutlineSoft.copy(alpha = 0.8f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatPrice(price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CtaGreen
                )
            }
        }
    }
}

@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, OutlineSoft.copy(alpha = 0.9f), shape),
        shape = shape,
        color = SurfaceLight
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .background(OutlineSoft.copy(alpha = 0.6f))
            )
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(OutlineSoft.copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(OutlineSoft.copy(alpha = 0.6f))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(OutlineSoft.copy(alpha = 0.8f))
                )
            }
        }
    }
}

private fun formatPrice(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return trimmed

    val normalized = trimmed
        .replace("₺", "TL")
        .replace(Regex("\\s+"), " ")

    if (Regex("\\bTL\\b", RegexOption.IGNORE_CASE).containsMatchIn(normalized)) {
        return normalized.replace(Regex("\\bTL\\b", RegexOption.IGNORE_CASE), "TL")
    }

    if (Regex("[A-Za-zÇĞİÖŞÜçğıöşü]").containsMatchIn(normalized)) {
        return normalized
    }

    return "$normalized TL"
}

@Composable
private fun RoundedActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    tint: Color
) {
    Surface(
        shape = CircleShape,
        color = SurfaceLight.copy(alpha = 0.95f),
        shadowElevation = 2.dp,
        modifier = Modifier.size(34.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
