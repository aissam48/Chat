package com.android.chat.ui.search

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProviders
import com.android.chat.Constants
import com.android.chat.R
import com.android.chat.databinding.ActivitySearchActivityBinding
import com.android.chat.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {

    private val socketIO = IO.socket(Constants.SOCKETIO_URL)

    lateinit var binding: ActivitySearchActivityBinding
    lateinit var customViewModel: SearchActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        val retrofit = com.android.chat.network.retrofit().retrofit()
        customViewModel = ViewModelProviders.of(this)[SearchActivityViewModel::class.java]
        customViewModel.connectSocket(socketIO)

        binding.back.setOnClickListener {
            finish()
        }


        binding.editSearch.doOnTextChanged { text, start, before, count ->
                when(text?.length){
                    0->{

                    }
                    else ->{

                        customViewModel.search(retrofit, text.toString())
                        customViewModel.searchMutableLiveData.observe(this){
                            if(it.code() == 500){
                                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
                                return@observe
                            }


                            if (it.code() == 200){

                                val data  = it.body()?.getAsJsonObject("data")
                                val user = data?.get("users")
                                val type = object :TypeToken<MutableList<User>>(){}.type
                                val dat = Gson().fromJson<MutableList<User>>(user.toString(), type)

                                runOnUiThread {
                                    val customAdapterLookingFor = SearchAdapter(dat, this)
                                    binding.recyclerLookingFor.adapter = customAdapterLookingFor
                                }
                            }
                        }

                    }
                }
            }


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

























