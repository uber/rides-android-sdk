package com.uber.sdk.android.rides;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static com.uber.sdk.android.rides.UberSdk.Region.CHINA;
import static com.uber.sdk.android.rides.UberSdk.Region.WORLD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UberSdkTest extends RobolectricTestBase {

    private static final String CLIENT_ID = "clientId";

    private SdkPreferences mSdkPreferences;

    @Before
    public void setup() {
        UberSdkAccessor.clearPrefs();
        mSdkPreferences = new SdkPreferences(RuntimeEnvironment.application);
    }

    @Test(expected = IllegalStateException.class)
    public void getClientId_whenNotInitialized_shouldThrowException() {
        UberSdk.getClientId();
    }

    @Test(expected = IllegalStateException.class)
    public void getServerToken_whenNotInitialized_shouldThrowException() {
        UberSdk.getServerToken();
    }

    @Test(expected = IllegalStateException.class)
    public void getRedirectUri_whenNotInitialized_shouldThrowException() {
        UberSdk.getRedirectUri();
    }

    @Test(expected = IllegalStateException.class)
    public void getRegion_whenNotInitialized_shouldThrowException() {
        UberSdk.getRegion();
    }

    @Test(expected = IllegalStateException.class)
    public void isSandboxMode_whenNotInitialized_shouldThrowException() {
        UberSdk.isSandboxMode();
    }

    @Test(expected = IllegalStateException.class)
    public void setRedirectUri_whenNotInitialized_shouldThrowException() {
        UberSdk.setRedirectUri("redirectUri");
    }

    @Test(expected = IllegalStateException.class)
    public void setRegion_whenNotInitialized_shouldThrowException() {
        UberSdk.setRegion(WORLD);
    }

    @Test(expected = IllegalStateException.class)
    public void setSandboxMode_whenNotInitialized_shouldThrowException() {
        UberSdk.setSandboxMode(true);
    }

    @Test
    public void getClientId_whenInitialized_shouldSucceed() {
        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);

        assertEquals("clientId", UberSdk.getClientId());
    }

    @Test
    public void getServerToken_whenInitialized_shouldSucceed() {
        mSdkPreferences.setServerToken("serverToken");

        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);

        assertEquals("serverToken", UberSdk.getServerToken());
    }

    @Test
    public void getRedirectUri_whenInitialized_shouldSucceed() {
        mSdkPreferences.setRedirectUri("redirectUri");

        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);

        assertEquals("redirectUri", UberSdk.getRedirectUri());
    }

    @Test
    public void getRegion_whenInitialized_shouldSucceed() {
        mSdkPreferences.setRegion(UberSdk.Region.CHINA);

        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);

        assertEquals(UberSdk.Region.CHINA, UberSdk.getRegion());
    }

    @Test
    public void isSandboxMode_whenInitialized_shouldSucceed() {
        mSdkPreferences.setSandboxMode(true);

        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);

        assertTrue(UberSdk.isSandboxMode());
    }

    @Test
    public void setRedirectUri_whenInitialized_shouldSucceed() {
        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);

        UberSdk.setRedirectUri("redirectUri");

        assertEquals("redirectUri", mSdkPreferences.getRedirectUri());
    }

    @Test
    public void setRegion_whenInitialized_shouldSucceed() {
        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);

        UberSdk.setRegion(CHINA);

        assertEquals(CHINA, mSdkPreferences.getRegion());
    }

    @Test
    public void setSandboxMode_whenInitialized_shouldSucceed() {
        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);

        UberSdk.setSandboxMode(true);

        assertTrue(mSdkPreferences.isSandboxMode());
    }

    @Test
    public void setServerToken_whenInitialized_shouldSucceed() {
        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);

        UberSdk.setServerToken("serverToken");

        assertEquals("serverToken", mSdkPreferences.getServerToken());
    }
}
