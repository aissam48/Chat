package com.android.chat

import android.app.Activity
import android.content.Context

object Constants {


    const val AT_ANOTHER_CALL = "user_at_another_call"
    const val FULL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz" //yyyy-MM-dd'T'HH:mm:ss.X
    const val JOIN_CHAT_ROOM = "user_join_chat_room"
    const val LEAVE_CHAT_ROOM = "user_leave_chat_room"
    const val JOIN_CHAT = "user_join_chat"
    const val IS_TYPING  = "user_is_typing"
    const val LOG_OUT_EVENT = "user_leave_chat"
    const val TYPING_STATE = "user_typing"
    const val NOTIFY_OTHER_MY_LOG_OUT = "user_disconnected"
    const val RECEIVE_MESSAGE = "new_message"
    const val IAM_ONLINE = "user_connected"
    const val REFRESH_GROUPS = "chat_updated"
    const val END_CALL = "end_call"
    const val OTHER_ANSWERED_STATUS = "otherAnsweredStatus"
    const val END_CALL_STATUS = "end_call_status"
    const val MAKE_CALL = "make_call"
    const val ANSWER_CALL = "answer_call"
    const val RETROFIT_URL =  "https://expressjs-backend-side.herokuapp.com/api/v1/"
    const val SOCKETIO_URL = "https://expressjs-backend-side.herokuapp.com/"


}