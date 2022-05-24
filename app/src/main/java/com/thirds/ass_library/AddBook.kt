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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import com.thirds.ass_library.Api.NotificationData
import com.thirds.ass_library.Api.PushNotification
import com.thirds.ass_library.Api.RetrofitInstance
import kotlinx.android.synthetic.main.activity_add_book.*
import kotlinx.android.synthetic.main.activity_add_book.B_author
import kotlinx.android.synthetic.main.activity_add_book.B_image
import kotlinx.android.synthetic.main.activity_add_book.B_name
import kotlinx.android.synthetic.main.activity_add_book.B_price
import kotlinx.android.synthetic.main.activity_add_book.B_video
import kotlinx.android.synthetic.main.activity_add_book.B_year
import kotlinx.android.synthetic.main.activity_add_book.Rating
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

class AddBook : AppCompatActivity() {

    lateinit var database: DatabaseReference
    private var db: FirebaseFirestore? = null
    val TAG = "MoM"
    var path =
        "https://firebasestorage.googleapis.com/v0/b/ass-library.appspot.com/o/images%2FScreenshot_2022-05-23-23-22-00-909_com.instagram.android.jpg?alt=media&token=a81f0f73-af17-4262-905f-5f811a12c31f"
    var video =
        "https://firebasestorage.googleapis.com/v0/b/ass-library.appspot.com/o/images%2FVID-20220516-WA0000.mp4?alt=media&token=3fd2f239-cd76-4f12-a833-3909e58a4f35"
    lateinit var progDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

//        db = Firebase.firestore

        //database = Firebase.database.getReference("Books")
        val storage = Firebase.storage
        progDialog = ProgressDialog(this)
        progDialog.setTitle("Loading")
        progDialog.setMessage("Loading")
        progDialog.setCancelable(false)


        addBtn.setOnClickListener {

            if (B_name.text.isEmpty() && B_author.text.isEmpty() && B_year.text.isEmpty() && B_price.text.isEmpty()) {
                Toast.makeText(this, "please fill texts", Toast.LENGTH_SHORT).show()
            } else if (path == "") {
                Toast.makeText(this, "please add image", Toast.LENGTH_SHORT).show()
            } else {
                val book = Book_Class(
                    UUID.randomUUID().toString(),
                    B_name.text.toString(),
                    B_author.text.toString(),
                    path,
                    video,
                    B_year.text.toString(),
                    B_price.text.toString().toDouble(),
                    Rating.rating
                )

                addBook(book)
            }
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
            resultLauncher.launch(Intent.createChooser(intent, "Select a image"))
        }

        B_video.setOnClickListener {
            val intent = Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            resultLauncher2.launch(Intent.createChooser(intent, "Select a video"))
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            RetrofitInstance.api.postNotification(notification)
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun addBook(book: Book_Class) {

        Firebase.database.reference.child("Books").child(book.bookId).setValue(book).addOnSuccessListener {

            FirebaseMessaging.getInstance().subscribeToTopic("/topics/myTopic2")
            Handler().postDelayed({
                PushNotification(
                    NotificationData("Library App", "${book.bookName} book has Added !!"), "/topics/myTopic2"
                ).also {
                    sendNotification(it)
                }
            },1000)

            Toast.makeText(this, "added", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))

        }.addOnFailureListener {
            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
        }

        /*
        db!!.collection("Books").add(book).addOnSuccessListener {
            Toast.makeText(this, "added", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
        }

         */
    }

    // region Image
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

    // endregion

}