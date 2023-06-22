package com.example.houserent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class OtherWebsiteAdapter(private val postlist: ArrayList<Post>) :RecyclerView.Adapter<OtherWebsiteAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemview=LayoutInflater.from(parent.context).inflate(R.layout.parse_item,
        parent,false)
        return MyViewHolder(itemview)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem=postlist[position]
        if (currentitem.imgurl != null) {
            Picasso.get().load(currentitem.imgurl).into(holder.imageView)
        }
        holder.space.text=currentitem.space
    }

    override fun getItemCount(): Int {
        return postlist.size
    }
    class MyViewHolder(itemview:View) : RecyclerView.ViewHolder(itemview){
        val imageView:ImageView=itemView.findViewById(R.id.postimageview)
        val space:TextView=itemview.findViewById(R.id.posttextview)

    }
}