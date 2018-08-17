package com.uber.sdk.android.core.utils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import com.uber.sdk.android.core.RobolectricTestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.Robolectric;

import java.security.NoSuchAlgorithmException;

import static junit.framework.Assert.assertFalse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AppProtocolTest extends RobolectricTestBase {

    private static final String GOOD_SIGNATURE =
            "3082022730820190a00302010202044cb88a8e300d06092a864886f70d01010505003057311330110603550408130a43616c696" +
                    "66f726e6961311630140603550407130d53616e204672616e636973636f3110300e060355040a130755626572436162" +
                    "311630140603550403130d4a6f7264616e20426f6e6e65743020170d3130313031353137303833305a180f323036303" +
                    "13030323137303833305a3057311330110603550408130a43616c69666f726e6961311630140603550407130d53616e" +
                    "204672616e636973636f3110300e060355040a130755626572436162311630140603550403130d4a6f7264616e20426" +
                    "f6e6e657430819f300d06092a864886f70d010101050003818d00308189028181009769b8ee7e4af5eae5bfbac410a0" +
                    "b0daf8d58ca8c9503878cbfb9461d617b2a5695a639962492ee7f5938f036c7927e4e1a680f186d98fdebf38955fb3f" +
                    "c23077bd3ff39551cdb35690fd451411c643b26f31d280dc4a55b501e9a0d53d8f8f72a407854516f0f2a4e4d48c02b" +
                    "dfae408d162a5da34397f845ddfa17de57cd3d0203010001300d06092a864886f70d010105050003818100283f752dc" +
                    "67c2d8ea2a7e47b1269b2cb37f961c53db3d1c9158af0722978f6a3c396149447557fcf63caa497a795514922f3a4e8" +
                    "5990608c47d90955ce9cc71f93199a5f3c7624cca8fac70ff70b1e4cf9eb887a92f358aa21ba42e0e86bbecf7d030d8" +
                    "1a383b716f22ac98746f2956e90b96e8f35d298498e55cdbe4d42a762";

    private static final String GOOD_HASH = "QRxAsx9tAdrGjXEd+Ztur+7I5zs=";

    private static final String BAD_SIGNATURE =
            "3082022730820190a00302010202044cb88a8e300d06092a864886f70d01010505003057311330110603550408130a43616c696" +
                    "66f726e6961311630140603550407130d53616e204672616e636973636f3110300e060355040a130755626572436162" +
                    "311630140603550403130d4a6f7264616e20426f6e6e65743020170d3130313031353137303833305a180f323036303" +
                    "13030323137303833305a3057311330110603550408130a43616c69666f726e6961311630140603550407130d53616e" +
                    "204672616e636973636f3110300e060355040a130755626572436162311630140603550403130d4a6f7264616e20426" +
                    "f6e6e657430819f300d06092a864886f70d010101050003818d00308189028181009769b8ee7e4af5eae5bfbac410a0" +
                    "b0daf8d58ca8c9503878cbfb9461d617b2a5695a639962492ee7f5938f036c7927e4e1a680f186d98fdebf38955fb3f" +
                    "c23077bd3ff39551cdb35690fd451411c643b26f31d280dc4a55b501e9a0d53d8f8f72a407854516f0f2a4e4d48c02b" +
                    "dfae408d162a5da34397f845ddfa17de57cd3d0203010001300d06092a864886f70d010105050003818100283f752dc" +
                    "67c2d8ea2a7e47b1269b2cb37f961c53db3d1c9158af0722978f6a3c396149447557fcf63caa497a795514922f3a4e8" +
                    "5990608c47d90955ce9cc71f93199a5f3c7624cca8fac70ff70b1e4cf9eb887a92f358aa21ba42e0e86bbecf7d030d8" +
                    "1a383b716f22ac98746f2956e90b96e8f35d298498e55cdbe4d42a764";

    @Mock
    PackageManager packageManager;
    @Mock
    PackageInfo packageInfo;

    private Activity activity;
    private AppProtocol appProtocol;

    @Before
    public void setUp() {
        activity = spy(Robolectric.setupActivity(Activity.class));
        appProtocol = new AppProtocol();
    }

    @Test
    public void validateSignature_whenValid_returnsTrue() {
        stubAppSignature(GOOD_SIGNATURE);
        assertTrue(appProtocol.validateSignature(activity, AppProtocol.RIDER_PACKAGE_NAMES[0]));
    }

    @Test
    public void validateSignature_whenInvalid_returnsFalse() {
        stubAppSignature(BAD_SIGNATURE);
        assertFalse(appProtocol.validateSignature(activity, AppProtocol.RIDER_PACKAGE_NAMES[0]));
    }

    @Test
    public void validateSignature_whenGoodAndBad_returnsFalse() {
        stubAppSignature(GOOD_SIGNATURE, BAD_SIGNATURE);
        assertFalse(appProtocol.validateSignature(activity, AppProtocol.RIDER_PACKAGE_NAMES[0]));
    }

    @Test
    public void getApplicationSignature_whenValidPackageSignature_shouldSucceed() {
        stubAppSignature(GOOD_SIGNATURE);
        assertThat(appProtocol.getAppSignature(activity)).isEqualTo(GOOD_HASH);
    }

    @Test
    public void getPackageSignature_whenNameNotFoundException_shouldReturnNull() throws Exception {
        stubAppSignature(GOOD_SIGNATURE);
        final Throwable throwable = new PackageManager.NameNotFoundException();
        doThrow(throwable).when(packageManager)
                .getPackageInfo(AppProtocol.RIDER_PACKAGE_NAMES[0], PackageManager.GET_SIGNATURES);

        assertThat(appProtocol.getAppSignature(activity)).isNull();
    }

    @Test
    public void getPackageSignature_whenNullPackageInfo_shouldReturnNull() throws Exception {
        stubAppSignature(GOOD_SIGNATURE);
        when(packageManager.getPackageInfo(AppProtocol.RIDER_PACKAGE_NAMES[0], PackageManager.GET_SIGNATURES))
                .thenReturn(null);

        assertThat(appProtocol.getAppSignature(activity)).isNull();
    }

    @Test
    public void getPackageSignature_whenEmptyPackageInfo_shouldReturnNull() {
        stubAppSignature(GOOD_SIGNATURE);
        packageInfo.signatures = new Signature[]{};

        assertThat(appProtocol.getAppSignature(activity)).isNull();
    }

    @Test
    public void getPackageSignature_whenNoSuchAlgorithmException_shouldReturnNull() throws Exception {
        stubAppSignature(GOOD_SIGNATURE);

        final Throwable throwable = new NoSuchAlgorithmException();
        appProtocol = spy(appProtocol);
        doThrow(throwable).when(appProtocol).getSha1MessageDigest();

        assertThat(appProtocol.getAppSignature(activity)).isNull();
    }

    private void stubAppSignature(String... sig) {
        when(activity.getPackageName()).thenReturn(AppProtocol.RIDER_PACKAGE_NAMES[0]);
        when(activity.getPackageManager()).thenReturn(packageManager);

        Signature[] signatures = new Signature[sig.length];
        for (int i = 0; i < sig.length; i++) {
            signatures[i] = new Signature(sig[i]);
        }

        packageInfo.signatures = signatures;

        try {
            when(packageManager.getPackageInfo(eq(AppProtocol.RIDER_PACKAGE_NAMES[0]), anyInt()))
                    .thenReturn(packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            fail("Unable to mock Package Manager");
        }
    }
}