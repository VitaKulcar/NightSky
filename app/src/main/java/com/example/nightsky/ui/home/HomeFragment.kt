package com.example.nightsky.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.nightsky.PictureOfDay
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val pictureData = getJsonData()
                val picture: PictureOfDay = Gson().fromJson(pictureData, PictureOfDay::class.java)

                Picasso.get().load(picture.url).into(binding.imageViewPojav)
                binding.textViewNaslov.text = picture.title
                binding.textViewOpisPojava.text = picture.explanation

            } catch (e: Exception) {
                showDialog(requireContext(), "Ni dostopa do interneta")
                e.printStackTrace()
            }
        }
    }

    private suspend fun getJsonData(): String {
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

    private fun showDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Obvestilo")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}