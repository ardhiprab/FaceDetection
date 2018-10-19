package dev.prab.facedetection

import android.app.AlertDialog
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.wonderkiln.camerakit.*
import dev.prab.facedetection.Helper.GraphicOverlay
import dev.prab.facedetection.Helper.RectOverlay
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var waitingDialog: AlertDialog

    override fun onResume() {
        super.onResume()
        camera_view.start()
    }

    override fun onPause() {
        super.onPause()
        camera_view.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the Mobile Ads SDK with an AdMob App ID.
        MobileAds.initialize(this, "ca-app-pub-5537249287674941~9265746319")

        // Create an ad request . If you're running this on a physical device, check your logcat to
        // learn how to enable test ads for it. Look for a line like this one:
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        val adRequest = AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)

        //Init
        waitingDialog = SpotsDialog.Builder().setContext(this)
            .setMessage("Tunggu sebentar...")
            .setCancelable(false)
            .build()

        //Event
        btn_detect.setOnClickListener {
            camera_view.start()
            camera_view.captureImage()
            graphic_overlay.clear()
        }

        camera_view.addCameraKitListener(object : CameraKitEventListener {
            override fun onVideo(p0: CameraKitVideo?) {

            }

            override fun onEvent(p0: CameraKitEvent?) {

            }

            override fun onImage(p0: CameraKitImage?) {
                waitingDialog.show()

                var bitmap = p0!!.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, camera_view.width, camera_view.height, false)
                camera_view.stop()

                runFaceDetector(bitmap)
            }

            override fun onError(p0: CameraKitError?) {

            }

            private fun processFaceResult(result: List<FirebaseVisionFace>) {
                var count = 0
                for (face in result) {
                    val bounds = face.boundingBox
                    val rectOverlay = RectOverlay(graphic_overlay, bounds)
                    graphic_overlay.add(rectOverlay)

                    count++
                }

                waitingDialog.dismiss()
                //Toast.makeText(this, String.format("Terdeteksi %d wajah di gambar", count), Toast.LENGTH_SHORT).show()
            }

            private fun runFaceDetector(bitmap: Bitmap?) {
                val image = FirebaseVisionImage.fromBitmap(bitmap!!)
                val options = FirebaseVisionFaceDetectorOptions.Builder().build()
                val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

                detector.detectInImage(image)
                    .addOnSuccessListener { result -> processFaceResult(result) }
                //.addOnFailureListener { e -> Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show() }
            }

        })
    }



}
