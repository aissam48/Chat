package com.android.chat.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.android.chat.Constants
import com.android.chat.CustomRequestBody
import com.android.chat.R
import com.android.chat.models.MessageModel
import com.android.chat.network.endpoints
import com.android.chat.network.retrofit
import com.android.chat.ui.PlayVideo
import com.android.chat.ui.ShowImage
import com.android.chat.utils.DateUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.item_audio_currentuser.view.*
import kotlinx.android.synthetic.main.item_audio_receiver.view.*
import kotlinx.android.synthetic.main.item_image_currentuser.view.*
import kotlinx.android.synthetic.main.item_image_receiver.view.*
import kotlinx.android.synthetic.main.item_message_currentuser.view.*
import kotlinx.android.synthetic.main.item_message_receiver.view.*
import kotlinx.android.synthetic.main.item_video_currentuser.view.*
import kotlinx.android.synthetic.main.item_video_receiver.view.*
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.lang.Runnable


class ChatAdapter(
    val messages: MutableList<MessageModel>,
    val context: Context,
    val mediaPlayer: MediaPlayer,
) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val currentUser = context.getSharedPreferences("CurrentUser", Context.MODE_PRIVATE)
        .getString("_id", "Empty")!!

    var messagesSize = 0
    lateinit var customViewModel: ChatActivityViewModel

    val retrofit = retrofit().retrofit()

    private var playingHolder: ViewHolder? = null
    val seekBarUpdater = SeekBarUpdater()


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // get id For CurrentUser
        val item_message_currentuser_message = itemView.item_message_currentuser_message
        val item_message_currentuser_time = itemView.item_message_currentuser_time

        val item_image_currentuser_img = itemView.item_image_currentuser_img
        val item_image_currentuser_time = itemView.item_image_currentuser_time
        val cardImageCurrenrUser = itemView.cardImageCurrenrUser
        val isLoadedProgressImageCurrentUser = itemView.isLoadedProgressImageCurrentUser


        val item_audio_currentuser_play_pause = itemView.item_audio_currentuser_play_pause
        val item_audio_currentuser_time = itemView.item_audio_currentuser_time
        val item_audio_currentuser_slider = itemView.item_audio_currentuser_slider
        val isLoadedProgressAudioCurrentUser = itemView.isLoadedProgressAudioCurrentUser

        val item_video_currentuser_video = itemView.item_video_currentuser_video
        val item_video_currentuser_play_video = itemView.item_video_currentuser_play_video
        val item_video_currentuser_time = itemView.item_video_currentuser_time
        val isLoadedProgressVideoCurrentUser = itemView.isLoadedProgressVideoCurrentUser

        /////////////////////////////////////////////////////////////////////////////////////////////////////
        //get id For Receiver
        val item_message_receiver_message = itemView.item_message_receiver_message
        val item_message_receiver_time = itemView.item_message_receiver_time

        val item_image_receiver_img = itemView.item_image_receiver_img
        val item_image_receiver_time = itemView.item_image_receiver_time
        val cardImageReceiver = itemView.cardImageReceiver

        val item_audio_receiver_play_pause = itemView.item_audio_receiver_play_pause
        val item_audio_receiver_time = itemView.item_audio_receiver_time
        val item_audio_receiver_slider = itemView.item_audio_receiver_slider


        val item_video_receiver_video = itemView.item_video_receiver_video
        val item_video_receiver_play_video = itemView.item_video_receiver_play_video
        val item_video_receiver_time = itemView.item_video_receiver_time
        val isLoadedProgressVideoReceiver = itemView.isLoadedProgressVideoReceiver


    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].sender_id) {
            currentUser -> {
                when (true) {
                    (messages[position].content_type == "text") -> 10
                    (messages[position].content_type == "image") -> 11
                    (messages[position].content_type == "audio") -> 12
                    (messages[position].content_type == "video") -> 13

                    else -> 14
                }
            }
            else -> {
                when (true) {
                    (messages[position].content_type == "text") -> 110
                    (messages[position].content_type == "image") -> 111
                    (messages[position].content_type == "audio") -> 112
                    (messages[position].content_type == "video") -> 113

                    else -> 114
                }
            }
            //1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return when (viewType) {
            10 -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_message_currentuser,
                        parent,
                        false
                    )

                ViewHolder(v)
            }

            11 -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_image_currentuser,
                        parent,
                        false
                    )
                ViewHolder(v)
            }

            12 -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_audio_currentuser,
                        parent,
                        false
                    )
                ViewHolder(v)
            }

            13 -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_video_currentuser,
                        parent,
                        false
                    )
                ViewHolder(v)
            }

            110 -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_message_receiver,
                        parent,
                        false
                    )
                ViewHolder(v)
            }

            111 -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_image_receiver, parent, false)
                ViewHolder(v)
            }

            112 -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_audio_receiver, parent, false)
                ViewHolder(v)
            }

            113 -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_video_receiver, parent, false)
                ViewHolder(v)
            }

            else -> {
                val v = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.item_message_receiver,
                        parent,
                        false
                    )
                ViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        customViewModel = ViewModelProviders.of(context as ChatActivity)[ChatActivityViewModel::class.java]
        val dataMessage = messages[position]

        when (dataMessage.sender_id) {
            currentUser -> {

                when (dataMessage.content_type) {
                    "text" -> {
                        holder.item_message_currentuser_time.text = DateUtils.sinceFrom(context, dataMessage.created_at, Constants.FULL_DATE_FORMAT)
                        holder.item_message_currentuser_message.text = dataMessage.message
                    }

                    "image" -> {

                        try {
                            holder.isLoadedProgressImageCurrentUser.visibility = View.GONE

                            if(dataMessage.byCurrentUser && dataMessage.uploadAction){
                                dataMessage.uploadAction = false
                                uploadPosition = position

                                uploadHolder = holder
                                uploadMediaImage(retrofit, dataMessage, uploadHolder!!)
                                holder.isLoadedProgressImageCurrentUser.visibility = View.VISIBLE
                            }else{
                                if(dataMessage.byCurrentUser){

                                    holder.isLoadedProgressImageCurrentUser.visibility = View.VISIBLE

                                    customViewModel.currentBufferImage.observe(context){
                                        holder.isLoadedProgressImageCurrentUser.progress = dataMessage.currentBuffer.toFloat()
                                    }
                                    customViewModel.totalBufferImage.observe(context){
                                        holder.isLoadedProgressImageCurrentUser.progressMax = dataMessage.totalBuffer.toFloat()
                                    }

                                }else{
                                    if (dataMessage.isLoaded){
                                        holder.isLoadedProgressImageCurrentUser.visibility = View.GONE
                                    }else{
                                        holder.isLoadedProgressImageCurrentUser.visibility = View.GONE
                                    }
                                }
                            }

                            Glide.with(context).load(dataMessage.image).addListener(object :
                                RequestListener<Drawable> {
                                @SuppressLint("CheckResult")
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    holder.item_image_currentuser_img.setImageDrawable(ContextCompat.getDrawable(context,
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

                            }).into(holder.item_image_currentuser_img)


                        } catch (e: Exception) {

                        }
                        holder.item_image_currentuser_time.text = DateUtils.sinceFrom(context, dataMessage.created_at, Constants.FULL_DATE_FORMAT)

                        holder.cardImageCurrenrUser.setOnClickListener {
                            val intent = Intent(context, ShowImage::class.java)
                            intent.putExtra("ImageUrl", dataMessage.image)
                            context.startActivity(intent)
                        }

                    }

                    "audio" -> {

                        handlerSeekBar(position, holder)
                        if (position == currentPlaying) {
                            playingHolder = holder
                            updatePlaying(playingHolder!!)

                        } else {
                            updateNonPlaying(position, holder)
                        }

                        holder.item_audio_currentuser_play_pause.visibility = View.VISIBLE
                        holder.isLoadedProgressAudioCurrentUser.visibility = View.GONE

                        if(dataMessage.byCurrentUser && dataMessage.uploadAction){
                            dataMessage.uploadAction = false
                            uploadPosition = position
                            uploadHolder = holder
                            uploadMediaAudio(retrofit, dataMessage, uploadHolder!!)
                            holder.item_audio_currentuser_play_pause.visibility = View.INVISIBLE
                            holder.item_audio_currentuser_play_pause.isEnabled = false
                            holder.isLoadedProgressAudioCurrentUser.visibility = View.VISIBLE
                        }else{
                            if(dataMessage.byCurrentUser){
                                holder.item_audio_currentuser_play_pause.visibility = View.INVISIBLE
                                holder.item_audio_currentuser_play_pause.isEnabled = false
                                holder.isLoadedProgressAudioCurrentUser.visibility = View.VISIBLE

                                customViewModel.currentBufferAudio.observe(context){
                                    holder.isLoadedProgressAudioCurrentUser.progress = dataMessage.currentBuffer.toFloat()

                                }
                                customViewModel.totalBufferAudio.observe(context){
                                    holder.isLoadedProgressAudioCurrentUser.progressMax = dataMessage.totalBuffer.toFloat()
                                }

                            }else{

                                if (dataMessage.isLoaded){
                                    holder.item_audio_currentuser_play_pause.visibility = View.VISIBLE
                                    holder.item_audio_currentuser_play_pause.isEnabled = true
                                    holder.isLoadedProgressAudioCurrentUser.visibility = View.GONE

                                }else{
                                    holder.item_audio_currentuser_play_pause.visibility = View.VISIBLE
                                    holder.item_audio_currentuser_play_pause.isEnabled = true
                                    holder.isLoadedProgressAudioCurrentUser.visibility = View.GONE
                                }
                            }
                        }



                        holder.item_audio_currentuser_play_pause.setOnClickListener {
                            //currentId = dataMessage.id
                            messagesSize = messages.size
                            if (position == currentPlaying) {
                                playingHolder = holder

                                if (mediaPlayer.isPlaying) {
                                    playingHolder!!.item_audio_currentuser_play_pause.setImageResource(
                                        R.drawable.ic_play
                                    )
                                    mediaPlayer.pause()
                                } else {
                                    playingHolder!!.item_audio_currentuser_play_pause.setImageResource(
                                        R.drawable.ic_baseline_pause_24
                                    )
                                    mediaPlayer.start()
                                }
                            } else {
                                if (currentPlaying != -1)
                                    updateNonPlaying(currentPlaying, playingHolder!!)
                                playingHolder = holder
                                currentPlaying = position
                                playAudio(playingHolder!!, dataMessage)

                            }

                        }

                        holder.item_audio_currentuser_time.text = DateUtils.sinceFrom(context, dataMessage.created_at, Constants.FULL_DATE_FORMAT)

                    }

                    "video" -> {

                        holder.item_video_currentuser_play_video.visibility = View.VISIBLE
                        holder.isLoadedProgressVideoCurrentUser.visibility = View.GONE

                        if(dataMessage.byCurrentUser && dataMessage.uploadAction){
                            dataMessage.uploadAction = false
                            uploadPosition = position
                            uploadHolder = holder
                            uploadMediaVideo(retrofit, dataMessage, holder!!, position)
                            holder.item_video_currentuser_play_video.visibility = View.GONE
                            holder.isLoadedProgressVideoCurrentUser.visibility = View.VISIBLE


                            GlobalScope.launch {
                                withContext(Dispatchers.Main){
                                    holder.isLoadedProgressVideoCurrentUser.progressBarColor= ContextCompat.getColor(context,
                                        R.color.blueSky
                                    )
                                    customViewModel.currentBufferVideo.observe(context){
                                        holder.isLoadedProgressVideoCurrentUser.progress = dataMessage.currentBuffer.toFloat()
                                    }
                                    customViewModel.totalBufferVideo.observe(context){
                                        holder.isLoadedProgressVideoCurrentUser.progressMax = dataMessage.totalBuffer.toFloat()
                                    }
                                }


                            }


                        }else{
                            if(dataMessage.byCurrentUser){
                                holder.item_video_currentuser_play_video.visibility = View.GONE
                                holder.isLoadedProgressVideoCurrentUser.visibility = View.VISIBLE
                                GlobalScope.launch {
                                    withContext(Dispatchers.Main){
                                        holder.isLoadedProgressVideoCurrentUser.progressBarColor= ContextCompat.getColor(context,
                                            R.color.blueSky
                                        )
                                        customViewModel.currentBufferVideo.observe(context){
                                            holder.isLoadedProgressVideoCurrentUser.progress = dataMessage.currentBuffer.toFloat()

                                        }
                                        customViewModel.totalBufferVideo.observe(context){
                                            holder.isLoadedProgressVideoCurrentUser.progressMax = dataMessage.totalBuffer.toFloat()
                                        }
                                    }
                                }

                            }else{

                                if (dataMessage.isLoaded){
                                    holder.item_video_currentuser_play_video.visibility = View.VISIBLE
                                    holder.isLoadedProgressVideoCurrentUser.visibility = View.GONE

                                }else{
                                    holder.item_video_currentuser_play_video.visibility = View.VISIBLE
                                    holder.isLoadedProgressVideoCurrentUser.visibility = View.GONE
                                }
                            }
                        }

                        val requestOptions = RequestOptions()
                        Glide.with(context)
                            .load(dataMessage.video)
                            .apply(requestOptions)
                            .thumbnail(Glide.with(context).load(dataMessage.video)).addListener(object :RequestListener<Drawable>{
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    holder.item_video_currentuser_video.setImageResource(R.color.grayClick)

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

                            })
                            .into(holder.item_video_currentuser_video)

                        holder.item_video_currentuser_time.text = DateUtils.sinceFrom(context, dataMessage.created_at, Constants.FULL_DATE_FORMAT)

                        holder.item_video_currentuser_play_video.setOnClickListener {
                            val intent = Intent(context, PlayVideo::class.java)
                            intent.putExtra("videoUrl", dataMessage.video)
                            context.startActivity(intent)
                        }
                    }
                }
            }
            else -> {
                when (dataMessage.content_type) {
                    "text" -> {
                        holder.item_message_receiver_message.text = dataMessage.message
                        holder.item_message_receiver_time.text = DateUtils.sinceFrom(context, dataMessage.created_at, Constants.FULL_DATE_FORMAT)
                    }

                    "image" -> {
                        try {

                            Glide.with(context).load(dataMessage.image).addListener(object :
                                RequestListener<Drawable> {
                                @SuppressLint("CheckResult")
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    holder.item_image_receiver_img.setImageDrawable(ContextCompat.getDrawable(context,
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

                            }).into(holder.item_image_receiver_img)
                        } catch (e: Exception) {

                        }
                        holder.item_image_receiver_time.text = DateUtils.sinceFrom(context, dataMessage.created_at, Constants.FULL_DATE_FORMAT)

                        holder.cardImageReceiver.setOnClickListener {
                            val intent = Intent(context, ShowImage::class.java)
                            intent.putExtra("ImageUrl", dataMessage.image)
                            context.startActivity(intent)
                        }

                    }


                    "audio" -> {
                        handlerSeekBar(position, holder)

                        if (position == currentPlaying) {
                            playingHolder = holder
                            updatePlaying(playingHolder!!)

                        } else {
                            updateNonPlaying(position, holder)
                        }

                        holder.item_audio_receiver_play_pause.setOnClickListener {
                            messagesSize = messages.size

                            if (position == currentPlaying) {
                                playingHolder = holder
                                updatePlaying(playingHolder!!)

                                if (mediaPlayer.isPlaying) {
                                    playingHolder!!.item_audio_receiver_play_pause.setImageResource(
                                        R.drawable.ic_play
                                    )
                                    mediaPlayer.pause()
                                } else {
                                    playingHolder!!.item_audio_receiver_play_pause.setImageResource(
                                        R.drawable.ic_baseline_pause_24
                                    )
                                    mediaPlayer.start()
                                }
                            } else {
                                if (currentPlaying != -1)
                                    updateNonPlaying(currentPlaying, playingHolder!!)
                                playingHolder = holder

                                currentPlaying = position
                                playAudio(playingHolder!!, dataMessage)
                            }
                        }
                        holder.item_audio_receiver_time.text = DateUtils.sinceFrom(context, dataMessage.created_at, Constants.FULL_DATE_FORMAT)
                    }
                    "video" -> {


                        val requestOptions = RequestOptions()

                        Glide.with(context)
                            .load(dataMessage.video)
                            .apply(requestOptions)
                            .thumbnail(Glide.with(context).load(dataMessage.video)).addListener(object :RequestListener<Drawable>{
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {

                                    holder.item_video_receiver_video.setImageResource(R.color.grayClick)
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

                            })
                            .into(holder.item_video_receiver_video)

                        //holder.item_video_receiver_video.setVideoURI(Uri.parse(dataMessage.video))
                        holder.item_video_receiver_time.text = DateUtils.sinceFrom(context, dataMessage.created_at, Constants.FULL_DATE_FORMAT)

                        holder.item_video_receiver_play_video.setOnClickListener {
                            val intent = Intent(context, PlayVideo::class.java)
                            intent.putExtra("videoUrl", dataMessage.video)
                            context.startActivity(intent)
                        }


                    }
                }
            }
        }
    }

    private fun updateNonPlaying(position: Int?, holder: ViewHolder) {

        if (holder.item_audio_currentuser_play_pause == null) {
            holder.item_audio_receiver_play_pause.setImageResource(R.drawable.ic_play)
            holder.item_audio_receiver_slider.progress = 0
            holder.item_audio_receiver_slider.removeCallbacks(seekBarUpdater)
        } else {
            holder.item_audio_currentuser_play_pause.setImageResource(R.drawable.ic_play)
            holder.item_audio_currentuser_slider.progress = 0
            holder.item_audio_currentuser_slider.removeCallbacks(seekBarUpdater)
        }
    }

    private fun updatePlaying(holder: ViewHolder) {

        if (holder.item_audio_currentuser_play_pause == null) {
            holder.item_audio_receiver_slider.postDelayed(seekBarUpdater, 1000)
            holder.item_audio_receiver_play_pause?.setImageResource(R.drawable.ic_baseline_pause_24)
            holder.item_audio_receiver_slider?.max = mediaPlayer.duration
        } else {
            holder.item_audio_currentuser_slider.postDelayed(seekBarUpdater, 1000)
            holder.item_audio_currentuser_play_pause.setImageResource(R.drawable.ic_baseline_pause_24)
            holder.item_audio_currentuser_slider?.max = mediaPlayer.duration
        }
    }

    inner class SeekBarUpdater : Runnable {
        override fun run() {
            if (playingHolder!!.item_audio_currentuser_slider == null) {
                playingHolder!!.item_audio_receiver_slider.progress = mediaPlayer.currentPosition
                playingHolder!!.item_audio_receiver_slider.postDelayed(this, 1000)
            } else {
                playingHolder!!.item_audio_currentuser_slider.progress = mediaPlayer.currentPosition
                playingHolder!!.item_audio_currentuser_slider.postDelayed(this, 1000)
            }

        }
    }

    private fun handlerSeekBar(position: Int, holder: ViewHolder) {

        if (holder.item_audio_currentuser_slider != null) {

            holder.item_audio_currentuser_slider.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if (p2) {
                        if (mediaPlayer.isPlaying && currentPlaying == position)
                            mediaPlayer.seekTo(p1)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }

            })

        } else {

            holder.item_audio_receiver_slider.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                    if (p2) {
                        if (mediaPlayer.isPlaying && currentPlaying == position)
                            mediaPlayer.seekTo(p1)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }

            })
        }

    }

    private fun playAudio(playingHolder: ViewHolder, dataMessage: MessageModel) {

        try {

            mediaPlayer.reset()
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.setDataSource(dataMessage.audio)
            mediaPlayer.prepare()
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
                updatePlaying(playingHolder)
            }

            mediaPlayer.setOnCompletionListener {
                if (playingHolder.item_audio_currentuser_play_pause == null) {

                    playingHolder.item_audio_receiver_play_pause?.setImageResource(R.drawable.ic_play)
                    playingHolder.item_audio_receiver_slider?.progress = 0
                    playingHolder.item_audio_receiver_slider.removeCallbacks(seekBarUpdater)
                } else {
                    playingHolder.item_audio_currentuser_slider.removeCallbacks(seekBarUpdater)

                    playingHolder.item_audio_currentuser_play_pause.setImageResource(R.drawable.ic_play)
                    playingHolder.item_audio_currentuser_slider?.progress = 0
                }
            }
        } catch (e: Exception) {

        }
    }

    var currentPlaying = -1

    override fun getItemCount(): Int = messages.size

    fun uploadMediaVideo(retrofit: Retrofit, dataMessage: MessageModel, holder: ViewHolder, position: Int){

        val messagePart = RequestBody.create(MultipartBody.FORM, "")
        val groupIdPart = RequestBody.create(MultipartBody.FORM, dataMessage.group_id)
        val content_typePart =
            RequestBody.create(MultipartBody.FORM, dataMessage.content_type)
        val sender_idPart = RequestBody.create(MultipartBody.FORM, dataMessage.sender_id)
        val chat_message_typePart =
            RequestBody.create(MultipartBody.FORM, dataMessage.chat_message_type)
        val filePart = RequestBody.create(MultipartBody.FORM, dataMessage.file)
        val idPart = RequestBody.create(MultipartBody.FORM, dataMessage._id)
        val imagePart = RequestBody.create(MultipartBody.FORM, dataMessage.image)
        val videoPart = RequestBody.create(MultipartBody.FORM, dataMessage.video)
        val audioPart = RequestBody.create(MultipartBody.FORM, dataMessage.audio)
        val currentUserIdPart = RequestBody.create(MultipartBody.FORM, dataMessage.participants[0])
        val receiverIdPart = RequestBody.create(MultipartBody.FORM, dataMessage.participants[1])

        val file = File(dataMessage.video)

        val req = CustomRequestBody(file, "video/*"){ now, total ->
            Handler(Looper.getMainLooper()).post(Runnable {

                dataMessage.totalBuffer = total
                dataMessage.currentBuffer = now
                customViewModel.notifyVideoItemForCurrentBuffer(now, total)
                /*holder.isLoadedProgressVideoCurrentUser.apply {
                    progress = now.toFloat()
                    progressMax = total.toFloat()
                }*/
                if (now == total){
                    holder.item_video_currentuser_play_video.visibility = View.VISIBLE
                    holder.isLoadedProgressVideoCurrentUser.visibility = View.GONE
                    Toast.makeText(context, "Sent Successful", Toast.LENGTH_LONG).show()
                }
            })

        }
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, req)

        retrofit.create(endpoints::class.java)
            .postMessage(
                multipartBody,
                messagePart,
                groupIdPart,
                content_typePart,
                sender_idPart,
                chat_message_typePart,
                filePart,
                idPart,
                imagePart,
                videoPart,
                audioPart,
                currentUserIdPart,
                receiverIdPart
            ).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    dataMessage.isLoaded = true
                    dataMessage.byCurrentUser = false
                    holder.item_video_currentuser_play_video.visibility = View.VISIBLE
                    holder.isLoadedProgressVideoCurrentUser.visibility = View.GONE
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(context, "onFailure send video", Toast.LENGTH_LONG).show()

                }

            })

    }

    fun uploadMediaAudio(retrofit: Retrofit, MessageModel: MessageModel, holder: ViewHolder){


        val file = File(MessageModel.audio)

        val messagePart = RequestBody.create(MultipartBody.FORM, "")
        val groupIdPart = RequestBody.create(MultipartBody.FORM, MessageModel.group_id)
        val content_typePart = RequestBody.create(MultipartBody.FORM, MessageModel.content_type)
        val sender_idPart = RequestBody.create(MultipartBody.FORM, MessageModel.sender_id)
        val chat_message_typePart =
            RequestBody.create(MultipartBody.FORM, MessageModel.chat_message_type)
        val filePart = RequestBody.create(MultipartBody.FORM, MessageModel.file)
        val idPart = RequestBody.create(MultipartBody.FORM, MessageModel._id)
        val imagePart = RequestBody.create(MultipartBody.FORM, MessageModel.image)
        val videoPart = RequestBody.create(MultipartBody.FORM, MessageModel.video)
        val audioPart = RequestBody.create(MultipartBody.FORM, MessageModel.audio)
        val currentUserIdPart = RequestBody.create(MultipartBody.FORM, MessageModel.participants[0])
        val receiverIdPart = RequestBody.create(MultipartBody.FORM, MessageModel.participants[1])

        val req = CustomRequestBody(file, "audio/*"){ now, total ->

            Handler(Looper.getMainLooper()).post(Runnable {

                MessageModel.totalBuffer = total
                MessageModel.currentBuffer = now
                customViewModel.notifyAudioItemForCurrentBuffer(now, total)
                holder.isLoadedProgressAudioCurrentUser.apply {
                    progress = now.toFloat()
                    progressMax = total.toFloat()
                }
                if (now == total){
                    holder.item_audio_currentuser_play_pause.visibility = View.VISIBLE
                    holder.isLoadedProgressAudioCurrentUser.visibility = View.GONE
                }
            })

        }

        val multipartBody = MultipartBody.Part.createFormData("file", file.name, req)


        retrofit.create(endpoints::class.java)
            .postMessage(
                multipartBody,
                messagePart,
                groupIdPart,
                content_typePart,
                sender_idPart,
                chat_message_typePart,
                filePart,
                idPart,
                imagePart,
                videoPart,
                audioPart,
                currentUserIdPart,
                receiverIdPart
            ).enqueue(object :Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    MessageModel.isLoaded = true
                    MessageModel.byCurrentUser = false
                    holder.item_audio_currentuser_play_pause.visibility = View.VISIBLE
                    holder.item_audio_currentuser_play_pause.isEnabled = true
                    holder.isLoadedProgressAudioCurrentUser.visibility = View.GONE
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(context, "onFailure send audio", Toast.LENGTH_LONG).show()

                }

            })



    }

    fun uploadMediaImage(retrofit: Retrofit, MessageModel: MessageModel, holder: ViewHolder){

        val file = File(MessageModel.image)

        val messagePart = RequestBody.create(MultipartBody.FORM, "")
        val groupIdPart = RequestBody.create(MultipartBody.FORM, MessageModel.group_id)
        val content_typePart = RequestBody.create(MultipartBody.FORM, MessageModel.content_type)
        val sender_idPart = RequestBody.create(MultipartBody.FORM, MessageModel.sender_id)
        val chat_message_typePart = RequestBody.create(MultipartBody.FORM, MessageModel.chat_message_type)
        val filePart = RequestBody.create(MultipartBody.FORM, MessageModel.file)
        val idPart = RequestBody.create(MultipartBody.FORM, MessageModel._id)
        val imagePart = RequestBody.create(MultipartBody.FORM, MessageModel.image)
        val videoPart = RequestBody.create(MultipartBody.FORM, MessageModel.video)
        val audioPart = RequestBody.create(MultipartBody.FORM, MessageModel.audio)
        val currentUserIdPart = RequestBody.create(MultipartBody.FORM, MessageModel.participants[0])
        val receiverIdPart = RequestBody.create(MultipartBody.FORM, MessageModel.participants[1])

        val req = CustomRequestBody(file, "audio/*"){ now, total ->

            Handler(Looper.getMainLooper()).post(Runnable {

                MessageModel.totalBuffer = total
                MessageModel.currentBuffer = now
                customViewModel.notifyImageItemForCurrentBuffer(now, total)
                holder.isLoadedProgressImageCurrentUser.apply {
                    progress = now.toFloat()
                    progressMax = total.toFloat()
                    progressBarColor = Color.WHITE
                }
                if (now == total){
                    holder.isLoadedProgressImageCurrentUser.visibility = View.GONE
                }
            })


        }
        val multipartBody = MultipartBody.Part.createFormData("file", file.name, req)

        retrofit.create(endpoints::class.java)
            .postMessage(
                multipartBody,
                messagePart,
                groupIdPart,
                content_typePart,
                sender_idPart,
                chat_message_typePart,
                filePart,
                idPart,
                imagePart,
                videoPart,
                audioPart,
                currentUserIdPart,
                receiverIdPart
            ).enqueue(object :Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    MessageModel.isLoaded = true
                    MessageModel.byCurrentUser = false

                    holder.isLoadedProgressImageCurrentUser.visibility = View.GONE
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(context, "onFailure send image", Toast.LENGTH_LONG).show()

                }
            })
    }

    var uploadHolder: ViewHolder? = null
    var uploadPosition = -1

}