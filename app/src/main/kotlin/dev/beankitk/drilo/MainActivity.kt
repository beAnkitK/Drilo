package dev.beankitk.drilo

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import dev.beankitk.drilo.ui.theme.DriloTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DriloTheme {
                AppNavigation()
            }
        }
    }
}

sealed interface Screen {
    data object Home : Screen
    data class ShaderPreview(
        val caption: String,
        val shaderSrc: String
    ) : Screen
}

@Composable
fun AppNavigation() {
    val activity = LocalActivity.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var clipImage by remember { mutableStateOf(false) }
    var clipPreviewPane by remember { mutableStateOf(false) }

    BackHandler(currentScreen is Screen.Home) { activity?.finish() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val isForward = targetState is Screen.ShaderPreview

                if (isForward) {
                    fadeIn() + slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { it / 2 }
                    ) togetherWith fadeOut() + slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { -it / 3 }
                    )
                } else {
                    fadeIn() + slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { -it / 3 }
                    ) togetherWith fadeOut() + slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { it / 2 }
                    )
                }
            },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                is Screen.Home -> {
                    HomeScreen(
                        clipImage = clipImage,
                        clipPreviewPane = clipPreviewPane,
                        onClipImageChange = { clipImage = it },
                        onClipPreviewPaneChange = { clipPreviewPane = it },
                        onNavigateToShader = { caption, shadersrc ->
                            currentScreen = Screen.ShaderPreview(caption, shadersrc)
                        }
                    )
                }

                is Screen.ShaderPreview -> {
                    ShaderScreen(
                        caption = screen.caption,
                        shaderSrc = screen.shaderSrc,
                        clipImage = clipImage,
                        clipPreviewPane = clipPreviewPane,
                        onBackClick = {
                            currentScreen = Screen.Home
                        }
                    )
                }
            }
        }
    }
}