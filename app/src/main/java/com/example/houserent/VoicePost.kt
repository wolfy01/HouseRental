package com.example.houserent

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class VoicePost : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var questionTextView: TextView
    private lateinit var locationEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var photoImageView: ImageView
    private lateinit var imageUri: Uri

    private val questions = listOf(
        "Where is the location of the house?",
        "Provide some description about it.",
        "Provide the rental price.",
        "Do you want to add a Photo?"
    )
    private var currentQuestionIndex = 0

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_post)

        questionTextView = findViewById(R.id.questionTextView)
        locationEditText = findViewById(R.id.locationEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        priceEditText = findViewById(R.id.priceEditText)
        submitButton = findViewById(R.id.submitButton)
        photoImageView = findViewById(R.id.photoImageView)

        submitButton.isEnabled = false

        submitButton.setOnClickListener {
            val location = locationEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            val details = "$location||$description||$price"//storing rental details
            // If the user has entered post details and selected an image, upload the image to Firebase Storage
            if (details.isNotEmpty() && imageUri != null) {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()

                // reference to the Firebase Storage location where the image will be uploaded
                val storageRef = FirebaseStorage.getInstance().getReference("images/${UUID.randomUUID()}")

                // Upload the image to Firebase Storage
                storageRef.putFile(imageUri)
                    .addOnSuccessListener {
                        // Image uploaded successfully, get the download URL
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            // Save the download URL and other details to the Realtime Database
                            Toast.makeText(this, "Post downloaded successfully!", Toast.LENGTH_SHORT).show()
                            val postMap = HashMap<String, Any>().apply {
                                put("space", details)
                                put("imgurl", uri.toString())
                            }
                            FirebaseDatabase.getInstance().getReference("posts")
                                .push()
                                .setValue(postMap)
                                .addOnSuccessListener {
                                    // ...
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to upload post: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                .addOnCompleteListener { task ->
                                    progressDialog.dismiss()
                                    if (task.isSuccessful) {
                                        // Post uploaded successfully
                                        Toast.makeText(this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, HomeActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        // Post upload failed
                                        Toast.makeText(this, "Failed to upload post. Please try again later.", Toast.LENGTH_SHORT).show()
                                    }
                                }

                        }
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        // Image upload failed
                        Toast.makeText(this, "Failed to upload image. Please try again later.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Either post details or image was not provided
                Toast.makeText(this, "Please enter post details and select an image.", Toast.LENGTH_SHORT).show()
            }
        }

        photoImageView.setOnClickListener {
            openGallery()
        }

        textToSpeech = TextToSpeech(this, this)
    }

    private fun askQuestion(questionIndex: Int) {
        if (questionIndex < questions.size) {
            val question = questions[questionIndex]
            questionTextView.text = question
            speakOut(question)
        } else {
            //questions answered, enable submit button
            submitButton.isEnabled = true
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your answer")
        startActivityForResult(intent, SPEECH_RECOGNITION_REQUEST)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_RECOGNITION_REQUEST && resultCode == RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0)
            handleSpeechRecognitionResult(spokenText)
        } else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.data!!
            photoImageView.setImageURI(imageUri)
        }
    }

    private fun handleSpeechRecognitionResult(spokenText: String?) {
        spokenText?.let { text ->
            when (currentQuestionIndex) {
                0 -> {
                    locationEditText.setText(text)
                }
                1 -> {
                    descriptionEditText.setText(text)
                }
                2 -> {
                    priceEditText.setText(text)
                }
                3 -> {
                    if (text.contains("yes", ignoreCase = true)) {
                        openGallery()
                    }
                }
            }
        }
        currentQuestionIndex++ // Increment the question index
        if (currentQuestionIndex < questions.size) {
            askQuestion(currentQuestionIndex) // Ask the next question
            startSpeechRecognition() // Start speech recognition for the next question
        } else {
            submitButton.isEnabled = true // All questions answered, enable submit button
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Text-to-speech not supported", Toast.LENGTH_SHORT).show()
            } else {
                askQuestion(currentQuestionIndex) // Ask the first question
                startSpeechRecognition() // Start speech recognition after asking the first question
            }
        } else {
            Toast.makeText(this, "Failed to initialize text-to-speech engine", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakOut(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    companion object {
        private const val SPEECH_RECOGNITION_REQUEST = 1
        private const val GALLERY_REQUEST = 2
    }
}