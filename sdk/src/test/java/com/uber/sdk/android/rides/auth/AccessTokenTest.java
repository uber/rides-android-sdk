package com.uber.sdk.android.rides.auth;

import android.os.Parcel;

import com.google.common.collect.ImmutableList;
import com.uber.sdk.android.rides.RobolectricTestBase;

import org.junit.Test;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccessTokenTest extends RobolectricTestBase {

    private static final Collection<Scope> SCOPES = ImmutableList.of(Scope.PROFILE, Scope.HISTORY);
    private static final Date EXPIRATION_DATE = new Date(1458770906206l);
    private static final String ACCESS_TOKEN = "thisIsAnAccessToken";

    @Test
    public void writeToParcelAndCreateFromParcel_shouldWork() {
        Parcel parcel = Parcel.obtain();
        new AccessToken(EXPIRATION_DATE, SCOPES, ACCESS_TOKEN).writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        AccessToken accessTokenFromParcel = new AccessToken(parcel);
        assertEquals(ACCESS_TOKEN, accessTokenFromParcel.getToken());
        assertEquals(EXPIRATION_DATE.getTime(), accessTokenFromParcel.getExpirationTime().getTime());
        assertTrue(accessTokenFromParcel.getScopes().containsAll(SCOPES));
    }
}
