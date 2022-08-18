package com.android.chat.models

data class User(
    val first_name:String,
    val last_name:String,
    val photo_url:String,
    val _id:String,
    val is_connected :Boolean,
    val last_opened:String,
    val groups:List<String>,
    val token_firebase:String = "",
    val at_another_call:Boolean = false
    )
