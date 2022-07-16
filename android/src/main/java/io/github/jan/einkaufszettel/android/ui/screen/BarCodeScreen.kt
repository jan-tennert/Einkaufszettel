package io.github.jan.einkaufszettel.android.ui.screen

import android.Manifest
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import com.ramcosta.composedestinations.annotation.Destination
import io.github.jan.einkaufszettel.android.analyser.BarCodeAnalyser
import io.github.jan.einkaufszettel.android.components.ProductInfoCard
import io.github.jan.einkaufszettel.android.ui.dialog.SelectShopDialog
import io.github.jan.einkaufszettel.android.viewmodel.ProductViewModel
import io.github.jan.einkaufszettel.common.event.UIEvent
import io.github.jan.einkaufszettel.common.icons.FlashOff
import io.github.jan.einkaufszettel.common.icons.FlashOn
import io.github.jan.einkaufszettel.common.icons.MIcon
import io.github.jan.einkaufszettel.common.repositories.barcode.BarcodeProduct
import io.github.jan.einkaufszettel.common.repositories.barcode.BarcodeProductRepository
import io.github.jan.einkaufszettel.common.repositories.product.Shop
import io.github.jan.einkaufszettel.common.ui.dialog.CreateProductDialog
import io.github.jan.supacompose.auth.auth
import org.koin.androidx.compose.inject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
@Destination
fun BarCodeScreen(viewModel: ProductViewModel) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    var barCodeVal by remember { mutableStateOf("") }
    var notFound by remember { mutableStateOf(false) }
    var showCreateScreen by remember { mutableStateOf(false) }
    val productInfoRepository: BarcodeProductRepository by inject()
    val product = remember { mutableStateOf<BarcodeProduct?>(null) }
    var shop by remember { mutableStateOf<Shop?>(null) }
    SideEffect {
        if (!cameraPermissionState.hasPermission) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    if (cameraPermissionState.hasPermission && barCodeVal.isBlank()) {
        CameraPreview {
            barCodeVal = it
            true
        }
    } else if(barCodeVal.isNotBlank() && !showCreateScreen) {
        LaunchedEffect(Unit) {
            product.value = try {
                productInfoRepository.getProductByBarCode(barCodeVal) ?: kotlin.run {
                    notFound = true
                    null
                }
            } catch(e: Exception) {
                e.printStackTrace()
                viewModel.pushEvent(UIEvent.AlertEvent("Konnte Barcode nicht auswerten. Bitte überprüfe deine Internetverbindung."))
                barCodeVal = ""
                null
            }
        }
        Dialog(onDismissRequest = { barCodeVal = ""; product.value = null }) {
            Box(modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.7f), contentAlignment = Alignment.Center) {
                if(product.value == null) {
                    CircularProgressIndicator()
                } else {
                    Card {
                        ProductInfoCard(product.value!!) {
                            showCreateScreen = true
                        }
                    }
                }
            }
        }
    }
    if(notFound) {
        AlertDialog(onDismissRequest = { barCodeVal = ""; notFound = false }, confirmButton = {
            Button(onClick = { barCodeVal = ""; notFound = false }) {
                Text("Ok")
            }
        }, text = {
            Text("Kein Produkt mit diesem Barcode gefunden")
        })
    }
    if(showCreateScreen && shop == null) {
        SelectShopDialog(onDismissRequest = { showCreateScreen = false }, viewModel = viewModel) {
            shop = it
        }
    }
    if(showCreateScreen && shop != null) {
        val context = LocalContext.current
        CreateProductDialog(shop = shop!!.id, disable = {
            product.value = null
            showCreateScreen = false
            barCodeVal = ""
            shop = null
        }, value = product.value!!.name, onDone = { viewModel.createProductInShop(context, shop!!.id, it, viewModel.supabaseClient.auth.currentSession.value!!.user!!.id)})
    }
}

@Composable
fun CameraPreview(onBarCode: (String) -> Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }
    var enableTorch by remember { mutableStateOf(false) }

    AndroidView(
        factory = { AndroidViewContext ->
            PreviewView(AndroidViewContext).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = Modifier
            .fillMaxSize(),
        update = { previewView ->
            val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val barcodeAnalyser = BarCodeAnalyser { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let {
                        if (onBarCode(it)) {
                            cameraProvider.unbindAll()
                        }
                    }

                }
                val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, barcodeAnalyser)
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    ).also {
                        if(it.cameraInfo.hasFlashUnit()) {
                            it.cameraControl.enableTorch(enableTorch)
                        }
                    }
                } catch (e: Exception) {
                    Log.d("TAG", "CameraPreview: ${e.localizedMessage}")
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(end = 20.dp, bottom = 20.dp), contentAlignment = Alignment.BottomEnd) {
        Icon(if(!enableTorch) MIcon.FlashOff else MIcon.FlashOn, "", modifier = Modifier.clickable {
            enableTorch = !enableTorch
        })
    }
}





