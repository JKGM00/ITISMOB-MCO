package com.itismob.grpfive.mco

import java.io.Serializable

data class User(
    val userID: String = "",
    val storeName: String = "",
    val profilePic: Int = 0,
    val userEmail: String = "",
    val userHashedPw: String = ""
) : Serializable
