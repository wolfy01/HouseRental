package com.example.houserent

import android.content.Context
import android.content.Intent
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class MuserAdapter( private val userList: ArrayList<Muser>) :
    RecyclerView.Adapter<MuserAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.m_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.txtUserName.text = user.userName
        holder.layoutUser.setOnClickListener {
            val intent = Intent(it.context,ChatActivity::class.java)
            intent.putExtra("userID",user.userID)
            intent.putExtra("userName",user.userName)
            it.context.startActivity(intent)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUserName:TextView = view.findViewById(R.id.userName)
        val layoutUser:LinearLayout = view.findViewById(R.id.layoutUser)
    }
}