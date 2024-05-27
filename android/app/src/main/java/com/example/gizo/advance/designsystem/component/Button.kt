package com.example.gizo.advance.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gizo.advance.R
import com.example.gizo.advance.designsystem.theme.urbanistFontFamily

@Composable
internal fun GizoFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Dp = 16.dp,
    small: Boolean = false,
    colors: ButtonColors = GizoButtonDefaults.filledButtonColors(),
    contentPadding: PaddingValues = GizoButtonDefaults.buttonContentPadding(small = small),
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = if (small) {
            modifier.heightIn(min = GizoButtonDefaults.SmallButtonHeight)
        } else {
            modifier
        },
        enabled = enabled,
        shape = RoundedCornerShape(shape),
        colors = colors,
        contentPadding = contentPadding,
        content = {
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                content()
            }
        }
    )
}

@Composable
internal fun GizoOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    small: Boolean = false,
    shape: Dp,
    border: BorderStroke? = GizoButtonDefaults.outlinedButtonBorder(enabled = enabled),
    colors: ButtonColors = GizoButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = GizoButtonDefaults.buttonContentPadding(small = small),
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = if (small) {
            modifier.heightIn(min = GizoButtonDefaults.SmallButtonHeight)
        } else {
            modifier
        },
        shape = RoundedCornerShape(shape),
        enabled = enabled,
        border = border,
        colors = colors,
        contentPadding = contentPadding,
        content = {
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                content()
            }
        }
    )
}


@Composable
internal fun GizoTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    small: Boolean = false,
    colors: ButtonColors = GizoButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = GizoButtonDefaults.buttonContentPadding(small = small),
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = if (small) {
            modifier.heightIn(min = GizoButtonDefaults.SmallButtonHeight)
        } else {
            modifier
        },
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        content = {
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                content()
            }
        }
    )
}

/**
 * button with default values.
 */
internal object GizoButtonDefaults {
    val SmallButtonHeight = 32.dp
    private const val DisabledButtonContainerAlpha = 0.5f
    const val DisabledButtonContentAlpha = 0.38f
    private val ButtonHorizontalPadding = 24.dp
    private val ButtonHorizontalIconPadding = 16.dp
    private val ButtonVerticalPadding = 8.dp
    private val SmallButtonHorizontalPadding = 16.dp
    private val SmallButtonHorizontalIconPadding = 12.dp
    private val SmallButtonVerticalPadding = 7.dp
    fun buttonContentPadding(
        small: Boolean,
        leadingIcon: Boolean = false,
        trailingIcon: Boolean = false,
    ): PaddingValues {
        return PaddingValues(
            start = when {
                small && leadingIcon -> SmallButtonHorizontalIconPadding
                small -> SmallButtonHorizontalPadding
                leadingIcon -> ButtonHorizontalIconPadding
                else -> ButtonHorizontalPadding
            },
            top = if (small) SmallButtonVerticalPadding else ButtonVerticalPadding,
            end = when {
                small && trailingIcon -> SmallButtonHorizontalIconPadding
                small -> SmallButtonHorizontalPadding
                trailingIcon -> ButtonHorizontalIconPadding
                else -> ButtonHorizontalPadding
            },
            bottom = if (small) SmallButtonVerticalPadding else ButtonVerticalPadding
        )
    }

    @Composable
    fun filledButtonColors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor: Color = MaterialTheme.colorScheme.primary.copy(
            alpha = DisabledButtonContainerAlpha
        ),
        disabledContentColor: Color = MaterialTheme.colorScheme.onBackground.copy(
            alpha = DisabledButtonContentAlpha
        ),
    ) = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    @Composable
    fun outlinedButtonBorder(
        enabled: Boolean,
        width: Dp = 1.dp,
        color: Color = Color.White,
        disabledColor: Color = MaterialTheme.colorScheme.onBackground.copy(
            alpha = DisabledButtonContainerAlpha
        ),
    ): BorderStroke = BorderStroke(
        width = width,
        color = if (enabled) color else disabledColor
    )

    @Composable
    fun outlinedButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.onBackground,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onBackground.copy(
            alpha = DisabledButtonContentAlpha
        ),
    ) = ButtonDefaults.outlinedButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    @Composable
    fun textButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.onBackground,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onBackground.copy(
            alpha = DisabledButtonContentAlpha
        ),
    ) = ButtonDefaults.textButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
}

@Preview
@Composable
fun GizoOutlinedButtonPreview() {
    GizoOutlinedButton(

        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(56.dp),
        onClick = {
        },
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xB2F44336)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0x1AF44336),
            contentColor = Color(0x1AF44336),
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color(0xFFDB4324).copy(
                alpha = GizoButtonDefaults.DisabledButtonContentAlpha
            ),
        ),
        shape = 10.dp,
    ) {
        Text(
            text = "End trip",

            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontFamily = urbanistFontFamily,
                fontSize = 16.sp
            ),
            color = Color(0xFFDB4324),
            maxLines = 1
        )
    }
}

@Preview
@Composable
fun GizoFilledButtonPreview() {
    GizoFilledButton(
        onClick = {
        },
        modifier = Modifier
            .fillMaxWidth(),
        shape = 10.dp,
    ) {
        Text(

            text = stringResource(R.string.gizo_close),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontFamily = urbanistFontFamily,
                fontSize = 16.sp
            ),
            color = Color.White,
            overflow = TextOverflow.Visible,
            maxLines = 1
        )
    }
}