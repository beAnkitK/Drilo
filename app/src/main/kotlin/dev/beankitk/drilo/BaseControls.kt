package dev.beankitk.drilo

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import dev.beankitk.drilo.ui.icon.Icons

@Composable
fun ShaderScreenScaffold(
    caption: String,
    clipImage: Boolean,
    clipPreviewPane: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewModifier: Modifier = Modifier,
    @DrawableRes initialSampleImage: Int = R.drawable.sample_image,
    topBarActions: @Composable RowScope.() -> Unit = {},
    fabPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    controlsContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val contentInsets = WindowInsets.systemBars
    var imagePainter: Painter? by remember { mutableStateOf(null) }
    var expanded by remember { mutableStateOf(false) }
    val transition = updateTransition(expanded, label = "expanded_transition")

    LookaheadScope {
        Scaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                ShaderTopBar(
                    onBackBtnClick = onBackClick,
                    previewExpanded = expanded,
                    transition = transition,
                    onExpandBtnClick = { expanded = !expanded },
                    containsImage = imagePainter != null,
                    onResetBtnClick = { imagePainter = null },
                    scrollBehavior = scrollBehavior,
                    topBarActions = topBarActions,
                )
            },
            floatingActionButton = {
                ImagePickerFAB(
                    isFabExpanded = imagePainter == null,
                    onImagePicked = { image -> imagePainter = BitmapPainter(image) },
                )
            },
            floatingActionButtonPosition = fabPosition,
            containerColor = containerColor,
            contentColor = contentColor,
            contentWindowInsets = WindowInsets(0)
        ) { insets ->
            ShaderContent(
                caption = caption,
                clipImage = clipImage,
                clipPreviewPane = clipPreviewPane,
                lookaheadScope = this@LookaheadScope,
                contentInsets = insets,
                initialSampleImage = initialSampleImage,
                imagePainter = imagePainter,
                previewExpanded = expanded,
                transition = transition,
                previewModifier = previewModifier,
                controlsContent = controlsContent,
            )
        }
    }
}

@Composable
private fun ShaderTopBar(
    onBackBtnClick: () -> Unit,
    previewExpanded: Boolean,
    transition: Transition<Boolean>,
    onExpandBtnClick: () -> Unit,
    containsImage: Boolean,
    onResetBtnClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    topBarActions: @Composable RowScope.() -> Unit,
) {
    val colors: TopAppBarColors = if (previewExpanded) {
        TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        )
    } else {
        TopAppBarDefaults.topAppBarColors()
    }

    TopAppBar(
        modifier = Modifier.padding(horizontal = 4.dp),
        title = {
            transition.AnimatedVisibility(
                visible = { !it },
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 })
            ) {
                Text("Ripple Shader")
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackBtnClick) {
                Icon(Icons.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            AnimatedVisibility(containsImage) {
                IconButton(onClick = onResetBtnClick) {
                    Icon(Icons.Reset, contentDescription = "Reset Image")
                }
            }
            IconButton(onClick = onExpandBtnClick) {
                Icon(Icons.Expand, contentDescription = "Expand Image")
            }
            topBarActions()
        },
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun ImagePickerFAB(
    isFabExpanded: Boolean,
    onImagePicked: (ImageBitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val fabInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }
            }.getOrNull()?.let(onImagePicked)
        }
    }

    ExtendedFloatingActionButton(
        modifier = modifier.windowInsetsPadding(fabInsets),
        onClick = {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        expanded = isFabExpanded,
        interactionSource = interactionSource,
        icon = { Icon(Icons.Add, contentDescription = "Pick Image") },
        text = { Text("Pick Image") }
    )
}

@Composable
fun ShaderContent(
    caption: String,
    clipImage: Boolean,
    clipPreviewPane: Boolean,
    lookaheadScope: LookaheadScope,
    contentInsets: PaddingValues,
    @DrawableRes initialSampleImage: Int,
    imagePainter: Painter?,
    previewExpanded: Boolean,
    transition: Transition<Boolean>,
    previewModifier: Modifier = Modifier,
    controlsContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val previewShape = MaterialTheme.shapes.medium
    val topPadding by transition.animateDp(label = "top_padding") { expanded ->
        if (expanded) 0.dp else contentInsets.calculateTopPadding()
    }

    Box(
        Modifier
            .padding(top = topPadding)
            .fillMaxSize()
            .animateContentSize(spring(0.7f, 120f), Alignment.Center)
    ) {
        val boxScope = this
        val previewControlModifier = Modifier.align(Alignment.BottomCenter)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            with(boxScope) {
                PreviewPane(
                    clipImage = clipImage,
                    clipPreviewPane = clipPreviewPane,
                    lookaheadScope = lookaheadScope,
                    previewExpanded = previewExpanded,
                    controlsPresent = controlsContent != null,
                    previewShape = previewShape,
                    previewModifier = previewModifier,
                    imagePainter = imagePainter,
                    initialSampleImage = initialSampleImage,
                )

                if (controlsContent != null) {
                    transition.AnimatedVisibility(
                        visible = { !it },
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = previewControlModifier
                    ) {
                        PreviewControls(
                            caption = caption,
                            clipImage = clipImage,
                            clipPreviewPane = clipPreviewPane,
                            content = controlsContent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.PreviewPane(
    clipImage: Boolean,
    clipPreviewPane: Boolean,
    lookaheadScope: LookaheadScope,
    previewExpanded: Boolean,
    controlsPresent: Boolean,
    previewShape: androidx.compose.ui.graphics.Shape,
    previewModifier: Modifier,
    imagePainter: Painter?,
    initialSampleImage: Int,
) {
    val boxModifier = Modifier
        .zIndex(10f)
        .animateBounds(
            lookaheadScope,
            modifier = if (!previewExpanded && controlsPresent) {
                Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)
            } else {
                Modifier
            }
        )
        .then(
            if (previewExpanded) {
                Modifier.matchParentSize()
            } else {
                Modifier
                    .fillMaxWidth(if (controlsPresent) 1f else 0.75f)
                    .height(400.dp)
            }
        )
        .then(if (controlsPresent) Modifier else Modifier.align(Alignment.Center))
        .background(MaterialTheme.colorScheme.surfaceVariant, previewShape)
        .then(if (clipPreviewPane) Modifier.clip(previewShape) else Modifier)

    Box(boxModifier) {
        Image(
            painter = imagePainter ?: painterResource(initialSampleImage),
            contentDescription = "Sample Image",
            modifier = Modifier
                .fillMaxSize()
                .then(previewModifier)
                .then(if (clipImage) Modifier.clip(previewShape) else Modifier),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun PreviewControls(
    caption: String,
    clipImage: Boolean,
    clipPreviewPane: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PillText(caption)
            PillText(if (clipImage) "Image Clipped" else "Image Not Clipped")
            PillText(if (clipPreviewPane) "Preview Clipped" else "Preview Not Clipped")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            content = content
        )
    }
}

@Composable
private fun PillText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    )
}

@Composable
fun ControlsColumn(
    scrollState: ScrollState,
    elapsedTime: Animatable<Float, *>,
    amplitude: Float,
    frequency: Float,
    decay: Float,
    speed: Float,
    onAmplitudeChange: (Float) -> Unit,
    onFrequencyChange: (Float) -> Unit,
    onDecayChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    coroutineScope: CoroutineScope
) {
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SliderRow(
            title = "Elapsed Time",
            value = elapsedTime.value,
            valueText = String.format("%.1f", elapsedTime.value),
            onValueChange = { coroutineScope.launch { elapsedTime.snapTo(it) } },
            valueRange = 0f..2f
        )
        SliderRow(
            title = "Amplitude",
            value = amplitude,
            valueText = String.format("%.0f", amplitude),
            onValueChange = onAmplitudeChange,
            valueRange = 0f..100f
        )
        SliderRow(
            title = "Frequency",
            value = frequency,
            valueText = String.format("%.0f", frequency),
            onValueChange = onFrequencyChange,
            valueRange = 0f..30f
        )
        SliderRow(
            title = "Decay",
            value = decay,
            valueText = String.format("%.0f", decay),
            onValueChange = onDecayChange,
            valueRange = 0f..20f
        )
        SliderRow(
            title = "Speed",
            value = speed,
            valueText = String.format("%.0f", speed),
            onValueChange = onSpeedChange,
            valueRange = 100f..2000f
        )
    }
}

@Composable
fun SliderRow(
    title: String,
    value: Float,
    valueText: String,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title)
            Text(text = valueText)
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}