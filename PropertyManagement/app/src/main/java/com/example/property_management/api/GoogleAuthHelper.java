package com.example.property_management.api;

import android.app.Activity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;

public class GoogleAuthHelper {
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private Activity activity;

    public GoogleAuthHelper(Activity activity) {
        this.activity = activity;
        init();
    }

    private void init() {
//        oneTapClient = Identity.getSignInClient(activity);
////        signInRequest = BeginSignInRequest.builder()
////                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
////                        .setSupported(true)
////                        .build())
////                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
////                        .setSupported(true)
////                        .setServerClientId(activity.getString(R.string.default_web_client_id))
////                        .setFilterByAuthorizedAccounts(true)
////                        .build())
////                .setAutoSelectEnabled(true)
////                .build();
//        signInRequest = BeginSignInRequest.builder()
//                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                        .setSupported(true)
//                        // Your server's client ID, not your Android client ID.
//                        .setServerClientId(getString(R.string.default_web_client_id))
//                        // Only show accounts previously used to sign in.
//                        .setFilterByAuthorizedAccounts(true)
//                        .build())
//                .build();
    }

    public SignInClient getOneTapClient() {
        return oneTapClient;
    }

    public BeginSignInRequest getSignInRequest() {
        return signInRequest;
    }
}
