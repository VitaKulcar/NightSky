package com.example.nightsky.ui.notifications

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nightsky.PlanetJSON
import com.example.nightsky.databinding.FragmentNotificationsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class NotificationsFragment : Fragment() {
    private var myLatitude: Double = 0.0 //Zemljepisna širina
    private var myLongitude: Double = 0.0 //Zemljepisna dolžina
    private var myAltitude: Double = 0.0 //Nadmorska višina

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getLocation()
        showDialog(
            requireContext(),
            "Zemljepisna širina:${myLatitude}, Zemljepisna dolžina:${myLongitude}, Nadmorska višina:${myAltitude}"
        )
        getPlanets()
    }

    private suspend fun getData(): String {
        val apiUrl = """
            https://api.astronomyapi.com/api/v2/bodies/positions?
            longitude=${myLongitude}&latitude=${myLatitude}&elevation=${myAltitude}&
            from_date=2023-11-25&to_date=2023-11-25&time=17%3A09%3A45
        """.trimIndent()
        return withContext(Dispatchers.IO) {
            val url = URL(apiUrl)
            val urlConnection = url.openConnection() as HttpURLConnection

            val authorizationHeaderValue =
                "Basic NTYxMTMyNzUtNDJjMS00MWI5LTgyOWQtMGVkM2NhNjIzNzQ3OmNhZjBkYjhiMWJjZDcwMjczZDU3MWRlODc4MjAyMWMwZjJhMTFjOWU2Y2FjMDVlMmMwOWYwNDFlMTVjZTE1M2IxN2I1MmMzNjdkMTEwYmJjNmEzZmYxMjY3MTAyY2FhODNjNGEyMWIyMzdhMDJkMTRlNTk4ZTM2YjNhYzg4YTMxNDljZTQxNzQ3ZmM2NDJiMDVlNzM3OWZhNjkzMWJjYmQ1ZDcwNjAwMTk2MjI4YmIzNWFhNjEzNDZlYjk0OTkyZjcwZTFhMGI4ODVmNzIxNDkyYjRjYjJhYmRhM2YwYTdl"
            urlConnection.setRequestProperty("Authorization", authorizationHeaderValue)

            val data = BufferedInputStream(urlConnection.inputStream)
            val pictureString = data.bufferedReader().use { it.readText() }
            urlConnection.disconnect()
            pictureString
        }
    }

    private fun getPlanets() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                var jsonString = getData()
                val visibleObjects: PlanetJSON = Gson().fromJson(jsonString, PlanetJSON::class.java)
                showDialog(requireContext(), visibleObjects.toString())
            } catch (e: Exception) {
                showDialog(requireContext(), e.toString())
            }
        }
    }

    private fun showDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Obvestilo")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getLocation() {
        var fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            myLatitude = location.latitude
                            myLongitude = location.longitude
                            myAltitude = location.altitude
                        } else showDialog(requireContext(), "Ni dostopa do lokacije")
                    }
            } catch (exc: Exception) {
                showDialog(requireContext(), "Ni dostopa do lokacije")
                Log.e(ContentValues.TAG, "Error geting location", exc)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}