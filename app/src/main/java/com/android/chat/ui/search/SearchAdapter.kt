package com.android.chat.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.android.chat.R
import com.android.chat.models.User
import com.android.chat.ui.chat.ChatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_user.view.*


class SearchAdapter(val userList : MutableList<User>, val context: Context):
    RecyclerView.Adapter<SearchAdapter.ViewHolder>(){

    lateinit var customViewModel: SearchActivityViewModel
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val item_user_img = itemView.item_user_img
        val item_user_username = itemView.item_user_username
        val layout_user = itemView.layout_user

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataUser = userList[position]
        customViewModel = ViewModelProviders.of(context as SearchActivity)[SearchActivityViewModel::class.java]

        try {
            Glide.with(context).load(dataUser.photo_url).addListener(object :
                RequestListener<Drawable> {
                @SuppressLint("CheckResult")
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.item_user_img.setImageDrawable(
                        ContextCompat.getDrawable(context,
                        R.drawable.ic_gallary
                    ))
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {

                    return false
                }

            }).into(holder.item_user_img)

        }catch (e:Exception){
            Log.d("CustomAdapterLookingFor", e.message.toString())
        }


        holder.item_user_username.text = dataUser.first_name
        holder.layout_user.setOnClickListener {

            val currentUser = context.getSharedPreferences("CurrentUser", Context.MODE_PRIVATE)
                .getString("_id", "Empty")!!

            if(dataUser._id == currentUser){
                return@setOnClickListener
            }

            customViewModel.setOrderClearEditText()
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("receiver_id", dataUser._id)
            intent.putExtra("group_id", "Empty")
            intent.putExtra("receiver_photo_url", dataUser.photo_url)
            intent.putExtra("receiver_first_name", dataUser.first_name)
            intent.putExtra("receiver_last_name", dataUser.last_name)
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int = userList.size
}



