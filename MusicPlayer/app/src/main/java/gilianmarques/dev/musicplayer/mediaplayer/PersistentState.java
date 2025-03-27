package gilianmarques.dev.musicplayer.mediaplayer;


import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Criado por Gilian Marques
 * Quinta-feira, 02 de Janeiro de 2020  as 20:04:16.
 */
public class PersistentState {

    public HashMap<Long, Integer> positions;
    public ArrayList<Long> queueIds;
    public boolean shuffle;
    public RepeatMode repeatMode = RepeatMode.repeat_disabled;
    public int index;
    public long currentPosition;

    @UiThread
    public void save() {

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();

                Prefs.putString("positions", gson.toJson(positions));
                Prefs.putString("queueIds", gson.toJson(queueIds));
                Prefs.putString("repeatMode", gson.toJson(repeatMode));

                Prefs.putBoolean("shuffle", shuffle);

                Prefs.putInt("index", index);

                Prefs.putLong("currentPosition", currentPosition);

            }
        };
        new Thread(mRunnable).start();


    }

    @WorkerThread
    public void restore() {
        Gson gson = new Gson();

        String str_positions = Prefs.getString("positions", null);

        if (str_positions != null) {
            positions = gson.fromJson(str_positions, new TypeToken<HashMap<Long, Integer>>() {
            }.getType());
        }

        String str_queueIds = Prefs.getString("queueIds", null);

        if (str_queueIds != null) {
            queueIds = gson.fromJson(str_queueIds, new TypeToken<ArrayList<Long>>() {
            }.getType());
        }

        String str_repeatMode = Prefs.getString("repeatMode", null);

        if (str_repeatMode != null) {
            repeatMode = gson.fromJson(str_repeatMode, new TypeToken<RepeatMode>() {
            }.getType());
        }

        shuffle = Prefs.getBoolean("shuffle", false);
        index = Prefs.getInt("index", 0);
        currentPosition = Prefs.getLong("currentPosition", 0);
    }
}
