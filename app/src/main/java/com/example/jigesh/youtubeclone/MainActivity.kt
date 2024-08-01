package com.example.jigesh.youtubeclone


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jigesh.youtubeclone.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var iconHome: LinearLayout
    private lateinit var shortsIcon: LinearLayout
    private lateinit var addIcon: LinearLayout
    private lateinit var subscriptionIcon: LinearLayout
    private lateinit var profileIcon: LinearLayout

    private lateinit var homeImage: ImageView
    private lateinit var shortsImage: ImageView
    private lateinit var addImage: ImageView
    private lateinit var subscriptionImage: ImageView
    private lateinit var profileImage: ImageView

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        iconHome = findViewById(R.id.iconhome)
        shortsIcon = findViewById(R.id.shortsicon)
        addIcon = findViewById(R.id.addicon)
        subscriptionIcon = findViewById(R.id.subscriptionicon)
        profileIcon = findViewById(R.id.profileicon)

        homeImage = findViewById(R.id.homeimage)
        shortsImage = findViewById(R.id.shortsimage)
        addImage = findViewById(R.id.addimage)
        subscriptionImage = findViewById(R.id.subscriptionimage)
        profileImage = findViewById(R.id.profileimage)

        // Set click listeners
        iconHome.setOnClickListener(this)
        shortsIcon.setOnClickListener(this)
        addIcon.setOnClickListener(this)
        subscriptionIcon.setOnClickListener(this)
        profileIcon.setOnClickListener(this)

        // Set the home icon as selected by default
        setSelectedIcon(homeImage)

        // Load the user's profile picture
        loadUserProfile()

        // Show home fragment initially
        showFragment(HomeFragment())
    }

    override fun onClick(v: View) {
        var fragment: Fragment? = null

        when (v.id) {
            R.id.iconhome -> {
                fragment = HomeFragment()
                setSelectedIcon(homeImage)
            }
            R.id.shortsicon -> {
                fragment = ShortsFragment()
                setSelectedIcon(shortsImage)
            }
            R.id.addicon -> {
                startActivity(Intent(this, AddActivity::class.java)) // Navigate to AddActivity
                setSelectedIcon(addImage)
                return // Exit the method early
            }
            /*

            R.id.addicon -> {
                fragment = AddFragment()
                setSelectedIcon(addImage)
            }*/
            R.id.subscriptionicon -> {
                fragment = SubscriptionFragment()
                setSelectedIcon(subscriptionImage)
            }
            R.id.profileicon -> {
                fragment = ProfileFragment()
                setSelectedIcon(profileImage)
            }
        }

        fragment?.let { showFragment(it) }
    }

    private fun setSelectedIcon(imageView: ImageView) {
        // Reset all icons to unselected state
        homeImage.setImageResource(R.drawable.homenotselected)
        shortsImage.setImageResource(R.drawable.shortsnotselected)
        addImage.setImageResource(R.drawable.addselected)
        subscriptionImage.setImageResource(R.drawable.subnotselected)
        profileImage.setImageResource(R.drawable.profileselected)

        // Set the clicked icon to selected state
        imageView.setImageResource(
            when (imageView) {
                homeImage -> R.drawable.homeselected
                shortsImage -> R.drawable.shortsselected
                addImage -> R.drawable.addselected
                subscriptionImage -> R.drawable.subselected
                profileImage -> R.drawable.profileselected
                else -> throw IllegalArgumentException("Unknown ImageView")
            }
        )
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.framelayout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return // Get the actual user ID

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject<UserModel>()
                if (user != null) {
                    user.profilePic?.let {
                        Glide.with(this)
                            .load(it)
                            .placeholder(R.drawable.profileselected)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profileImage)
                    }
                } else {
                    Log.e("UserProfile", "User data is null")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserProfile", "Failed to load user profile: ${exception.message}", exception)
            }
    }

}
