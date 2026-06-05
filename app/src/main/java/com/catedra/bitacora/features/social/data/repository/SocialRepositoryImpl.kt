package com.catedra.bitacora.features.social.data.repository

import com.catedra.bitacora.features.social.data.mapper.toDomain
import com.catedra.bitacora.features.social.data.remote.SocialRemoteDataSource
import com.catedra.bitacora.features.social.domain.model.Comment
import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SocialRepositoryImpl @Inject constructor(
    private val remoteDataSource: SocialRemoteDataSource
) : SocialRepository {

    override suspend fun toggleLike(collectionPath: String, isLiked: Boolean): Result<Unit> = runCatching {
        remoteDataSource.toggleLike(collectionPath, isLiked)
    }

    override fun getIsLiked(collectionPath: String): Flow<Boolean> = 
        remoteDataSource.isLiked(collectionPath)

    override fun getLikesCount(tripId: String, poiId: String): Flow<Int> = 
        remoteDataSource.getLikesCount(tripId, poiId)

    override fun getCommentsCount(tripId: String, poiId: String): Flow<Int> = 
        remoteDataSource.getCommentsCount(tripId, poiId)

    override fun getComments(tripId: String, poiId: String): Flow<List<Comment>> = 
        remoteDataSource.getComments(tripId, poiId).map { it.toDomain() }

    override suspend fun saveComment(tripId: String, poiId: String, content: String, parentId: String?): Result<Unit> = runCatching {
        remoteDataSource.saveComment(tripId, poiId, content, parentId)
    }

    override suspend fun deleteSocialMessage(tripId: String, poiId: String, commentId: String, replyId: String?): Result<Unit> = runCatching {
        remoteDataSource.deleteSocialMessage(tripId, poiId, commentId, replyId)
    }

    override fun getReplies(tripId: String, poiId: String, commentId: String): Flow<List<Comment>> = 
        remoteDataSource.getReplies(tripId, poiId, commentId).map { it.toDomain() }

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
