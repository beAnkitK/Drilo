package dev.beankitk.drilo

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch

@Composable
fun ShaderScreen(
    caption: String,
    shaderSrc: String,
    clipImage: Boolean,
    clipPreviewPane: Boolean,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val rippleShader = remember { RuntimeShader(shaderSrc) }

    var duration by remember { mutableStateOf(1.5f) }
    var size by remember { mutableStateOf(Size.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }

    var elapsedTime = remember { Animatable(0f) }
    var amplitude by remember { mutableStateOf(65f) }
    var frequency by remember { mutableStateOf(15f) }
    var decay by remember { mutableStateOf(2f) }
    var speed by remember { mutableStateOf(1500f) }

    BackHandler(true) { onBackClick() }

    ShaderScreenScaffold(
        caption = caption,
        clipImage = clipImage,
        clipPreviewPane = clipPreviewPane,
        onBackClick = onBackClick,
        previewModifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset -> 
                        origin = offset
                        coroutineScope.launch {
                            elapsedTime.animateTo(1.5f, tween(1500))
                            elapsedTime.snapTo(0f)
                        }
                    }
                )
            }
            .onSizeChanged {
                size = it.toSize()
                origin = Offset(size.width / 2, size.height / 2)
            }
            .then(
                if (elapsedTime.value > 0 && elapsedTime.value < duration)
                    Modifier.graphicsLayer {
                        with(rippleShader) {
                            setFloatUniform("size", size.width, size.height)
                            setFloatUniform("origin", origin.x, origin.y)
                            setFloatUniform("elapsedTime", elapsedTime.value)
                            setFloatUniform("amplitude", amplitude)
                            setFloatUniform("frequency", frequency)
                            setFloatUniform("decay", decay)
                            setFloatUniform("speed", speed)
                        }

                        //compositingStrategy = CompositingStrategy.Offscreen
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(rippleShader, "layer")
                            .asComposeRenderEffect()
                    }
                else Modifier
            ),
        controlsContent = {
            ControlsColumn(
                scrollState = scrollState,
                elapsedTime = elapsedTime,
                amplitude = amplitude,
                frequency = frequency,
                decay = decay,
                speed = speed,
                onAmplitudeChange = { amplitude = it },
                onFrequencyChange = { frequency = it },
                onDecayChange = { decay = it },
                onSpeedChange = { speed = it },
                coroutineScope = coroutineScope
            )
        }
    )
}