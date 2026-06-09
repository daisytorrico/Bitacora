package com.catedra.bitacora.features.social.presentation.comments

// UI de comentarios (Bottom Sheet)
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catedra.bitacora.R
import com.catedra.bitacora.core.ui.components.social.CommentItem
import com.catedra.bitacora.features.social.domain.model.Comment

@Composable
fun CommentsSheetContent(
    currentUserId: String?,
    isTripOwner: Boolean = false,
    onProfileClick: (String) -> Unit,
    viewModel: CommentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    var replyToDelete by remember { mutableStateOf<Pair<String, Comment>?>(null) }

    if (commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { commentToDelete = null },
            title = { Text(stringResource(R.string.delete_comment_question)) },
            text = { Text(stringResource(R.string.delete_comment_warning)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteComment(commentToDelete!!.id)
                        commentToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { commentToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (replyToDelete != null) {
        AlertDialog(
            onDismissRequest = { replyToDelete = null },
            title = { Text(stringResource(R.string.delete_response_question)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val (parentId, reply) = replyToDelete!!
                        viewModel.deleteReply(parentId, reply.id)
                        replyToDelete = null
                    }
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { replyToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.comments),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (!uiState.isLoading && uiState.comments.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_comments_yet),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    uiState.comments.forEach { comment ->
                        item(key = comment.id) {
                            CommentItem(
                                userId = comment.userId,
                                username = comment.username,
                                userPhotoUrl = comment.userPhotoUrl,
                                content = comment.content,
                                timestamp = comment.timestamp,
                                likesCount = comment.likesCount,
                                isLiked = uiState.likedCommentIds.contains(comment.id),
                                onCommentIconClick = {
                                    viewModel.setReplyingTo(comment.id, comment.username, null)
                                    viewModel.toggleReplies(comment.id)
                                    viewModel.onCommentTextChange("@${comment.username} ")
                                },
                                onProfileClick = onProfileClick,
                                onLikeClick = { viewModel.toggleLike(comment.id) },
                                onDeleteClick = if (comment.userId == currentUserId || isTripOwner) {
                                    { commentToDelete = comment }
                                } else null,
                                repliesCount = comment.replies.size
                            )
                        }

                        val isExpanded = uiState.expandedCommentIds.contains(comment.id)

                        if (isExpanded && comment.replies.isNotEmpty()) {
                            items(comment.replies, key = { it.id }) { reply ->
                                Row(modifier = Modifier.padding(start = 48.dp)) {
                                    CommentItem(
                                        userId = reply.userId,
                                        username = reply.username,
                                        userPhotoUrl = reply.userPhotoUrl,
                                        content = reply.content,
                                        timestamp = reply.timestamp,
                                        likesCount = reply.likesCount,
                                        isLiked = uiState.likedReplyIds.contains(reply.id),
                                        onCommentIconClick = {
                                            viewModel.setReplyingTo(reply.id, reply.username, comment.id)
                                            viewModel.onCommentTextChange("@${reply.username} ")
                                        },
                                        onProfileClick = onProfileClick,
                                        onLikeClick = { viewModel.toggleReplyLike(comment.id, reply.id) },
                                        onDeleteClick = if (reply.userId == currentUserId || isTripOwner) {
                                            { replyToDelete = comment.id to reply }
                                        } else null,
                                        isReply = true
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        CommentInput(
            text = uiState.currentCommentText,
            replyingToName = uiState.replyingToName,
            onTextChange = viewModel::onCommentTextChange,
            onCancelReply = { viewModel.setReplyingTo("", "", null) },
            onSend = viewModel::sendComment,
            isSending = uiState.isSending
        )
    }
}

@Composable
fun CommentInput(
    text: String,
    replyingToName: String?,
    onTextChange: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(replyingToName) {
        if (!replyingToName.isNullOrBlank()) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.imePadding()
    ) {
        Column {
            if (!replyingToName.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.responding_to, replyingToName),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    IconButton(onClick = onCancelReply, modifier = Modifier.size(18.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text(if (replyingToName == null) stringResource(R.string.write_a_comment) else stringResource(
                        R.string.write_a_response
                    )) },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    maxLines = 4,
                    enabled = !isSending,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = onSend,
                        enabled = text.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.send),
                            tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    }
}