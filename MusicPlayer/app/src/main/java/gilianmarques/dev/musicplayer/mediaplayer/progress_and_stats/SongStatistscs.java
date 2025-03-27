package gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats;

import android.support.annotation.NonNull;
import android.util.Log;

import gilianmarques.dev.musicplayer.mediaplayer.structure.MusicPlayer;
import gilianmarques.dev.musicplayer.mediaplayer.structure.Notifyer;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.utils.App;
import io.realm.Realm;

/**
 * Criado por Gilian Marques
 * Domingo, 28 de Abril de 2019  as 16:04:46.
 */
public class SongStatistscs {
    // TODO: 28/04/2019 ganrantir que os listeners do player n sao chamados varias vezes criar listsners de musicSerive mehlores

    private Track playingNowTrack;
    private boolean statsSetForThisSong;
    private final int PERCENT = 30;
    private int timePlayed;
    private PlayerProgressListener playerProgressListener = new PlayerProgressListener(getClass().getSimpleName()) {


        @Override protected void trackChanged(final Track newTrack) {

            if (playingNowTrack == null) playingNowTrack = newTrack;

            else if (playingNowTrack.getId() != newTrack.getId()) {
                playingNowTrack = newTrack;
                statsSetForThisSong = false;
                timePlayed = 0;
            }

            UIRealm.get().executeTransactionAsync(new Realm.Transaction() {
                @Override public void execute(@NonNull Realm realm) {
                    newTrack.setLastReproductionDate(System.currentTimeMillis());
                    realm.insertOrUpdate(newTrack);
                    //    Log.d(App.myFuckingUniqueTAG + "SongStatistscs", "execute: last Play date for " + newTrack.getTitle() + " is: " + new LocalDateTime(newTrack.getLastReproductionDate()));
                }
            });

            super.trackChanged(newTrack);
        }

        @Override
        protected void progressChanged(float percent, final String timer, long millis) {
            /*Track must have a least 'PERCENT'% of its size to have its playedTime marker increased in +1*/

            if (statsSetForThisSong || playingNowTrack == null) return;
            timePlayed += Notifyer.NOTIFY_PROGRESS_INTERVAL;
            /* 'PERCENT'% of track duration in millis (200/100*30 = 60) */
            int trackDurPercent = (int) (playingNowTrack.getDurationMillis() / 100 * PERCENT);
          //  Log.d(App.myFuckingUniqueTAG + "SongStatistscs", "progressChanged: " + PERCENT + "% :" + trackDurPercent + "  actualMillis: " + timePlayed);
            if (timePlayed >= trackDurPercent)
                UIRealm.get().executeTransaction(new Realm.Transaction() {
                    @Override public void execute(@NonNull Realm realm) {
                        statsSetForThisSong = true;
                        playingNowTrack.setplayedTime(playingNowTrack.getPlayedTime() + 1);
                        realm.insertOrUpdate(playingNowTrack);
                        Log.d(App.myFuckingUniqueTAG + "SongStatistscs", "execute: Marked played +1 for " + playingNowTrack.getTitle());
                    }
                });

            super.progressChanged(percent, timer, millis);
        }

    };

    public SongStatistscs(MusicPlayer musicPlayer) {

        MusicPlayer musicPlayer1 = musicPlayer;
    }


    public void listen() {
        playerProgressListener.startReceiving();
        //   Log.d(App.myFuckingUniqueTAG + "SongStatistscs", "SongStatistscs: listening to player");
    }
}
