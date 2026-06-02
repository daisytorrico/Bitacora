package com.catedra.bitacora.di

import com.catedra.bitacora.features.auth.data.repository.AuthRepositoryFirebase
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import com.catedra.bitacora.features.discovery.data.repository.DiscoveryRepositoryImpl
import com.catedra.bitacora.features.discovery.domain.repository.DiscoveryRepository
import com.catedra.bitacora.features.social.data.repository.SocialRepositoryImpl
import com.catedra.bitacora.features.social.domain.repository.SocialRepository
import com.catedra.bitacora.features.travel.data.repository.LocationRepositoryImpl
import com.catedra.bitacora.features.travel.data.repository.TravelRepositoryFirebase
import com.catedra.bitacora.features.travel.domain.repository.LocationRepository
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import com.catedra.bitacora.core.data.repository.GeocodingRepositoryImpl
import com.catedra.bitacora.core.domain.repository.GeocodingRepository
import com.catedra.bitacora.core.data.local.ImageManager
import com.catedra.bitacora.core.domain.repository.ImageRepository
import com.catedra.bitacora.core.data.remote.CloudinaryDataSource
import com.catedra.bitacora.core.domain.repository.CloudinaryRepository
import com.catedra.bitacora.core.data.repository.NearbyPointsRepositoryImpl
import com.catedra.bitacora.core.domain.repository.NearbyPointsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTravelsRepository(
        impl: TravelRepositoryFirebase
    ): TravelsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryFirebase
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(
        impl: GeocodingRepositoryImpl
    ): GeocodingRepository
    @Binds
    @Singleton
    abstract fun bindImageRepository(
        impl: ImageManager
    ): ImageRepository

    @Binds
    @Singleton
    abstract fun bindCloudinaryRepository(
        impl: CloudinaryDataSource
    ): CloudinaryRepository

    @Binds
    @Singleton
    abstract fun bindNearbyPointsRepository(
        impl: NearbyPointsRepositoryImpl
    ): NearbyPointsRepository

    @Binds
    @Singleton
    abstract fun bindDiscoveryRepository(
        impl: DiscoveryRepositoryImpl
    ): DiscoveryRepository

    @Binds
    @Singleton
    abstract fun bindSocialRepository(
        impl: SocialRepositoryImpl
    ): SocialRepository
}
