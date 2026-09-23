package id.qshield.scanner.ui.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.TimeUnit
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Paksa kamera ke lensa utama (1x), bukan ultra-wide (0,5x).
 *
 * Dilaporkan dari lapangan: pada Samsung S25 aplikasi terbuka di 0,5x
 * dan QRIS sulit difokuskan. HP lain di tim tidak kena.
 *
 * Sebabnya bukan setelan pengguna. `DEFAULT_BACK_CAMERA` memilih kamera
 * LOGIS, dan pada HP multi-lensa modern rentang zoom kamera logis itu
 * dimulai dari lensa ultra-wide. CameraX tidak memaksa rasio apa pun,
 * jadi HP yang rentangnya mulai 0,5x akan terbuka di ultra-wide.
 *
 * Lensa ultra-wide punya jarak fokus minimum yang jauh lebih panjang.
 * QRIS yang dipegang 20 cm berada DI DALAM jarak itu — jadi bukan
 * "susah fokus", melainkan tidak bisa fokus sama sekali. Ditambah
 * QR-nya jadi kecil di bingkai karena sudut pandangnya lebar.
 *
 * Nilainya dijepit ke rentang yang didukung: ada HP yang minimumnya
 * justru di atas 1,0x, dan memaksa 1,0x di situ akan gagal.
 */
private fun paksaLensaUtama(camera: Camera) {
    val zoom = camera.cameraInfo.zoomState.value
    val target = if (zoom != null) {
        1.0f.coerceIn(zoom.minZoomRatio, zoom.maxZoomRatio)
    } else {
        1.0f
    }
    Log.i("CameraPreviewScreen",
        "rentang zoom ${zoom?.minZoomRatio}..${zoom?.maxZoomRatio}, " +
            "dipakai $target")
    camera.cameraControl.setZoomRatio(target)
}

/**
 * Minta fokus ke titik tengah bingkai panduan.
 *
 * Fokus otomatis berkelanjutan milik CameraX menilai seluruh bidang,
 * dan pada meja yang ramai ia sering mengunci ke latar belakang alih-alih
 * ke kertas stikernya. Menunjuk titik tengah membuatnya menilai bagian
 * yang memang sedang dibidik pengguna.
 */
private fun fokusTengah(camera: Camera, previewView: PreviewView) {
    if (previewView.width == 0 || previewView.height == 0) return
    val titik = previewView.meteringPointFactory.createPoint(
        previewView.width / 2f, previewView.height / 2f)
    val aksi = FocusMeteringAction.Builder(titik, FocusMeteringAction.FLAG_AF)
        // Dibiarkan mengulang: QRIS dipindai sambil tangan bergerak, dan
        // fokus yang terkunci sekali akan meleset begitu jaraknya berubah.
        .setAutoCancelDuration(3, TimeUnit.SECONDS)
        .build()
    camera.cameraControl.startFocusAndMetering(aksi)
}

/** Ketuk layar untuk memfokuskan ulang — jalan keluar kalau tetap buram. */
private fun aktifkanKetukUntukFokus(previewView: PreviewView, camera: Camera) {
    previewView.setOnTouchListener { view, event ->
        if (event.action == android.view.MotionEvent.ACTION_UP) {
            val titik = previewView.meteringPointFactory
                .createPoint(event.x, event.y)
            camera.cameraControl.startFocusAndMetering(
                FocusMeteringAction.Builder(titik, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build())
            view.performClick()
        }
        true
    }
}


@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewScreen(
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasDetected by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val barcodeScannerOptions = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(executor) { imageProxy ->
                                if (hasDetected) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty() && !hasDetected) {
                                                val payload = barcodes.first().rawValue
                                                if (payload != null) {
                                                    hasDetected = true
                                                    // Stop camera immediately to prevent multiple scans
                                                    cameraProvider.unbindAll()
                                                    onQrCodeScanned(payload)
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                        paksaLensaUtama(camera)
                        previewView.post { fokusTengah(camera, previewView) }
                        aktifkanKetukUntukFokus(previewView, camera)
                    } catch (exc: Exception) {
                        Log.e("CameraPreviewScreen", "Use case binding failed", exc)
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Bingkai panduan, plus area di luarnya yang diredupkan.
        //
        // Redupnya bukan hiasan: tanpa itu tidak ada yang memberi tahu
        // pengguna seberapa dekat QR harus dibawa. Bingkai yang terisi
        // penuh adalah jarak yang benar, dan itu terbaca tanpa kalimat.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sisi = size.width * 0.7f
            val kiri = (size.width - sisi) / 2
            val atas = (size.height - sisi) / 2
            val redup = Color.Black.copy(alpha = 0.45f)

            drawRect(redup, size = Size(size.width, atas))
            drawRect(redup, topLeft = Offset(0f, atas + sisi),
                     size = Size(size.width, size.height - atas - sisi))
            drawRect(redup, topLeft = Offset(0f, atas), size = Size(kiri, sisi))
            drawRect(redup, topLeft = Offset(kiri + sisi, atas),
                     size = Size(size.width - kiri - sisi, sisi))

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(kiri, atas),
                size = Size(sisi, sisi),
                cornerRadius = CornerRadius(16f, 16f),
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // Petunjuk ditaruh relatif terhadap DASAR layar, bukan digeser
        // sekian dp dari tengah. Pergeseran tetap terlempar keluar layar
        // di HP kecil, dan posisinya ikut berubah-ubah antar perangkat.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Arahkan ke kode QRIS", color = Color.White)
            Text(
                "Isi penuh bingkai — sekitar 15-25 cm. Ketuk layar kalau buram.",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, start = 24.dp, end = 24.dp)
            )
        }
    }
}
