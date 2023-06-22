package com.example.houserent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.Voice
import android.widget.Toast
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val createPostCardView = findViewById<CardView>(R.id.cardview1)
        val seeMessageCardView = findViewById<CardView>(R.id.cardview2)
        val seeOtherPostsCardView = findViewById<CardView>(R.id.cardview3)
        val voiceSearch=findViewById<CardView>(R.id.cardview4)
        val postviavoice=findViewById<CardView>(R.id.cardview5)
        val seelocation=findViewById<CardView>(R.id.cardview6)
        val seemap=findViewById<CardView>(R.id.cardview7)
        val seedetailedpost=findViewById<CardView>(R.id.cardview8)
        createPostCardView.setOnClickListener {
            Toast.makeText(this, "Create post clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CreatePost::class.java)
            startActivity(intent)
        }

        seeMessageCardView.setOnClickListener {
            Toast.makeText(this, "See Message clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Message::class.java)
            startActivity(intent)
        }

        seeOtherPostsCardView.setOnClickListener {
            Toast.makeText(this, "See all posts clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, OtherWebsite::class.java)
            startActivity(intent)
        }
        voiceSearch.setOnClickListener {
            Toast.makeText(this, "Voice Search clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, VoiceSearch::class.java)
            startActivity(intent)
        }
        postviavoice.setOnClickListener {
            Toast.makeText(this, "Voice post clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, VoicePost::class.java)
            startActivity(intent)
        }
        seelocation.setOnClickListener{
            Toast.makeText(this, "see location clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AugRea::class.java)
            startActivity(intent)
        }
        seemap.setOnClickListener{
            Toast.makeText(this, "Map activity clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        seedetailedpost.setOnClickListener{
            Toast.makeText(this, "Detailed post clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DetailedPost::class.java)
            startActivity(intent)
        }

    }
}