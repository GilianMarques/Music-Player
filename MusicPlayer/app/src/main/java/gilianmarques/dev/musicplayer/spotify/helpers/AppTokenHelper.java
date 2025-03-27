package gilianmarques.dev.musicplayer.spotify.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.BuildConfig;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.spotify.objects.AppAuth;
import gilianmarques.dev.musicplayer.utils.App;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Criado por Gilian Marques
 * Quinta-feira, 16 de Maio de 2019  as 19:25:44.
 */
public class AppTokenHelper {

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static String CL_ID = null;
    private static String CL_SEC = null;

    public static AppAuth mAppAuth;

    public static void init(@Nullable final Runnable runnable) {
        Log.d(App.myFuckingUniqueTAG + "AppTokenHelper", "init: Initializing AppTokenHelper class");
        if (mAppAuth == null)
            mAppAuth = new Gson().fromJson(Prefs.getString(c.spotify_auth, null), AppAuth.class);

        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mFirebaseRemoteConfig.setDefaults(R.xml.rem_config_defs);

        mFirebaseRemoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
                                                             .setMinimumFetchIntervalInSeconds(5)
                                                             .build());

        mFirebaseRemoteConfig.fetch().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override public void onSuccess(Void aVoid) {

                mFirebaseRemoteConfig.fetchAndActivate();
                CL_ID = mFirebaseRemoteConfig.getString("CL_ID");
                CL_SEC = mFirebaseRemoteConfig.getString("CL_SEC");
                if (BuildConfig.DEBUG)
                    Log.d(App.myFuckingUniqueTAG + "AppTokenHelper", "onComplete: " + CL_ID + ":" + CL_SEC);
                if (CL_ID != null && !CL_ID.equals("null")) {
                    if (runnable != null) runnable.run();
                } else init(runnable);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override public void onFailure(@NonNull Exception e) {
                Log.d(App.myFuckingUniqueTAG + "AppTokenHelper", "onComplete: Error loading remoteConfig: " + e.getMessage());
                Toasty.error(App.binder.get(), App.binder.get().getString(R.string.Erro_carregando_remote_config), 3000).show();
            }
        });

    }


    public static void refreshToken(@Nullable final AppTokenHelper.CallbackI callbackI1) {

        if (CL_ID == null) {
            init(new Runnable() {
                @Override public void run() {
                    refreshToken(callbackI1);
                }
            });
            return;
        }

        byte[] bytes = (CL_ID + ":" + CL_SEC).getBytes();
        String encodedData = Base64.encodeToString(bytes, Base64.DEFAULT);
        String authorizationHeaderString = "Basic " + encodedData.replace("\n", "");

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        final Request request = new Request.Builder()
                .url(TOKEN_URL)
                .addHeader("Authorization", authorizationHeaderString)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build();


        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, IOException e) {
                Log.d(App.myFuckingUniqueTAG + "RedirectActivity", "onFailure: " + e.getMessage());
                if (callbackI1 != null) callbackI1.done(false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (mAppAuth == null) mAppAuth = new AppAuth();
                // json returned here may vary so i have to check vars
                final String resp = response.body().string();
                mAppAuth = new Gson().fromJson(resp, AppAuth.class);
                //put update object
                Prefs.putString(c.spotify_auth, new Gson().toJson(mAppAuth));

                if (callbackI1 != null) callbackI1.done(true);

            }
        });
    }

    public interface CallbackI {
        void done(boolean success);
    }
}


