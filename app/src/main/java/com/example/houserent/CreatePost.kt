package com.example.houserent

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class CreatePost : AppCompatActivity() {

    private lateinit var postButton: AppCompatButton
    private lateinit var postDetails: EditText
    private lateinit var postlatitude: EditText
    private lateinit var postlongitude: EditText
    private lateinit var postImage: ImageView
    private lateinit var imageUri: Uri
    var firebaseUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        postButton = findViewById(R.id.button)
        postDetails = findViewById(R.id.editpost)
        postImage = findViewById(R.id.imageView6)
        postlatitude=findViewById(R.id.edittext1)
        postlongitude=findViewById(R.id.edittext2)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        // Disable the post button initially
        postButton.isEnabled = false

        postButton.setOnClickListener {
            val details = postDetails.text.toString().trim()
            val latitude=postlatitude.text.toString()
            val longitude=postlongitude.text.toString()
            var username=""
            val uidRef = Firebase.database.reference.child("Users").child(firebaseUser!!.uid)
            uidRef.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val snapshot = it.result
                    username = snapshot.child("userName").value.toString()
                }
            }

            // If the user has entered post details and selected an image, upload the image to Firebase Storage
            if (details.isNotEmpty() && imageUri != null) {
                // Show progress dialog
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()

                // Get a reference to the Firebase Storage location where the image will be uploaded
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
                                put("latitude",latitude)
                                put("longitude",longitude)
                                put("postedBY",firebaseUser!!.uid)
                                put("userName",username)
                                //put("userId", FirebaseAuth.getInstance().currentUser?.uid ?: "")
                            }
                            FirebaseDatabase.getInstance().getReference("Detailedposts")
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

        // Set up click listener for post image
        postImage.setOnClickListener {
            // Create an intent to open the image gallery
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
        }

        // Set up text change listener for post details
        postDetails.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // If the post details edit text is not empty, enable the post button
                postButton.isEnabled = !s.isNullOrEmpty() && s.trim().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    companion object {
        const val REQUEST_IMAGE_GALLERY = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            // Get the selected image URI
            imageUri = data.data!!

            // Set the selected image in the post image view
            postImage.setImageURI(imageUri)
        }
    }}
