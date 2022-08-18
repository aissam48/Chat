package com.android.chat.ui.login

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.android.chat.MyService
import com.android.chat.R
import com.android.chat.databinding.ActivityLogInBinding
import com.android.chat.network.retrofit
import com.android.chat.ui.groups.GroupsActivity
import com.android.chat.ui.signup.SignUp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.alert_setting.view.*
import org.json.JSONObject


class LogIn : AppCompatActivity() {

    private lateinit var retrofit: retrofit
    lateinit var customViewModel: LogInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        val binding: ActivityLogInBinding =  ActivityLogInBinding.inflate(layoutInflater)

        setContentView(binding.root)

        startService(Intent(this, MyService::class.java))
        customViewModel = ViewModelProviders.of(this)[LogInViewModel::class.java]

        customViewModel.onGetInMutableLiveData.observe(this){

            binding.btnGetIn.visibility = View.VISIBLE
            binding.waitGetIn.visibility = View.GONE
            binding.btnGetIn.isEnabled = true

            if(it.code() == 500){
                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
                return@observe
            }

            if(it.code() == 400){
                val serverResponse = it.errorBody()?.string()?.let { it1 -> JSONObject(it1) }!!
                val message = JSONObject(serverResponse.getJSONArray("errors")[0].toString()).getString("message").toString()
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return@observe
            }

            if (it.code() == 401){
                val serverResponse = it.errorBody()?.string()?.let { it1 -> JSONObject(it1) }!!
                val message = serverResponse.getString("message").toString()
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return@observe
            }

            if (it.code() == 200){

                val jsonObject  = it.body()?.string()?.let { it1 -> JSONObject(it1) }!!
                val data  = jsonObject.getJSONObject("data")
                val user = data.getJSONObject("user")

                val shared = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).edit()
                shared.putString("email", user.getString("email"))
                shared.putString("first_name", user.getString("first_name"))
                shared.putString("last_name", user.getString("last_name"))
                shared.putString("photo_url", user.getString("photo_url"))
                shared.putString("_id", user.getString("_id"))
                shared.apply()
                val intent = Intent(this@LogIn, GroupsActivity::class.java)
                startActivity(intent)
            }


        }
        customViewModel.onGetinMutableLiveDataFailure.observe(this){
            Toast.makeText(this, "Please try later", Toast.LENGTH_LONG).show()
            binding.btnGetIn.visibility = View.VISIBLE
            binding.waitGetIn.visibility = View.GONE
            binding.btnGetIn.isEnabled = true
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        retrofit = retrofit()

        binding.btnGetIn.setOnClickListener {
            val email = binding.editInputEmail.text!!.toString().trim()
            val password = binding.editInputPassword.text!!.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (email.isEmpty()){
                Toast.makeText(this, "Enter email please", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.isEmpty()){
                Toast.makeText(this, "Enter password please", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.length < 6){
                Toast.makeText(this, "Password must be more than 6 chars", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            binding.btnGetIn.visibility = View.INVISIBLE
            binding.btnGetIn.isEnabled = false
            binding.waitGetIn.visibility = View.VISIBLE

            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                customViewModel.MethodGetIn(retrofit.retrofit(), email, password, it.result!!)
            }.addOnFailureListener{
                customViewModel.MethodGetIn(retrofit.retrofit(), email, password, "")
            }


        }

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }


    }
}