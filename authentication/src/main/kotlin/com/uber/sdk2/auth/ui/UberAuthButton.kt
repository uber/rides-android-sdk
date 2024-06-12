package com.uber.sdk2.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uber.sdk2.core.R

@Composable
fun UberAuthButton(
    text: String,
    isWhite: Boolean = false,
    shape: Shape = RoundedCornerShape(4.dp),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value

    val backgroundColor = if (isPressed) {
        if (isWhite) colorResource(id = R.color.uber_white_40) else colorResource(id = R.color.uber_black_90)
    } else {
        if (isWhite) colorResource(id = R.color.uber_white) else colorResource(id = R.color.uber_black)
    }

    val textColor = if (isWhite) {
        colorResource(id = R.color.uber_black)
    } else {
        colorResource(id = R.color.uber_white)
    }

    val iconResId = if (isWhite) R.drawable.uber_logotype_black else R.drawable.uber_logotype_white

    Button(
        onClick = onClick,
        modifier = Modifier
            .wrapContentSize(),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = textColor),
        shape = RoundedCornerShape(4.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.padding(end = dimensionResource(id = com.uber.sdk2.auth.R.dimen.ub__signin_margin))
        )
        Text(
            text = text.uppercase(),
            color = textColor,
            style = TextStyle(fontSize = dimensionResource(id = R.dimen.ub__secondary_text_size).value.sp),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.ub__standard_padding))
                .wrapContentWidth()
        )
    }
}
