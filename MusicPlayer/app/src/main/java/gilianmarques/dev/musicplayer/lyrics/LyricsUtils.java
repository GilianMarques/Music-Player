package gilianmarques.dev.musicplayer.lyrics;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.lyrics.models.Lyric;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Domingo, 01 de Julho de 2018  as 20:35:22.
 *
 * @Since 0.1 Beta
 */
public class LyricsUtils {
    private final static String EXTENSION = ".lrc";
    private final String FOLDER_NAME = App.binder.get().getString(R.string.app_name).concat("_lyrics");
    private final String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            .getPath().concat(File.separator)
            .concat(FOLDER_NAME).concat(File.separator);

    public boolean writeLyric(long fileId, Lyric mLyric) {


        File folder = new File(PATH);

        if (!folder.exists()) if (!folder.mkdirs()) {
            Log.d("LyricsUtils", "writeLyric: ERRO _1");
            return false;
        }

        final File lrc = new File(PATH, String.valueOf(fileId).concat(EXTENSION));


        try {


            FileOutputStream fOut = new FileOutputStream(lrc);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(mLyric.asString());
            myOutWriter.close();
            fOut.flush();
            fOut.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


    }

    private Lyric readLyric(long fileId) {

        File file = new File(PATH, String.valueOf(fileId).concat(EXTENSION));

        try {

            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }

            br.close();
            return Lyric.fromString(text.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static void getLyric(final Track mTrack, final Activity activity, final Callback callback) {

        final Runnable mRunnableOnline = new Runnable() {
            @Override
            public void run() {
                new Vagalume(mTrack, activity).getLyric(new Vagalume.Callback() {
                    @Override public void done(final Lyric lyric) {
                        if (lyric != null) {
                            // put lyric so that it will be available offline next time!
                            Runnable mRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    new LyricsUtils().writeLyric(mTrack.getId(), lyric);
                                }
                            };
                            new Thread(mRunnable).start();
                        }

                        callback.done(lyric);
                    }
                });
            }
        };


        Runnable mRunnableLocal = new Runnable() {
            @Override
            public void run() {
                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Lyric lyric = new LyricsUtils().readLyric(mTrack.getId());
                        if (lyric != null) callback.done(lyric);

                            // if lyric does not exist offline, try to download it
                        else new Thread(mRunnableOnline).start();
                    }
                };
                activity.runOnUiThread(mRunnable);
            }
        };

        // try to find a lyric at internal storage
        new Thread(mRunnableLocal).start();


    }

    public interface Callback {
        void done(Lyric lyric);
    }

}
