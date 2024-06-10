package com.uber.sdk2.auth.ui

import android.media.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uber.sdk2.core.R

/** A button that looks like the Uber button. */
@Composable
fun UberCustomButton(
    text: String,
    isWhite: Boolean = false,
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

    Text(
        text = text,
        color = textColor,
        style = TextStyle(
            fontSize = dimensionResource(id = R.dimen.ub__text_size).value.sp
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(16.dp)
    )
}

@Composable
fun UberAuthButton(
    text: String,
    isWhite: Boolean = false,
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

    val logo = if (isWhite) {
        painterResource(id = R.drawable.uber_logotype_black)
    } else {
        painterResource(id = R.drawable.uber_logotype_white)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(dimensionResource(id = R.dimen.ub__standard_padding))
    ) {
        Image(
            painter = logo,
            contentDescription = null,
            modifier = Modifier.padding(end = dimensionResource(id = com.uber.sdk2.auth.R.dimen.ub__signin_margin))
        )
        Text(
            text = text,
            color = textColor,
            style = TextStyle(
                fontSize = dimensionResource(id = R.dimen.ub__secondary_text_size).value.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UberButtonPreview() {
    Column {
        UberCustomButton(text = "Default Uber Button", onClick = { /* Do something */ })
        UberCustomButton(text = "White Uber Button", isWhite = true, onClick = { /* Do something */ })
        UberAuthButton(text = "Sign in with Uber", onClick = { /* Do something */ })
        UberAuthButton(text = "Sign in with Uber (White)", isWhite = true, onClick = { /* Do something */ })
    }
}
