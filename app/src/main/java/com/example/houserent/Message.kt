package com.example.houserent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class Message : AppCompatActivity() {
    private lateinit var  userRecyclerView: RecyclerView
    private lateinit var imgBack:ImageView
    private lateinit var adapter: MuserAdapter
    private lateinit var userArrayList :ArrayList<Muser>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

       // userRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        userRecyclerView=findViewById(R.id.userRecyclerView)
        imgBack=findViewById(R.id.imgBack)
        userRecyclerView.layoutManager=LinearLayoutManager(this)
        userRecyclerView.setHasFixedSize(true)

        imgBack.setOnClickListener {
            onBackPressed()
        }
        userArrayList= arrayListOf()
        adapter= MuserAdapter(userArrayList)
        userRecyclerView.adapter=adapter

        getUsersList()
    }
    fun getUsersList() {
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")


        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userArrayList.clear()
                //val currentUser = snapshot.getValue(Muser::class.java)
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val user = dataSnapShot.getValue(Muser::class.java)

                    if (!user!!.userID.equals(firebase.uid)) {
                        userArrayList.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }

        })
    }
}