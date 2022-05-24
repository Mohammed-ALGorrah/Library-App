package com.thirds.ass_library

data class Book_Class(
    var bookId: String = "",
    var bookName: String = "",
    var bookAuthor: String = "",
    var image: String = "",
    var video: String = "",
    var bookYear: String = "",
    var bookPrice: Double = 0.0,
    var bookRate: Float = 0f
)