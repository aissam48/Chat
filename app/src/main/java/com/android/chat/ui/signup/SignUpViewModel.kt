package com.android.chat.ui.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.chat.models.SignUpModel
import com.android.chat.network.endpoints
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SignUpViewModel:ViewModel() {

    val onSignUpMutableLiveData : MutableLiveData<Response<ResponseBody>> = MutableLiveData()
    val onSignUpMutableLiveDataFailure : MutableLiveData<String> = MutableLiveData()
    fun MethodSignUp(retrofit: Retrofit, email: String, firstName: String, lastName: String, password: String, token_firebase: String){
        val signUpModel = SignUpModel(email, firstName, lastName, password, token_firebase)
        retrofit.create(endpoints::class.java).SignUp(signUpModel).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onSignUpMutableLiveData.postValue(response)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                onSignUpMutableLiveDataFailure.postValue(t.message.toString())
            }
        })
    }
}