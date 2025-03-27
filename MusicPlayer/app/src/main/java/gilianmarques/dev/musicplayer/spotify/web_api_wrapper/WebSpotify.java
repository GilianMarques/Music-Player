package gilianmarques.dev.musicplayer.spotify.web_api_wrapper;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.spotify.helpers.AppTokenHelper;
import gilianmarques.dev.musicplayer.spotify.objects.AlbunsResult;
import gilianmarques.dev.musicplayer.spotify.objects.AppAuth;
import gilianmarques.dev.musicplayer.spotify.objects.ArtistsResult;
import gilianmarques.dev.musicplayer.spotify.objects.TracksResult;
import gilianmarques.dev.musicplayer.spotify.objects.WebSpotifyCache;
import gilianmarques.dev.musicplayer.utils.App;
import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Criado por Gilian Marques
 * Segunda-feira, 03 de Junho de 2019  as 21:14:38.
 */
public class WebSpotify {
    private AppAuth mUserAuth;
    private String endPoint = "https://api.spotify.com/v1/search";

    public WebSpotify() {
        mUserAuth = AppTokenHelper.mAppAuth;
    }

    public void search(final QueryType type, final String query, final WebCallback callback) {
        final Runnable mRunnable = new Runnable() {
            @Override
            public void run() {


                HttpUrl.Builder urlBuilder = HttpUrl.parse(endPoint).newBuilder();
                urlBuilder.addQueryParameter("q", query);
                urlBuilder.addQueryParameter("type", type.value);

                final Request request = new Request.Builder()
                        .url(urlBuilder.build().toString())
                        .header("Authorization", "Bearer " + mUserAuth.getAccess_token())
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .build();

                final String url = request.url().toString();
                WebSpotifyCache cache = Realm.getDefaultInstance().where(WebSpotifyCache.class).equalTo("url", url).findFirst();

                if (cache != null) {
                    Log.d(App.myFuckingUniqueTAG + "WebSpotify", "run: results from cache.");

                    if (type.value.equals(QueryType.track.value)) {
                        TracksResult result = new Gson().fromJson(cache.getJson(), TracksResult.class);
                        callback.tracksRes(result, true);
                    } else if (type.value.equals(QueryType.album.value)) {
                        AlbunsResult result = new Gson().fromJson(cache.getJson(), AlbunsResult.class);
                        callback.albunsRes(result, true);
                    } else if (type.value.equals(QueryType.artist.value)) {
                        ArtistsResult result = new Gson().fromJson(cache.getJson(), ArtistsResult.class);
                        callback.artistsRes(result, true);
                    }

                } else new OkHttpClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.d(App.myFuckingUniqueTAG + "WebSpotify", "onFailure: " + e.getLocalizedMessage());

                        if (type.value.equals(QueryType.track.value)) {
                            callback.tracksRes(null, false);
                        } else if (type.value.equals(QueryType.album.value)) {
                            callback.albunsRes(null, false);
                        } else if (type.value.equals(QueryType.artist.value)) {
                            callback.artistsRes(null, false);
                        }
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        final String json = response.body() != null ? response.body().string() : null;

                        if (json != null) {
                            UIRealm.get().executeTransactionAsync(new Realm.Transaction() {
                                @Override public void execute(@NonNull Realm realm) {
                                    realm.insertOrUpdate(new WebSpotifyCache(url, json));
                                }
                            });
                        } else {
                            callback.tracksRes(null, false);
                            return;
                        }

                        if (type.value.equals(QueryType.track.value)) {
                            TracksResult result = new Gson().fromJson(json, TracksResult.class);
                            callback.tracksRes(result, true);
                        } else if (type.value.equals(QueryType.album.value)) {
                            AlbunsResult result = new Gson().fromJson(json, AlbunsResult.class);
                            callback.albunsRes(result, true);
                        } else if (type.value.equals(QueryType.artist.value)) {
                            ArtistsResult result = new Gson().fromJson(json, ArtistsResult.class);
                            callback.artistsRes(result, true);
                        }
                    }
                });


            }
        };

        if (mUserAuth.isExpired()) AppTokenHelper.refreshToken(new AppTokenHelper.CallbackI() {
            @Override public void done(boolean success) {
                mRunnable.run();
            }
        });
        else mRunnable.run();


    }

    public enum QueryType {
        album("album"), artist("artist"), track("track");

        String value;

        QueryType(String s) {
            value = s;
        }

    }

    public static class WebCallback {
        public void albunsRes(@Nullable AlbunsResult result, boolean success) {
        }

        public void tracksRes(@Nullable TracksResult result, boolean success) {
        }

        public void artistsRes(ArtistsResult result, boolean success) {

        }
    }
}
