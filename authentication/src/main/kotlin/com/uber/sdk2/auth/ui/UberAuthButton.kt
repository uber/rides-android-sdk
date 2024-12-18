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
package com.uber.sdk2.auth.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.uber.sdk2.auth.ui.theme.UberDimens
import com.uber.sdk2.auth.ui.theme.UberTypography
import com.uber.sdk2.core.R

@Composable
fun UberAuthButton(
  isWhite: Boolean = false,
  shape: Shape = MaterialTheme.shapes.large,
  onClick: () -> Unit,
) {
  val text = stringResource(id = com.uber.sdk2.auth.R.string.ub__sign_in)
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed = interactionSource.collectIsPressedAsState().value
  val backgroundColor =
    if (isPressed) {
      MaterialTheme.colorScheme.onSecondary
    } else {
      MaterialTheme.colorScheme.onPrimary
    }

  val textColor = MaterialTheme.colorScheme.primary

  val iconResId = if (isWhite) R.drawable.uber_logotype_black else R.drawable.uber_logotype_white

  Button(
    onClick = onClick,
    modifier = Modifier.wrapContentSize(),
    colors =
      ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = textColor),
    shape = shape,
    interactionSource = interactionSource,
  ) {
    Icon(
      painter = painterResource(id = iconResId),
      contentDescription = null,
      modifier = Modifier.padding(end = UberDimens.signInMargin),
    )
    Text(
      text = text.uppercase(),
      color = textColor,
      style = UberTypography.bodyMedium,
      modifier = Modifier.padding(UberDimens.standardPadding).wrapContentWidth(),
    )
  }
}
