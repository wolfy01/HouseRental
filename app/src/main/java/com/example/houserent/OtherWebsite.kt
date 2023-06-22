package com.example.houserent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class OtherWebsite : AppCompatActivity() {
    private lateinit var dbref:DatabaseReference
    private lateinit var userRecyclerview: RecyclerView
    private lateinit var userArrayList :ArrayList<Post>
    private lateinit var adapter: OtherWebsiteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_website)

        userRecyclerview=findViewById(R.id.postlist)
        userRecyclerview.layoutManager=LinearLayoutManager(this)
        userRecyclerview.setHasFixedSize(true)

        userArrayList= arrayListOf()
        adapter = OtherWebsiteAdapter(userArrayList)
        userRecyclerview.adapter = adapter

        getpostdata()
    }

    private fun getpostdata() {
        dbref=FirebaseDatabase.getInstance().getReference("posts")
        dbref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(postsnapshot in snapshot.children){
                        val post=postsnapshot.getValue(Post::class.java)
                        if (post?.space?.contains("sale", ignoreCase = true) == false
                            && post?.space?.contains("land", ignoreCase = true) == false
                            && post?.space?.contains("office", ignoreCase = true) == false
                            && post?.space?.contains("shop", ignoreCase = true) == false) {
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
}
