package dev.beankitk.drilo.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val _add: ImageVector by lazy {
    ImageVector.Builder(
        name = "Add",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.Black)
        ) {
            moveTo(19.0f, 13.0f)
            horizontalLineToRelative(-6.0f)
            verticalLineToRelative(6.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(-6.0f)
            horizontalLineTo(5.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(6.0f)
            verticalLineTo(5.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(6.0f)
            horizontalLineToRelative(6.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }.build()
}