package com.android.chat.ui.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.chat.Constants
import com.android.chat.models.*
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

class ChatActivityViewModel:ViewModel() {

    lateinit var socket: Socket

    fun connectSocket( socket: Socket){
        this.socket = socket
        socket.connect()
    }

    fun joinChatRoom(group_id:String,){
        socket.emit(Constants.JOIN_CHAT_ROOM, group_id)
    }

    val onChatListChanged : MutableLiveData<TypingStateModel> = MutableLiveData()
    fun state(){
        socket.on(Constants.TYPING_STATE){
            val state = it[0]!!.toString()
            val type = object :TypeToken<TypingStateModel>(){}.type
            val typingStateModel = Gson().fromJson<TypingStateModel>(state, type)

            when(typingStateModel.is_typing){
                true->{
                    onChatListChanged.postValue(typingStateModel)
                }
                false->{
                    onChatListChanged.postValue(typingStateModel)
                }
            }
        }
    }


    val receiverInformationMutableData :MutableLiveData<Response<ResponseBody>> = MutableLiveData()
    val receiverInformationMutableDataFailure :MutableLiveData<Throwable> = MutableLiveData()

    fun requestReceiverInformation(retrofit: Retrofit, receiver_id: String){

        retrofit.create(endpoints::class.java).requestUserById(receiver_id).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                receiverInformationMutableData.value = response
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                receiverInformationMutableDataFailure.value = t
            }

        })
    }

    val sendTextMutableLiveData : MutableLiveData<MessageModel> = MutableLiveData()
    val sendTextMutableLiveDataFailure : MutableLiveData<String> = MutableLiveData()
    fun sendText(retrofit: Retrofit, MessageModel: MessageModel){
        retrofit.create(endpoints::class.java).sendText(MessageModel).enqueue(object :
            Callback<MessageModel> {
            override fun onResponse(call: Call<MessageModel>, response: Response<MessageModel>) {
                sendTextMutableLiveData.value = MessageModel
            }

            override fun onFailure(call: Call<MessageModel>, t: Throwable) {
                sendTextMutableLiveDataFailure.value = t.message.toString()
            }

        })
    }



    val BringChalWithMutableLiveData : MutableLiveData<Response<ResponseBody>> = MutableLiveData()
    val BringChalWithMutableLiveDataFailer : MutableLiveData<String> = MutableLiveData()
    fun MethodBringChalWithMutableLiveData(retrofit: Retrofit, groupIdModule: GroupIdModule, skip:Int, limit:Int){
        retrofit.create(endpoints::class.java).RequestMyMessages(groupIdModule.group_id, limit, skip)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    BringChalWithMutableLiveData.value = response
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    BringChalWithMutableLiveDataFailer.value = t.message.toString()
                }
            })
    }


    val onChatListReceiveNewMessage : MutableLiveData<String> = MutableLiveData()
    fun ReceiveNewMessage(){
        socket.on(Constants.RECEIVE_MESSAGE){
            onChatListReceiveNewMessage.postValue(it[0].toString())

        }
    }



    // this get notify once receiver get in app
    val GetNotifyMutableLivedata : MutableLiveData<String> = MutableLiveData()
    fun MethodGetNotify(receiver_id: String){
        socket.on(Constants.IAM_ONLINE){
            val data = it[0].toString()
            GetNotifyMutableLivedata.postValue(data)
        }
    }

    val currentBufferVideo : MutableLiveData<Long> = MutableLiveData()
    val totalBufferVideo : MutableLiveData<Long> = MutableLiveData()
    fun notifyVideoItemForCurrentBuffer(buffer: Long, total:Long){
        currentBufferVideo.value = buffer
        totalBufferVideo.value = total
    }



    val currentBufferAudio : MutableLiveData<Long> = MutableLiveData()
    val totalBufferAudio : MutableLiveData<Long> = MutableLiveData()
    fun notifyAudioItemForCurrentBuffer(buffer: Long, total:Long){
        currentBufferAudio.value = buffer
        totalBufferAudio.value = total
    }



    val currentBufferImage : MutableLiveData<Long> = MutableLiveData()
    val totalBufferImage : MutableLiveData<Long> = MutableLiveData()
    fun notifyImageItemForCurrentBuffer(buffer: Long, total:Long){
        currentBufferImage.value = buffer
        totalBufferImage.value = total
    }


    fun MethodLogOutMutableLiveData(_id: String){
        socket.emit(Constants.LOG_OUT_EVENT, _id)
    }

    fun joinChat(currentUser: String){

        socket.emit(Constants.JOIN_CHAT, currentUser)
    }



    val chatIdMutableLiveData :MutableLiveData<Response<ResponseBody>> = MutableLiveData()
    val chatIdMutableLiveDataFailure :MutableLiveData<Throwable> = MutableLiveData()
    fun createChat(retrofit: Retrofit, participants:ParticipantsModel){
        retrofit.create(endpoints::class.java).createChat(participants).enqueue(object :Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
               chatIdMutableLiveData.value = response
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                chatIdMutableLiveDataFailure.value = t
            }
        })
    }


    val notifyLogOutMutableLiveData :MutableLiveData<User> = MutableLiveData()
    fun notifyLogOut(){
        socket.on(Constants.NOTIFY_OTHER_MY_LOG_OUT) {
            val data = it[0].toString()
            val type = object : TypeToken<User>() {}.type
            val user = Gson().fromJson<User>(data, type)
            notifyLogOutMutableLiveData.postValue(user)
        }
    }

    fun isTypingStatus(group_id:String, current_user_id:String, receiver_id:String, status:Boolean){
        socket.emit(Constants.IS_TYPING, Gson().toJson(TypingStateModel(group_id, current_user_id, receiver_id, status)))

    }



}