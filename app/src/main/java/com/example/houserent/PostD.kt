package com.example.houserent


data class PostD(
    val imgurl: String = "",
    val latitude:String="",
    val longitude:String="",
    val postedBY:String="",
    val space: String = "",
    val userName:String=""
) {
    constructor() : this("", "","","","","")
}


