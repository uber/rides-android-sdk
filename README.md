# Uber Rides Android SDK (beta) [![Build Status](https://travis-ci.org/uber/rides-android-sdk.svg?branch=master)](https://travis-ci.org/uber/rides-android-sdk) [![CLA assistant](https://cla-assistant.io/readme/badge/agraebe/rides-android-sdk)](https://cla-assistant.io/agraebe/rides-android-sdk)

Official Android SDK (beta) to support:
 - Ride Request Button
 - Ride Request Widget
 - REST APIs

At a minimum, this SDK is designed to work with Android SDK 14.

## Before you begin

Before using this SDK, register your application on the [Uber Developer Site](https://developer.uber.com/).

## Installation

To use the Uber Rides Android SDK, add the compile dependency with the latest version of the Uber SDK.

### Gradle

Add the Uber Rides Android SDK to your `build.gradle`:
```gradle
dependencies {
    compile 'com.uber.sdk:rides-android:0.5.4'
}
```

### Maven

In the `pom.xml` file:
```xml
<dependency>
    <groupId>com.uber.sdk</groupId>
    <artifactId>rides-android</artifactId>
    <version>0.5.4</version>
</dependency>
```

## SDK Configuration

In order for the SDK to function correctly, you need to add some information about your app. In your application, create a `SessionConfiguration` to use with the various components of the library. If you prefer the set it and forget it model, use the `UberSdk` class to initialize with a default `SessionConfiguration`.

```java

SessionConfiguration config = new SessionConfiguration.Builder()
    .setClientId("YOUR_CLIENT_ID") //This is necessary
    .setRedirectUri("YOUR_REDIRECT_URI") //This is necessary if you'll be using implicit grant
    .setEnvironment(Environment.SANDBOX) //Useful for testing your app in the sandbox environment
    .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS)) //Your scopes for authentication here
    .build();

//This is a convenience method and will set the default config to be used in other components without passing it directly.
UberSdk.initialize(config);
```

## Ride Request Button

The `RideRequestButton` offers the quickest ways to integrate Uber into your application. You can add a Ride Request Button to your View like you would any other View:
```java
RideRequestButton requestButton = new RideRequestButton(context);
layout.addView(requestButton);
```
This will create a request button with default deeplinking behavior, with the pickup pin set to the user’s current location. The user will need to select a product and input additional information when they are switched over to the Uber application.

You can also add your button through XML:
```xml
<LinearLayout
   xmlns:android="http://schemas.android.com/apk/res/android"
   xmlns:uber="http://schemas.android.com/apk/res-auto"
   android:layout_width="match_parent"
   android:layout_height="match_parent">

   <com.uber.sdk.android.rides.RideRequestButton
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      uber:ub__style="black"/>
</LinearLayout>
```

To use the `uber` XML namespace, be sure to add `xmlns:uber="http://schemas.android.com/apk/res-auto"` to your root view element.

### Color Style

The default color has a black background with white text:
```xml
<com.uber.sdk.android.rides.RideRequestButton
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"/>
```
For a button with a white background and black text:
```xml
<com.uber.sdk.android.rides.RideRequestButton
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      uber:ub__style="white"/>
```

### Deep linking parameters
Without any extra configuration, the `RideRequestButton` will deeplink to the Uber app. We suggest passing additional parameters to make the Uber experience even more seamless for your users. For example, dropoff location parameters can be used to automatically pass the user’s destination information over to the driver:

```java
RideParameters rideParams = new RideParameters.Builder()
  .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
  .setPickupLocation(37.775304, -122.417522, "Uber HQ", "1455 Market Street, San Francisco")
  .setDropoffLocation(37.795079, -122.4397805, "Embarcadero", "One Embarcadero Center, San Francisco")
  .build();
requestButton.setRideParameters(rideParams);
```

With all the necessary parameters set, pressing the button will seamlessly prompt a ride request confirmation screen.

## Ride Request Widget
The Uber Rides SDK provides a simple way to integrate the Ride Request View using the `RideRequestButton` via the `RideRequestActivityBehavior`. The button can be configured with this behavior object to show the `RideRequestActivity` on click, rather than deeplinking to the Uber app. Without any ride parameters, it will attempt to use the user's current location for pickup - for this, you must ask your user for location permissions. Otherwise, any pickup/dropoff location information passed via `RideParameters` to the button will be pre-filled in the Ride Request View.

```java
// The REQUEST_CODE is used to pass back error information in onActivityResult
requestButton.setRequestBehavior(new RideRequestActivityBehavior(this, REQUEST_CODE));
```

That's it! With this configuration, when a user clicks on the request button, an activity will be launched that contains a login view (on first launch) where the user can authorize your app. After authorization, this activity will contain the Ride Request View. If any unexpected errors occur that the SDK can't handle, the activity will finish with an error in the result Intent using either the key `RideRequestActivity.AUTHENTICATION_ERROR` or `RideRequestActivity.RIDE_REQUEST_ERROR` depending on where the error occurred.

> **Note:** The environment ([sandbox](https://developer.uber.com/docs/rides/sandbox) or production) is considered by the Ride Request Widget. If you use the sample source code from above, your calls will be issued to the Sandbox. The widget will display a `sandbox` badge to indicate that. To change the mode, set environment to `Environment.PRODUCTION`.

## Ride Request Button with ETA and price
To further enhance the button with destination and price information, add a Session to it and call `loadRideInformation()` function.

```java

RideParameters rideParams = new RideParameters.Builder()
  .setPickupLocation(37.775304, -122.417522, "Uber HQ", "1455 Market Street, San Francisco")
  .setDropoffLocation(37.795079, -122.4397805, "Embarcadero", "One Embarcadero Center, San Francisco") // Price estimate will only be provided if this is provided.
  .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d") // Optional. If not provided, the cheapest product will be used.
  .build();

SessionConfiguration config = new SessionConfiguration.Builder()
  .setClientId("YOUR_CLIENT_ID")
  .setServerToken("YOUR_SERVER_TOKEN")
  .build();
ServerTokenSession session = new ServerTokenSession(config);

RideRequestButtonCallback callback = new RideRequestButtonCallback() {

    @Override
    public void onRideInformationLoaded() {

    }

    @Override
    public void onError(ApiError apiError) {

    }

    @Override
    public void onError(Throwable throwable) {

    }
};

requestButton.setRideParameters(rideParams);
requestButton.setSession(session);
requestButton.setCallback(callback);
requestButton.loadRideInformation();
```

## Custom Integration
If you want to provide a more custom experience in your app, there are a few classes to familiarize yourself with. Read the sections below and you'll be requesting rides in no time!

### Login
The Uber SDK allows for three login flows: Implicit Grant (local web view), Single Sign On with the Uber App, and Authorization Code Grant (requires a backend to catch the local web view redirect and complete OAuth).

To use Single Sign On you must register a hash of your application's signing certificate in the Application Signature section of the [developer dashboard] (https://developer.uber.com/dashboard).

To get the hash of your signing certificate, run this command with the alias of your key and path to your keystore:

```sh
keytool -exportcert -alias <your_key_alias> -keystore <your_keystore_path> | openssl sha1 -binary | openssl base64
```

Before you can request any rides, you need to get an `AccessToken`. The Uber Rides SDK provides the `LoginManager` class for this task. Simply create a new instance and use its login method to present the login screen to the user.

```java
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
AccessTokenManager accessTokenManager = new AccessTokenManager(context);
LoginManager loginManager = new LoginManager(accessTokenManager, loginCallback);
loginManager.login(activity);
```

The only required scope for the Ride Request Widget is the `RIDE_WIDGETS` scope, but you can pass in any other (general) scopes that you'd like access to. The call to `loginWithScopes()` presents an activity with a WebView where the user logs into their Uber account, or creates an account, and authorizes the requested scopes. In your `Activity#onActivityResult()`, call `LoginManager#onActivityResult()`:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data){
    super.onActivityResult(requestCode, resultCode, data);
    loginManager.onActivityResult(activity, requestCode, resultCode, data);
}
```

The default behavior of calling `LoginManager.login(activity)` is to activate Single Sign On, and if that is unavailable, fallback to Implicit Grant if privileged scopes are not requested, otherwise redirect to the Play Store. If Authorization Code Grant is required, set `LoginManager.setRedirectForAuthorizationCode(true)` to prevent the redirect to the Play Store. Implicit Grant will allow access to all non-privileged scopes, where as the other two both grant access to privileged scopes. [Read more about scopes](https://developer.uber.com/docs/scopes).

#### Login Errors
Upon a failure to login, an `AuthenticationError` will be provided in the `LoginCallback`. This enum provides a series of values that provide more information on the type of error.

### Custom Authorization / TokenManager

If your app allows users to authorize via your own customized logic, you will need to create an `AccessToken` manually and save it in shared preferences using the `AccessTokenManager`.

```java
AccessTokenManager accessTokenManager = new AccessTokenManager(context);
Date expirationTime = 2592000;
List<Scope> scopes = Arrays.asList(Scope.RIDE_WIDGETS);
String token = "obtainedAccessToken";
String refreshToken = "obtainedRefreshToken";
String tokenType = "obtainedTokenType";
AccessToken accessToken = new AccessToken(expirationTime, scopes, token, refreshToken, tokenType);
accessTokenManager.setAccessToken(accessToken);
```
The `AccessTokenManager` can also be used to get an access token or delete it.

```java
accessTokenManger.getAccessToken();
accessTokenManager.removeAccessToken();
```

To keep track of multiple users, create an AccessTokenManager for each AccessToken.

```java
AccessTokenManager user1Manager = new AccessTokenManager(activity, "user1");
AccessTokenManager user2Manager = new AccessTokenManager(activity, "user2");
user1Manager.setAccessToken(accessToken);
user2Manager.setAccessToken(accessToken2);
```

### RideRequestView
The `RideRequestView` is like any other view you'd add to your app. Create a new instance in your XML layout or programmatically. You can optionally add custom `RideParameters` or a custom `AccessTokenSession`. When you're ready to show the Ride Request View, just call `load()`.

```java
RideRequestView rideRequestView = new RideRequestView(context);

//Optionally set Session, will use default session from UberSDK otherwise
//rideRequestView.setSession(session);

rideRequestView.setRideParameters(rideParameters)
rideRequestView.setRideRequestViewCallback(new RideRequestViewErrorCallback() {
    @Override
    public void onErrorReceived(RideRequestViewError error) {
        switch (error) {
            // Handle errors
        }
    }
});
layout.addView(rideRequestView);
rideRequestView.load();
```

## Making an API Request
The Android Uber SDK uses a dependency on the Java Uber SDK for API requests.
After authentication is complete, create a `Session` to use the Uber API.

```java
Session session = loginManager.getSession();
```

Now create an instance of the `RidesService` using the `Session`

```java
RidesService service = UberRidesApi.with(session).createService();
```

### Sync vs. Async Calls
Both synchronous and asynchronous calls work with the [Uber Rides Java SDK](https://github.com/uber/rides-java-sdk). The networking stack for the Uber SDK is powered by [Retrofit 2](https://github.com/square/retrofit) and the same model of threading is available.

#### Sync
```java
Response<UserProfile> response = service.getUserProfile().execute();
if (response.isSuccessful()) {
    //Success
    UserProfile profile = response.body();
} else {
    //Failure
    ApiError error = ErrorParser.parseError(response);
}

```

#### Async
```java
service.getUserProfile().enqueue(new Callback<UserProfile>() {
    @Override
    public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
        if (response.isSuccessful()) {
            //Success
            UserProfile profile = response.body();
        } else {
            //Api Failure
            ApiError error = ErrorParser.parseError(response);
        }
    }

    @Override
    public void onFailure(Call<UserProfile> call, Throwable t) {
        //Network Failure
    }
});
```

## Sample Apps

Sample apps can be found in the `samples` folder. Alternatively, you can also download a sample from the [releases page](https://github.com/uber/rides-android-sdk/releases/tag/v0.5.4).

The Sample apps require configuration parameters to interact with the Uber API, these include the client id, redirect uri, and server token. They are provided on the [Uber developer dashboard](https://developer.uber.com/dashboard).

Specify your configuration parameters in the sample's gradle.properties file, where examples can be found adhering to the format `UBER_CLIENT_ID=insert_your_client_id_here`. These are generated into the BuildConfig during compilation.

For a more idiomatic storage approach, define these in your home gradle.properties file to keep them out of the git repo.

~/.gradle/gradle.properties
```xml
UBER_CLIENT_ID=insert_your_client_id_here
UBER_REDIRECT_URI=insert_your_redirect_uri_here
UBER_SERVER_TOKEN=insert_your_server_token_here
```

To install the sample app from Android Studio, File > New > Import Project and select the extracted folder from the downloaded sample.

## Getting help

Uber developers actively monitor the `uber-api` tag on StackOverflow. If you need help installing or using the library, you can ask a question there. Make sure to tag your question with `uber-api` and `android`!

For full documentation about our API, visit our Developer Site.

## Migrating from a previous version
As the Uber Android SDK get closer to a 1.0 release, the API's will become more stable. In the meantime, be sure to check out the changelog to know what differs!

## Contributing

We :heart: contributions. Found a bug or looking for a new feature? Open an issue and we'll respond as fast as we can. Or, better yet, implement it yourself and open a pull request! We ask that you include tests to show the bug was fixed or the feature works as expected.

**Note:** All contributors also need to fill out the [Uber Contributor License Agreement](http://t.uber.com/cla) before we can merge in any of your changes.

## MIT Licensed
