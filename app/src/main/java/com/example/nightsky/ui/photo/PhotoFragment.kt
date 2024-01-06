package com.example.nightsky.ui.photo

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nightsky.databinding.FragmentCameraBinding


class PhotoFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var photo: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        var root = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receivePicture()

        //overlayPlanetaryPositions()

        val button: Button = requireView().findViewById(com.example.nightsky.R.id.buttonSave)
        button.setOnClickListener {
            savePhoto()
        }
    }

    private fun receivePicture() {
        showDialog(requireContext(), "prejemanje forografije")
        arguments?.getParcelable<Bitmap>("capturedBitmap")?.let { receivedBitmap ->
            photo = receivedBitmap
            val imageView: ImageView = requireView().findViewById(com.example.nightsky.R.id.imageViewPojav)
            imageView.setImageBitmap(receivedBitmap)
        }
    }

    private fun showDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Obvestilo")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun savePhoto() {
        /*
        photo?.let { bitmap ->
            val savedUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())?.also { uri ->
                requireActivity().contentResolver.openOutputStream(uri).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }

            // Show a toast message indicating the save status
            if (savedUri != null) {
                Toast.makeText(requireContext(), "Photo saved successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save photo", Toast.LENGTH_SHORT).show()
            }

         */
        }


    /*
    private fun overlayPlanetaryPositions() {
        val textureView: TextureView = requireView().findViewById(R.id.textureView)
        textureView.post {
            val canvas = textureView.lockCanvas() ?: return@post
            // val bitmap = Bitmap.createBitmap(textureView.width, textureView.height, Bitmap.Config.ARGB_8888)
            // val canvas = Canvas(bitmap)
            canvas.drawColor(
                Color.TRANSPARENT,
                PorterDuff.Mode.CLEAR
            ) // Set background to transparent

            val paint = Paint().apply {
                color = Color.GREEN
                style = Paint.Style.FILL
            }

            canvas.drawCircle(40f, 70f, 20f, paint)
            textureView.unlockCanvasAndPost(canvas)
        }
    }
     */

    /*
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CameraPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraInternal()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required to use the camera.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
     */

    /*
    private fun displayCapturedImage(image:ImageProxy) {
      //  Bitmap capturedBitmap = /* Convert ImageProxy to Bitmap */;

        // Display the captured image in an ImageView or another view
        ImageView imageView = rootView.findViewById(R.id.textureView);
        imageView.setImageBitmap(capturedBitmap);
    }
*/

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}