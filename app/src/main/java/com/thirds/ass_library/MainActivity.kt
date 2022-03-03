package com.thirds.ass_library

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.template.view.*

class MainActivity : AppCompatActivity() {

    private var db: FirebaseFirestore? = null
    private var myAdapter: FirestoreRecyclerAdapter<Book_Class, ViewH>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Firebase.firestore



        add_Book.setOnClickListener {
            startActivity(Intent(this, AddBook::class.java))
        }

        getAllBooks()
        myAdapter!!.startListening()

    }

    private fun getAllBooks() {
        var num = 1
        val query = db!!.collection("Books")
        val option =
            FirestoreRecyclerOptions.Builder<Book_Class>().setQuery(query, Book_Class::class.java)
                .build()
        myAdapter = object : FirestoreRecyclerAdapter<Book_Class, ViewH>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewH {
                val i = LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.template, parent, false)
                return ViewH(i)
            }

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: ViewH, position: Int, model: Book_Class) {

                holder.itemView.Book_num.text = (num++).toString()
                holder.itemView.Book_name.text = model.bookName
                holder.itemView.Book_author.text = model.bookAuthor
                holder.itemView.Book_year.text = model.bookYear
                holder.itemView.rate.text = model.bookRate.toString()
                holder.itemView.rateShape.rating = model.bookRate
                holder.itemView.Book_price.text = "$ ${model.bookPrice}"

                holder.itemView.Book_edit.setOnClickListener {
                    val i = Intent(this@MainActivity, EditBooks::class.java)
                    i.putExtra("id", model.bookId)
                    startActivity(i)
                }
            }
        }
        recycleVi.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycleVi.adapter = myAdapter

    }

    class ViewH(i: View) : RecyclerView.ViewHolder(i)


    override fun onStop() {
        super.onStop()
        myAdapter!!.stopListening()
    }
}
