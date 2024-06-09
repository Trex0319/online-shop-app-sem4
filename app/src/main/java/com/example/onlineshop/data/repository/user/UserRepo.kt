package com.example.onlineshop.data.repository.user

import com.example.onlineshop.data.modal.User

interface UserRepo {
    suspend fun getUser(): User?
    suspend fun addNewUser(user: User)
    suspend fun updateUserDetail(user: User)
}