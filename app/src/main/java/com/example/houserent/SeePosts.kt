package com.example.houserent

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException

class SeePosts : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_posts)
        val postsRef = FirebaseDatabase.getInstance().getReference("posts")
        data class Post(val imgurl: String,val space:String)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                //Firebase Realtime Database URL
                val postsRef = FirebaseDatabase.getInstance().getReference("posts")
                data class Post(val imgurl: String,val space:String)
                var page = 1
                while (true) {
                    val doc = Jsoup.connect("https://bikroy.com/en/ads/bangladesh/property?page=$page").get()
                    val elements = doc.select("div.container--2uFyv")
                    val size=elements.size
                    if (size == 0) break // no more pages
                    for (i in 0 until size) {
                        val imgUrl=doc.select("div.image--2hizm").select("div.featured-member--1CQta").select("img").eq(i).attr("src")
                        val space=doc.select("div.content--3JNQz").eq(i).text()
                        val post = Post(imgUrl,space)
                        val postId = postsRef.push().key
                        postsRef.child(postId!!).setValue(post)
                            .addOnSuccessListener {
                                Log.d("MainActivity", "Post uploaded successfully!")
                            }
                            .addOnFailureListener { e ->
                                Log.e("MainActivity", "Failed to upload post", e)
                            }
                    }
                    page++
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "Error scraping website", e)
            }
        }

    }
}

