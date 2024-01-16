package com.example.nightsky.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.nightsky.ObservationEntry
import com.example.nightsky.PlanetJSON
import com.example.nightsky.databinding.FragmentCameraBinding
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

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var latitude: Double = 0.0 //Zemljepisna širina
    private var longitude: Double = 0.0 //Zemljepisna dolžina
    private var azimut: Float = 0.0F
    private var smerNeba: String = ""

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
        getLocation()
        getDirection()
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

    private suspend fun getData(): String {
        val apiUrl = """
            https://api.astronomyapi.com/api/v2/bodies/positions?
            longitude=${longitude}&latitude=${latitude}&elevation=1&
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

    private fun getDirection() {
        val sensorManager =
            requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val sensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    val orientationValues = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientationValues)

                    val directions = arrayOf("Sever", "Vzhod", "Jug", "Zahod")
                    val azimuthInDegrees = orientationValues[0]
                    val azimuth = (azimuthInDegrees + 360) % 360
                    azimut = azimuth
                    val index = (azimuth / 90).toInt() % 4
                    val direction = directions[index]
                    smerNeba = direction
                }
            }
        }

        sensorManager.registerListener(
            sensorEventListener,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
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
                    if (planetAltitude > 0 && (planetAzimuth in azimut - 90.0..azimut + 90.0)) {
                        visiblePlanets.add(observationEntry)
                    }
                }
                val visiblePlanetNames = visiblePlanets.map { it.entry.name }

                val tableRow1 = createTableRow("Zemljepisna širina", latitude.toString())
                val tableRow2 = createTableRow("Zemljepisna dolžina", longitude.toString())
                val tableRow3 = createTableRow("Azimut", azimut.toString())
                val tableRow4 = createTableRow("Smer neba", smerNeba)
                val tableRow5 =
                    createTableRow("Število vseh planetov", objects.data.table.rows.size.toString())
                val tableRow6 =
                    createTableRow("Število vidnih planetov", visiblePlanets.size.toString())
                val tableRow7 =
                    createTableRow("Imena vidnih planetov", visiblePlanetNames.joinToString(", "))

                binding.tableLayout.addView(tableRow1)
                binding.tableLayout.addView(tableRow2)
                binding.tableLayout.addView(tableRow3)
                binding.tableLayout.addView(tableRow4)
                binding.tableLayout.addView(tableRow5)
                binding.tableLayout.addView(tableRow6)
                binding.tableLayout.addView(tableRow7)

            } catch (e: Exception) {
                showDialog(requireContext(), "Ni dostopa do interneta")
                e.printStackTrace()
            }
        }
    }

    private fun createTableRow(label: String, value: String): TableRow {
        val tableRow = TableRow(requireContext())
        val labelTextView = TextView(requireContext())
        val valueTextView = TextView(requireContext())

        labelTextView.text = label
        labelTextView.setTextSize(16f)
        labelTextView.gravity = Gravity.CENTER

        val labelLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
        labelTextView.layoutParams = labelLayoutParams

        valueTextView.text = value
        valueTextView.setTextSize(16f)
        valueTextView.gravity = Gravity.CENTER

        val valueLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
        valueTextView.layoutParams = valueLayoutParams

        tableRow.addView(labelTextView)
        tableRow.addView(valueTextView)

        return tableRow
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
                            latitude = location.latitude
                            longitude = location.longitude
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