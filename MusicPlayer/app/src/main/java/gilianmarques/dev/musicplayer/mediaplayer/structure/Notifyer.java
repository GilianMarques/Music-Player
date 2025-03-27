package gilianmarques.dev.musicplayer.mediaplayer.structure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Timer;
import java.util.TimerTask;

import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.utils.App;

import static gilianmarques.dev.musicplayer.mediaplayer.structure.Const.FORCE_NOTIFY;
import static gilianmarques.dev.musicplayer.mediaplayer.structure.Const.PLAY_PAUSE;
import static gilianmarques.dev.musicplayer.mediaplayer.structure.Const.PROGRESS;
import static gilianmarques.dev.musicplayer.mediaplayer.structure.Const.RELEASING;
import static gilianmarques.dev.musicplayer.mediaplayer.structure.Const.STOP;
import static gilianmarques.dev.musicplayer.mediaplayer.structure.Const.TRACK_CHANGED;
import static gilianmarques.dev.musicplayer.mediaplayer.structure.Const.TRACK_ENDED;
import static gilianmarques.dev.musicplayer.mediaplayer.structure.Const.TRACK_NOT_FOUND;

/**
 * Criado por Gilian Marques
 * Quinta-feira, 23 de Maio de 2019  as 21:10:23.
 */
public class Notifyer {


    public static final long NOTIFY_PROGRESS_INTERVAL = 100;
    private MusicPlayer player;
    private LocalBroadcastManager broadcastManager;

    public Notifyer(MusicPlayer player) {
        this.player = player;
        broadcastManager = LocalBroadcastManager.getInstance(App.binder.get());
        broadcastManager.registerReceiver(notifyListener, new IntentFilter(FORCE_NOTIFY));
    }

    /**
     * Notifica a todos os listener que a musica foi alterada
     *
     * @param lid
     */
    void notifyTrackChanged(String lid) {
        if (lid == null) lid = PlayerProgressListener.BROADCASTED_FOR_ALL;
        Intent intent = new Intent(TRACK_CHANGED).putExtra("track", player.flow.currentTrack()).putExtra("lid", lid);
        broadcastManager.sendBroadcast(intent);
    }

    void notifyTrackNotFound() {
        Intent intent = new Intent(TRACK_NOT_FOUND).putExtra("track", player.flow.currentTrack())
                .putExtra("lid", PlayerProgressListener.BROADCASTED_FOR_ALL);
        broadcastManager.sendBroadcast(intent);

    }

    void notifyPlayPause(String lid) {
        if (lid == null) lid = PlayerProgressListener.BROADCASTED_FOR_ALL;
        Intent intent = new Intent(PLAY_PAUSE).putExtra("play", player.isPlaying()).putExtra("lid", lid);
        broadcastManager.sendBroadcast(intent);
    }

    void notifyProgress() {
        if (player.listeningProgress) return;


        player.listeningProgress = true;
        // always notify for all listeners
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (player.released) {
                    cancel();
                } else if (!player.preparing) {
                    // n interajo com o player se ele estiver preparando a proxima track (ou anterior)

                    Intent intent = new Intent(PROGRESS)
                            .putExtra("percent", player.getProgressPercent(player.getCurrentPosition()))
                            .putExtra("millis", player.getCurrentPosition() + "")
                            .putExtra("timer", player.getProgressTimer()).putExtra("lid", PlayerProgressListener.BROADCASTED_FOR_ALL);
                    broadcastManager.sendBroadcast(intent);

                    if (!player.isPlaying()) {
                        // n esta tocando, notifico uma unica vez pra atualizar o proresso da ultima classe a registrar um evento aqui
                        cancel();
                        player.listeningProgress = false;
                    }

                }

            }
        }, 0, NOTIFY_PROGRESS_INTERVAL);

    }

    final BroadcastReceiver notifyListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String lid;
            if (intent != null) lid = intent.getStringExtra("lid");
            else lid = PlayerProgressListener.BROADCASTED_FOR_ALL;


            //   Log.d(App.myFuckingUniqueTAG + "MusicPlayer", "onReceive: onReceive: WIll notify only for: "+lid);
            if (player.flow.currentTrack() != null) {
                notifyTrackChanged(lid);
                notifyPlayPause(lid);
            }
            notifyProgress();
        }
    };

    /**
     * Only broadcasted when a track is over
     */
    void notifyTrackEnded() {
        broadcastManager.sendBroadcast(new Intent(TRACK_ENDED).putExtra("lid", PlayerProgressListener.BROADCASTED_FOR_ALL));
    }


    void stop() {
        broadcastManager.sendBroadcast(new Intent(STOP).putExtra("lid", PlayerProgressListener.BROADCASTED_FOR_ALL));
    }

    void release() {
        broadcastManager.sendBroadcast(new Intent(RELEASING).putExtra("lid", PlayerProgressListener.BROADCASTED_FOR_ALL));
        broadcastManager.unregisterReceiver(notifyListener);
        broadcastManager = null;
    }
}
