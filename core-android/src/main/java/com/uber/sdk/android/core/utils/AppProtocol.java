package com.uber.sdk.android.core.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Base64;
import android.util.Pair;
import com.uber.sdk.android.core.SupportedAppType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.uber.sdk.android.core.SupportedAppType.UBER;
import static com.uber.sdk.android.core.SupportedAppType.UBER_EATS;

public class AppProtocol {
    @Deprecated
    public static final String[] UBER_PACKAGE_NAMES =
            {"com.ubercab", "com.ubercab.presidio.app", "com.ubercab.presidio.exo", "com.ubercab.presidio.development"};

    @VisibleForTesting
    static final String[] RIDER_PACKAGE_NAMES =
            {"com.ubercab.presidio.development", "com.ubercab.presidio.exo", "com.ubercab.presidio.app", "com.ubercab"};
    @VisibleForTesting
    static final String[] EATS_PACKAGE_NAMES =
            {"com.ubercab.eats.debug", "com.ubercab.eats.exo", "com.ubercab.eats.nightly", "com.ubercab.eats"};
    public static final String PLATFORM = "android";

    private static final String UBER_RIDER_HASH = "411c40b31f6d01dac68d711df99b6eafeec8e73b";
    private static final String UBER_EATS_HASH = "ae0b86995f174533b423067837beba13d922fbb0";
    private static final String HASH_ALGORITHM_SHA1 = "SHA-1";
    private static final int DEFAULT_MIN_VERSION = 0;

    private static final HashSet<String> validAppSignatureHashes = buildAppSignatureHashes();

    @NonNull
    private static HashSet<String> buildAppSignatureHashes() {
        HashSet<String> set = new HashSet<>();
        set.add(UBER_RIDER_HASH);
        set.add(UBER_EATS_HASH);
        return set;
    }

    /**
     * @return Map of SupportedAppType and their respective package names.
     */
    @NonNull
    private static Map<SupportedAppType, List<String>> getSupportedPackageNames() {
        Map<SupportedAppType, List<String>> packageNames = new HashMap<>();
        packageNames.put(UBER, Arrays.asList(RIDER_PACKAGE_NAMES));
        packageNames.put(UBER_EATS, Arrays.asList(EATS_PACKAGE_NAMES));
        return packageNames;
    }

    /**
     * Validates minimum version of app required or returns true if in debug.
     */
    public boolean validateMinimumVersion(
            @NonNull Context context, @NonNull PackageInfo packageInfo, int minimumVersion) {
        if (isDebug(context)) {
            return true;
        }

        return packageInfo.versionCode >= minimumVersion;
    }

    /**
     * @deprecated Use {@link #isInstalled(Context, SupportedAppType)}.
     */
    @Deprecated
    public boolean isUberInstalled(@NonNull Context context) {
        return isInstalled(context, UBER);
    }

    /**
     * Verify if any version of the app has been installed on this device.
     *
     * @param context      A {@link Context}.
     * @param supportedApp The {@link SupportedAppType}.
     * @return {@code true} if any version of the app is installed (min version of 0).
     */
    public boolean isInstalled(@NonNull Context context, @NonNull SupportedAppType supportedApp) {
        return isInstalled(context, supportedApp, DEFAULT_MIN_VERSION);
    }

    /**
     * Check if the minimum version of {@link SupportedAppType} is installed.
     *
     * @param context        A {@link Context}.
     * @param supportedApp   The {@link SupportedAppType}.
     * @param minimumVersion The minimum version of {@link SupportedAppType} that must be installed.
     * @return {@code true} if any valid package for the app is installed.
     */
    public boolean isInstalled(@NonNull Context context, @NonNull SupportedAppType supportedApp, int minimumVersion) {
        return !getInstalledPackages(context, supportedApp, minimumVersion).isEmpty();

    }

    /**
     * Find the installed and validated packages for a {@link SupportedAppType}.
     * <p>
     * This will validate the signature and minimum version of the installed package or exclude it from returned list.
     *
     * @param context         A {@link Context}.
     * @param supportedApp   The {@link SupportedAppType}.
     * @param minimumVersion The minimum version of {@link SupportedAppType} that must be installed.
     * @return A list of {@link PackageInfo} which is installed and has been validated.
     */
    @NonNull
    public List<PackageInfo> getInstalledPackages(
            @NonNull Context context, @NonNull SupportedAppType supportedApp, int minimumVersion) {
        List<PackageInfo> packageInfos = new ArrayList<>();

        List<Pair<SupportedAppType, PackageInfo>> installedApps = getInstalledPackagesByApp(context, supportedApp);

        for (Pair<SupportedAppType, PackageInfo> installedApp : installedApps) {
            PackageInfo packageInfo = installedApp.second;
            if (packageInfo != null
                    && validateSignature(context, packageInfo.packageName)
                    && validateMinimumVersion(context, packageInfo, minimumVersion)) {
                packageInfos.add(packageInfo);
            }
        }
        return packageInfos;
    }

    @NonNull
    private List<Pair<SupportedAppType, PackageInfo>> getInstalledPackagesByApp(
            @NonNull Context context, @NonNull SupportedAppType selectedApp) {
        List<Pair<SupportedAppType, PackageInfo>> installedPackages = new ArrayList<>();

        for (String installedPackage : getSupportedPackageNames().get(selectedApp)) {
            if (PackageManagers.isPackageAvailable(context, installedPackage)) {
                installedPackages.add(
                        Pair.create(selectedApp, PackageManagers.getPackageInfo(context, installedPackage)));
            }
        }
        return installedPackages;
    }

    /**
     * @return true if the device supports App Links
     */
    public boolean isAppLinkSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Validates the app signature required or returns true if in debug.
     */
    public boolean validateSignature(@NonNull Context context, @NonNull String packageName) {
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
     * @throws NoSuchAlgorithmException thrown by {@link MessageDigest#getInstance(String)}.
     */
    @NonNull
    MessageDigest getSha1MessageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(HASH_ALGORITHM_SHA1);
    }

    private boolean isDebug(@NonNull Context context) {
        String brand = Build.BRAND;
        int applicationFlags = context.getApplicationInfo().flags;
        // We are debugging on an emulator, don't validate package signature.
        return (brand.startsWith("Android") || brand.startsWith("generic")) &&
                (applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
}
