# Uber Rides Android SDK (beta)

Official Android SDK (beta) to support Uber’s deeplinks.

This library allows you to integrate Uber into your Android app.

At a minimum, this SDK is designed to work with Android SDK 16.

## Before you begin

Before using this SDK, register your application on the [Uber Developer Site](https://developer.uber.com/).

## Installation

To use the Uber Rides Android SDK, add the compile dependency with the latest version of the Uber SDK.

### Gradle

Add the Uber Rides Android SDK to your `build.gradle`:
```gradle
dependencies {
    compile 'com.uber.sdk:rides-android:0.3.1'
}
```

### Maven

In the `pom.xml` file:
```xml
<dependency>
    <groupId>com.uber.sdk</groupId>
    <artifactId>rides-android</artifactId>
    <version>0.3.1</version>
</dependency>
```

## SDK Configuration

In order for the SDK to function correctly, you need to add some information about your app. In your application, initialize `UberSdk` at your earliest convenience with an application context and your client ID, and set any app-level configurations you need.

```java
// This is necessary for the SDK to function.
UberSdk.initialize(this, "YOUR_CLIENT_ID");
// This is necessary if you'll be using the SDK for implicit grant
UberSdk.setRedirectUri("YOUR_REDIRECT_URI");
// This is useful for testing your application in the sandbox environment
UberSdk.setSandboxMode(true);
// China based apps should specify the region
UberSdk.setRegion(UberSdk.Region.CHINA);
```

## Example Usage
### Quick Integration
The `RideRequestButton` offers the quickest ways to integrate Uber into your application. You can add a Ride Request Button to your View like you would any other View:
```java
RideRequestButton requestButton = new RideRequestButton(context);
layout.addView(requestButton);
```
This will create a request button with default deeplinking behavior, with pickup pin set to the user’s current location. The user will need to select a product and input additional information when they are switched over to the Uber application.

You can also add your button through XML:
```xml
<LinearLayout
   xmlns:android="http://schemas.android.com/apk/res/android"
   xmlns:uber="http://schemas.android.com/apk/res-auto"
   android:layout_width="match_parent"
   android:layout_height="match_parent">

   <com.uber.sdk.android.rides.RequestButton
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      uber:ub__style="black"/>
</LinearLayout>
```

To use the `uber` custom attribute be sure to add `xmlns:uber="http://schemas.android.com/apk/res-auto"` to your root view element.

### Color Style

The default color has a black background with white text:
```xml
<com.uber.sdk.android.rides.RequestButton
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"/>
```
For a button with a white background and black text:
```xml
<com.uber.sdk.android.rides.RequestButton
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      uber:ub__style="white"/>
```

#### Deeplinking
Without any extra configuration, the `RideRequestButton` will deeplink to the Uber app. We suggest passing additional parameters to make the Uber experience even more seamless for your users. For example, dropoff location parameters can be used to automatically pass the user’s destination information over to the driver:

```java
RideParameters rideParams = new RideParameters.Builder()
  .setProductID("a1111c8c-c720-46c3-8534-2fcdd730040d")
  .setPickupLocation(37.775304f, -122.417522f, "Uber HQ", "1455 Market Street, San Francisco")
  .setDropoffLocation(37.795079f, -122.4397805f, "Embarcadero", "One Embarcadero Center, San Francisco")
  .build();
requestButton.setRideParameters(rideParams);
```

With all the necessary parameters set, pressing the button will seamlessly prompt a ride request confirmation screen.

#### Ride Request Widget
The Uber Rides SDK provides a simple way to integrate the Ride Request View using the `RideRequestButton` via the `RideRequestActivityBehavior`. The button can be configured with this behavior object to show the `RideRequestActivity` on click, rather than deeplinking to the Uber app. Without any ride parameters, it will attempt to use the user's current location for pickup - for this, you must ask your user for location permissions. Otherwise, any pickup/dropoff location information passed via `RideParameters` to the button will be pre-filled in the Ride Request View.

```java
// The REQUEST_CODE is used to pass back error information in onActivityResult
requestButton.setRequestBehavior(new RideRequestActivityBehavior(this, REQUEST_CODE));
```

That's it! With this configuration, when a user clicks on the request button, an activity will be launched that contains a login view (on first launch) where the user can authorize your app. After authorization, this activity will contain the Ride Request View. If any unexpected errors occur that the SDK can't handle, the activity will finish with an error in the result Intent using either the key `RideRequestActivity.AUTHENTICATION_ERROR` or `RideRequestActivity.RIDE_REQUEST_ERROR` depending on where the error occurred.

## Custom Integration
If you want to provide a more custom experience in your app, there are a few classes to familiarize yourself with. Read the sections below and you'll be requesting rides in no time!

### Implicit Grant Authorization
Before you can request any rides, you need to get an `AccessToken`. The Uber Rides SDK provides the `LoginManager` class for this task. Simply create a new instance and use its login method to present the login screen to the user.

```java
AccessTokenManager accessTokenManager = new AccessTokenManager(context);
LoginManager loginManager = new LoginManager(accessTokenManager);
// Replace these scopes with any general scopes you would like to request.
List<Scope> scopes = new ArrayList<Scope>();
scopes.add(Scope.PROFILE);
scopes.add(Scope.RIDE_WIDGETS);
loginManager.loginWithScopes(activity, scopes);
```

The only required scope for the Ride Request Widget is the `RIDE_WIDGETS` scope, but you can pass in any other (general) scopes that you'd like access to. The call to `loginWithScopes()` presents an activity with a WebView where the user logs into their Uber account, or creates an account, and authorizes the requested scopes. In your `Activity#onActivityResult()`, call `LoginManager#onActivityResult()`:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data){
    super.onActivityResult(requestCode, resultCode, data);
    LoginCallback loginCallback = new LoginCallback() {
            @Override
            public void onLoginCancel() {
                // User canceled login
            }

            @Override
            public void onLoginError(@NonNull AuthenticationError error) {
                // Error occurred during login
            }

            @Override
            public void onLoginSuccess(@NonNull AccessToken accessToken) {
                // Successful login!  The AccessToken will have already been saved.
            }
        }

    loginManager.onActivityResult(requestCode, resultCode, data, 
    loginCallback);
}
```

### Custom Authorization / TokenManager

If your app allows users to authorize via your own customized logic, you will need to create an `AccessToken` manually and save it in the keychain using the `AccessTokenManager`.

```java
AccessTokenManager accessTokenManager = new AccessTokenManager(context);
Date expirationTime = new Date(1458770906206l);
List<Scope> scopes = new ArrayList<Scope>();
scopes.add(Scope.RIDE_WIDGETS);
String token = "obtainedAccessToken";
AccessToken accessToken = new AccessToken(expirationTime, scopes, token);
accessTokenManager.setAccessToken(accessToken);
```
The `AccessTokenManager` can also be used to get an access token or delete it.

```java
accessTokenManger.getAccessToken();
accessTokenManager.removeAccessToken();
```

To keep track of multiple users, add a tag for each unique user.

```java
accessTokenManager.setAccessToken(accessToken, "user1");
accessTokenManager.setAccessToken(accessToken, "user2");

AccessToken accessTokenForUserOne = accessTokenManger.getAccessToken("user1");
accessTokenManger.removeAccessToken("user2");
```

### RideRequestView
The `RideRequestView` is like any other view you'd add to your app. Create a new instance in your XML layout or programatically. You can optionally add custom `RideParameters` or a custom `AccessToken`. When you're ready to show the Ride Request View, just call `load()`.

```java
RideRequestView rideRequestView = new RideRequestView(context);
rideRequestView.setRideParameters(rideParameters)
rideRequestView.setRideRequestViewCallback(new RideRequestViewErrorCallback() {
    @Override
    public void onErrorReceived(RideReqeustViewError error) {
        switch (error) {
            // Handle errors
        }
    }
});
layout.addView(rideRequestView);
rideRequestView.load();
```

## Sample Apps

A sample app can be found in the `samples` folder. Alternatively, you can also download a sample from the [releases page](https://github.com/uber/rides-android-sdk/releases/tag/v0.3.1).

Don’t forget to configure the client ID in the `SampleActivity` file.

To install the sample app from your IDE, File > New > Import Project and select the extracted folder from the downloaded sample.

## Getting help

Uber developers actively monitor the `uber-api` tag on StackOverflow. If you need help installing or using the library, you can ask a question there. Make sure to tag your question with `uber-api` and `android`!

For full documentation about our API, visit our Developer Site.

## Contributing

We love contributions. If you’ve found a bug in the library or would like new features added, go ahead and open issues or pull requests against this repo. Write a test to show your bug was fixed or the feature works as expected.

## MIT Licensed
