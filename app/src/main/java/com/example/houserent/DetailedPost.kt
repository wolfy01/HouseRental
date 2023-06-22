package com.example.houserent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class DetailedPost : AppCompatActivity() {
    private lateinit var dbref: DatabaseReference
    private lateinit var userRecyclerview: RecyclerView
    private lateinit var userArrayList :ArrayList<PostD>
    private lateinit var adapter: DetailedPostAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_post)

        userRecyclerview=findViewById(R.id.detailedpostlist)
        userRecyclerview.layoutManager= LinearLayoutManager(this)
        userRecyclerview.setHasFixedSize(true)

        userArrayList= arrayListOf()
        adapter = DetailedPostAdapter(userArrayList)
        userRecyclerview.adapter = adapter

        getpostdata()
    }

    private fun getpostdata() {
        dbref= FirebaseDatabase.getInstance().getReference("Detailedposts")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(postsnapshot in snapshot.children){
                        val PostD=postsnapshot.getValue(PostD::class.java)
                        if (PostD != null) {
                            userArrayList.add(PostD)
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
}