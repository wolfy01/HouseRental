package com.example.houserent


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class DetailedPostAdapter(private val postlist: ArrayList<PostD>) :RecyclerView.Adapter<DetailedPostAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemview=LayoutInflater.from(parent.context).inflate(R.layout.parse_item2,
            parent,false)
        return MyViewHolder(itemview)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem=postlist[position]
        if (currentitem.imgurl != null) {
            Picasso.get().load(currentitem.imgurl).into(holder.imageView)
        }
        holder.space.text=currentitem.space
        holder.btn.setOnClickListener {
            val intent = Intent(it.context,ChatActivity::class.java)
            intent.putExtra("userID",currentitem.postedBY)
            intent.putExtra("userName",currentitem.userName)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return postlist.size
    }
    class MyViewHolder(itemview:View) : RecyclerView.ViewHolder(itemview){
        val imageView:ImageView=itemView.findViewById(R.id.postimageview)
        val space:TextView=itemview.findViewById(R.id.posttextview)
        val btn:Button=itemview.findViewById(R.id.messageowner)

    }
}