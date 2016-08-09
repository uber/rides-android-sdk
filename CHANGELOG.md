v0.5.3 - TBD
------------

v0.5.2 - 7/11/2016
------------------
### Fixed

- [Issue #31](https://github.com/uber/rides-android-sdk/issues/31) Multiple warnings "Ignoring InnerClasses attribute for an anonymous inner class" when building project


v0.5.1 - 6/7/2016
-----------------
### Fixed

- [Issue #21] (https://github.com/uber/rides-android-sdk/issues/21) Remove sdk folder
- [Issue #23] (https://github.com/uber/rides-android-sdk/issues/23) Add additional error logging when invalid app signature is returned.
- [Issue #24] (https://github.com/uber/rides-android-sdk/issues/24) Remove product Id requirement for Ride Request Button

v0.5.0 - 6/2/2016
-----------------
### Added

#### Single Sign On

Introducing **SSO**. Allows a user to login & authorize your application via the native Uber app. Users no longer have to remember their username & password as long as they are signed into the Uber app.

- Added Uber Application Single Sign On using `LoginManager.login(activity)`
- Added `LoginButton` to ease signing in using Uber account.
- Added `PRIVILEGED` scopes to `Scope`.

#### Support all REST API Endpoints

- Added dependency on [Uber Rides Java SDK](https://github.com/uber/rides-java-sdk) to access the Uber API.

### Changed

#### Split Libraries

Now split into the `core-android` and `rides-android` libraries.

- `core-android` contains common classes and auth related functionality.
- `rides-android` contains only rides related features.

#### RideRequestButton

The RideRequestButton has been updated to show information about your Uber ride.

- Added ETA and Price estimate to `RideRequestButton` if a product ID is set in the RideParameters.

### Breaking

- Moved core functionality and authentication related classes to `core-android` and the Java SDK. Imports require updating.
- Removed `UberSdk.initialize(context, clientId)` and all `UberSdk` setters in favor of `UberSdk.initialize(sessionConfiguration)`
- Removed `LoginManager.loginWithScopes(activity, scopes)` in favor of `LoginManager.login(activity)` after using `new LoginManager(accessTokenManager, callback)`
- Removed `AccessTokenManager.getAccessToken(key)` and `AccessTokenManager.setAccessToken(key, token)` in favor of `new AccessTokenManager(context, key)`
- Removed `LoginManager.onActivityResult(requestCode, resultCode, data, callback)` in favor of `LoginManager.onActivityResult(activity, requestCode, resultCode, data)`

v0.3.2 - 5/12/2016
------------------
### Fixed

- [Issue #18] (https://github.com/uber/rides-android-sdk/issues/18) Unable to signup in widget
- [Issue #13] (https://github.com/uber/rides-android-sdk/issues/13) Android Studio preview not working

v0.3.1 - 4/18/2016
------------------
### Fixed

- [Issue #15] (https://github.com/uber/rides-android-sdk/issues/15) RideRequestView correctly handles redirecting to
call or message the driver

v0.3.0 - 4/11/2016
------------------
### Added

#### UberSdk

Now used to initialize the Uber SDK with configuration information, including `ClientID` and `RedirectURI`.

#### LoginManager / Implicit Grant flow
Added implicit grant (i.e. token) login authorization flow for non-privileged scopes (profile, history, places, ride_widgets)

- Added `LoginActivity` & `LoginView`
- Added `LoginManager` to handle login flow
- Added `AccessTokenManager` to handle access token management

#### Ride Request Widget
Introducing the **Ride Request Widget**. Allows for an end to end Uber experience without leaving your application.

- Requires the `RIDE_WIDGETS` scope
- `RideRequestButton` can be used with `RideRequestActivityBehavior` to send the user to `RideRequestActivity`
- `RideRequestActivity` for easy implementation that handles presenting login to the user
- Base view is `RideRequestView`

#### RideRequestButton Updates
`RequestButton` has been renamed to `RideRequestButton`

`RideRequestButton` now works by using a `RideParameters` and a `RequestingBehavior`. The `RideParameters` defines the parameters for the ride and the `requestingBehavior` defines how to execute it.
Currently available `requestingBehaviors` are:

- `DeeplinkRequestingBehavior`
  - Deeplinks into the Uber app to request a ride
- `RideRequestViewRequestingBehavior`
  - Presents the **Ride Request Widget** modally in your app to provide and end to end Uber experience

### Fixed

- [Issue #1] (https://github.com/uber/rides-android-sdk/issues/1) Renamed custom attribute `style` to `ub__style`
- [Pull #6] (https://github.com/uber/rides-android-sdk/pull/6) Updated Gradle Plugin to 2.0
- [Pull #7] (https://github.com/uber/rides-android-sdk/pull/7) Updated Gradle Wrapper to 2.12
- [Pull #8] (https://github.com/uber/rides-android-sdk/pull/8) Removed Guava dependency
- [Pull #9] (https://github.com/uber/rides-android-sdk/pull/9) Removed Google HTTP Client dependency
- [Issue #11] (https://github.com/uber/rides-android-sdk/issues/11) Added new Uber logo for `RideRequestButton`
- [Issue #14] (https://github.com/uber/rides-android-sdk/issues/14) Updated README

### Breaking
- `ClientID` must now be used to initialize the `UberSdk`
- `RequestButton` --> `RideRequestButton`
  - Removed custom attribute `client_id` use `UberSdk.initialize`
  - Renamed custom attribute `style` to `ub__style`
- `RideParameters`
  - Now accepts `double` instead of `float` for location information

v0.2.0 - 2/3/2016
------------------
  - Localization of request button text for zh-rCN.

v0.1.0 - 11/24/2015
------------------
  - Initial version.
