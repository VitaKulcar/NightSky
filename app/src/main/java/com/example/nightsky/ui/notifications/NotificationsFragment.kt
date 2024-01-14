package com.example.nightsky.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.nightsky.PictureOfDay
import com.example.nightsky.databinding.FragmentNotificationsBinding
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val data = getJsonData()
                showDialog(requireContext(), data)
            } catch (e: Exception) {
                showDialog(requireContext(), "Ni dostopa do interneta")
                e.printStackTrace()
            }
        }
    }

    private suspend fun getJsonData(): String {
        val apiUrl = ""

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