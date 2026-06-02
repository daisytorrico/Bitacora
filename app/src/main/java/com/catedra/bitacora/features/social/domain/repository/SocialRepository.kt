package com.catedra.bitacora.features.social.domain.repository

import com.catedra.bitacora.features.social.domain.model.Comment
import com.catedra.bitacora.features.social.domain.model.Reply
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    suspend fun giveLike(tripId: String, poiId: String): Result<Unit>
    suspend fun removeLike(tripId: String, poiId: String): Result<Unit>
    fun getLikesCount(tripId: String, poiId: String): Flow<Int>
    fun getIsLiked(tripId: String, poiId: String): Flow<Boolean>
    
    fun getCommentsCount(tripId: String, poiId: String): Flow<Int>
    fun getComments(tripId: String, poiId: String): Flow<List<Comment>>
    suspend fun saveComment(tripId: String, poiId: String, content: String): Result<Unit>
    
    fun getReplies(tripId: String, poiId: String, commentId: String): Flow<List<Reply>>
    suspend fun saveReply(tripId: String, poiId: String, commentId: String, content: String): Result<Unit>
    
    suspend fun followUser(targetUserId: String): Result<Unit>
    suspend fun unfollowUser(targetUserId: String): Result<Unit>
    suspend fun isFollowing(targetUserId: String): Result<Boolean>
}
