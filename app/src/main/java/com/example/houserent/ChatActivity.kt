package com.example.houserent

import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class ChatActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    private lateinit var imgBack:ImageView
    private lateinit var btnSendMessage:ImageView
    private lateinit var tvUserName:TextView
    private lateinit var etMessage:EditText
    private lateinit var  chatRecyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var chatList :ArrayList<Chat>
    private lateinit var miccc: ImageView
    private var isRecording: Boolean = false
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var audioFilePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        var intent = getIntent()
        var userId = intent.getStringExtra("userID")
        var userName = intent.getStringExtra("userName")
        imgBack=findViewById(R.id.imgBack)
        tvUserName=findViewById(R.id.tvUserName)
        etMessage=findViewById(R.id.etMessage)
        btnSendMessage=findViewById(R.id.btnSendMessage)
        chatRecyclerView=findViewById(R.id.chatRecyclerView)
        chatRecyclerView.layoutManager= LinearLayoutManager(this)
        chatRecyclerView.setHasFixedSize(true)
        miccc = findViewById(R.id.miccc)
        isRecording = false
        mediaRecorder = MediaRecorder()
        audioFilePath = ""

        chatList= arrayListOf()
        adapter= ChatAdapter(chatList)
        chatRecyclerView.adapter=adapter

        imgBack.setOnClickListener {
            onBackPressed()
        }
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)


        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(Muser::class.java)
                tvUserName.text = user!!.userName
            }
        })
        btnSendMessage.setOnClickListener {
            var message: String = etMessage.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "message is empty", Toast.LENGTH_SHORT).show()
                etMessage.setText("")
            } else {
                sendMessage(firebaseUser!!.uid, userId, message,"nourl")
                etMessage.setText("")
            }
        }
        miccc.setOnClickListener {
            if (!isRecording) {
                startRecording()
                isRecording = true
            } else {
                stopRecording()
                isRecording = false
                uploadAudio(userId)
            }
        }
        readMessage(firebaseUser!!.uid, userId)
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }
    private fun startRecording() {
        // Check if the RECORD_AUDIO permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Set the output file path
            audioFilePath = "${externalCacheDir?.absolutePath}/recording.3gp"

            // Set up the MediaRecorder
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        } else {
            // RECORD_AUDIO permission not granted, show a toast or handle the permission denied case
            Toast.makeText(this, "RECORD_AUDIO permission not granted", Toast.LENGTH_SHORT).show()
        }
    }
    private fun stopRecording() {
        mediaRecorder.apply {
            stop()
            reset()
            release()
        }
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
    }

    private fun uploadAudio(userId: String) {
        val audioFile = File(audioFilePath)
        if (audioFile.exists()) {
            // Check if the WRITE_EXTERNAL_STORAGE permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val storageRef = FirebaseStorage.getInstance().reference
                val audioRef = storageRef.child("audio").child("${System.currentTimeMillis()}.3gp")

                val uploadTask = audioRef.putFile(Uri.fromFile(audioFile))
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    audioRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val audiourl = task.result.toString()
                        sendMessage(firebaseUser!!.uid, userId, "Tap to Play Voice Message", audiourl)
                    }
                }
            }else {
                // WRITE_EXTERNAL_STORAGE permission not granted, show a toast or handle the permission denied case
                Toast.makeText(this, "WRITE_EXTERNAL_STORAGE permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun sendMessage(senderId: String, receiverId: String, message: String,audiourl:String) {
        var reference: DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap: HashMap<String, String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)
        hashMap.put("audiourl",audiourl)
        reference!!.child("Chat").push().setValue(hashMap)

    }
    fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if (chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)
                    ) {
                        chatList.add(chat)
                    }
                }
                adapter.notifyDataSetChanged()
            }
        })
    }
}