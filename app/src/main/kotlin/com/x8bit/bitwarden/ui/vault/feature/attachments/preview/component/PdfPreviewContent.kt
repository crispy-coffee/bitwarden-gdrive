package com.x8bit.bitwarden.ui.vault.feature.attachments.preview.component

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

private const val MAX_ZOOM_SCALE = 5f
private const val MIN_ZOOM_SCALE = 1f

private val OffsetSaver: Saver<Offset, *> = listSaver(
    save = { listOf(it.x, it.y) },
    restore = { Offset(it[0], it[1]) }
)

/**
 * Displays a preview of a PDF [file].
 */
@Composable
fun PdfPreviewContent(
    file: File,
    onLoaded: () -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by rememberSaveable { mutableFloatStateOf(MIN_ZOOM_SCALE) }
    var offset by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }

    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    val rendererMutex = remember { Mutex() }

    DisposableEffect(file) {
        val parcelFileDescriptor = try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open PDF file")
            onError()
            null
        }

        if (parcelFileDescriptor != null) {
            try {
                val newRenderer = PdfRenderer(parcelFileDescriptor)
                renderer = newRenderer
                pageCount = newRenderer.pageCount
                onLoaded()
            } catch (e: Exception) {
                Timber.e(e, "Failed to create PdfRenderer")
                onError()
            }
        }

        onDispose {
            try {
                renderer?.close()
                parcelFileDescriptor?.close()
            } catch (e: Exception) {
                Timber.e(e, "Error closing PdfRenderer")
            }
            renderer = null
        }
    }

    if (renderer != null && pageCount > 0) {
        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            scale = (scale * zoomChange).coerceIn(MIN_ZOOM_SCALE, MAX_ZOOM_SCALE)
            offset += offsetChange
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.05f))
                .transformable(state = state)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(MIN_ZOOM_SCALE, MAX_ZOOM_SCALE)
                        offset = if (scale > MIN_ZOOM_SCALE) offset + pan else Offset.Zero
                    }
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(pageCount) { index ->
                    PdfPage(renderer!!, index, rendererMutex)
                }
            }
        }
    }
}

@Composable
private fun PdfPage(renderer: PdfRenderer, index: Int, mutex: Mutex) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(renderer, index) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                try {
                    val page = renderer.openPage(index)
                    // Use a reasonable density for the bitmap.
                    // Too high might cause OOM, too low looks blurry.
                    val width = page.width * 2
                    val height = page.height * 2
                    val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(newBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap = newBitmap
                    page.close()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to render PDF page %d", index)
                }
            }
        }
    }

    DisposableEffect(index) {
        onDispose {
            bitmap?.recycle()
            bitmap = null
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Page ${index + 1}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .background(Color.White),
            contentScale = ContentScale.FillWidth
        )
    }
}
