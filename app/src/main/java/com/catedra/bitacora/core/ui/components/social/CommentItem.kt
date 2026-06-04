package com.catedra.bitacora.core.ui.components.social

// Item de la lista de comentarios
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catedra.bitacora.core.ui.components.profile.ProfileImage
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// Timestamp a texto
private fun relativeTime(timestamp: LocalDateTime): String {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(timestamp, now)
    val hours = ChronoUnit.HOURS.between(timestamp, now)
    val days = ChronoUnit.DAYS.between(timestamp, now)
    return when {
        minutes < 1 -> "ahora"
        minutes < 60 -> "hace $minutes min"
        hours < 24 -> "hace $hours h"
        else -> "hace $days d"
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CommentItem(
    userId: String,
    username: String,
    userPhotoUrl: String?,
    content: String,
    timestamp: LocalDateTime,
    likesCount: Int,
    isLiked: Boolean,
    onCommentIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: (String) -> Unit = {},
    onLikeClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null,
    isReply: Boolean = false,
    repliesCount: Int = 0
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onDeleteClick
            )
            .padding(vertical = if (isReply) 4.dp else 8.dp)
    ) {
        ProfileImage(
            imageUrl = userPhotoUrl,
            size = if (isReply) 32 else 40,
            modifier = Modifier.clickable { onProfileClick(userId) }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // @username en negrita
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("@$username")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onProfileClick(userId) }
                )
                // Fecha relativa
                Text(
                    text = relativeTime(timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }

            Text(
                text = buildAnnotatedString {
                    content.split(" ").forEachIndexed { index, word ->
                        if (word.startsWith("@") && word.length > 1) {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                                append(word)
                            }
                        } else {
                            append(word)
                        }
                        if (index < content.split(" ").size - 1) append(" ")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp)
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onLikeClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    if (likesCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$likesCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))

                Row(
                    modifier = Modifier.clickable { onCommentIconClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Responder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    if (!isReply && repliesCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$repliesCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}