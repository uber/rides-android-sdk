# Uber Rides Android SDK (beta) [![Build Status](https://travis-ci.org/uber/rides-android-sdk.svg?branch=master)](https://travis-ci.org/uber/rides-android-sdk)

Official Android SDK to support:
 - Ride Request Button
 - Ride Request Deeplinks
 - Uber Authentication
 - REST APIs

At a minimum, this SDK is designed to work with Android SDK 15.

## Before you begin

Before using this SDK, register your application on the [Uber Developer Site](https://developer.uber.com/).

## Installation

To use the Uber Rides Android SDK, add the compile dependency with the latest version of the Uber SDK.

### Gradle

[![Maven Central](https://img.shields.io/maven-central/v/com.uber.sdk/rides-android.svg)](https://mvnrepository.com/artifact/com.uber.sdk/rides-android)

```gradle
dependencies {
    compile 'com.uber.sdk:rides-android:x.y.z'
}
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
```
## Ride Request Deeplink
The Ride Request Deeplink provides an easy to use method to provide ride functionality against 
the install Uber app or the mobile web experience.


Without any extra configuration, the `RideRequestDeeplink` will deeplink to the Uber app. We 
suggest passing additional parameters to make the Uber experience even more seamless for your users. For example, dropoff location parameters can be used to automatically pass the user’s destination information over to the driver:

```java
RideParameters rideParams = new RideParameters.Builder()
  .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
  .setPickupLocation(37.775304, -122.417522, "Uber HQ", "1455 Market Street, San Francisco")
  .setDropoffLocation(37.795079, -122.4397805, "Embarcadero", "One Embarcadero Center, San Francisco")
  .build();
requestButton.setRideParameters(rideParams);
```

After configuring the Ride Parameters, pass them into the `RideRequestDeeplink` builder object to
 construct and execute the deeplink.

```java
RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(context)
                        .setSessionConfiguration(config))
                        .setRideParameters(rideParameters)
                        .build();
                deeplink.execute();

```

### Deeplink Fallbacks
The Ride Request Deeplink will prefer to use deferred deeplinking by default, where the user is 
taken to the Play Store to download the app, and then continue the deeplink behavior in the app 
after installation. However, an alternate fallback may be used to prefer the mobile web 
experience instead.

To prefer mobile web over an app installation, set the fallback on the builder:

```java
RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(context)
                        .setSessionConfiguration(config)
                        .setFallback(Deeplink.Fallback.MOBILE_WEB)
                        .setRideParameters(rideParameters)
                        .build();
                deeplink.execute();

```


## Ride Request Button

The `RideRequestButton` offers the quickest ways to integrate Uber into your application. You can add a Ride Request Button to your View like you would any other View:
```java
RideRequestButton requestButton = new RideRequestButton(context);
layout.addView(requestButton);
```
This will create a request button with deeplinking behavior, with the pickup pin set to the user’s current location. The user will need to select a product and input additional information when they are switched over to the Uber application.

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

### Customization

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

To specify the mobile web deeplink fallback over app installation when using the 
`RideRequestButton`:

```java
rideRequestButton.setDeeplinkFallback(Deeplink.Fallback.MOBILE_WEB);
```


With all the necessary parameters set, pressing the button will seamlessly prompt a ride request confirmation screen.

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


#### Dashboard configuration
To use SDK features, two configuration details must be set on the Uber Developer Dashboard.

 1. Sign into to the [developer dashboard](https://developer.uber.com/dashboard)

 1. Register a redirect URI to be used to communication authentication results. The default used 
 by the SDK is in the format of `applicationId.uberauth://redirect`. ex: `com.example
 .uberauth://redirect`. To configure the SDK to use a different redirect URI, see the steps below. 
 
 1. To use Single Sign On you must register a hash of your application's signing certificate in the 
 Application Signature section of the settings page of your application.

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
AccessTokenStorage accessTokenStorage = new AccessTokenManager(context);
LoginManager loginManager = new LoginManager(accessTokenStorage, loginCallback);
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

#### Authentication Migration and setup (Version 0.8 and above)
With Version 0.8 and above of the SDK, the redirect URI is more strongly enforced to meet IETF
standards [IETF RFC](https://tools.ietf.org/html/draft-ietf-oauth-native-apps-12).

The SDK will automatically created a redirect URI to be used in the oauth callbacks with
the format "applicationId.uberauth://redirect", ex "com.example.app.uberauth://redirect". **This URI must be registered in 
the [developer dashboard](https://developer.uber.com/dashboard)**

If this differs from the previous specified redirect URI configured in the SessionConfiguration, 
there are a few options.

 1. Change the redirect URI to match the new scheme in the configuration of the Session. If this 
 is left out entirely, the default will be used. 

```java
SessionConfiguration config = new SessionConfiguration.Builder()
    .setRedirectUri("com.example.app.uberauth://redirect")
    .build();
```

 2. Override the LoginRedirectReceiverActivity in your main manifest and provide a custom intent
filter. Register this custom URI in the developer dashboard for your application.

```xml
<activity
        android:name="com.uber.sdk.android.core.auth.LoginRedirectReceiverActivity"
        tools:node="replace">
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:scheme="com.example.app"
                android:host="redirect" />
    </intent-filter>
</activity>
```

3. If using [Authorization Code Flow](https://developer.uber.com/docs/riders/guides/authentication/user-access-token), you will need to configure your server to redirect to 
   the Mobile Application with an access token either via the generated URI or a custom URI as defined in steps 1 and 2.

The Session should be configured to redirect to the server to do a code exchange and the login 
manager should indicate the SDK is operating in the Authorization Code Flow.

```java
SessionConfiguration config = new SessionConfiguration.Builder()
    .setRedirectUri("https://example.com/redirect") //Where this is your configured server
    .build();

loginManager.setAuthCodeFlowEnabled(true); // or loginManager.setForceAuthCodeFlowEnabled(true) (see below)
loginManager.login(this);

```
   
 Once the code is exchanged, the server should redirect to a URI in the standard OAUTH format of 
 `com.example.app.uberauth://redirect#access_token=ACCESS_TOKEN&token_type=Bearer&expires_in=TTL&scope=SCOPES&refresh_token=REFRESH_TOKEN`
  for the SDK to receive the access token and continue operation.``
  

##### Authorization Code Flow


The default behavior of calling   `LoginManager.login(activity)` is to activate Single Sign On, 
and if SSO is unavailable, fallback to Implicit Grant if privileged scopes are not requested, 
otherwise redirect to the Play Store. If you require Authorization Code Grant, you have two options 
for overriding this behavior: set `LoginManager.setAuthCodeFlowEnabled(true)` to use the Authorization Code Flow only for 
requesting privileged scopes (and fallback to ImplicitGrant otherwise) or set `LoginManager.setForceAuthCodeFlowEnabled(true)`
to prefer the Authorization Code Flow regardless of scope. Either option will prevent the redirect to the Play Store.
Implicit Grant will allow access to all non-privileged scopes (and will not grant a refresh token), whereas the other options grant access to privileged scopes. [Read more about scopes](https://developer.uber.com/docs/scopes).


#### Login Errors
Upon a failure to login, an `AuthenticationError` will be provided in the `LoginCallback`. This enum provides a series of values that provide more information on the type of error.

### Custom Authorization / TokenManager

If your app allows users to authorize via your own customized logic, you will need to create an `AccessToken` manually and save it in shared preferences using the `AccessTokenManager`.

```java
AccessTokenStorage accessTokenStorage = new AccessTokenManager(context);
Date expirationTime = 2592000;
List<Scope> scopes = Arrays.asList(Scope.RIDE_WIDGETS);
String token = "obtainedAccessToken";
String refreshToken = "obtainedRefreshToken";
String tokenType = "obtainedTokenType";
AccessToken accessToken = new AccessToken(expirationTime, scopes, token, refreshToken, tokenType);
accessTokenStorage.setAccessToken(accessToken);
```
The `AccessTokenManager` can also be used to get an access token or delete it.

```java
accessTokenManger.getAccessToken();
accessTokenStorage.removeAccessToken();
```

To keep track of multiple users, create an AccessTokenManager for each AccessToken.

```java
AccessTokenManager user1Manager = new AccessTokenManager(activity, "user1");
AccessTokenManager user2Manager = new AccessTokenManager(activity, "user2");
user1Manager.setAccessToken(accessToken);
user2Manager.setAccessToken(accessToken2);
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

Sample apps can be found in the `samples` folder. Alternatively, you can also download a sample from the [releases page](https://github.com/uber/rides-android-sdk/releases).

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

## Contributing

We :heart: contributions. Found a bug or looking for a new feature? Open an issue and we'll 
respond as fast as we can. Or, better yet, implement it yourself and open a pull request! We ask 
that you open an issue to discuss feature development prior to undertaking the work and that you 
include tests to show the bug was fixed or the feature works as expected.

## MIT Licensed
