package com.catedra.bitacora.features.social.domain.repository

import com.catedra.bitacora.features.social.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    // --- LIKES ---
    suspend fun toggleLike(collectionPath: String, isLiked: Boolean): Result<Unit>
    fun getIsLiked(collectionPath: String): Flow<Boolean>
    fun getLikesCount(tripId: String, poiId: String): Flow<Int>
    
    // --- COMMENTS ---
    fun getCommentsCount(tripId: String, poiId: String): Flow<Int>
    fun getComments(tripId: String, poiId: String): Flow<List<Comment>>
    suspend fun saveComment(tripId: String, poiId: String, content: String, parentId: String? = null): Result<Unit>
    suspend fun deleteSocialMessage(tripId: String, poiId: String, commentId: String, replyId: String? = null): Result<Unit>
    
    // --- REPLIES ---
    fun getReplies(tripId: String, poiId: String, commentId: String): Flow<List<Comment>>
    
    // --- FOLLOW ---
    suspend fun followUser(targetUserId: String): Result<Unit>
    suspend fun unfollowUser(targetUserId: String): Result<Unit>
    suspend fun isFollowing(targetUserId: String): Result<Boolean>
}
