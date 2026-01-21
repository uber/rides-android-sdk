# Login via Uber without SDK - Demo App

This app demonstrates how a third party app can integrate Login via Uber to their app without using
the rides-android-sdk using applink flow with proof-key code exchange,
RFC-7636 (https://datatracker.ietf.org/doc/html/rfc7636).

The app link to invoke the first party Uber app is -
```
https://auth.uber.com/oauth/v2/authorize?client_id={client-id}&redirect_uri={redirect-uri}&scope={space-separated-scopes}&flow_type=DEFAULT&sdk=android&response_type=code&prompt=consent
```

Here are the main components of the app -

- `AuthUriAssembler` - to assemble the a launch uri which would launch an Uber app (rides, eats or driver)
  if installed or a browser app otherwise
- `PkceUtil` - Generates code challenge and code verifier pair
- `AuthService` - A retrofit service which sends request to token endpoint
- `AuthorizationCodeGrantFlow` - Sends an async request to token endpoint using AuthService
- `TokenRequestFlowCallback` - Callback to send back the tokens back to the client, if request is
  successful, or error otherwise
- `DemoActivity` - A basic android activity that contains a button to login using app link

The launch uri contains a code challenge query parameter (generated as part of code challenge and
code verifier pair, a.k.a pkce pair) along with other relevant query parameters (like `client_id`
, `redirect_uri`,`response_type` etc.). The launch uri is basically an applink on android which can
be handled in 3 ways -

1. When one of Uber, Eats or Driver app is installed
   It will launch the specific app and show an authorization web page to the user to allow a third
   party app to use
   Uber's credentials to login. If the user grants permission, auth code is returned to the 3p app.
   If not, the SSO flow is canceled
2. When both all 3 Uber apps are installed
   User will be shown a disambiguation dialogue to choose the app they want to use for logging in.
   Once app is chosen it's same as #1
3. When no Uber apps are not installed
   Uber auth flow is launched in a custom tab, if available, or system browser. User completes the
   flow and auth code is returned to the 3P app

Then, make a request to Uber's backend (token endpoint) with `client_id`, `grant_type`
, `redirect_uri`, `code_verifier` (generated as part of pkce pair) along with the
received `auth_code`. If request results in successful response you would get the OAuth tokens (access token and refresh
token) in the activity result's intent bundle which are saved in the app's private shared preferences; or in case of failure an error is returned back via `ERROR` in the activity result's intent bundle.

## Should I use this same pattern in my own apps?

We (the rides-android-sdk maintainers) have no strong opinion on this one way or another. The design
considerations are at the discretion of the app developer.

With this demo, we are merely presenting
a new way of authentication supported by Uber for third parties. Previously, we
supported `auth_code` flow with oauth secret and now, we added support for pkce flow as well which
does not require the third party backend to maintain the oauth secret.

## How does Uber app return the result?
The result success or error is set in a bundle and returned as the activity result. The caller needs to make sure that the app link is invoked with `startActivityForResult` api. Check this [android documentation](https://developer.android.com/training/basics/intents/result) for references. If the caller does not use this api then we will not be able to validate the signature of the caller and it would result in an error response.

## Uber App versions that support applink flow
Rides - 4.482.10000+

Eats - 6.172.10000+

Driver - 4.447.10000+
