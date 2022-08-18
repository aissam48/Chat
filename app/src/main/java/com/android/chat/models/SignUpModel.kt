package com.android.chat.models

data class SignUpModel(
    val email: String,
    val first_name: String,
    val last_name: String,
    val password: String,
    val token_firebase: String
)
