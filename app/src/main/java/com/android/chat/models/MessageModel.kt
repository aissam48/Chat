package com.android.chat.models


data class MessageModel(
    val message: String,
    val participants: List<String>,
    val group_id:String,
    val receiver_id:String,
    val sender_id: String,
    val content_type: String,
    val created_at: String,
    val chat_message_type: String,
    val file: String,
    val _id: String,
    val image: String,
    val video: String,
    val audio: String,
    var isLoaded:Boolean = true,
    var byCurrentUser:Boolean = false,
    var uploadAction: Boolean = false,
    var currentBuffer:Long,
    var totalBuffer:Long

)
