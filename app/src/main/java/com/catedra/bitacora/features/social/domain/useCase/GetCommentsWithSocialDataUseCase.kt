package com.catedra.bitacora.features.social.domain.useCase

// Agregador de comentarios, respuestas y likes en un solo flujo reactivo
import com.catedra.bitacora.features.social.domain.model.Comment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CommentsFullData(
    val comments: List<Comment>,
    val likedCommentIds: Set<String>,
    val likedReplyIds: Set<String>
)

class GetCommentsWithSocialDataUseCase @Inject constructor(
    private val commentUseCases: CommentUseCases,
    private val replyUseCases: ReplyUseCases,
    private val likeUseCases: LikeUseCases
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(travelId: String, pointId: String): Flow<CommentsFullData> {
        return commentUseCases.getComments(travelId, pointId)
            .flatMapLatest { comments ->
                if (comments.isEmpty()) {
                    flowOf(CommentsFullData(emptyList(), emptySet(), emptySet()))
                } else {
                    val commentFlows = comments.map { comment ->
                        combine(
                            replyUseCases.getReplies(travelId, pointId, comment.id),
                            likeUseCases.isLiked(LikeTarget.COMMENT, travelId, pointId, comment.id)
                        ) { replies, isLiked ->
                            Triple(comment, replies, isLiked)
                        }
                    }
                    
                    combine(commentFlows) { it.toList() }.flatMapLatest { commentResults ->
                        val allReplies = commentResults.flatMap { (_, replies, _) -> replies }

                        if (allReplies.isEmpty()) {
                            flowOf(processResults(commentResults, emptySet()))
                        } else {
                            val replyLikeFlows = allReplies
                                .filter { it.parentId != null }
                                .map { reply ->
                                    likeUseCases.isLiked(
                                        target = LikeTarget.REPLY,
                                        tripId = travelId,
                                        poiId = pointId,
                                        commentId = reply.parentId!!,
                                        replyId = reply.id
                                    ).map { isLiked -> reply.id to isLiked }
                                }
                            
                            combine(replyLikeFlows) { pairs ->
                                val likedReplyIds = pairs
                                    .filter { (_, liked) -> liked }
                                    .map { (id, _) -> id }
                                    .toSet()
                                processResults(commentResults, likedReplyIds)
                            }
                        }
                    }
                }
            }
    }

    private fun processResults(
        results: List<Triple<Comment, List<Comment>, Boolean>>,
        likedReplyIds: Set<String>
    ): CommentsFullData {
        val commentsWithReplies = results.map { (comment, replies, _) ->
            comment.copy(replies = replies)
        }
        val likedCommentIds = results
            .filter { (_, _, isLiked) -> isLiked }
            .map { (comment, _, _) -> comment.id }
            .toSet()
            
        return CommentsFullData(commentsWithReplies, likedCommentIds, likedReplyIds)
    }
}
