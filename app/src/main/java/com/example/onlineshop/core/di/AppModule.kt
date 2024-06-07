package com.example.onlineshop.core.di

import com.example.onlineshop.data.repository.FirebaseImageStorage.FirebaseImageStorage
import com.example.onlineshop.data.repository.FirebaseImageStorage.FirebaseImageStorageImpl
import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.example.onlineshop.data.repository.authentication.UserAuthenticationImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideAuthService() : UserAuthentication {
        return UserAuthenticationImpl()
    }

    @Provides
    @Singleton
    fun provideStorageService() :FirebaseImageStorage {
        return FirebaseImageStorageImpl()
    }
}