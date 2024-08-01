package com.example.jigesh.youtubeclone

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_USER_ID = "user_id"

class YoutuberFragment : Fragment() {

    private lateinit var userId: String
    private lateinit var viewPager: ViewPager2
    private lateinit var textView4: TextView
    private lateinit var textView5: TextView
    private lateinit var textView6: TextView
    private lateinit var textView7: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_youtuber, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        textView4 = view.findViewById(R.id.textView4)
        textView5 = view.findViewById(R.id.textView5)
        textView6 = view.findViewById(R.id.textView6)
        textView7 = view.findViewById(R.id.textView7)

        val adapter = ViewPagerAdapter(requireActivity())
        viewPager.adapter = adapter

        textView4.setOnClickListener { viewPager.currentItem = 0 }
        textView5.setOnClickListener { viewPager.currentItem = 1 }
        textView6.setOnClickListener { viewPager.currentItem = 2 }
        textView7.setOnClickListener { viewPager.currentItem = 3 }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabSelection(position)
            }
        })


        return view
    }

    private fun updateTabSelection(position: Int) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.white) // Define in colors.xml
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.gray) // Define in colors.xml

        textView4.setTextColor(if (position == 0) selectedColor else defaultColor)
        textView5.setTextColor(if (position == 1) selectedColor else defaultColor)
        textView6.setTextColor(if (position == 2) selectedColor else defaultColor)
        textView7.setTextColor(if (position == 3) selectedColor else defaultColor)
    }


    companion object {
        fun newInstance(userId: String): YoutuberFragment {
            val fragment = YoutuberFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }
}
