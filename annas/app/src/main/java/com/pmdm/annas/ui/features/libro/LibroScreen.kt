package com.pmdm.annas.ui.features.libro

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.pmdm.annas.model.Libro
import com.pmdm.annas.ui.features.UIStateEnum
import com.pmdm.annas.ui.features.components.ErrorScreen
import com.pmdm.annas.ui.features.components.PantallaCarga
import com.pmdm.annas.ui.features.libro.components.MostrarLibro
import com.pmdm.annas.utils.DownloadEvents
import com.pmdm.annas.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.CancellationException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun LibroScreen(
    libro: Libro,
    descripcion: String,
    uiStateEnum: UIStateEnum?,
    enlacesServidor: List<String>,
    onReintentar: () -> Unit,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    var showWebView by remember { mutableStateOf(false) }
    var currentDownloadUrl by remember { mutableStateOf("") }
    var currentFileName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val notificationHelper = remember { NotificationHelper(context) }
    val okHttpClient = remember { OkHttpClient() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            onNavigateBack()
            scope.launch {
                downloadFileWithNotification(
                    context,
                    okHttpClient,
                    currentDownloadUrl,
                    it,
                    currentFileName,
                    notificationHelper
                )
            }
        }
    }

    when (uiStateEnum) {
        UIStateEnum.CARGANDO -> PantallaCarga()

        UIStateEnum.CARGADO -> MostrarLibro(
            portada = libro.portada,
            titulo = libro.titulo,
            autor = libro.autor,
            descripcion = descripcion,
            enlacesServidor = enlacesServidor,
            idioma = libro.idioma,
            formato = libro.formato,
            tamano = libro.tamano,
            onDownloadClick = { url ->
                currentDownloadUrl = url
                showWebView = true
            },
            enlaceKey = libro.enlace,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope
        )

        else -> ErrorScreen(
            mensaje = "Error al abrir el libro",
            onReintentar = onReintentar
        )
    }

    if (showWebView) {
        ModalBottomSheet(
            onDismissRequest = { showWebView = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                DownloadWebView(
                    url = currentDownloadUrl,
                    onDownloadStart = { url, _, contentDisposition, mimetype, _ ->
                        currentDownloadUrl = url
                        currentFileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                        createFileLauncher.launch(currentFileName)
                        showWebView = false
                    }
                )

                IconButton(
                    onClick = { showWebView = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DownloadWebView(url: String, onDownloadStart: (String, String, String, String, Long) -> Unit) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        return false
                    }
                }

                setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength)
                }

                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

suspend fun downloadFileWithNotification(
    context: Context,
    client: OkHttpClient,
    url: String,
    destinationUri: Uri,
    fileName: String,
    notificationHelper: NotificationHelper
) {
    coroutineScope {
        val cancelJob = launch {
            DownloadEvents.cancelFlow.first()
            this@coroutineScope.cancel("Descarga cancelada")
        }

        withContext(Dispatchers.IO) {
            try {
                notificationHelper.showProgressNotification(fileName, 0)

                val response = client.newCall(Request.Builder().url(url).build()).execute()

                response.use { resp ->
                    if (!resp.isSuccessful) {
                        notificationHelper.showErrorNotification(fileName)
                        return@withContext
                    }

                    val body = resp.body
                    val totalBytes = body.contentLength()

                    context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                        val input = body.byteStream()
                        val buffer = ByteArray(8 * 1024)
                        var bytes: Int
                        var downloaded = 0L

                        while (input.read(buffer).also { bytes = it } != -1) {
                            if (!isActive) throw CancellationException()

                            output.write(buffer, 0, bytes)
                            downloaded += bytes

                            if (totalBytes > 0) {
                                val progress = ((downloaded * 100) / totalBytes).toInt()
                                notificationHelper.showProgressNotification(fileName, progress)
                            }
                        }
                    }
                    cancelJob.cancel()
                    notificationHelper.showCompletedNotification(fileName, destinationUri)
                }
            } catch (e: Exception) {
                cancelJob.cancel()
                if (e is CancellationException) {
                    notificationHelper.cancelNotification()
                } else {
                    notificationHelper.showErrorNotification(fileName)
                }
            }
        }
    }
}
