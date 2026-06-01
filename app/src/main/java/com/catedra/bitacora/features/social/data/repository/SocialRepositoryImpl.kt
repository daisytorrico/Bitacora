package com.catedra.bitacora.features.social.data.repository

import com.catedra.bitacora.features.social.data.remote.SocialRemoteDataSource
import com.catedra.bitacora.features.social.domain.model.Comment
import com.catedra.bitacora.features.social.domain.model.Reply
import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SocialRepositoryImpl @Inject constructor(
    private val remoteDataSource: SocialRemoteDataSource
) : SocialRepository {
    override suspend fun giveLike(tripId: String, poiId: String): Result<Unit> = runCatching {
        remoteDataSource.giveLike(tripId, poiId)
    }

    override suspend fun removeLike(tripId: String, poiId: String): Result<Unit> = runCatching {
        remoteDataSource.removeLike(tripId, poiId)
    }

    override fun getLikesCount(tripId: String, poiId: String): Flow<Int> = 
        remoteDataSource.getLikesCount(tripId, poiId)

    override fun getIsLiked(tripId: String, poiId: String): Flow<Boolean> = 
        remoteDataSource.isLiked(tripId, poiId)

    override fun getCommentsCount(tripId: String, poiId: String): Flow<Int> = 
        remoteDataSource.getCommentsCount(tripId, poiId)

    override fun getComments(tripId: String, poiId: String): Flow<List<Comment>> = 
        remoteDataSource.getComments(tripId, poiId)

    override suspend fun saveComment(tripId: String, poiId: String, content: String): Result<Unit> = runCatching {
        remoteDataSource.saveComment(tripId, poiId, content)
    }

    override fun getReplies(tripId: String, poiId: String, commentId: String): Flow<List<Reply>> = 
        remoteDataSource.getReplies(tripId, poiId, commentId)

    override suspend fun saveReply(tripId: String, poiId: String, commentId: String, content: String): Result<Unit> = runCatching {
        remoteDataSource.saveReply(tripId, poiId, commentId, content)
    }

    override suspend fun followUser(targetUserId: String): Result<Unit> = runCatching {
        remoteDataSource.followUser(targetUserId)
    }

    override suspend fun unfollowUser(targetUserId: String): Result<Unit> = runCatching {
        remoteDataSource.unfollowUser(targetUserId)
    }

    override suspend fun isFollowing(targetUserId: String): Result<Boolean> = runCatching {
        remoteDataSource.isFollowing(targetUserId)
    }
}
