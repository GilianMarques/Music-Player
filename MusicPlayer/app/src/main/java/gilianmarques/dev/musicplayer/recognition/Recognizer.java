package gilianmarques.dev.musicplayer.recognition;

import android.util.Log;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.ACRCloudResult;
import com.acrcloud.rec.sdk.IACRCloudListener;
import com.acrcloud.rec.sdk.IACRCloudResultWithAudioListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Domingo, 19 de Maio de 2019  as 14:21:22.
 */
public class Recognizer   {
    ACRCloudClient client;




    public void Recognizer() {


        client = new ACRCloudClient();
        ACRCloudConfig config = new ACRCloudConfig();

        config.accessKey = "7d31713ef9c2c9c9ed4cf4d2d953879a";
        config.accessSecret = "FNCeq49MdiG5qWE203mZanrW29hlKRUaFRS1ubtW";
        config.host = "identify-eu-west-1.acrcloud.com";
        config.context = App.binder.get();
        config.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;

        config.acrcloudResultWithAudioListener = new IACRCloudResultWithAudioListener() {
            @Override public void onResult(ACRCloudResult acrCloudResult) {
                Log.d(App.myFuckingUniqueTAG + "Recognizer", "onResult: 1" + acrCloudResult.getResult());
            }

            @Override public void onVolumeChanged(double v) {
                Log.d(App.myFuckingUniqueTAG + "Recognizer", "onVolumeChanged: 1" + v);
            }
        };

        config.acrcloudListener = new IACRCloudListener() {
            @Override public void onResult(String s) {
                Log.d(App.myFuckingUniqueTAG + "Recognizer", "onResult: 2 " + s);
            }

            @Override public void onVolumeChanged(double v) {
                Log.d(App.myFuckingUniqueTAG + "Recognizer", "onVolumeChanged: " + v);
            }
        };

        Log.d(App.myFuckingUniqueTAG + "Recognizer", "Recognizer: chicking: Initialized? " + client.initWithConfig(config));


        final Track track = new NativeTracks(false).getTracksByName("Stressed out", 1).get(0);
        Log.d(App.myFuckingUniqueTAG + "Recognizer", "Recognizer:  " + track.getTitle() + " " + track.getAlbumName() + "  " + track.getArtistName());
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    recognize(track.getFilePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(mRunnable).start();
    }

    private void recognize(String path) throws IOException {


        File file = new File(path);
        byte[] b = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(b);


        String result = client.recognize(b, b.length);
        Log.d(App.myFuckingUniqueTAG + "Recognizer", "recognize: " + result + " <> " + b.length);

    }
}
