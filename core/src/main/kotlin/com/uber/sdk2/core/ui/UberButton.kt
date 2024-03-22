package com.uber.sdk2.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun UberButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    textStyle: TextStyle = TextStyle.Default.copy(color = Color.White),
    backgroundColor: Color = Color.Black,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
        ),
        contentPadding = padding
    ) {
        Text(text = text, style = textStyle)
    }
}