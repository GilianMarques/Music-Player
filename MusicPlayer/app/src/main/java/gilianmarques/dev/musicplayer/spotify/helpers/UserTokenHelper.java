package gilianmarques.dev.musicplayer.spotify.helpers;

import android.content.Intent;
import android.net.Uri;
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
import gilianmarques.dev.musicplayer.spotify.activity.RedirectActivity;
import gilianmarques.dev.musicplayer.spotify.objects.UserAuth;
import gilianmarques.dev.musicplayer.utils.App;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Criado por Gilian Marques
 * Quinta-feira, 16 de Maio de 2019  as 19:25:44.
 */
public class UserTokenHelper {

    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final String AUTHORIZE_URL = "https://accounts.spotify.com/authorize";
    private static String CL_ID = null;
    private static String CL_SEC = null;
    private static final String REDIRECT_URI = "gilianmarquesdevmusicplayer://callback110520191425";

    public static UserAuth mUserAuthHelper;

    public static void init(@Nullable final Runnable runnable) {
        Log.d(App.myFuckingUniqueTAG + "AppTokenHelper", "init: Initializing AppTokenHelper class");
        if (mUserAuthHelper == null)
            mUserAuthHelper = new Gson().fromJson(Prefs.getString(c.spotify_auth, null), UserAuth.class);

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
                if (CL_ID != null&&!CL_ID.equals("null")) {
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


    /**
     * Make the request to auth end point to get authorization code then it receives an url to send user to auth screen
     * It opens the browser an then call {@link RedirectActivity}. The flows continues on onCreate method who calls fetchTokens method
     */
    public static void makeRequest() {

        if (CL_ID == null || CL_SEC == null) {
            init(new Runnable() {
                @Override public void run() {
                    makeRequest();
                }
            });
            return;
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse(UserTokenHelper.AUTHORIZE_URL).newBuilder();
        urlBuilder.addQueryParameter("client_id", UserTokenHelper.CL_ID);
        urlBuilder.addQueryParameter("response_type", "code");
        urlBuilder.addQueryParameter("redirect_uri", REDIRECT_URI);

        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .build();


        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("RedirectActivity.onFailure " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String[] strResponse = response.toString().split(",");
                String url = "";
                for (String s : strResponse) {
                    if (s.contains("url=http")) {
                        url = s.replace("{", "").replace("}", "").replace("url=", "").replace(" ", "");
                        break;
                    }

                }

                if (!url.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    App.binder.get().getActivity().startActivity(browserIntent);
                    Prefs.putLong(c.spotify_req_stamp, System.currentTimeMillis());
                } else {
                    Toasty.error(App.binder.get(), "Erro", 3200).show();
                }

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
                .add("grant_type", "refresh_token")
                .add("refresh_token", mUserAuthHelper.getRefresh_token())
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

                // json returned hre may vary so i have to check vars
                UserAuth pojo = new Gson().fromJson(response.body().string(), UserAuth.class);

                if (pojo.getAccess_token() != null)
                    mUserAuthHelper.setAccess_token(pojo.getAccess_token());
                if (pojo.getToken_type() != null)
                    mUserAuthHelper.setToken_type(pojo.getToken_type());
                if (pojo.getExpires_in() > 0)
                    mUserAuthHelper.setExpires_in(System.currentTimeMillis() + (pojo.getExpires_in() * 1000));
                if (pojo.getRefresh_token() != null)
                    mUserAuthHelper.setRefresh_token(pojo.getRefresh_token());

                //put update object
                Prefs.putString(c.spotify_auth, new Gson().toJson(mUserAuthHelper));

                if (callbackI1 != null) callbackI1.done(true);

            }
        });
    }


    /**
     * @param code The auth code obtained from user auth after user consent screen
     *             <p>
     *             It will use the received code to obtain a new token to be used. It will wrap the
     *             fetched data in an object, store it in prefs under 'spotify_auth' key and set as static reference
     *             in this class.
     */
    public static void fetchTokens(String code, @javax.annotation.Nullable final CallbackI callbackI1) {

        byte[] bytes = (CL_ID + ":" + CL_SEC).getBytes();
        String encodedData = Base64.encodeToString(bytes, Base64.DEFAULT);
        String authorizationHeaderString = "Basic " + encodedData.replace("\n", "");

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
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

                UserAuth pojo = new Gson().fromJson(response.body().string(), UserAuth.class);
                pojo.setExpires_in((/*convert secs to millis*/pojo.getExpires_in() * 1000) + System.currentTimeMillis());// 1 hour from now
                Prefs.putString(c.spotify_auth, new Gson().toJson(pojo));
                mUserAuthHelper = pojo;
                if (callbackI1 != null) callbackI1.done(true);

            }
        });
    }

    public interface CallbackI {
        void done(boolean success);
    }
}


