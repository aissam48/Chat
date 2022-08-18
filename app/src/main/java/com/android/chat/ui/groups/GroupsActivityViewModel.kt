package com.android.chat.ui.groups

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.chat.Constants
import com.android.chat.models.CallModel
import com.android.chat.network.endpoints
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class GroupsActivityViewModel: ViewModel() {

    lateinit var socket: Socket

    fun connectSocket( IO: Socket){
        socket = IO
        socket.connect()
    }

    fun joinChat(currentUser: String){
        socket.emit(Constants.JOIN_CHAT, currentUser)
    }

    val BringChalListMutableLiveData : MutableLiveData<Response<ResponseBody>> = MutableLiveData()
    val BringChalListMutableLiveDataFailer : MutableLiveData<String> = MutableLiveData()
    fun MethodBringChalListMutableLiveData(retrofit: Retrofit, currentUser: String){

        retrofit.create(endpoints::class.java).RequestMyChats(currentUser)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    BringChalListMutableLiveData.value = response
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    BringChalListMutableLiveDataFailer.value = t.message.toString()
                }
            })
    }


    fun MethodLogOutMutableLiveData(_id: String){
        socket.emit(Constants.LOG_OUT_EVENT, _id)
    }


    val listenCallMutableLiveData :MutableLiveData<CallModel> = MutableLiveData()
    fun listenCall(){
        socket.on("receive_call") {
            val data = it[0].toString()
            val type = object : TypeToken<CallModel>() {}.type
            val callModel = Gson().fromJson<CallModel>(data, type)
            listenCallMutableLiveData.postValue(callModel)
        }
    }


    val refreshMutableLiveData:MutableLiveData<String> = MutableLiveData()
    fun refresh(){
        socket.on(Constants.REFRESH_GROUPS){
            refreshMutableLiveData.postValue("")
        }
    }


}