package com.example.houserent

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class EmailSync : AppCompatActivity() {
    private lateinit var postImage: ImageView
    private var imageUri: Uri? = null
    private lateinit var postButton: Button
    var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_sync)
        postImage = findViewById(R.id.imageView6)
        postButton = findViewById(R.id.button)
        val uri: Uri? = intent.data
        var afterPath: String = ""
        if (uri != null) {
            val path: String = uri.toString()
            afterPath = path.substring(path.indexOf(".com/") + 5)
            Toast.makeText(this, "$path $afterPath", Toast.LENGTH_SHORT).show()
        }

        postButton.setOnClickListener {
            if (imageUri != null) {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()

                val storageRef = FirebaseStorage.getInstance().getReference("images/${UUID.randomUUID()}")
                storageRef.putFile(imageUri!!)
                    .addOnSuccessListener { uploadTask ->
                        uploadTask.storage.downloadUrl.addOnSuccessListener { uri ->
                            Toast.makeText(this, "Post downloaded successfully!", Toast.LENGTH_SHORT).show()
                            val postMap = HashMap<String, Any>().apply {
                                put("Posted Image Link", uri.toString())
                            }
                            val ref = FirebaseDatabase.getInstance().getReference("OwnPosts")
                            ref.child(afterPath).updateChildren(postMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Pic uploaded successfully!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(this, "Failed to upload pic. Please try again later.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnCompleteListener { task ->
                                    progressDialog.dismiss()
                                    if (!task.isSuccessful) {
                                        Toast.makeText(this, "Failed to upload pic. Please try again later.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to upload image. Please try again later.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter post details and select an image.", Toast.LENGTH_SHORT).show()
            }
        }

        postImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
        }
    }

    companion object {
        const val REQUEST_IMAGE_GALLERY = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            imageUri = data.data!!
            postImage.setImageURI(imageUri)
        }
    }
}
