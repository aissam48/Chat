package com.android.chat.ui.groups

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.chat.Constants
import com.android.chat.R
import com.android.chat.models.GroupModel
import com.android.chat.ui.chat.ChatActivity
import com.android.chat.utils.DateUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_chat.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.*
import java.util.concurrent.TimeUnit


class GroupsAdapter(val chatList:MutableList<GroupModel>, val context: Context): RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val item_chat_img = itemView.item_chat_img
        val item_chat_username = itemView.item_chat_username
        val item_chat_txt_last_message = itemView.item_chat_txt_last_message
        val item_chat_time_send = itemView.item_chat_time_send
        val item_chat_container = itemView.item_chat_container
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(v)

    }

    @SuppressLint("CheckResult")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val dataChat = chatList[position]
        val current_user_id = context.getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!
        try {

            Glide.with(context).load(dataChat.user.photo_url).addListener(object : RequestListener<Drawable>{
                @SuppressLint("CheckResult")
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.item_chat_img.setImageResource(R.drawable.ic_face)
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

            }).into(holder.item_chat_img)
            //Picasso.with(context).load(dataChat.user.photo_url).into(holder.item_chat_img)
            holder.item_chat_username.text = dataChat.user.first_name

        }catch (e:Exception){

        }

        when(dataChat.recent_message.content_type){
            "image" ->{
                holder.item_chat_txt_last_message.text = "image"
            }
            "text" ->{
                holder.item_chat_txt_last_message.text = dataChat.recent_message.message
            }
            "audio" ->{
                holder.item_chat_txt_last_message.text = "audio"
            }
            "video" ->{
                holder.item_chat_txt_last_message.text = "video"
            }
        }

        holder.item_chat_time_send.text =  DateUtils.sinceFrom(context, dataChat.updated_at, Constants.FULL_DATE_FORMAT)

        holder.item_chat_container.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            for (i in dataChat.participants){
                if(i != current_user_id){
                    intent.putExtra("group_id",dataChat._id)
                    intent.putExtra("receiver_id", i)
                    intent.putExtra("receiver_photo_url", dataChat.user.photo_url)
                    intent.putExtra("receiver_first_name", dataChat.user.first_name)
                    intent.putExtra("receiver_last_name", dataChat.user.last_name)
                    intent.flags =  Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int = chatList.size

}