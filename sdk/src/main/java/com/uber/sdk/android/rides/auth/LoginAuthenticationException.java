package com.uber.sdk.android.rides.auth;

/**
 * Exception to throw when generating an {@link AccessToken} and encountering an error.
 */
class LoginAuthenticationException extends Exception {
    private AuthenticationError mAuthenticationError;

    /**
     * Construct the exception with an {@link AuthenticationError}
     */
    LoginAuthenticationException(AuthenticationError error) {
        super(error.toString());

        mAuthenticationError = error;
    }

    /**
     * Gets the {@link AuthenticationError} for the exception
     */
    AuthenticationError getAuthenticationError() {
        return mAuthenticationError;
    }
}
