package com.android.chat.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.chat.models.LogInModel
import com.android.chat.network.endpoints
import io.socket.client.Socket
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LogInViewModel:ViewModel() {

    lateinit var socket: Socket

    val onGetInMutableLiveData : MutableLiveData<Response<ResponseBody>> = MutableLiveData()
    val onGetinMutableLiveDataFailure : MutableLiveData<String> = MutableLiveData()
    fun MethodGetIn(retrofit: Retrofit, email: String, password: String, token_firebase: String){

        retrofit.create(endpoints::class.java).getIn(LogInModel(email, password, token_firebase)).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onGetInMutableLiveData.postValue(response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onGetinMutableLiveDataFailure.postValue(t.message.toString())
            }
        })
    }

}