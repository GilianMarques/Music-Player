package gilianmarques.dev.musicplayer.spotify.fetcher;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import gilianmarques.dev.musicplayer.spotify.helpers.AppTokenHelper;
import gilianmarques.dev.musicplayer.spotify.objects.ArtistsResult;
import gilianmarques.dev.musicplayer.spotify.web_api_wrapper.WebSpotify;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;


/**
 * Criado por Gilian Marques
 * Sábado, 11 de Maio de 2019  as 16:58:50.
 */
public class ArtistFetcher {
    private static final ArtistFetcher ourInstance = new ArtistFetcher();

    public static ArtistFetcher get() {
        return ourInstance;
    }


    public void fetchArtURL(final String artistName, final Callback callback) {
        if (!Utils.canConnect() || AppTokenHelper.mAppAuth == null)
            callback.onFetch("", false);
        else
            new WebSpotify().search(WebSpotify.QueryType.artist, artistName, new WebSpotify.WebCallback() {
                @Override
                public void artistsRes(final ArtistsResult result, final boolean success) {
                    Log.d(App.myFuckingUniqueTAG + "ArtistFetcher", "artistsRes: " + success);

                    App.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (result == null || result.artists == null) {
                                callback.onFetch("", false);
                                return;
                            }
                            ArrayList<ArtistsResult.Items> artists = result.artists.items;
                            if (artists.size() > 0) {
                                ArtistsResult.Items spotArtist = artists.get(0);
                                List<ArtistsResult.Images> images = spotArtist.images;
                                if (images.size() > 0) {
                                    callback.onFetch(images.get(0).url, true);
                                } else callback.onFetch("", true);
                            }
                            callback.onFetch("", true);


                        }
                    });
                }
            });

    }


    public interface Callback {

        void onFetch(String url, boolean fetchSuccess);
    }


}
