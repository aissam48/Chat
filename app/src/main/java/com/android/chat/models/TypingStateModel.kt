package com.android.chat.models

data class TypingStateModel(val chat_id:String ,val current_user_id:String, val receiver_id:String, val is_typing:Boolean)
