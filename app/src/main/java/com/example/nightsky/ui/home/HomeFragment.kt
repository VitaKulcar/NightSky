package com.example.nightsky.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.nightsky.PictureOfDay
import com.example.nightsky.R
import com.example.nightsky.databinding.FragmentHomeBinding
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val pictureData = fetchJsonData()
                val picture: PictureOfDay = Gson().fromJson(pictureData, PictureOfDay::class.java)
                picture.title?.let { setTitle(it) }

                picture.url?.let { setImage(it) }
                picture.explanation?.let { setText(it) }
            } catch (e: Exception) {
                context?.let { showErrorDialog(it, "Ni dostopa do interneta") }
                e.printStackTrace()
            }
        }
    }

    private fun setImage(imageUrl: String) {
        val imageView: ImageView = requireView().findViewById(R.id.imageViewPojav)
        Picasso.get()
            .load(imageUrl)
            .into(imageView)
    }

    private fun setText(text: String) {

        val textView: TextView = requireView().findViewById(R.id.textViewOpisPojava)
        textView.text = text
    }


    private fun setTitle(text: String) {

        val textView: TextView = requireView().findViewById(R.id.textViewNaslov)
        textView.text = text
    }

    private suspend fun fetchJsonData(): String {
        val apiUrl =
            "https://api.nasa.gov/planetary/apod?api_key=mncTMkevksCAWLjvN5mijXRIyed88XNg1QTFpaZl"

        return withContext(Dispatchers.IO) {
            val url = URL(apiUrl)
            val urlConnection = url.openConnection() as HttpURLConnection
            val data = BufferedInputStream(urlConnection.inputStream)
            val pictureString = data.bufferedReader().use { it.readText() }
            urlConnection.disconnect()
            pictureString
        }
    }

    private fun showErrorDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Napaka")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}