package com.example.nightsky.ui.camera

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.nightsky.databinding.FragmentCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.navigation.fragment.findNavController

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var cameraProvider: ProcessCameraProvider
    private var CameraPermissionRequestCode = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        var root = binding.root

        val button: Button = root.findViewById(com.example.nightsky.R.id.buttonPicture)
        /*
        button.setOnClickListener {
            capturePhoto { bitmap ->
                showDialog(requireContext(), "pripravljena forografija 1")
                val bundle = Bundle().apply {
                    putParcelable("capturedBitmap", bitmap)
                }
                showDialog(requireContext(), "pripravljena forografija 2")
                findNavController().navigate(com.example.nightsky.R.id.action_cameraFragment_to_photoFragment, bundle)
            }
        }
         */

        button.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("capturedBitmap", createWhiteBitmap(100, 100))
            }
            showDialog(requireContext(), "pripravljena forografija 1")
            findNavController().navigate(com.example.nightsky.R.id.action_cameraFragment_to_photoFragment, bundle)
        }


        return root
    }
    private fun createWhiteBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        return bitmap
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
    }

    private fun capturePhoto(callback: (Bitmap) -> Unit) {
        val imageCapture = ImageCapture.Builder().build()
        imageCapture.takePicture(cameraExecutor, object : OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                image.close()
                callback(bitmap)
            }
            override fun onError(exception: ImageCaptureException) {
                showDialog(requireContext(), "${exception.message}")
                Log.e("CaptureError", "Error capturing image: ${exception.message}")
            }
        })
    }

    private fun startCamera() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCameraInternal()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CameraPermissionRequestCode
            )
        }
    }

    private fun startCameraInternal() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                val textureView: TextureView =
                    requireView().findViewById(com.example.nightsky.R.id.textureView)
                val surfaceProvider = Preview.SurfaceProvider { request ->
                    val surface = Surface(textureView.surfaceTexture)
                    request.provideSurface(surface, cameraExecutor) {}
                }
                preview.setSurfaceProvider(surfaceProvider)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Error starting camera", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun showDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Obvestilo")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}