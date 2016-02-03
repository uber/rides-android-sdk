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
    compile 'com.uber.sdk:rides-android:0.1.0'
}
```

### Maven

In the `pom.xml` file:
```xml
<dependency>
    <groupId>com.uber.sdk</groupId>
    <artifactId>rides-android</artifactId>
    <version>0.2.0/version>
</dependency>
```

## How to use

You can add a Ride Request Button to your View like you would any other View:
```java
RequestButton requestButton = new RequestButton(context);
requestButton.setClientId("your_client_id");
layout.addView(requestButton);
```

This will create a request button with default behavior, with pickup pin set to the user’s current location. The user will need to select a product and input additional information when they are switched over to the Uber application.

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
   	  uber:client_id="clientId"
   	  uber:style="black"/>

</LinearLayout>
```

To use the `uber` custom attribute be sure to add `xmlns:uber="http://schemas.android.com/apk/res-auto"` to your root view element.

### Adding Parameters

We suggest passing additional parameters to make the Uber experience even more seamless for your users. For example, dropoff location parameters can be used to automatically pass the user’s destination information over to the driver:
```java
RequestButton requestButton = RequestButton(context);
requestButton.setClientId("your_client_id");
RideParameters rideParams = new RideParameters.Builder()
  .setProductID("abc123-productID")
  .setPickupLocation(37.775304f, -122.417522f, "Uber HQ", "1455 Market Street, San Francisco")
  .setDropoffLocation(37.795079f, -122.4397805f, "Embarcadero", "One Embarcadero Center, San Francisco")
  .build();
requestButton.setRideParameters(rideParams);
layout.addView(requestButton);
```
With all the necessary parameters set, pressing the button will seamlessly prompt a ride request confirmation screen.

### Color Style

The default color has a black background with white text:
```xml
<com.uber.sdk.android.rides.RequestButton
   	  android:layout_width="wrap_content"
   	  android:layout_height="wrap_content"
   	  uber:client_id="clientId"/>
```
For a button with a white background and black text:
```xml
<com.uber.sdk.android.rides.RequestButton
   	  android:layout_width="wrap_content"
   	  android:layout_height="wrap_content"
   	  uber:client_id="clientId"
   	  uber:style="white"/>
```

## Sample Apps


A sample app can be found in the `samples` folder. Alternatively, you can also download a sample from the [releases page](https://github.com/uber/rides-android-sdk/releases/tag/v0.2.0).

Don’t forget to configure the appropriate `res/values/strings.xml` file and add your client ID.

To install the sample app from your IDE, File > New > Import Project and select the extracted folder from the downloaded sample.

## Getting help

Uber developers actively monitor the Uber Tag on StackOverflow. If you need help installing or using the library, you can ask a question there. Make sure to tag your question with `uber-api` and `android`!

For full documentation about our API, visit our Developer Site.

## Contributing

We love contributions. If you’ve found a bug in the library or would like new features added, go ahead and open issues or pull requests against this repo. Write a test to show your bug was fixed or the feature works as expected.

## MIT Licensed
