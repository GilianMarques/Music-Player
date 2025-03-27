package gilianmarques.dev.musicplayer.mediaplayer;

import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.UIUtils;

/**
 * Criado por Gilian Marques
 * Domingo, 29 de Dezembro de 2019  as 16:56:41.
 */
class ListenerHandler {
    private ArrayList<StateListener> listeners = new ArrayList<>();


    void removeAll() {
        listeners.clear();
    }

    void removeCallback(StateListener toRemove) {
        StateListener target = null;

        for (StateListener listener : listeners)
            if (listener.getId().equals(toRemove.getId())) {
                target = listener;
                break;
            }

        if (target != null) listeners.remove(target);
        else
            Log.d(App.myFuckingUniqueTAG + "ListenerHandler", "unregisterListener: no callback from " + toRemove.getId() + " to remove");
    }

    void registerCallback(StateListener toAdd) {

        StateListener target = null;

        for (StateListener listener : listeners)
            if (listener.getId().equals(toAdd.getId())) {
                target = listener;
                break;
            }

        if (target != null) listeners.remove(target);


        listeners.add(toAdd);
        //send all listeners to update caller
        Track currSong = MusicPlayer.getInstance().getCurrentSong();
        if (currSong != null) {
            notifyTrackChanged(currSong);
            notifyProgress();
            notifyStateChanged(MusicPlayer.getInstance().isPlaying());
        }


    }


    void notifyTrackChanged(Track track) {
        for (StateListener listener : listeners)
            listener.trackChanged(track);
    }

    void notifyStateChanged(boolean isPlaying) {
        for (StateListener listener : listeners)
            listener.stateChanged(isPlaying);
        if (isPlaying) notifyProgress();
    }

    void notifyPlayerStopped() {
        for (StateListener listener : listeners)
            listener.onPlayerStop();
    }

    private boolean notifyProgress;

    private void notifyProgress() {

        if (notifyProgress) return;
        notifyProgress = true;

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // avoiding ConcurrentModificationException
                final ArrayList<StateListener> listeners = new ArrayList<>(ListenerHandler.this.listeners);


                /*quando o player e fechado (stop) ele remove todos os callbacks do array
                antes da instance no singleton ser nulada*/
                if (listeners.size() == 0) cancel();
                else {
                    //   Log.d(App.myFuckingUniqueTAG + "ListenerHandler", "run: "+listeners.size());
                    for (final StateListener listener : listeners) {

                        long currPosition = MusicPlayer.getInstance().getCurrentPosition();

                        final float percent = UIUtils.getProgressPercent(currPosition, MusicPlayer.getInstance().getDuration());
                        final String timer = UIUtils.getProgressTimer(currPosition);

                        App.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.progressChanged(percent, timer, 10);
                            }
                        });
                    }

                    if (!MusicPlayer.getInstance().isPlaying()) {
                        cancel();
                        notifyProgress = false;
                    }
                }

            }
        }, 0, 100);

    }


}
