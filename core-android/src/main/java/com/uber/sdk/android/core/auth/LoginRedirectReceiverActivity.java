package com.uber.sdk.android.core.auth;

import android.app.Activity;
import android.os.Bundle;

public class LoginRedirectReceiverActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        // while this does not appear to be achieving much, handling the redirect in this way
        // ensures that we can remove the browser tab from the back stack. See the documentation
        // on AuthorizationManagementActivity for more details.
        startActivity(LoginActivity.newResponseIntent(
                this, getIntent().getData()));
        finish();
    }

}
