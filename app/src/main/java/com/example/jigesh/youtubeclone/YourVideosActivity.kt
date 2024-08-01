package com.example.jigesh.youtubeclone

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.jigesh.youtubeclone.databinding.ActivityYourvideosBinding
import com.example.jigesh.youtubeclone.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class YourVideosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityYourvideosBinding
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYourvideosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.create.setOnClickListener {
            checkUserChannel()
        }
    }

    private fun checkUserChannel() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(UserModel::class.java)
                        if (user?.channelname.isNullOrEmpty()) {
                            promptForChannelName(userId)
                        } else {
                            showBottomSheetDialog()
                        }
                    } else {
                        promptForChannelName(userId)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting user details: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun promptForChannelName(userId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Channel Name")

        val input = android.widget.EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val channelName = input.text.toString()
            if (channelName.isNotEmpty()) {
                db.collection("users").document(userId).update("channelname", channelName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Channel name saved", Toast.LENGTH_SHORT).show()
                        showBottomSheetDialog()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving channel name: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showBottomSheetDialog() {
        val bottomSheet = MyBottomSheetDialogFragment()
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }
}
