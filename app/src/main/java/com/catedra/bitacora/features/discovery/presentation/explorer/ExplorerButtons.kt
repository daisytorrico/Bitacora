package com.catedra.bitacora.features.discovery.presentation.explorer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catedra.bitacora.core.ui.theme.*

@Composable
fun BotonDescubrirViajes(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, animationSpec = tween(100), label = "scale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(AzulOscuro)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centroTierra = Offset(size.width * 0.90f, size.height * 0.5f)
            val radioTierra = size.height * 0.38f

            val caminoMascara = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(centroTierra, radioTierra))
            }

            clipPath(caminoMascara) {
                drawCircle(color = AzulClaro, radius = radioTierra, center = centroTierra)
                drawOval(
                    color = VerdeMentaTexto,
                    topLeft = Offset(centroTierra.x - (radioTierra * 0.8f), centroTierra.y - (radioTierra * 0.5f)),
                    size = androidx.compose.ui.geometry.Size(radioTierra * 1.1f, radioTierra * 1.1f)
                )
                drawOval(
                    color = Blanco.copy(alpha = 0.5f),
                    topLeft = Offset(centroTierra.x - (radioTierra * 0.4f), centroTierra.y - (radioTierra * 0.2f)),
                    size = androidx.compose.ui.geometry.Size(radioTierra * 1.2f, radioTierra * 0.3f)
                )
            }
            drawCircle(color = AzulProfundo.copy(alpha = 0.4f), radius = radioTierra, center = centroTierra)
        }

        Text(
            text = "DESCUBRIR VIAJES",
            color = Blanco,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp)
        )
    }
}

@Composable
fun BotonAventuraSeguidos(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, animationSpec = tween(100), label = "scale")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Naranja)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centroBrujula = Offset(size.width * 0.90f, size.height * 0.5f)
            val radioBrujula = size.height * 0.35f

            drawCircle(
                color = Blanco,
                radius = radioBrujula,
                center = centroBrujula,
                style = Stroke(width = 2.dp.toPx())
            )

            val pathNorte = Path().apply {
                moveTo(centroBrujula.x + 8.dp.toPx(), centroBrujula.y - 12.dp.toPx())
                lineTo(centroBrujula.x + 4.dp.toPx(), centroBrujula.y + 4.dp.toPx())
                lineTo(centroBrujula.x - 4.dp.toPx(), centroBrujula.y - 4.dp.toPx())
                close()
            }
            drawPath(path = pathNorte, color = RojoPin)

            val pathSur = Path().apply {
                moveTo(centroBrujula.x - 8.dp.toPx(), centroBrujula.y + 12.dp.toPx())
                lineTo(centroBrujula.x + 4.dp.toPx(), centroBrujula.y + 4.dp.toPx())
                lineTo(centroBrujula.x - 4.dp.toPx(), centroBrujula.y - 4.dp.toPx())
                close()
            }
            drawPath(path = pathSur, color = GrisOscuro)

            drawCircle(color = Blanco, radius = 2.5.dp.toPx(), center = centroBrujula)
            drawCircle(color = AzulOscuro, radius = 3.dp.toPx(), center = Offset(centroBrujula.x + radioBrujula + 6.dp.toPx(), centroBrujula.y - 4.dp.toPx()))
            drawCircle(color = AzulProfundo, radius = 3.dp.toPx(), center = Offset(centroBrujula.x - radioBrujula - 6.dp.toPx(), centroBrujula.y + 4.dp.toPx()))
        }

        Text(
            text = "VIAJES SEGUIDOS",
            color = NegroAzulado,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp)
        )
    }
}
