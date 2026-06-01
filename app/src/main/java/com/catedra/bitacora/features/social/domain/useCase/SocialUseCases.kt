package com.catedra.bitacora.features.social.domain.useCase

import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import javax.inject.Inject

class GiveLikeUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(tripId: String, poiId: String) = repository.giveLike(tripId, poiId)
}

class RemoveLikeUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(tripId: String, poiId: String) = repository.removeLike(tripId, poiId)
}

class GetLikesCountUseCase @Inject constructor(private val repository: SocialRepository) {
    operator fun invoke(tripId: String, poiId: String) = repository.getLikesCount(tripId, poiId)
}

class GetIsLikedUseCase @Inject constructor(private val repository: SocialRepository) {
    operator fun invoke(tripId: String, poiId: String) = repository.getIsLiked(tripId, poiId)
}

class GetCommentsCountUseCase @Inject constructor(private val repository: SocialRepository) {
    operator fun invoke(tripId: String, poiId: String) = repository.getCommentsCount(tripId, poiId)
}

class GetCommentsUseCase @Inject constructor(private val repository: SocialRepository) {
    operator fun invoke(tripId: String, poiId: String) = repository.getComments(tripId, poiId)
}

class SaveCommentUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(tripId: String, poiId: String, content: String) = 
        repository.saveComment(tripId, poiId, content)
}

class GetRepliesUseCase @Inject constructor(private val repository: SocialRepository) {
    operator fun invoke(tripId: String, poiId: String, commentId: String) = 
        repository.getReplies(tripId, poiId, commentId)
}

class SaveReplyUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(tripId: String, poiId: String, commentId: String, content: String) = 
        repository.saveReply(tripId, poiId, commentId, content)
}

class FollowUserUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(targetUserId: String) = repository.followUser(targetUserId)
}

class UnfollowUserUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(targetUserId: String) = repository.unfollowUser(targetUserId)
}

class GetIsFollowingUseCase @Inject constructor(private val repository: SocialRepository) {
    suspend operator fun invoke(targetUserId: String) = repository.isFollowing(targetUserId)
}
