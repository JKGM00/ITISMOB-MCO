package com.itismob.grpfive.mco.models

import com.google.firebase.firestore.Exclude
import com.itismob.grpfive.mco.R
import java.io.Serializable

data class User(
    // UserID won't be included in the db when saved. userID saved as a documentID (UID in Firestore equal to userID dito)
    @get:Exclude @set:Exclude var userID: String = "",
    val storeName: String = "",
    val profilePic: String = "",
    val userEmail: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var isActive: Boolean = true
    // Password is removed as Firebase Auth would handle it already gng
) : Serializable
