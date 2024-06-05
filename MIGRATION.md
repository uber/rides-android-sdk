# Migration Guide from Old SDK

We've simplified the SDK for consumers by providing a single point of entry. Information passing from the client app to the library is now divided into two parts:

1. **Static Information**: Provided as a one-time configuration with the `sso_config.json` file.
2. **Dynamic Information**: Contains parameters that can change over time, such as the type of flow needed, prefill information, use of SSO, or in-app authentication.

For detailed SDK integration documentation, please refer to the [authentication guide](https://github.com/uber/rides-android-sdk/tree/2.x/authentication).

This guide focuses on modifying your codebase when migrating from the older 0.10.X version of the SDK to the 2.X version.

## Steps to Follow:

### 1. Providing Application Information
- Remove `UBER_CLIENT_ID` and `UBER_REDIRECT_URI` entries from the `gradle.properties` file.
- Create a `sso_config.json` file in your application's `res/raw` folder with the following details:

    ```json
    {
      "client_id": "YOUR_CLIENT_ID",
      "redirect_uri": "YOUR_APPLICATION_ID.uberauth://redirect",
      "scope": "YOUR_SCOPES COMMA SEPARATED"
    }
    ```

### 2. Deprecating `SessionConfiguration` Object
- Remove references to the `SessionConfiguration` object built like this:

    ```java
    SessionConfiguration configuration = new SessionConfiguration.Builder()
        .setClientId(CLIENT_ID)
        .setRedirectUri(REDIRECT_URI)
        .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS))
        .setProfileHint(new ProfileHint
            .Builder()
            .email("john@doe.com")
            .firstName("John")
            .lastName("Doe")
            .phone("1234567890")
            .build())
        .build();
    ```

- Instead, use `AuthContext`:

    ```java
    AuthContext authContext = new AuthContext(
            new AuthDestination.CrossAppSso(),
            new AuthType.PKCE(),
            new PrefillInfo(
                    "john@doe.com",
                    "John",
                    "Doe",
                    "1234567890"
            )
    );
    ```

### 3. Replace `LoginManager` with `UberAuthClient`

Replace

```java
LoginManager loginManager = new LoginManager(accessTokenStorage,
        new SampleLoginCallback(),
        configuration,
        CUSTOM_BUTTON_REQUEST_CODE);
loginManager.login(LoginSampleActivity.this);
```
with

```java
UberAuthClient uberAuthClient = new UberAuthClientImpl();
uberAuthClient.authenticate(LoginSampleActivity.this, authContext);
```

### 4. Custom buttons (Future)

The Uber custom buttons provide apis for `setSessionConfiguration()` `setCallback()` and `setRequestCode()` with the changes in the authentication module we will not be needing these anymore as there will be only one entry point for authentication module of the sdk
