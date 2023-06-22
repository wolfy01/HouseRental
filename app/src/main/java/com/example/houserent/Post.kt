package com.example.houserent

data class Post(
    val imgurl: String = "",
    val space: String = ""
) {
    constructor() : this("", "")
}

