package com.itismob.grpfive.mco

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class User(
    // UserID won't be saved or loaded from the database. userID as documentID in Firestore
    @get:Exclude @set:Exclude var userID: String = "",
    val storeName: String = "",
    val profilePic: Int = R.drawable.account_profile,
    val userEmail: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var isActive: Boolean = true
    // Password is removed as Firebase Auth would handle it already
) : Serializable
