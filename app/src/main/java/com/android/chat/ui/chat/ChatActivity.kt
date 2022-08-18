package com.android.chat.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.android.chat.Constants
import com.android.chat.R
import com.android.chat.databinding.ActivityChatActivityBinding
import com.android.chat.models.*
import com.android.chat.network.retrofit
import com.android.chat.ui.call.CallActivity
import com.android.chat.ui.camera.CameraActivity
import com.android.chat.utils.DateUtils
import com.android.chat.utils.gone
import com.android.chat.utils.visible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import io.socket.client.IO
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {
    private val socketIO = IO.socket(Constants.SOCKETIO_URL)
    private var receiver_id = ""
    private var group_id = ""
    private var receiver_first_name = ""
    private var receiver_last_name = ""
    private var receiver_photo_url = ""
    private var current_first_name = ""
    private var current_last_name = ""
    private var current_photo_url = ""
    lateinit var current_user_id: String
    private lateinit var retrofit: Retrofit
    private var messages = mutableListOf<MessageModel>()
    lateinit var customViewModel: ChatActivityViewModel
    lateinit var binding: ActivityChatActivityBinding
    private val REQUEST_IMAGE = 123
    private val REQUEST_RECORD_AUDIO = 124
    private val REQUEST_VIDEO = 125
    lateinit var path: String
    private var pauseOffset: Long = 0
    private var running = false
    lateinit var mediaPlayer: MediaPlayer
    private var FIRST_SCROLL = true
    private var LoadMore = false
    private var skip = 0
    private var limit = 50


    ///////////////////////////////////////////////////////
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    ///////////////////////////////////////////////////////


    lateinit var customAdapterMessage: ChatAdapter


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayer = MediaPlayer()

        customViewModel = ViewModelProviders.of(this)[ChatActivityViewModel::class.java]

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        retrofit = retrofit().retrofit()

        customViewModel.connectSocket(socketIO)


        binding.recordingTimer.format = "%s"
        binding.recordingTimer.base = SystemClock.elapsedRealtime()


        // this for isTyping
        customViewModel.state()
        customViewModel.onChatListChanged.observe(this) {
            if (it.current_user_id != current_user_id && it.receiver_id != receiver_id) {
                when (it.is_typing) {
                    true -> {
                        runOnUiThread {
                            binding.chatWithTypingState.visibility = View.VISIBLE
                            binding.chatWithActiveState.visibility = View.GONE
                        }
                    }
                    false -> {
                        runOnUiThread {
                            binding.chatWithTypingState.visibility = View.GONE
                            binding.chatWithActiveState.visibility = View.VISIBLE
                        }
                    }
                }
            }

        }


        // finish activity
        binding.chatItemBack.setOnClickListener {
            finish()
        }


        current_user_id = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString(
            "_id",
            "Empty"
        )!!

        current_first_name = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString(
                "first_name",
                "Empty"
            )!!

        current_last_name = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString(
            "first_name",
            "Empty"
        )!!

        current_photo_url = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString(
            "photo_url",
            "Empty"
        )!!

        try {
            if(intent.hasExtra("receiver_id")){
                receiver_id = intent.getStringExtra("receiver_id")!!
            }

            if(intent.hasExtra("group_id")){
                group_id = intent.getStringExtra("group_id")!!
            }

            if(intent.hasExtra("receiver_first_name")){
                receiver_first_name = intent.getStringExtra("receiver_first_name")!!
            }

            if(intent.hasExtra("receiver_last_name")){
                receiver_last_name = intent.getStringExtra("receiver_last_name")!!
            }

            if(intent.hasExtra("receiver_photo_url")){
                receiver_photo_url = intent.getStringExtra("receiver_photo_url")!!
            }

            Glide.with(this).load(receiver_photo_url).into(binding.chatItemImg)
        } catch (e: Exception) {
            Log.d("ChatWith ----------", e.message.toString())
        }


        /////////////////////////////////////////////////////
        //create chat if between me and him if does not exist
        when(group_id){
            "Empty"->{
                val participants = mutableListOf<String>()
                participants.add(current_user_id)
                participants.add(receiver_id)
                val participantsModel = ParticipantsModel(participants)
                customViewModel.createChat(retrofit, participantsModel)
                customViewModel.chatIdMutableLiveData.observe(this) {
                    if (it.code() == 500) {
                        Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
                        return@observe
                    }

                    if (it.code() == 400) {
                        val serverResponse = it.errorBody()?.string()?.let { it1 -> JSONObject(it1) }!!
                        val message =
                            JSONObject(serverResponse.getJSONArray("errors")[0].toString()).getString("message")
                                .toString()
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        return@observe
                    }


                    if (it.code() == 200) {
                        val jsonObject = it.body()?.string()?.let { it1 -> JSONObject(it1) }!!
                        val data = jsonObject.getJSONObject("data")
                        val user = data.getJSONObject("chat")
                        group_id = user.getString("_id")
                        binding.constraintChat2.visibility = View.VISIBLE

                        customViewModel.MethodBringChalWithMutableLiveData(
                            retrofit,
                            GroupIdModule(group_id),
                            skip,
                            limit
                        )
                        customViewModel.joinChatRoom(group_id)

                        binding.callUser.setOnClickListener {

                            requestOpenCamPermission.launch(arrayOf(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA
                            ))

                        }
                    }
                }

            }

            else ->{
                binding.constraintChat2.visibility = View.VISIBLE
                customViewModel.MethodBringChalWithMutableLiveData(
                    retrofit,
                    GroupIdModule(group_id),
                    skip,
                    limit
                )
                customViewModel.joinChatRoom(group_id)

                binding.callUser.setOnClickListener {

                    requestOpenCamPermission.launch(arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA
                    ))

                }
            }
        }



        //get receiver Info
        customViewModel.requestReceiverInformation(retrofit, receiver_id)
        customViewModel.receiverInformationMutableData.observe(this) { it ->

            if (it.code() == 500) {
                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
                return@observe
            }

            if (it.code() == 400) {
                val serverResponse = it.errorBody()?.string()?.let { it1 -> JSONObject(it1) }!!
                val message =
                    JSONObject(serverResponse.getJSONArray("errors")[0].toString()).getString("message")
                        .toString()
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return@observe
            }


            if (it.code() == 200) {
                val jsonObject = it.body()?.string()?.let { it1 -> JSONObject(it1) }!!
                val data = jsonObject.getJSONObject("data")
                val user = data.getJSONObject("user")

                binding.chatWithUsername.text = user.getString("first_name")
                Glide.with(this).load(user.getString("photo_url")).addListener(object :
                    RequestListener<Drawable> {
                    @SuppressLint("CheckResult")
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        binding.chatItemImg.setImageResource(R.drawable.ic_face)
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {

                        return false
                    }

                }).into(binding.chatItemImg)

                when (user.getBoolean("is_connected")) {
                    true -> {
                        binding.chatWithActiveState.text = "Active maintenant"
                        binding.greenImg.visibility = View.VISIBLE
                    }

                    false -> {

                        when (user.getString("last_opened")) {
                            "" -> {
                                binding.chatWithActiveState.text = ""
                            }
                            else -> {

                                binding.chatWithActiveState.text = DateUtils.sinceFrom(
                                    this,
                                    user.getString("last_opened"),
                                    Constants.FULL_DATE_FORMAT
                                )
                            }
                        }
                        binding.greenImg.visibility = View.GONE
                        binding.chatWithActiveState.textSize = 10F
                    }
                }
            }

        }


        // join a room between CurrentUser and Receiver
        //customViewModel.getIntoRoom(group_id)

        // this for send isTyping data
        binding.editTextMessage.doOnTextChanged { text, start, before, count ->
            when (text!!.length) {
                0 -> {
                    binding.btnSendImage.visibility = View.VISIBLE
                    binding.btnSendVideo.visibility = View.VISIBLE
                    binding.btnRecordAudio.visibility = View.VISIBLE
                    binding.btnOpenCamera.visibility = View.VISIBLE
                    binding.btnSendMessage.visibility = View.GONE

                    customViewModel.isTypingStatus(group_id, current_user_id, receiver_id, false)
                }
                else -> {

                    binding.btnSendImage.visibility = View.GONE
                    binding.btnSendVideo.visibility = View.GONE
                    binding.btnRecordAudio.visibility = View.GONE
                    binding.btnOpenCamera.visibility = View.GONE
                    binding.btnSendMessage.visibility = View.VISIBLE

                    customViewModel.isTypingStatus(group_id, current_user_id, receiver_id, true)

                    Handler().postDelayed({
                        customViewModel.isTypingStatus(group_id, current_user_id, receiver_id, false)
                    }, 5000)
                }
            }
        }


        // this for add messages to adapter and to recyclerView
        customAdapterMessage = ChatAdapter(messages, this, mediaPlayer)
        binding.recyclerChatWith.adapter = customAdapterMessage


        // this for send message to receiver
        binding.btnSendMessage.setOnClickListener {

            val msg = binding.editTextMessage.text.toString()
            if (msg.isEmpty()) {
                Toast.makeText(this, "Tap a message", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            binding.editTextMessage.text = null

            val message = msg
            val timeLong = Date().time

            val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(timeLong)

            val anotherFormat = createdAt.replace(" ", "T")
            val m = "${anotherFormat}+01:00"

            val currentUserId = current_user_id
            val receiverId = receiver_id
            val senderId = current_user_id

            // participants i send him only for check if group already has created
            val MessageModel = MessageModel(
                message,
                listOf(currentUserId, receiverId),
                group_id,
                receiver_id,
                senderId,
                "text",
                m,
                "normal",
                "",
                UUID.randomUUID().toString(),
                "",
                "",
                "",
                false,
                true,
                true,
                0L,
                0L
            )
            messages.add(0, MessageModel)
            customAdapterMessage.notifyItemInserted(0)

            customViewModel.sendText(retrofit, MessageModel)
            binding.recyclerChatWith.scrollToPosition(0)
            //customViewModel.SendNewMessage(json)

            val mediaPlayerMessage = MediaPlayer.create(this, R.raw.send)
            try {
                mediaPlayerMessage.start()
            }catch (e:Exception){

            }
        }

        customViewModel.sendTextMutableLiveData.observe(this) {
            //Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }

        customViewModel.sendTextMutableLiveDataFailure.observe(this) {
            //Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }


        // this get notify once receiver get in app
        customViewModel.MethodGetNotify(current_user_id)
        customViewModel.GetNotifyMutableLivedata.observe(this) {
            if (it == receiver_id) {
                binding.chatWithActiveState.text = "Active maintenant"
                binding.greenImg.visibility = View.VISIBLE
            }
        }


        // this for listening for a new message comes from receiver
        customViewModel.ReceiveNewMessage()
        customViewModel.onChatListReceiveNewMessage.observe(this) {
            runOnUiThread {

                val data = it
                val type = object : TypeToken<MessageModel>() {}.type
                val jsonData = Gson().fromJson<MessageModel>(data, type)

                if (jsonData.sender_id == receiver_id || jsonData.sender_id == current_user_id) {

                    //group_id = jsonData.group_id
                    try {
                        if (jsonData.sender_id == current_user_id) {

                                val itemList = messages.filter { item ->
                                    item._id == jsonData._id
                                }

                                if (itemList.isNotEmpty()) {

                                    val item = itemList[0]

                                    val position = messages.indexOf(item)
                                    messages[position] = jsonData

                                    customAdapterMessage.notifyItemChanged(position)
                                    binding.recyclerChatWith.scrollToPosition(0)
                                } else {

                                    messages.add(0, jsonData)
                                    customAdapterMessage.notifyItemInserted(0)
                                    binding.recyclerChatWith.scrollToPosition(0)
                                }


                        } else {
                            val mediaPlayerMessage = MediaPlayer.create(this, R.raw.receive)
                            try {
                                mediaPlayerMessage.start()
                            }catch (e:Exception){

                            }
                            messages.add(0, jsonData)
                            customAdapterMessage.notifyItemInserted(0)
                            binding.recyclerChatWith.scrollToPosition(0)
                        }
                    } catch (e: Exception) {
                    }

                }

            }
        }


        // this for bring all messages between
        customViewModel.BringChalWithMutableLiveDataFailer.observe(this) {
            binding.recyclerChatWith.visibility = View.VISIBLE
            binding.waitChatWith.visibility = View.GONE
        }

        customViewModel.BringChalWithMutableLiveData.observe(this) {

            if (it.code() == 500) {

            }

            if (it.code() == 400) {

            }

            if (it.code() == 200) {

                val jsonObject = it.body()?.string()?.let { it1 -> JSONObject(it1) }!!
                val data = JSONObject(jsonObject.getString("data"))
                val jsonObjectMessages = data.getString("messages")
                val type = object : TypeToken<MutableList<MessageModel>>() {}.type
                val formatMessages =
                    Gson().fromJson<MutableList<MessageModel>>(jsonObjectMessages, type)

                for (i in formatMessages) {
                    i.isLoaded = true
                    i.byCurrentUser = false
                }

                messages.addAll(formatMessages)
                messages.sortByDescending { sortByTime ->
                    sortByTime.created_at
                }

                if (FIRST_SCROLL) {
                    binding.recyclerChatWith.scrollToPosition(0)
                    FIRST_SCROLL = false
                }
                skip += limit
                binding.progressLoadMoreData.visibility = View.GONE
                LoadMore = true

                binding.recyclerChatWith.addOnScrollListener(object :
                    RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (!recyclerView.canScrollVertically(1) && dy > 0) {

                        } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                            if (LoadMore) {
                                binding.progressLoadMoreData.visibility = View.VISIBLE
                                customViewModel.MethodBringChalWithMutableLiveData(
                                    retrofit,
                                    GroupIdModule(group_id),
                                    skip,
                                    limit
                                )
                                LoadMore = false
                            }
                        }
                    }
                })


                customAdapterMessage.notifyDataSetChanged()
                binding.recyclerChatWith.visibility = View.VISIBLE
                binding.waitChatWith.visibility = View.GONE
                //customAdapterMessage.notifyItemRangeChanged(lastIndex, messages.lastIndex)

            }
        }



        // check if receiver still online
        customViewModel.notifyLogOut()
        customViewModel.notifyLogOutMutableLiveData.observe(this){
            runOnUiThread {
                if (it._id == receiver_id) {
                    when (it.is_connected) {
                        true -> {
                            binding.chatWithActiveState.text = "Active maintenant"
                            binding.greenImg.visibility = View.VISIBLE
                        }

                        false -> {

                            when (it.last_opened) {
                                "" -> {
                                    binding.chatWithActiveState.text = ""
                                }
                                else -> {
                                    binding.chatWithActiveState.text = DateUtils.sinceFrom(
                                        this,
                                        it.last_opened,
                                        Constants.FULL_DATE_FORMAT
                                    )
                                }
                            }
                            binding.greenImg.visibility = View.GONE
                            binding.chatWithActiveState.textSize = 10F
                        }
                    }
                }
            }
        }



        ////////////////////////////////////////////////////////////////////
        // click to pick up an image from device

        binding.btnSendImage.setOnClickListener {
            isVideo = false
            openGallery()

        }

        ////////////////////////////////////////////////////////////////////
        // click to send video
        binding.btnSendVideo.setOnClickListener {
            isVideo = true
            checkGalleryPermissions()
        }

        binding.scrollToDown.setOnClickListener {
            binding.recyclerChatWith.scrollToPosition(0)
        }

        binding.recyclerChatWith.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) {
                    runOnUiThread {
                        binding.scrollToDown.visibility = View.VISIBLE
                    }
                } else {
                    runOnUiThread {
                        binding.scrollToDown.visibility = View.GONE
                    }
                }

            }
        })

        binding.btnSendRecordingAudio.setOnClickListener {
            stopRecording(path)

            binding.btnAudio.visibility = View.VISIBLE
            binding.editImgVideo.visibility = View.VISIBLE
            binding.constraintRecordAudio.visibility = View.GONE
            binding.btnSendAudio.visibility = View.GONE


            binding.constraintEditText.visibility = View.VISIBLE
            binding.constraintRecordAudioTimer.visibility = View.GONE
            binding.btnSendMessage.visibility = View.GONE
            binding.btnSendRecordingAudio.visibility = View.GONE
            binding.btnRecordAudio.visibility = View.VISIBLE

        }

        binding.btnDeleteAudio.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder!!.pause()
            }
            mediaRecorder = null

            binding.btnAudio.visibility = View.VISIBLE
            binding.editImgVideo.visibility = View.VISIBLE
            binding.constraintRecordAudio.visibility = View.GONE
            binding.btnSendAudio.visibility = View.GONE


            binding.constraintEditText.visibility = View.VISIBLE
            binding.constraintRecordAudioTimer.visibility = View.GONE
            binding.btnSendMessage.visibility = View.GONE
            binding.btnSendRecordingAudio.visibility = View.GONE
            binding.btnRecordAudio.visibility = View.VISIBLE

            binding.recordingTimer.stop()
            pauseOffset = SystemClock.elapsedRealtime() - binding.recordingTimer.base
            running = false
            binding.recordingTimer.base = SystemClock.elapsedRealtime()
            pauseOffset = 0
        }

        binding.btnRecordAudio.setOnTouchListener { view, motionEvent ->
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_DOWN) {

                binding.constraintEditText.visibility = View.GONE
                binding.constraintRecordAudioTimer.visibility = View.VISIBLE
                binding.txtRecording.visibility = View.VISIBLE
                binding.btnRecordAudio.background = ContextCompat.getDrawable(this, R.drawable.tap_background)
                binding.btnDeleteAudio.visibility = View.GONE
                binding.btnSendMessage.visibility = View.GONE
                binding.btnSendRecordingAudio.visibility = View.GONE

                checkAudioPermissions()
                return@setOnTouchListener true
            } else {
                if (action == MotionEvent.ACTION_UP) {

                    if (!state) {
                        binding.constraintEditText.visible()
                        binding.constraintRecordAudioTimer.gone()
                        binding.btnRecordAudio.visible()
                        binding.btnSendRecordingAudio.gone()
                        binding.btnRecordAudio.background = null
                        return@setOnTouchListener true
                    }

                    binding.btnRecordAudio.background = null
                    binding.btnRecordAudio.visibility = View.GONE
                    binding.btnSendMessage.visibility = View.GONE
                    binding.btnSendRecordingAudio.visibility = View.VISIBLE
                    binding.txtRecording.visibility = View.GONE
                    binding.shapeRecording.visibility = View.GONE
                    binding.btnDeleteAudio.visibility = View.VISIBLE

                    binding.recordingTimer.stop()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener true
        }

        binding.btnOpenCamera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivityForResult(intent, 129)
        }

        val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).edit()
        sh.putBoolean("onlineState", false)
        sh.apply()

    }



    override fun onResume() {
        super.onResume()
        val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).edit()
        sh.putBoolean("onlineState", false)
        sh.apply()
        val currentUser = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!
        customViewModel.joinChat(currentUser)
        customViewModel.joinChatRoom(group_id)
    }

    override fun onPause() {
        super.onPause()
        val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).edit()
        sh.putBoolean("onlineState", true)
        sh.apply()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()

        Handler().postDelayed({
            val sh = getSharedPreferences("onlineState", Context.MODE_PRIVATE).getBoolean("onlineState", true)
            if (sh){
                val currentUser = getSharedPreferences("CurrentUser", Context.MODE_PRIVATE).getString("_id", "Empty")!!
                customViewModel.MethodLogOutMutableLiveData(currentUser)
            }

        }, 7000)
    }

    @SuppressLint("MissingPermission")
    val requestOpenCamPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Do something if some permissions granted or denied
            var acceptedAllPermission = true

            permissions.entries.forEach {
                if (!it.value) {
                    acceptedAllPermission = false
                    Toast.makeText(this, "you need to accepte all the permissions", Toast.LENGTH_SHORT).show()
                }
            }

            if (acceptedAllPermission) {
                val intent = Intent(this, CallActivity::class.java)
                intent.putExtra("group_id", group_id)
                intent.putExtra("current_user_id", current_user_id)
                intent.putExtra("receiver_id", receiver_id)
                intent.putExtra("caller_full_name", "$current_first_name $current_last_name")
                intent.putExtra("answerer_full_name", "$receiver_first_name $receiver_last_name")
                intent.putExtra("caller_photo_url", current_photo_url)
                intent.putExtra("answerer_photo_url", receiver_photo_url)
                intent.putExtra("incoming", "fromMe")
                startActivity(intent)
            } else {

            }
        }


    private fun SendVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_VIDEO)
    }

    private fun checkAudioPermissions() {
        requestAudioMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun checkGalleryPermissions() {
        requestGalleryMultiplePermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    }


    // RequestDto multiple permissions contract
    @SuppressLint("MissingPermission")
    private val requestAudioMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Do something if some permissions granted or denied
            var acceptedAllPermission = true

            permissions.entries.forEach {
                if (!it.value) {
                    acceptedAllPermission = false
                    Toast.makeText(
                        this,
                        "you need to accept all the permissions",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (acceptedAllPermission) {
                RecordeAudio()
            } else {
                Toast.makeText(
                    this,
                    "you need to accept all the permissions from settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    private val requestGalleryMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Do something if some permissions granted or denied
            var acceptedAllPermission = true

            permissions.entries.forEach {
                if (!it.value) {
                    acceptedAllPermission = false
                    Toast.makeText(
                        this,
                        "you need to accept all the permissions",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (acceptedAllPermission) {
                if (isVideo) {
                    SendVideo()
                } else {
                    openGallery()
                }
            }else{
                Toast.makeText(
                    this,
                    "you need to accept all the permissions from settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    var isVideo = false
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE)

    }

    private fun RecordeAudio() {

        output = Environment.getExternalStorageDirectory().absolutePath + "/${
            UUID.randomUUID().toString()
        }.mp3"
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        val photoFile = File(getOutputDirectory(), UUID.randomUUID().toString() + ".mp3")
        mediaRecorder?.setOutputFile(photoFile.absolutePath)

        path = photoFile.absolutePath

        startRecording()

    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun startRecording() {
        try {

            if (!running) {
                binding.recordingTimer.base = SystemClock.elapsedRealtime() - pauseOffset
                binding.recordingTimer.start()
                running = true
            }

            binding.recordingTimer.start()
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording(photoFile: String) {
        if (state) {
            mediaRecorder?.release()
            state = false

            binding.recordingTimer.stop()
            pauseOffset = SystemClock.elapsedRealtime() - binding.recordingTimer.base
            running = false
            binding.recordingTimer.base = SystemClock.elapsedRealtime()
            pauseOffset = 0
            sendAudioProcess(photoFile)

        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE) {
                sendImageProcess(data!!.data)
            }
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_RECORD_AUDIO) {
                //sendAudioProcess(data)
                //RecordeAudio()
            }
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_VIDEO) {
                sendVideoProcess(data!!.data)
            }
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == 129) {

                if (data?.getStringExtra("type") == "image") {
                    sendImageProcess(Uri.parse(data?.getStringExtra("uri")))
                } else {
                    sendVideoProcess(Uri.parse(data?.getStringExtra("uri")))
                }
            }
        }
    }

    private fun sendVideoProcess(data: Uri?) {

        var selectedImageUri: Uri? = data
        // Get the path from the Uri
        var videoUrl: String = ""
        val proj = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = selectedImageUri?.let { contentResolver.query(it, proj, null, null, null) }

        if (cursor != null) {
            cursor.moveToFirst()
            val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            videoUrl = cursor.getString(column_index)
        } else {
            videoUrl = selectedImageUri!!.path.toString()
        }

        val timeLong = Date().time
        val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(timeLong)

        val anotherFormat = createdAt.replace(" ", "T")
        val m = "${anotherFormat}+01:00"

        val currentUserId = current_user_id
        val receiverId = receiver_id
        val senderId = current_user_id
        val message_id = UUID.randomUUID().toString()

        val MessageModel = MessageModel(
            "",
            listOf(currentUserId, receiverId),
            group_id,
            receiver_id,
            senderId,
            "video",
            m,
            "normal",
            "",
            message_id,
            "",
            videoUrl,
            "",
            false,
            true,
            true,
            0L,
            0L
        )
        messages.add(0, MessageModel)
        customAdapterMessage.notifyItemInserted(0)
        binding.recyclerChatWith.scrollToPosition(0)
        val mediaPlayerMessage = MediaPlayer.create(this, R.raw.send)
        try {
            mediaPlayerMessage.start()
        }catch (e:Exception){

        }

    }

    private fun sendAudioProcess(data: String) {

        val timeLong = java.util.Date().time
        val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(timeLong)

        val anotherFormat = createdAt.replace(" ", "T")
        val m = "${anotherFormat}+01:00"

        val currentUserId = current_user_id
        val receiverId = receiver_id
        val senderId = current_user_id
        val message_id = UUID.randomUUID().toString()
        val MessageModel = MessageModel(
            "",
            listOf(currentUserId, receiverId),
            group_id,
            receiver_id,
            senderId,
            "audio",
            m,
            "normal",
            "",
            message_id,
            "",
            "",
            data,
            false,
            true,
            true,
            0L,
            0L
        )
        messages.add(0, MessageModel)
        customAdapterMessage.notifyItemInserted(0)
        binding.recyclerChatWith.scrollToPosition(0)
        val mediaPlayerMessage = MediaPlayer.create(this, R.raw.send)
        try {
            mediaPlayerMessage.start()
        }catch (e:Exception){

        }

    }

    private fun sendImageProcess(data: Uri?) {

        var selectedImageUri: Uri? = data
        var imgUrl: String = ""
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            selectedImageUri?.let { contentResolver.query(it, proj, null, null, null) }

        if (cursor != null) {
            cursor.moveToFirst()
            val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            imgUrl = cursor.getString(column_index)
        } else {
            imgUrl = selectedImageUri!!.path.toString()
        }


        val timeLong = Date().time
        val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .format(timeLong)

        val anotherFormat = createdAt.replace(" ", "T")
        val m = "${anotherFormat}+01:00"

        val currentUserId = current_user_id
        val receiverId = receiver_id
        val senderId = current_user_id
        val message_id = UUID.randomUUID().toString()
        val MessageModel = MessageModel(
            "",
            listOf(currentUserId, receiverId),
            group_id,
            receiver_id,
            senderId,
            "image",
            m,
            "normal",
            "",
            message_id,
            imgUrl,
            "",
            "",
            false,
            true,
            true,
            0L,
            0L
        )
        messages.add(0, MessageModel)
        customAdapterMessage.notifyItemInserted(0)
        binding.recyclerChatWith.scrollToPosition(0)
        val mediaPlayerMessage = MediaPlayer.create(this, R.raw.send)
        try {
            mediaPlayerMessage.start()
        }catch (e:Exception){

        }
    }

}