package com.android.chat.models

data class GroupModel(
    val created_at : String,
    val group_type : String,
    val participants : List<String>,
    val recent_message :MessageModel,
    val updated_at: String,
    val _id : String,
    val user:User
)