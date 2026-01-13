package com.uber.sdk.android.core.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import androidx.annotation.NonNull;
import android.util.Log;

import com.uber.sdk.android.core.R;
import com.uber.sdk.android.core.UberSdk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {
    private static final String HASH_ALGORITHM_SHA1 = "SHA-1";


    public static String sha1hash(String key) {
        return hashWithAlgorithm(HASH_ALGORITHM_SHA1, key);
    }

    public static String sha1hash(byte[] bytes) {
        return hashWithAlgorithm(HASH_ALGORITHM_SHA1, bytes);
    }

    /**
     * Detects if the Application is currently in a Debug state
     */
    public static boolean isDebugable(@NonNull Context context) {
        return ( 0 != ( context.getApplicationInfo().flags & ApplicationInfo
                .FLAG_DEBUGGABLE ) );

    }

    /**
     * Logs error and when debug is enabled, shows Alert Dialog with debug instructions.
     *
     * @param activity
     * @param logMessage
     * @param alertTitle
     * @param alertMessage
     * @return true if developer error is shown, false otherwise.
     */
    public static boolean logAndShowBlockingDebugUIAlert(@NonNull Activity activity,
            final @NonNull String logMessage,
            final @NonNull String alertTitle,
            final @NonNull String alertMessage,
            final @NonNull RuntimeException exception) {
        Log.e(UberSdk.UBER_SDK_LOG_TAG, logMessage, exception);

        if(Utility.isDebugable(activity)) {
            new AlertDialog.Builder(activity)
                    .setTitle(alertTitle)
                    .setMessage(alertMessage)
                    .setNeutralButton(R.string.ub__alert_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            return true;
        }
        return false;
    }

    private static String hashWithAlgorithm(String algorithm, String key) {
        return hashWithAlgorithm(algorithm, key.getBytes());
    }

    private static String hashWithAlgorithm(String algorithm, byte[] bytes) {
        MessageDigest hash;
        try {
            hash = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return hashBytes(hash, bytes);
    }

    private static String hashBytes(MessageDigest hash, byte[] bytes) {
        hash.update(bytes);
        byte[] digest = hash.digest();
        StringBuilder builder = new StringBuilder();
        for (int b : digest) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString((b >> 0) & 0xf));
        }
        return builder.toString();
    }
}
