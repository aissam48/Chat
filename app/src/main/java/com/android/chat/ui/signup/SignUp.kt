package com.android.chat.ui.signup

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.android.chat.R
import com.android.chat.databinding.ActivityShowImageBinding
import com.android.chat.databinding.ActivitySignUpBinding
import com.android.chat.network.retrofit
import com.android.chat.ui.groups.GroupsActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

class SignUp : AppCompatActivity() {

    lateinit var customViewModel: SignUpViewModel
    private lateinit var retrofit: retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivitySignUpBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
         customViewModel = ViewModelProviders.of(this)[SignUpViewModel::class.java]
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        retrofit = retrofit()

        binding.back.setOnClickListener {
            finish()
        }

        customViewModel.onSignUpMutableLiveData.observe(this){

            binding.btnCreateAccount.visibility = View.VISIBLE
            binding.btnCreateAccount.isEnabled = true
            binding.waitSignUp.visibility = View.INVISIBLE

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
                val message = serverResponse.getString("description").toString()
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
                val intent = Intent(this@SignUp, GroupsActivity::class.java)
                startActivity(intent)
            }

        }



        customViewModel.onSignUpMutableLiveDataFailure.observe(this){
            Toast.makeText(this, "Please try later", Toast.LENGTH_SHORT).show()
        }

        binding.btnCreateAccount.setOnClickListener {
            val email = binding.editInputSignUpEmail.text.toString()
            val firstName = binding.editInputSignUpFirstName.text.toString()
            val lastName = binding.editInputSignUpLastName.text.toString()
            val password = binding.editInputSignUpPassword.text.toString()
            if (email.isEmpty()){
                Toast.makeText(this, "Enter email", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (firstName.isEmpty()){
                Toast.makeText(this, "Enter firstName", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (lastName.isEmpty()){
                Toast.makeText(this, "Enter lastName", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password.isEmpty()){
                Toast.makeText(this, "Enter password", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password.length < 6){
                Toast.makeText(this, "Password must be more than 6 chars", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            binding.btnCreateAccount.visibility = View.INVISIBLE
            binding.btnCreateAccount.isEnabled = false
            binding.waitSignUp.visibility = View.VISIBLE

            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                customViewModel.MethodSignUp(retrofit.retrofit(), email, firstName, lastName, password, it.result!!)
                Log.e("FirebaseMessaging",  it.result!!)
            }.addOnFailureListener {
                customViewModel.MethodSignUp(retrofit.retrofit(), email, firstName, lastName, password, "")
                Log.e("FirebaseMessaging",  it.message!!)
            }

        }


    }
}