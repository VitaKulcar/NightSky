package com.example.nightsky.ui.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.nightsky.Location
import com.example.nightsky.ObservationEntry
import com.example.nightsky.PlanetJSON
import com.example.nightsky.databinding.FragmentCameraBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var myLatitude: Double = 46.557339 //Zemljepisna širina
    private var myLongitude: Double = 15.645910 //Zemljepisna dolžina

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                imageBitmap?.let { bitmap ->
                    binding.imageViewCapturePhoto.setImageBitmap(bitmap)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        takePicture()
        //getLocation()
        //getPlanets()
        getVisiblePlanets()
        binding.buttonSavePhoto.setOnClickListener {
            val drawable: Drawable? = binding.imageViewCapturePhoto.drawable
            val bitmap: Bitmap? = (drawable as? BitmapDrawable)?.bitmap
            bitmap?.let { imageBitmap ->
                savePicture(imageBitmap)
            }
        }
    }

    private fun takePicture() {
        val cameraPermission = Manifest.permission.CAMERA
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            cameraPermission
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    private fun savePicture(bitmap: Bitmap) {
        val fileName = "image.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val resolver = requireActivity().contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            Toast.makeText(requireContext(), "Slika je shranjena", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(requireContext(), "Napaka pri shranjevanju", Toast.LENGTH_SHORT).show()
        }
    }

    /*
    private fun getPlanets() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                var jsonString = getData()
                val visibleObjects: PlanetJSON = Gson().fromJson(jsonString, PlanetJSON::class.java)
                showDialog(requireContext(), visibleObjects.toString())
            } catch (e: Exception) {
                showDialog(requireContext(), "Ni dostopa do interneta")
                e.printStackTrace()
            }
        }
    }

     */

    private suspend fun getData(): String {
        val apiUrl = """
            https://api.astronomyapi.com/api/v2/bodies/positions?
            longitude=${myLongitude}&latitude=${myLatitude}&elevation=1&
            from_date=2024-01-10&to_date=2024-01-10&time=18%3A34%3A12
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

    private fun getVisiblePlanets() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val jsonString = getData()
                val objects: PlanetJSON = Gson().fromJson(jsonString, PlanetJSON::class.java)
                val visiblePlanets = mutableListOf<ObservationEntry>()
                objects.data.table.rows.forEach { observationEntry ->
                    val cells = observationEntry.cells
                    val planetPosition = cells.first().position
                    val planetAltitude = planetPosition.horizontal.altitude.degrees.toDouble()
                    val planetAzimuth = planetPosition.horizontal.azimuth.degrees.toDouble()
                    val isPlanetVisible = isPlanetVisible(planetAltitude, planetAzimuth)
                    if (isPlanetVisible) {
                        visiblePlanets.add(observationEntry)
                    }
                }

                showDialog(
                    requireContext(), "Število vidnih planetov: ${visiblePlanets.count()}" +
                            "\nŠtevilo vseh planetov: ${objects.data.table.rows.count()}"
                )
            } catch (e: Exception) {
                showDialog(requireContext(), "Ni dostopa do interneta")
                e.printStackTrace()
            }
        }
    }

    private fun isPlanetVisible(altitude: Double, azimuth: Double): Boolean {
        return altitude > 0 && azimuth in 180.0..360.0
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
                        } else showDialog(requireContext(), "Ni dostopa do lokacije")
                    }
            } catch (e: Exception) {
                showDialog(requireContext(), "Ni dostopa do lokacije")
                e.printStackTrace()
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
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