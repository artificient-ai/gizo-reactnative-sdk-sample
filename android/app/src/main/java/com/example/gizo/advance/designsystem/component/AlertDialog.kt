package com.example.gizo.advance.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gizo.advance.R
import com.example.gizo.advance.designsystem.theme.AppTheme
import com.example.gizo.advance.designsystem.theme.redesignTextGray
import com.example.gizo.advance.designsystem.theme.urbanistFontFamily

@Composable
fun GizoAlertDialog(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    icon: Painter,
    description: String,
    title: String,

    ) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true
        )
    ) {
        var size by remember { mutableStateOf(IntSize.Zero) }
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 22.dp, bottom = 22.dp),

                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier,

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .onSizeChanged {
                                size = it
                            }
                            .padding(start = 16.dp, end = 26.dp),
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier
                                .padding(bottom = 8.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                fontFamily = urbanistFontFamily,
                                lineHeight = 20.sp,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            ),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = description,
                            modifier = Modifier
                                .padding(bottom = 8.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                fontFamily = urbanistFontFamily,
                                lineHeight = 20.sp,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = redesignTextGray
                            ),
                            textAlign = TextAlign.Start
                        )

                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {

                        Image(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.CenterStart)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    GizoFilledButton(
                        onClick = {
                            onClose()
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
            }
        }
    }
}

@Composable
fun GizoAlertDialog(
    modifier: Modifier = Modifier,
    isDismissNeeded: Boolean = true,
    isConfirmedNeeded: Boolean = true,
    title: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
    confirmText: String = "",
    dismissText: String = "",
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {

    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        AlertDialog(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.outlineVariant,
            onDismissRequest = {
                onDismiss()
                openDialog.value = false
            },
            title = {
                if (title != null) title()
            },
            text = {
                if (content != null) content()
            },
            confirmButton = {
                if (isConfirmedNeeded) {
                    GizoTextButton(
                        onClick = {
                            onConfirm()
                            openDialog.value = false
                        }
                    ) {
                        Text(
                            confirmText,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                    }
                }
            },
            dismissButton = {
                if (isDismissNeeded) {
                    TextButton(
                        onClick = {
                            onDismiss()
                            openDialog.value = false
                        }
                    ) {
                        Text(dismissText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun AlertDialogPreview() {
    AppTheme {
        GizoAlertDialog(
            onClose = { },
            title = "Title",
            description = "Description",
            icon = painterResource(id = R.drawable.gizo_car_brake)
        )
    }
}