package com.thirds.ass_library

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import com.thirds.ass_library.Api.NotificationData
import com.thirds.ass_library.Api.PushNotification
import com.thirds.ass_library.Api.RetrofitInstance
import kotlinx.android.synthetic.main.activity_edit_books.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditBooks : AppCompatActivity() {

    //private var db: FirebaseFirestore? = null
    var path = ""
    var video = ""
    val storage = Firebase.storage
    val TAG = "MoM"
    lateinit var progDialog: ProgressDialog
    val TOPIC = "/topics/myTopic2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_books)

        //db = Firebase.firestore
        progDialog = ProgressDialog(this)
        progDialog.setTitle("Loading")
        progDialog.setMessage("Loading")
        progDialog.setCancelable(false)

        val bookID = intent.getStringExtra("id")!!

        getBookData(bookID)

        editBtn.setOnClickListener {
            editBook(bookID)
        }

        deleteBtn.setOnClickListener {
            deleteBook(bookID)
        }

        val storageRef = storage.reference

        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    progDialog.show()
                    // There are no request codes
                    val intent: Intent? = result.data
                    val uri = intent?.data  //The uri with the location of the file
                    Log.d(TAG, uri.toString())
                    val file = getFile(applicationContext, uri!!)
                    val new_uri = Uri.fromFile(file)

                    Toast.makeText(this, "${new_uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
                    val selected_file_Ref = storageRef.child("images/${new_uri.lastPathSegment}")
                    val uploadTask = selected_file_Ref.putFile(new_uri)

                    uploadTask.addOnFailureListener { e ->
                        Log.d(TAG, "Fail ! ${e.message}")
                    }.addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            progDialog.dismiss()
                            path = it.toString()
                            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        val resultLauncher2 =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    progDialog.show()
                    // There are no request codes
                    val intent: Intent? = result.data
                    val uri = intent?.data  //The uri with the location of the file
                    Log.d(TAG, uri.toString())
                    val file = getFile(applicationContext, uri!!)
                    val new_uri = Uri.fromFile(file)

                    Toast.makeText(this, "${new_uri.lastPathSegment}", Toast.LENGTH_SHORT).show()
                    val selected_file_Ref = storageRef.child("images/${new_uri.lastPathSegment}")
                    val uploadTask = selected_file_Ref.putFile(new_uri)

                    uploadTask.addOnFailureListener { e ->
                        Log.d(TAG, "Fail ! ${e.message}")
                    }.addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            progDialog.dismiss()
                            video = it.toString()
                            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        B_image.setOnClickListener {
            val intent = Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            resultLauncher.launch(Intent.createChooser(intent, "Select image"))
        }

        B_video.setOnClickListener {
            val intent = Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            resultLauncher2.launch(Intent.createChooser(intent, "Select video"))
        }

    }


    private fun getBookData(id: String) {

        val db = Firebase.database.reference.child("Books")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {

                    val obj = it.getValue(Book_Class::class.java)!!
                    if (obj.bookId == id) {
                        B_name.setText(obj.bookName)
                        B_author.setText(obj.bookAuthor)
                        B_year.setText(obj.bookYear)
                        B_price.setText(obj.bookPrice.toString())
                        Rating.rating = obj.bookRate
                        if (obj.image.isNotEmpty())
                            path = obj.image
                        if (obj.video.isNotEmpty())
                            video = obj.video
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


/*
        db!!.collection("Books").whereEqualTo("bookId", id).get().addOnSuccessListener { book ->
            B_name.setText(book.documents[0].get("bookName").toString())
            B_author.setText(book.documents[0].get("bookAuthor").toString())
            B_year.setText(book.documents[0].get("bookYear").toString())
            B_price.setText(book.documents[0].get("bookPrice").toString())
            Rating.rating = book.documents[0].get("bookRate").toString().toFloat()
            if (book.documents[0].get("image").toString().isNotEmpty())
                path = book.documents[0].get("image").toString()
        }*/


    }

    private fun editBook(id: String) {
        if (B_name.text.isNotEmpty() && B_author.text.isNotEmpty() && B_year.text.isNotEmpty() && B_price.text.isNotEmpty() && path != ""&& video != "") {

            val book = Book_Class(
                id,
                B_name.text.toString(),
                B_author.text.toString(),
                path,
                video,
                B_year.text.toString(),
                B_price.text.toString().toDouble(),
                Rating.rating
            )

            FirebaseDatabase.getInstance().getReference("Books").child(id).setValue(book)
                .addOnSuccessListener {

                    FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                    Handler().postDelayed({
                        PushNotification(
                            NotificationData("Library App", "${B_name.text} book has Updated !!"), TOPIC
                        ).also {
                            sendNotification(it)
                        }
                    },1000)

                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))

                }

/*
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
*/
        }else{
            Toast.makeText(this, "please fill Data", Toast.LENGTH_SHORT).show()

        }
/*
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
                        B_year.text.toString(),
                        "image",
                        path
                    ).addOnSuccessListener {
                        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                    }
            }
*/

    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            RetrofitInstance.api.postNotification(notification)
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun deleteBook(id: String) {

        FirebaseDatabase.getInstance().getReference("Books").child(id).removeValue()
            .addOnSuccessListener {
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                Handler().postDelayed({
                    PushNotification(
                        NotificationData("Library App", "book has Deleted !!"), TOPIC
                    ).also {
                        sendNotification(it)
                    }
                },1000)
                Toast.makeText(this, "deleted", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
            }


        /*
            db!!.collection("Books").whereEqualTo("bookId", id).get().addOnSuccessListener {
                db!!.collection("Books").document(it.documents[0].id).delete().addOnSuccessListener {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }

         */
    }

    private fun getFile(context: Context, uri: Uri): File? {
        val destinationFilename: File =
            File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri))
        try {
            context.getContentResolver().openInputStream(uri).use { ins ->
                createFileFromStream(
                    ins!!,
                    destinationFilename
                )
            }
        } catch (ex: Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
        return destinationFilename
    }

    private fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.getContentResolver().query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

}