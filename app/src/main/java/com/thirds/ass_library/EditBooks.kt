package com.thirds.ass_library

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_edit_books.*

class EditBooks : AppCompatActivity() {

    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_books)

        db = Firebase.firestore

        val bookID = intent.getStringExtra("id")!!

        getBookData(bookID)

        editBtn.setOnClickListener {
            editBook(bookID)
        }


        deleteBtn.setOnClickListener {
            deleteBook(bookID)
        }

    }


    private fun getBookData(id: String) {
        db!!.collection("Books").whereEqualTo("bookId", id).get().addOnSuccessListener { book ->
            B_name.setText(book.documents[0].get("bookName").toString())
            B_author.setText(book.documents[0].get("bookAuthor").toString())
            B_year.setText(book.documents[0].get("bookYear").toString())
            B_price.setText(book.documents[0].get("bookPrice").toString())
            Rating.rating = book.documents[0].get("bookRate").toString().toFloat()

        }
    }

    private fun editBook(id: String) {

        db!!.collection("Books").whereEqualTo("bookId", id).get().addOnSuccessListener {
            db!!.collection("Books").document(it.documents[0].id)
                .update(
                    "bookName",
                    B_name.text.toString(),
                    "bookAuthor",
                    B_author.text.toString(),
                    "bookPrice",
                    B_price.text.toString().toDouble(),
                    "bookRate",
                    Rating.rating.toString().toDouble(),
                    "bookYear",
                    B_year.text.toString()
                ).addOnSuccessListener {
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteBook(id: String) {
        db!!.collection("Books").whereEqualTo("bookId", id).get().addOnSuccessListener {
            db!!.collection("Books").document(it.documents[0].id).delete().addOnSuccessListener {
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

}