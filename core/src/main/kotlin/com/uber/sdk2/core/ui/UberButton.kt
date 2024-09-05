/**
 * Copyright (c) 2024 Uber Technologies, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.uber.sdk2.core.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.uber.sdk2.core.R

/** A button that looks like the Uber button. */
@Composable
fun UberButton(text: String, isWhite: Boolean = false, onClick: () -> Unit) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed = interactionSource.collectIsPressedAsState().value
  val backgroundColor =
    if (isPressed) {
      if (isWhite) colorResource(id = R.color.uber_white_40)
      else colorResource(id = R.color.uber_black_90)
    } else {
      if (isWhite) colorResource(id = R.color.uber_white)
      else colorResource(id = R.color.uber_black)
    }

  val textColor =
    if (isWhite) {
      colorResource(id = R.color.uber_black)
    } else {
      colorResource(id = R.color.uber_white)
    }

  Button(
    onClick = onClick,
    content = {
      Text(
        text = text,
        color = textColor,
        style = TextStyle(fontSize = dimensionResource(id = R.dimen.ub__text_size).value.sp),
      )
    },
    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = textColor),
  )
}
