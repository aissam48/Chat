package com.android.chat.ui.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.chat.Constants
import com.android.chat.network.endpoints
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SearchActivityViewModel:ViewModel() {


    lateinit var socket: Socket

    fun connectSocket( socket: Socket){
        this.socket = socket
        socket.connect()
    }


    fun MethodLogOutMutableLiveData(currentUser: String){
        socket.emit(Constants.LOG_OUT_EVENT, currentUser)

    }

    fun GetInToMyPrivateRoom(currentUser: String){
        socket.emit(Constants.JOIN_CHAT, currentUser)
    }


    val clearEditText :MutableLiveData<String> = MutableLiveData()
    fun setOrderClearEditText(){
        clearEditText.value = ""
    }

    val searchMutableLiveData:MutableLiveData<Response<JsonObject>> = MutableLiveData()
    val searchMutableLiveDataFailure:MutableLiveData<Throwable> = MutableLiveData()

    fun search(retrofit: Retrofit, name:String){

        retrofit.create(endpoints::class.java).search(name).enqueue(object :Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                searchMutableLiveData.value = response

            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                searchMutableLiveDataFailure.value = t

            }

        })
    }

    fun joinChat(currentUser: String){
        socket.emit(Constants.JOIN_CHAT, currentUser)
    }
}