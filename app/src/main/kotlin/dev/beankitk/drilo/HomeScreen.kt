package dev.beankitk.drilo

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    clipImage: Boolean,
    clipPreviewPane: Boolean,
    onClipImageChange: (Boolean) -> Unit,
    onClipPreviewPaneChange: (Boolean) -> Unit,
    onNavigateToShader: (caption: String, shader: String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Add tile mode or maxSampleOffset in Runtime Shader",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BugReportCard(
                title = "Actual behaviour",
                containerColor = MaterialTheme.colorScheme.errorContainer,
                onClick = { onNavigateToShader("Actual", actualRippleShader) }
            )

            BugReportCard(
                title = "Expected behaviour",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onNavigateToShader("Expected", expectedRippleShader) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            OptionCard(
                title = "Clip Image",
                checked = clipImage,
                onCheckedChange = onClipImageChange
            )

            OptionCard(
                title = "Clip Preview Pane",
                checked = clipPreviewPane,
                onCheckedChange = onClipPreviewPaneChange
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Guide",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = guidetext,
                modifier = Modifier.verticalScroll(scrollState),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun BugReportCard(
    title: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun OptionCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val toggle = { onCheckedChange(!checked) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        onClick = toggle,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            androidx.compose.material3.Switch(
                checked = checked,
                onCheckedChange = null
            )
        }
    }
}

private val guidetext = """
This sample demonstrates a limitation in RuntimeShader when sampling outside image bounds.

Recommended to read the README.md for more detailed exaplanation. Here is a summarised guide -

A ripple shader is applied to the image, simulating waves on water. When the ripple propagates, it should naturally flow beyond the image edges and reveal the underlying surface (like water revealing ground).

However, due to missing TileMode.Decal or maxSampleOffset support:
- Pixels outside bounds are stretched using edge color
- Ripple gets cut off when the preview is clipped
- Wave motion appears discontinuous and unrealistic

How to observe:

1. Open both "Actual behaviour" and "Expected behaviour"
2. Trigger ripple via touch or elapsed time slider
3. Focus on edges and corner of the image during wave propagation
4. Set elapsed time between 0.5 – 1.0 for clearer effect

Try toggling (affects only the Actual Behavior):

- Clip Image ->

Clips the image to its shape. During ripple propagation, this restricts distortion at the corners, so corner regions do not stretch. However, edge pixels still stretch along the image boundary. Without this enabled, the entire rectangular boundary (including corners) exhibits edge color stretching.

- Clip Preview Pane ->

Restricts the ripple to propagate strictly within the preview container. This removes visible stretching outside the container bounds, but the distortion inside still relies on edge clamping. As the wave propagates and recedes, internal stretching artifacts remain observable.

Expected: Smooth wave flow with transparency outside bounds.
Actual: Edge stretching or clipped wave.

This highlights the need for proper out-of-bounds sampling support in RuntimeShader.
""".trimIndent()