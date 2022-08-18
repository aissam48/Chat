package com.android.chat.models


data class CallModel(val current_user_id:String, val receiver_id:String, val group_id:String, val caller_photo_url:String, val caller_full_name:String, val answerer_full_name:String, val answerer_photo_url:String)
