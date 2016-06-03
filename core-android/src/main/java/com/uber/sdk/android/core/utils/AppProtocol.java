package com.uber.sdk.android.core.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.util.HashSet;

public class AppProtocol {
    public static final String UBER_PACKAGE_NAME = "com.ubercab";
    public static final String DEEPLINK_SCHEME = "uber";
    public static final String PLATFORM = "android";

    private static final String UBER_RIDER_HASH = "411c40b31f6d01dac68d711df99b6eafeec8e73b";

    private static final HashSet<String> validAppSignatureHashes = buildAppSignatureHashes();

    private static HashSet<String> buildAppSignatureHashes() {
        HashSet<String> set = new HashSet<>();
        set.add(UBER_RIDER_HASH);
        return set;
    }
    @SuppressLint("PackageManagerGetSignatures")
    public boolean validateSignature(Context context, String packageName) {
        String brand = Build.BRAND;
        int applicationFlags = context.getApplicationInfo().flags;
        if (brand.startsWith("generic") &&
                (applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            // We are debugging on an emulator, don't validate package signature.
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
}
