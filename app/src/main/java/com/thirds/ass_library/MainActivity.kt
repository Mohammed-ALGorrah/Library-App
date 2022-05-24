package com.thirds.ass_library

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.template.view.*

class MainActivity : AppCompatActivity() {

    private var db: FirebaseFirestore? = null
    //private var myAdapter: FirestoreRecyclerAdapter<Book_Class, ViewH>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //db = Firebase.firestore

        add_Book.setOnClickListener {
            startActivity(Intent(this, AddBook::class.java))
        }

        getAllBooks()
        //myAdapter!!.startListening()

    }

    private fun getAllBooks() {


        val arra = ArrayList<Book_Class>()

        val db = Firebase.database.reference.child("Books")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                arra.clear()
                snapshot.children.forEach {

                    val obj = it.getValue(Book_Class::class.java)!!

                    arra.add(obj)

                }
                recycleVi.apply {
                    adapter = Adapter(this@MainActivity, arra)
                    layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


/*
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

                holder.itemView.Book_name.text = model.bookName
                holder.itemView.Book_author.text = model.bookAuthor
                holder.itemView.Book_year.text = model.bookYear
                holder.itemView.rate.text = model.bookRate.toString()
                holder.itemView.rateShape.rating = model.bookRate
                holder.itemView.Book_price.text = "$ ${model.bookPrice}"
                if (model.image.isNotEmpty()){
                    holder.itemView.Book_Image.load(model.image)
                }

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
*/
    }

    class ViewH(i: View) : RecyclerView.ViewHolder(i)


//    override fun onStop() {
//        super.onStop()
//        myAdapter!!.stopListening()
//    }

    class Adapter(val context: Context, val ll: ArrayList<Book_Class>) :
        RecyclerView.Adapter<Adapter.viewHolder>() {

        class viewHolder(I: View) : RecyclerView.ViewHolder(I)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
            return viewHolder(
                LayoutInflater.from(context).inflate(R.layout.template, parent, false)
            )
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: viewHolder, position: Int) {

            holder.itemView.Book_Image.load(ll[position].image)


            holder.itemView.Book_name.text = ll[position].bookName
            holder.itemView.Book_author.text = ll[position].bookAuthor
            holder.itemView.Book_year.text = ll[position].bookYear

            holder.itemView.rateShape.rating = ll[position].bookRate
            holder.itemView.Book_price.text = ll[position].bookPrice.toString()

            holder.itemView.Book_edit.setOnClickListener {
                val i = Intent(context, EditBooks::class.java)
                i.putExtra("id", ll[position].bookId)
                context.startActivity(i)
            }

            holder.itemView.Book_preview.setOnClickListener {
                val i = Intent(context, previewVideo::class.java)
                i.putExtra("video", ll[position].video)
                context.startActivity(i)
            }

        }

        override fun getItemCount(): Int {
            return ll.size
        }


    }

}
