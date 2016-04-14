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
