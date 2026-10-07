package com.example.viewmodel

import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.ShopRepository

// Extension helper to access raw database safely for transaction logging
val ShopRepository.db: AppDatabase
    get() {
        val field = ShopRepository::class.java.getDeclaredField("db")
        field.isAccessible = true
        return field.get(this) as AppDatabase
    }
