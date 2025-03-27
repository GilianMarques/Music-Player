package gilianmarques.dev.musicplayer.lyrics;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import gilianmarques.dev.musicplayer.lyrics.models.Lyric;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Domingo, 01 de Julho de 2018  as 20:30:15.
 *
 * @Since 0.1 Beta
 */
class Vagalume {


    private static final String BASE_URL = "https://api.vagalume.com.br/search.php";
    private static final String API_KEY = "f247bf4073c6a7b935a7804faac96f90";
    private final Track mTrack;
    private final Activity mActivity;

    @WorkerThread
    public Vagalume(Track mTrack, Activity mActivity) {

        this.mTrack = mTrack;
        this.mActivity = mActivity;
    }

    @WorkerThread
    public void getLyric(final Callback callback) {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                String url = createURL();
                Log.d(App.myFuckingUniqueTAG + "Vagalume", "getLyric: URL:  " + url);

                String jsonLyric = fetchJson(url);
                if (jsonLyric == null || jsonLyric.isEmpty())
                    mActivity.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            callback.done(null);
                        }
                    });
                Lyric lyric = null;

                try {
                    lyric = createLyric(jsonLyric);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final Lyric finalLyric = lyric;
                mActivity.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        callback.done(finalLyric);
                    }
                });
            }
        };
        new Thread(mRunnable).start();
    }

    @NonNull
    private String createURL() {
        String artistName = mTrack.getArtistName().replace("&", "%26");
        String trackName = mTrack.getTitle().replace("&", "%26");

        return BASE_URL.concat("?art=").concat(artistName)
                .concat("&mus=").concat(trackName)
                .concat("&apikey={".concat(API_KEY).concat("}"));
    }

    @Nullable
    private static String fetchJson(String url) {
        /*ultilizo instancias de HttpURLConnection + InputStreamReader + BufferedReader para obter o Json da nuvem*/

        HttpURLConnection urlConnection = null;
        StringBuilder mStringBuilder = new StringBuilder();

        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();

            InputStream mInputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mInputStream));

            String line;
            while ((line = mReader.readLine()) != null) {
                mStringBuilder.append(line);
            }

            return mStringBuilder.toString();

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
    }

    @Nullable
    private Lyric createLyric(String strJson) throws JSONException {
        if (strJson == null) return null;
        JSONObject raw = new JSONObject(strJson);

        String type = raw.getString("type");
        if (!type.equals("exact")) return null;

        JSONObject lyricInfo = raw.getJSONArray("mus").getJSONObject(0);
        JSONObject translation = lyricInfo.getJSONArray("translate").getJSONObject(0);


        String url = lyricInfo.getString("url");
        String original = lyricInfo.getString("text");
        String translated = translation.getString("text");

        Lyric mLyric = Lyric.from(url, original, translated);
        mLyric.setFont(Lyric.FONT_VAGALUME);
        return mLyric;

    }

    public interface Callback {
        void done(Lyric lyric);
    }

}
