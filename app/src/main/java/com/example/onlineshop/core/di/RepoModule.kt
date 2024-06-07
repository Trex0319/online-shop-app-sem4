package com.example.onlineshop.core.di


import com.example.onlineshop.data.repository.user.UserRepo
import com.example.onlineshop.data.repository.product.ProductRepo
import com.example.onlineshop.data.repository.product.ProductRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepoModule {

    @Provides
    @Singleton
    fun provideProductRepo(userRepo: UserRepo): ProductRepo {
        return ProductRepoImpl(userRepo)
    }
}