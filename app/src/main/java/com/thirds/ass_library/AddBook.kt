package com.thirds.ass_library

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_book.*
import java.util.*

class AddBook : AppCompatActivity() {

    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        db = Firebase.firestore

        addBtn.setOnClickListener {

            val book = Book_Class(
                UUID.randomUUID().toString(),
                B_name.text.toString(),
                B_author.text.toString(),
                "1992",
                B_price.text.toString().toDouble(),
                Rating.rating
            )

            addBook(book)
            startActivity(Intent(this,MainActivity::class.java))

        }


    }

    private fun addBook(book: Book_Class) {
        db!!.collection("Books").add(book).addOnSuccessListener {
            Toast.makeText(this, "added", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
        }


    }


}