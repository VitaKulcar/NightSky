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
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var polje ="""
        [
            {
                "copyright": "Hubble Legacy Archive",
                "date": "2018-05-25",
                "explanation": "This stunning group of galaxies is far, far away, about 450 million light-years from planet Earth and cataloged as galaxy cluster Abell S0740. Dominated by the cluster's large central elliptical galaxy (ESO 325-G004), this reprocessed Hubble Space Telescope view takes in a remarkable assortment of galaxy shapes and sizes with only a few spiky foreground stars scattered through the field. The giant elliptical galaxy (right of center) spans over 100,000 light years and contains about 100 billion stars, comparable in size to our own spiral Milky Way galaxy. The Hubble data can reveal a wealth of detail in even these distant galaxies, including arms and dust lanes, star clusters, ring structures, and gravitational lensing arcs.",
                "hdurl": "https://apod.nasa.gov/apod/image/1805/ESO325-Pestana.jpg",
                "media_type": "image",
                "service_version": "v1",
                "title": "Galaxies Away",
                "url": "https://apod.nasa.gov/apod/image/1805/ESO325-Pestana1024.jpg"
            },
            {
                "date": "2000-01-18",
                "explanation": "What created this huge space bubble?  A massive star that is not only bright and blue, but also emitting a fast stellar wind of ionized gas.  The Bubble Nebula is actually the smallest of three bubbles surrounding massive star BD+602522, and part of gigantic bubble network S162 created with the help of other massive stars.  As fast moving gas expands off BD+602522, it pushes surrounding sparse gas into a shell.  The energetic starlight then ionizes the shell, causing it to glow.  The above picture taken with the Hubble Space Telescope and released last week shows many details of the Bubble Nebula never seen before and many still not understood.  The nebula, also known as NGC 7635, is about six light-years across and visible with a small telescope towards the constellation of Cassiopeia.",
                "hdurl": "https://apod.nasa.gov/apod/image/0001/bubble_hst_big.jpg",
                "media_type": "image",
                "service_version": "v1",
                "title": "NGC 7635: The Bubble Nebula",
                "url": "https://apod.nasa.gov/apod/image/0001/bubble_hst.jpg"
            },
            {
                "date": "2018-04-26",
                "explanation": "You couldn't really be caught in this blizzard while standing by a cliff on Churyumov-Gerasimenko, also known as comet 67P. Orbiting the comet in June of 2016 the Rosetta spacecraft's narrow angle camera did record streaks of dust and ice particles though, as they drifted across the field of view near the camera and above the comet's surface. Some of the bright specks in the scene are likely due to a rain of energetic charged particles or cosmic rays hitting the camera, and the dense background of stars in the direction of the constellation Canis Major. Click on this single frame to play and the background stars are easy to spot trailing from top to bottom in an animated gif (7.7MB). The 33 frames of the time compressed animation span about 25 minutes of real time. The stunning gif was constructed from consecutive images taken while Rosetta cruised some 13 kilometers from the comet's nucleus.",
                "hdurl": "https://apod.nasa.gov/apod/image/1804/RosettaOsirisC67P_JacintRoger.gif",
                "media_type": "image",
                "service_version": "v1",
                "title": "The Snows of Churyumov-Gerasimenko",
                "url": "https://apod.nasa.gov/apod/image/1804/RosettaOsirisFrame_r1-700.jpg"
            }
        ]
    """

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
        val pictureString: String? = getJsonData()
        context?.let { showErrorDialog(it, "Error occurred: $pictureString") }
        val picture : PictureOfDay = if(pictureString == null) {
            val random = Random().nextInt(3)
            val poljeSlik = Gson().fromJson(polje, Array<PictureOfDay>::class.java)
            poljeSlik[random]
        } else{
            Gson().fromJson(pictureString, PictureOfDay::class.java)
        }
        setImage(picture.url)
        setText(picture.explanation)
    }

    private fun setImage(imageUrl: String?){
        val imageView: ImageView = requireView().findViewById(R.id.imageViewPojav)
        Picasso.get()
            .load(imageUrl)
            .into(imageView)
    }

    private  fun setText(text: String?){
        val textView: TextView = requireView().findViewById(R.id.textViewOpisPojava)
        textView.text = text
    }

    private fun getJsonData() : String? {
        val apiUrl = "https://api.nasa.gov/planetary/apod?api_key=mncTMkevksCAWLjvN5mijXRIyed88XNg1QTFpaZl"
        var pictureString: String? = null

        try {
            val url = URL(apiUrl)
            val urlConnection = url.openConnection() as HttpURLConnection

            urlConnection.connectTimeout = 5000
            urlConnection.readTimeout = 5000

            try {
                val data = BufferedInputStream(urlConnection.inputStream)
                context?.let { showErrorDialog(it, "data: ${data.toString()}") }
                pictureString = data.read().toString()
                //pictureString = data.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                context?.let { showErrorDialog(it, "Napaka: ${e.printStackTrace()}") } //kotlin.Unit
                e.printStackTrace()
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            context?.let { showErrorDialog(it, "Error: ${e.printStackTrace()}") } //null
            e.printStackTrace()
        }
          return pictureString
    }

    private fun showErrorDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}