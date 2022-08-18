package com.android.chat.network

import com.android.chat.models.*
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface endpoints {

    @POST("users/login")
    fun getIn(@Body dataGetIn: LogInModel):Call<ResponseBody>

    @POST("users/register")
    fun SignUp(@Body user: SignUpModel):Call<ResponseBody>

    @GET("users/{userId}/chats")
    fun RequestMyChats (@Path("userId") userId: String): Call<ResponseBody>

    @GET("chats/{chatId}/messages/{limit}/{skip}")
    fun RequestMyMessages (
        @Path("chatId") chatId: String,
        @Path("limit") limit: Int,
        @Path("skip") skip: Int
    ): Call<ResponseBody>

    @GET("users/{userId}")
    fun requestUserById(@Path("userId") userId: String): Call<ResponseBody>

    @GET("users/search/{query}")
    fun search(@Path("query") query: String): Call<JsonObject>

    @POST("messages")
    fun sendText (@Body MessageModel: MessageModel): Call<MessageModel>

    @POST("chats")
    fun createChat(@Body participantsModel: ParticipantsModel): Call<ResponseBody>

    @Multipart
    @POST("messages")
    fun postMessage(

        @Part file: MultipartBody.Part,
        @Part("message") messagePart: RequestBody,
        @Part("group_id") group_id: RequestBody,
        @Part("content_type") content_type: RequestBody,
        @Part("sender_id") sender_idPart: RequestBody,
        @Part("chat_message_type") chat_message_typePart: RequestBody,
        @Part("file") filePart: RequestBody,
        @Part("_id") idPart: RequestBody,
        @Part("image") imagePart: RequestBody,
        @Part("video") videoPart: RequestBody,
        @Part("audio") audioPart: RequestBody,
        @Part("current_user_id") current_user_id: RequestBody,
        @Part("receiver_id") receiver_id: RequestBody,

    ):Call<JsonObject>


    @Multipart
    @POST("messages/message")
    fun postAudio(

        @Part file: MultipartBody.Part,
        @Part("message") messagePart: RequestBody,
        @Part("group_id") group_id: RequestBody,
        @Part("content_type") content_type: RequestBody,
        @Part("sender_id") sender_idPart: RequestBody,
        @Part("chat_message_type") chat_message_typePart: RequestBody,
        @Part("file") filePart: RequestBody,
        @Part("_id") idPart: RequestBody,
        @Part("image") imagePart: RequestBody,
        @Part("video") videoPart: RequestBody,
        @Part("audio") audioPart: RequestBody,
        @Part("current_user_id") current_user_id: RequestBody,
        @Part("receiver_id") receiver_id: RequestBody,

        ):Call<JsonObject>



    @Multipart
    @POST("messages/message")
    fun postVideo(

        @Part file: MultipartBody.Part,
        @Part("message") messagePart: RequestBody,
        @Part("group_id") group_id: RequestBody,
        @Part("content_type") content_type: RequestBody,
        @Part("sender_id") sender_idPart: RequestBody,
        @Part("chat_message_type") chat_message_typePart: RequestBody,
        @Part("file") filePart: RequestBody,
        @Part("_id") idPart: RequestBody,
        @Part("image") imagePart: RequestBody,
        @Part("video") videoPart: RequestBody,
        @Part("audio") audioPart: RequestBody,
        @Part("current_user_id") current_user_id: RequestBody,
        @Part("receiver_id") receiver_id: RequestBody,

        ):Call<JsonObject>

}