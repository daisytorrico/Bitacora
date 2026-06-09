package com.catedra.bitacora.core.ui.components.travel

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun PointStoriesDialog(
    imageUrls: List<String>,
    title: String = "",
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (imageUrls.isEmpty()) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        PointStoriesContent(
            imageUrls = imageUrls,
            title = title,
            initialIndex = initialIndex,
            onClose = onDismiss
        )
    }
}

@Composable
private fun PointStoriesContent(
    imageUrls: List<String>,
    title: String,
    initialIndex: Int,
    onClose: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(initialIndex) }
    val count = imageUrls.size
    val durationPerImage = 10000L
    val scope = rememberCoroutineScope()

    val progress = remember { Animatable(0f) }
    var isPaused by remember { mutableStateOf(false) }

    val animScale = remember { Animatable(1f) }
    val animOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val isZooming by remember { derivedStateOf { animScale.value > 1.05f } }
    val effectivePaused by remember { derivedStateOf { isPaused || isZooming } }

    // Avance automatico
    LaunchedEffect(currentIndex, effectivePaused) {
        if (!effectivePaused) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = ((1f - progress.value) * durationPerImage).toInt(),
                    easing = LinearEasing
                )
            )
            if (currentIndex < count - 1) {
                currentIndex++
                progress.snapTo(0f)
            } else {
                onClose()
            }
        } else {
            progress.stop()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Zoom y pan
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (animScale.value * zoom).coerceIn(1f, 10f)
                    val maxX = (size.width * (newScale - 1) / 2).coerceAtLeast(0f)
                    val maxY = (size.height * (newScale - 1) / 2).coerceAtLeast(0f)
                    
                    scope.launch {
                        animScale.snapTo(newScale)
                        val newOffset = Offset(
                            x = (animOffset.value.x + pan.x).coerceIn(-maxX, maxX),
                            y = (animOffset.value.y + pan.y).coerceIn(-maxY, maxY)
                        )
                        animOffset.snapTo(newOffset)
                    }
                }
            }
            // Pan con un dedo limitado o Swipe vertical para cerrar
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {},
                    onDrag = { change, dragAmount ->
                        if (isZooming) {
                            change.consume()
                            val s = animScale.value
                            val maxX = (size.width * (s - 1) / 2).coerceAtLeast(0f)
                            val maxY = (size.height * (s - 1) / 2).coerceAtLeast(0f)
                            
                            scope.launch {
                                val newOffset = Offset(
                                    x = (animOffset.value.x + dragAmount.x).coerceIn(-maxX, maxX),
                                    y = (animOffset.value.y + dragAmount.y).coerceIn(-maxY, maxY)
                                )
                                animOffset.snapTo(newOffset)
                            }
                        } else {
                            if (dragAmount.y > 100f || dragAmount.y < -100f) {
                                onClose()
                            }
                        }
                    }
                )
            }
            // Tap izquierda/derecha + pausa + reset zoom
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            animScale.animateTo(1f, spring())
                            animOffset.animateTo(Offset.Zero, spring())
                        }
                    },
                    onTap = { offset ->
                        if (isZooming) {
                            // Un toque cuando esta con zoom permite continuar
                            scope.launch {
                                animScale.animateTo(1f, spring())
                                animOffset.animateTo(Offset.Zero, spring())
                            }
                        } else {
                            // Navegacion normal
                            if (offset.x < size.width / 2) {
                                if (currentIndex > 0) {
                                    currentIndex--
                                    scope.launch { progress.snapTo(0f) }
                                }
                            } else {
                                if (currentIndex < count - 1) {
                                    currentIndex++
                                    scope.launch { progress.snapTo(0f) }
                                } else {
                                    onClose()
                                }
                            }
                        }
                    },
                    onPress = {
                        isPaused = true
                        try {
                            awaitRelease()
                        } finally {
                            isPaused = false
                            // Reanuda solo si no hay zoom
                            if (!isZooming && progress.value < 1f) {
                                scope.launch {
                                    progress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = ((1f - progress.value) * durationPerImage).toInt(),
                                            easing = LinearEasing
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            }
    ) {
        val currentUrl = imageUrls[currentIndex]

        AsyncImage(
            model = currentUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(40.dp)
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

        AsyncImage(
            model = currentUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .graphicsLayer(
                    scaleX = animScale.value,
                    scaleY = animScale.value,
                    translationX = animOffset.value.x,
                    translationY = animOffset.value.y
                )
        )

        // Barritas indicadoras
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0 until count) {
                val stepProgress = when {
                    i < currentIndex -> 1f
                    i == currentIndex -> progress.value
                    else -> 0f
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(Color.White.copy(alpha = 0.4f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stepProgress)
                            .fillMaxHeight()
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }

        // Título
        if (title.isNotEmpty() && !isZooming) {
            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
