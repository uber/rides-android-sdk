package com.uber.sdk.android.core.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import javax.annotation.Nullable;

public class AppProtocol {
    public static final String[] UBER_PACKAGE_NAMES =
            {"com.ubercab", "com.ubercab.presidio.app", "com.ubercab.presidio.exo"};
    public static final String DEEPLINK_SCHEME = "uber";
    public static final String PLATFORM = "android";

    private static final String UBER_RIDER_HASH = "411c40b31f6d01dac68d711df99b6eafeec8e73b";
    private static final String HASH_ALGORITHM_SHA1 = "SHA-1";

    private static final HashSet<String> validAppSignatureHashes = buildAppSignatureHashes();

    private static HashSet<String> buildAppSignatureHashes() {
        HashSet<String> set = new HashSet<>();
        set.add(UBER_RIDER_HASH);
        return set;
    }

    /**
     * Validates minimum version of app required or returns true if in debug.
     */
    public boolean validateMinimumVersion(Context context, PackageInfo packageInfo, int minimumVersion) {
        if (isDebug(context)) {
            return true;
        }

        return packageInfo.versionCode >= minimumVersion;
    }

    /**
     * Validates the app signature required or returns true if in debug.
     */
    @SuppressLint("PackageManagerGetSignatures")
    public boolean validateSignature(Context context, String packageName) {
        if (isDebug(context)) {
            return true;
        }

        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        for (Signature signature : packageInfo.signatures) {
            String hashedSignature = Utility.sha1hash(signature.toByteArray());
            if (!validAppSignatureHashes.contains(hashedSignature)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the Application Signature of the associated {@link Context} or null if it cannot be fetched.
     */
    @Nullable
    public String getAppSignature(@NonNull Context context) {
        final PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }

        if (packageInfo == null || packageInfo.signatures.length == 0) {
            return null;
        }

        final MessageDigest messageDigest;
        try {
            messageDigest = getSha1MessageDigest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        messageDigest.update(packageInfo.signatures[0].toByteArray());
        return Base64.encodeToString(messageDigest.digest(), Base64.NO_WRAP);
    }

    /**
     * Gets an instance of {@link MessageDigest} with the SHA-1 Algorithm.
     *
     * @return the message digest.
     * @throws NoSuchAlgorithmException
     */
    @NonNull
    MessageDigest getSha1MessageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(HASH_ALGORITHM_SHA1);
    }

    private boolean isDebug(Context context) {
        String brand = Build.BRAND;
        int applicationFlags = context.getApplicationInfo().flags;
        if ((brand.startsWith("Android") || brand.startsWith("generic")) &&
                (applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            // We are debugging on an emulator, don't validate package signature.
            return true;
        }
        return false;
    }
}
