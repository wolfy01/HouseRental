package com.example.houserent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.*

class VoiceSearch : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var searchEditText: EditText
    private lateinit var micButton: ImageButton
    private lateinit var dbref: DatabaseReference
    private lateinit var userRecyclerview: RecyclerView
    private lateinit var userArrayList :ArrayList<Post>
    private lateinit var adapter: OtherWebsiteAdapter

    companion object {
        // Request code for speech recognition permission
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_search)

        userRecyclerview=findViewById(R.id.postlist)
        userRecyclerview.layoutManager= LinearLayoutManager(this)
        userRecyclerview.setHasFixedSize(true)

        userArrayList= arrayListOf()
        adapter = OtherWebsiteAdapter(userArrayList)
        userRecyclerview.adapter = adapter

        // Check if permission to record audio is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_RECORD_AUDIO)
        }

        // Initialize the SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Initialize the UI components
        searchEditText = findViewById(R.id.searchEditText)
        micButton = findViewById(R.id.micButton)

        // Set up the recognition listener
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Toast.makeText(this@VoiceSearch, "Error: $error", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                // Get the recognized speech as text
                val speechResult = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                searchEditText.setText(speechResult);
                //show recognized post
                dbref= FirebaseDatabase.getInstance().getReference("posts")
                dbref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            userArrayList.clear() // clearing  list and adding only filtered posts
                            for(postsnapshot in snapshot.children){
                                val post = postsnapshot.getValue(Post::class.java)
                                if(post != null && speechResult != null && post.space?.contains(speechResult, ignoreCase = true) == true
                                    && !post.space.contains("sale", ignoreCase = true)
                                    && !post.space.contains("land", ignoreCase = true)
                                    && !post.space.contains("office", ignoreCase = true)
                                    && !post.space.contains("shop", ignoreCase = true)) {
                                    userArrayList.add(post)
                                }
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }


                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        // recognition listener for the SpeechRecognizer
        speechRecognizer.setRecognitionListener(recognitionListener)

        // Starting the speech recognition when the mic button is clicked
        micButton.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
            speechRecognizer.startListening(intent)
            searchEditText.setText("")
            Log.d("VoiceSearch", "Started speech recognition")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the resources used by the SpeechRecognizer
        speechRecognizer.destroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission to record audio is required for speech recognition", Toast.LENGTH_SHORT).show()
        }
    }
}
