package com.android.chat.ui.groups

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.chat.Constants
import com.android.chat.MyService
import com.android.chat.R
import com.android.chat.databinding.ActivityGroupsActivityBinding
import com.android.chat.models.CallModel
import com.android.chat.models.GroupModel
import com.android.chat.network.retrofit
import com.android.chat.ui.call.CallActivity
import com.android.chat.ui.search.SearchActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import org.json.JSONObject
import retrofit2.Retrofit

class GroupsActivity : AppCompatActivity() {

    private val socketIO = IO.socket(Constants.SOCKETIO_URL)
    private lateinit var retrofit: Retrofit
    lateinit var customViewModel: GroupsActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityGroupsActivityBinding = ActivityGroupsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentUser = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        retrofit = retrofit().retrofit()
        customViewModel = ViewModelProviders.of(this)[GroupsActivityViewModel::class.java]

        //connect by socket
        customViewModel.connectSocket(socketIO)


        // send request to join in private room
        customViewModel.joinChat(currentUser)

        binding.chatListSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }



        // chats list which requested by retrofit
        customViewModel.BringChalListMutableLiveData.observe(this){

            binding.waitChatList.visibility = View.GONE
            binding.recyclerChatList.visibility = View.VISIBLE

            if(it.code() == 500){

            }

            if(it.code() == 400){

            }

            if(it.code() == 200){

                val type = object :TypeToken<MutableList<GroupModel>>(){}.type
                val jsonObject = it.body()?.string()?.let { it1 -> JSONObject(it1) }!!
                val data = JSONObject(jsonObject.getString("data"))
                val messages = data.getString("chats")
                val formatMessages = Gson().fromJson<MutableList<GroupModel>>(messages, type)

                formatMessages.sortByDescending { timeLong->
                    timeLong.updated_at
                }

                val customAdapter = GroupsAdapter(formatMessages, this)
                runOnUiThread {
                    when(formatMessages.size){
                        0 ->{
                            binding.recyclerChatList.adapter = null
                        }
                        else -> {
                            binding.recyclerChatList.adapter = customAdapter
                        }
                    }
                }
            }
        }


        // here methode view model to fetch chats list by retrofit
        customViewModel.MethodBringChalListMutableLiveData(retrofit, currentUser)


        customViewModel.refresh()
        customViewModel.refreshMutableLiveData.observe(this){
            runOnUiThread {
                customViewModel.MethodBringChalListMutableLiveData(retrofit, currentUser)
            }
        }


        // this for process online state
        val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).edit()
        sh.putBoolean("onlineState", false)
        sh.apply()


        //////////////////////////////////////////////////////////////////////////
        // listen to call which is coming from others

        socketIO.on("receive_call"){
            val data = it[0].toString()
            val type = object : TypeToken<CallModel>() {}.type
            val callModel = Gson().fromJson<CallModel>(data, type)
            if (currentUser == callModel.receiver_id) {

                val intent = Intent(this, CallActivity::class.java)
                intent.putExtra("group_id", callModel.group_id)
                intent.putExtra("current_user_id", callModel.receiver_id)
                intent.putExtra("receiver_id", callModel.current_user_id)
                intent.putExtra("caller_full_name", callModel.caller_full_name)
                intent.putExtra("answerer_full_name", callModel.answerer_full_name)
                intent.putExtra("caller_photo_url", callModel.caller_photo_url)
                intent.putExtra("answerer_photo_url", callModel.answerer_photo_url)
                intent.putExtra("incoming", "fromHim")
                startActivity(intent)

            }
        }
//        customViewModel.listenCall()
//        customViewModel.listenCallMutableLiveData.observe(this){
//            if (currentUser == it.receiver_id) {
//
//                val intent = Intent(this, CallActivity::class.java)
//                intent.putExtra("group_id", it.group_id)
//                intent.putExtra("current_user_id", it.receiver_id)
//                intent.putExtra("receiver_id", it.current_user_id)
//                intent.putExtra("caller_full_name", it.caller_full_name)
//                intent.putExtra("answerer_full_name", it.answerer_full_name)
//                intent.putExtra("caller_photo_url", it.caller_photo_url)
//                intent.putExtra("answerer_photo_url", it.answerer_photo_url)
//                intent.putExtra("incoming", "fromHim")
//                startActivity(intent)
//
//            }
//        }
    }


    override fun onResume() {
        super.onResume()
        val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).edit()
        sh.putBoolean("onlineState", false)
        sh.apply()
        val currentUser = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!
        customViewModel.joinChat(currentUser)

    }

    override fun onPause() {
        super.onPause()
        val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).edit()
        sh.putBoolean("onlineState", true)
        sh.apply()
    }

    override fun onStop() {
        super.onStop()

        Handler().postDelayed({
            val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).getBoolean("onlineState", true)
            if (sh){
                val currentUser = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!
                customViewModel.MethodLogOutMutableLiveData(currentUser)
            }
        }, 7000)
    }


}













