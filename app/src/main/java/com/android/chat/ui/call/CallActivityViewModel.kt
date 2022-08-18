package com.android.chat.ui.call

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.chat.Constants
import com.android.chat.models.CallModel
import com.android.chat.models.CurRecUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.Socket

class CallActivityViewModel : ViewModel() {

    lateinit var socket: Socket

    fun connectSocket( socket: Socket){
        this.socket = socket
         socket.connect()
    }

    fun MethodLogOutMutableLiveData(_id: String){
        socket.emit(Constants.LOG_OUT_EVENT, _id)
    }

    fun joinChat(currentUser: String){
        socket.emit(Constants.JOIN_CHAT, currentUser)
    }

    fun endCall(current_user_id:String, receiver_id:String){
        socket.emit(Constants.END_CALL, Gson().toJson(CurRecUser(current_user_id, receiver_id)))
    }

    val otherAnsweredStatusMutableLiveData : MutableLiveData<CurRecUser> = MutableLiveData()
    fun otherAnsweredStatus(){
        socket.on(Constants.OTHER_ANSWERED_STATUS) {
            val data = it[0].toString()
            val type = object : TypeToken<CurRecUser>() {}.type
            val curRecUser = Gson().fromJson<CurRecUser>(data, type)
            otherAnsweredStatusMutableLiveData.postValue(curRecUser)
        }
    }

    val endCallStatusMutableLiveData : MutableLiveData<CurRecUser> = MutableLiveData()
    fun endCallStatus(){
        socket.on(Constants.END_CALL_STATUS) {
            val data = it[0].toString()
            val type = object : TypeToken<CurRecUser>() {}.type
            val curRecUser = Gson().fromJson<CurRecUser>(data, type)
            endCallStatusMutableLiveData.postValue(curRecUser)
        }
    }

    fun makeCall(current_user_id:String, receiver_id:String, group_id:String, caller_photo_url:String, caller_full_name:String, answerer_photo_url:String, answerer_full_name:String){
        socket.emit(Constants.MAKE_CALL, Gson().toJson(CallModel(current_user_id, receiver_id, group_id, caller_photo_url, caller_full_name, answerer_photo_url, answerer_full_name)))
    }

    fun answerCall(current_user_id:String, receiver_id:String){
        socket.emit(Constants.ANSWER_CALL, Gson().toJson(CurRecUser(current_user_id, receiver_id)))
    }

    val userAtAnotherMutableLiveData:MutableLiveData<CurRecUser> = MutableLiveData()
    fun userAtAnotherCall(){
        socket.on(Constants.AT_ANOTHER_CALL){
            val data = it[0].toString()
            val type = object :TypeToken<CurRecUser>(){}.type
            val curRecUser = Gson().fromJson<CurRecUser>(data, type)
            Log.e("userAtAnotherMutableLiveData", curRecUser.toString())
            userAtAnotherMutableLiveData.postValue(curRecUser)
        }
    }

}