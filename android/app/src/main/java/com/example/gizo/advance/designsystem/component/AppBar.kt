package com.example.gizo.advance.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gizo.advance.designsystem.theme.urbanistFontFamily

@Composable
fun GizoAppBar(
    text: String = "",
    backgroundColor: Color = Color.Transparent,
    hasBackIcon: Boolean = true,
    onBack: () -> Unit = {},
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = backgroundColor)
            .padding(start = 21.dp, end = 21.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val interactionSource = remember { MutableInteractionSource() }

        if (hasBackIcon) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onBack()
                    }
                    .size(30.dp)
                    .padding(top = 4.dp, bottom = 4.dp, end = 8.dp),
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = urbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(end = if (hasBackIcon) 30.dp else 0.dp, bottom = 2.dp)
        )
    }
}

@Preview
@Composable
fun GizoAppBarPreview(){
    GizoAppBar("Title")
}