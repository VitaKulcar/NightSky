package com.example.nightsky.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.nightsky.PictureOfDay
import com.example.nightsky.R
import com.example.nightsky.databinding.FragmentHomeBinding
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var test ="""{
    "copyright": "Julien Looten",
    "date": "2023-11-11",
    "explanation": "This broad, luminous red arc was a surprising visitor to partly cloudy evening skies over northern France. Captured extending toward the zenith in a west-to-east mosaic of images from November 5, the faint atmospheric ribbon of light is an example of a Stable Auroral Red (SAR) arc. The rare night sky phenomenon was also spotted at unusually low latitudes around world, along with more dynamic auroral displays during an intense geomagnetic storm. SAR arcs and their relation to auroral emission have been explored by citizen science and satellite investigations. From altitudes substantially above the normal auroral glow, the deep red SAR emission is thought to be caused by strong heating due to currents flowing in planet Earth's inner magnetosphere. Beyond this SAR, the Milky Way arcs above the cloud banks along the horizon, a regular visitor to night skies over northern France.",
    "hdurl": "https://apod.nasa.gov/apod/image/2311/SARarcLooten.jpg",
    "media_type": "image",
    "service_version": "v1",
    "title": "The SAR and the Milky Way",
    "url": "https://apod.nasa.gov/apod/image/2311/SARarcLooten1024.jpg"
    }"""

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
        val picture:PictureOfDay = getJsonData()
        setImage(picture.url)
    }

    private fun setImage(imageUrl: String){
        val imageView: ImageView = requireView().findViewById(R.id.imageViewPojav)
        Picasso.get()
            .load(imageUrl)
            .into(imageView)
    }

    private fun getJsonData() : PictureOfDay {
        val apiUrl = "https://api.nasa.gov/planetary/apod?api_key=mncTMkevksCAWLjvN5mijXRIyed88XNg1QTFpaZl"

        try {
            val url = URL(apiUrl)
            val urlConnection = url.openConnection() as HttpURLConnection

            try {
                val data: InputStream = BufferedInputStream(urlConnection.inputStream)
                val jsonString = data.bufferedReader().use { it.readText() }

                val astronomyPicture = Gson().fromJson(jsonString, PictureOfDay::class.java)
                return astronomyPicture
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Gson().fromJson(test, PictureOfDay::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}