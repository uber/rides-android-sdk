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
package com.ubercab.presidio.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.uber.sdk2.auth.UberAuthClientImpl
import com.uber.sdk2.auth.request.AuthContext
import com.uber.sdk2.auth.request.AuthDestination
import com.uber.sdk2.auth.request.AuthType
import com.uber.sdk2.auth.request.CrossApp
import com.uber.sdk2.auth.request.PrefillInfo
import com.uber.sdk2.auth.response.UberToken
import com.uber.sdk2.core.ui.UberButton
import com.ubercab.presidio.demo.ui.theme.UberandroidsdkTheme

class DemoActivity : ComponentActivity() {

  private val uberAuthLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      logResult(result.resultCode, result.data)
    }

  private fun logResult(resultCode: Int, data: Intent?) {
    when (resultCode) {
      RESULT_OK -> {
        val uberToken = data?.getParcelableExtra<UberToken>("EXTRA_UBER_TOKEN")
        Toast.makeText(this, "Uber Token: ${uberToken?.accessToken}", Toast.LENGTH_SHORT).show()
      }
      else -> {
        val errorMessage = data?.getStringExtra("EXTRA_ERROR")
        Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      UberandroidsdkTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            AuthScreen(
              context = this@DemoActivity,
              launcher = uberAuthLauncher,
              authDestination = AuthDestination.InApp,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
  context: Context,
  launcher: ActivityResultLauncher<Intent>,
  authDestination: AuthDestination,
) {
  val email = remember { mutableStateOf("") }
  val firstName = remember { mutableStateOf("") }
  val lastName = remember { mutableStateOf("") }
  val phoneNumber = remember { mutableStateOf("") }

  TextFieldWithLabel("Email", email)
  TextFieldWithLabel("First Name", firstName)
  TextFieldWithLabel("Last Name", lastName)
  TextFieldWithLabel("Phone Number", phoneNumber)

  var expanded by remember { mutableStateOf(false) }
  var selectedOptionText by remember { mutableStateOf("Select an option") }
  val options = listOf("Rider", "Eats", "Driver", "InApp")

  Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .clickable { expanded = true }
            .menuAnchor()
            .padding(16.dp) // Optional: padding for better touch target
      ) {
        TextField(
          value = selectedOptionText,
          onValueChange = {},
          label = { Text("Selected Option") },
          modifier = Modifier.fillMaxWidth(),
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          singleLine = true,
          enabled = false, // Disable the TextField to prevent keyboard
        )
      }

      ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth(),
      ) {
        options.forEach { label ->
          DropdownMenuItem(
            onClick = {
              selectedOptionText = label
              expanded = false
            },
            text = { Text(text = label) },
          )
        }
      }
    }
  }

  val authDestination =
    when (selectedOptionText) {
      "Rider" -> AuthDestination.CrossAppSso(listOf(CrossApp.Rider, CrossApp.Eats))
      "Eats" -> AuthDestination.CrossAppSso(listOf(CrossApp.Eats, CrossApp.Rider))
      "Driver" -> AuthDestination.CrossAppSso(listOf(CrossApp.Driver, CrossApp.Rider))
      "InApp" -> AuthDestination.InApp
      else -> AuthDestination.InApp
    }

  AuthButton(
    context = context,
    launcher = launcher,
    authDestination = authDestination,
    buttonText = "Authenticate",
    PrefillInfo(email.value, firstName.value, lastName.value, phoneNumber.value),
  )
}

@Composable
fun TextFieldWithLabel(label: String, textState: MutableState<String>) {
  TextField(
    value = textState.value,
    onValueChange = { textState.value = it },
    label = { Text(label) },
    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    colors =
      TextFieldDefaults.colors(
        disabledContainerColor = Color.White,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
      ),
  )
}

@Composable
fun AuthButton(
  context: Context,
  launcher: ActivityResultLauncher<Intent>,
  authDestination: AuthDestination,
  buttonText: String,
  prefillInfo: PrefillInfo? = null,
) {
  UberButton(
    text = buttonText,
    onClick = {
      // Invoke the Uber authentication call here
      UberAuthClientImpl()
        .authenticate(
          context as DemoActivity,
          launcher,
          AuthContext(authDestination, AuthType.PKCE(), prefillInfo),
        )
    },
  )
}
