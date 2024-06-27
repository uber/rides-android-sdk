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
