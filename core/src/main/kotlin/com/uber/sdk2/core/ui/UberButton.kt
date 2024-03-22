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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/** A button that looks like the Uber button. */
@Composable
fun UberButton(
  text: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  textStyle: TextStyle = TextStyle.Default.copy(color = Color.White),
  backgroundColor: Color = Color.Black,
  padding: PaddingValues = PaddingValues(0.dp),
) {
  Button(
    onClick = onClick,
    modifier = modifier,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = backgroundColor,
        disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
      ),
    contentPadding = padding,
  ) {
    Text(text = text, style = textStyle)
  }
}
