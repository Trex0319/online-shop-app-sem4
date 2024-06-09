package com.example.onlineshop.core.di


import com.example.onlineshop.data.repository.authentication.UserAuthentication
import com.example.onlineshop.data.repository.cart.CartRepo
import com.example.onlineshop.data.repository.cart.CartRepoImpl
import com.example.onlineshop.data.repository.order.OrderHistoryRepo
import com.example.onlineshop.data.repository.order.OrderHistoryRepoImpl
import com.example.onlineshop.data.repository.user.UserRepo
import com.example.onlineshop.data.repository.product.ProductRepo
import com.example.onlineshop.data.repository.product.ProductRepoImpl
import com.example.onlineshop.data.repository.user.UserRepoImpl
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
    fun provideUserRepo(UserAuthentication: UserAuthentication): UserRepo {
        return UserRepoImpl(UserAuthentication)
    }
    @Provides
    @Singleton
    fun provideProductRepo(UserAuthentication: UserAuthentication): ProductRepo {
        return ProductRepoImpl(UserAuthentication)
    }

    @Provides
    @Singleton
    fun provideCartRepo(UserAuthentication: UserAuthentication): CartRepo {
        return CartRepoImpl(UserAuthentication)
    }

    @Provides
    @Singleton
    fun provideOrderHistoryRepo(UserAuthentication: UserAuthentication): OrderHistoryRepo {
        return OrderHistoryRepoImpl(UserAuthentication)
    }
}