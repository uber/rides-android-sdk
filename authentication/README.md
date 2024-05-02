# Uber Authentication ![Build Status](https://github.com/uber/rides-android-sdk/workflows/CI/badge.svg)

This SDK is designed to work with Android SDK 26 and beyond.

## Getting Started

### App Registration
Start by registering your application in the [Uber Developer's Portal](https://developer.uber.com/dashboard/create). Note the ClientID under the `Application ID` field.
    <p align="center">
    <img src="../img/client_id.png?raw=true" alt="Request Buttons Screenshot"/>
</p>

In the [Uber Developer Dashboard](https://developer.uber.com/dashboard), under the Security section, enter your application's Bundle ID in the `App Signatures` text field and tap the plus icon.

<p align="center">
    <img src="../img/app_signatures.png?raw=true" alt="App Signatures Screenshot"/>
</p>

Next, add your application's Redirect URI to the list of URLs under `Redirect URIs`. The preferred format is `[Your App's Bundle ID]://oauth/consumer`, however any redirect URI may be used.

<p align="center">
    <img src="../img/redirect_uri.png?raw=true" alt="Request Buttons Screenshot"/>
</p>

## Installation

To use the Uber authentication, add the implementation dependency with the latest version of the authentication module to your gradle file.

### Gradle
[![Maven Central](https://img.shields.io/maven-central/v/com.uber.sdk/authentication.svg)](https://central.sonatype.com/namespace/com.uber.sdk)

```gradle
dependencies {
    implementation 'com.uber.sdk:authentication:x.y.z'  
}
```

### SDK Configuration

In order for the SDK to function correctly, you need to add some information about your app. In your application, create a `sso_config.json` file to fill the following details that you added to the developer portal at app registration:
```json
{
  "client_id": "your_client_id",
  "redirect_uri": "your_redirect_uri",
  "scope": "your_app_scope" // separated with comma
}

```
### Authenticating

To authenticate your app's user with Uber's backend, use the UberAuthClient API. If you prefer the default case, use the `UberAuthClientImpl.authenticate()` call with an `Activity` or `ActivityResultLauncher` as parameter and a default `AuthContext()` object.

Upon completion, the result will be delivered to the activity that started the Uber authentication flow. For success, the result will be an `UberToken` object delivered via Intent as parcelable extra with key `EXTRA_UBER_TOKEN`.

| Property  | Type | Description |
| ------------- | ------------- | ------------- |
| authCode  | String? | The authorization code received from the authorization server. If this property is non-nil, all other properties will be nil. |
| accessToken | String? | The access token issued by the authorization server. This property will only be populated if token exchange is enabled. |
| refreshToken | String? | The type of the token issued. |
| expiresIn | Int? | A token which can be used to obtain new access tokens. |
| scope | [String]? | A comma separated list of scopes requested by the client. |

For failure, the result will contain an error message inside the Intent.

#### AuthContext
To authenticate with a more controlled/custom experience an `AuthContext` may be supplied to the login function. Use this type to specify additional customizations for the login experience:

* [Auth Destination](#auth-destination) - Where the login should occur; in the native Uber app or inside your application.
* [Auth Type](#auth-type) - The type of grant flow that should be used. Authorization Code Grant Flow is the only supported type.
* [PrefillInfo](#prefill) - Optional user information that should be prefilled when presenting the login screen.

```kotlin
val context = AuthContext(
    authDestination: authDestination, // CrossApp() or InApp
    authType: AuthType, // AuthCode or PKCE()
    prefill: prefill?
)

UberAuthClientImpl.authenticate(
    context: Context, // activity context
    activityResultLauncher: ActivityResultLauncher<Intent>, // launcher to launch the AuthActivity
    authContext: AuthContext
)
```
### Auth Destination

There are two locations or `AuthDestination`s where authentication can be handled.

1. `InApp` - Presents the login screen inside the host application using a secure web browser via Custom Tabs. If there are no browsers installed on the users device that support custom tab then authentication flow will launch the default browser app to complete the flow.
2. `CrossApp` - Links to the native Uber app, if installed. If not installed, falls back to InApp. By default, native will attempt to open each of the following Uber apps in the following order: Uber Rides, Uber Eats, Uber Driver. If you would like to customize this order you can supply the order as a parameter to `CrossApp()`. For example:
`CrossApp(listOf(Eats, Rider, Driver))` will prefer the Uber Eats app first, and `CrossApp(Driver)` will only attempt to open the Uber Driver app and fall back to `InApp` if unavailable.


```kotlin
val context = AuthContext(
    authDestination: CrossApp(Rider) // Only launch the Uber Rides app, fallback to inApp
)

UberAuthClientImpl.authenticate(
    context: Context, // activity context
    activityResultLauncher: ActivityResultLauncher<Intent>, // launcher to launch the AuthActivity
    authContext: AuthContext
)
```

### Auth Type

An Auth type supplies logic for a specific authentication grant flow. An Auth Provider that supplies performs the Authorization Code Grant Flow as specified in the [OAuth 2.0 Framework](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1).
We perform authorization grant flow in two ways:
* AuthorizationCode - The authentication flow will return only the auth code back to the calling app. It is the calling app's responsibility to exchange it for Uber tokens.
* PKCE - The authentication flow will perform proof key code exchange after the auth code is received and return the `UberToken` object to the calling app
**Note:** authCode **will be null** as it has been used for the token exchange and is no longer valid.

### Prefilling User Information
If you would like text fields during signup to be pre-populated with user information you can do so using the prefill API. Partial information is accepted.

**Supply the PrefillInfo parameter to AuthContext and this info will be used during authenticate call**
```kotlin
val prefill = Prefill(
    email: "jane@test.com",
    phoneNumber: "12345678900",
    firstName: "Jane",
    lastName: "Doe"
)

val authContext = AuthContext(prefillInfo = prefill)

UberAuthClientImpl.authenticate(
    context: Context, // activity context
    activityResultLauncher: ActivityResultLauncher<Intent>, // launcher to launch the AuthActivity
    authContext: AuthContext
)
```


### Responding to Redirects

When using the `InApp` auth destination, the sdk will is built to handle the callback deeplink in order to receive the users's credentials. To enable this the sdk assumes that the redirect uri mentioned in the developer portal for your app is `${applicationId}.uberauth://redirect`.

Once handled, the calling activity will get the result back via `intent` as mentioned above.


### Login Button
Coming Soon

## MIT Licensed
