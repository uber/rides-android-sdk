/*
 * Copyright (C) 2024. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
