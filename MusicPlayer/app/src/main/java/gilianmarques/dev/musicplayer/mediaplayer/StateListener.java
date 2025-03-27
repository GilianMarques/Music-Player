package gilianmarques.dev.musicplayer.mediaplayer;

import gilianmarques.dev.musicplayer.models.Track;

/**
 * Criado por Gilian Marques em 24/05/2018 as 19:12:17.
 */
public abstract class StateListener {
    private final String id;


    public StateListener(Object caller) {
        this.id = caller.getClass().getSimpleName();
    }


    private void trackNotFound() {
       // Log.d("StateListener", "trackNotFound: ");
    }


    protected void trackChanged(final Track newTrack) {
       // Log.d(App.myFuckingUniqueTAG + "StateListener", "trackChanged: " + newTrack.getTitle());
    }

    /**
     * Only broadcasted when a track is over
     */
    protected void trackEnded() {
       // Log.d(App.myFuckingUniqueTAG + "StateListener", "trackEnded: ");
    }


    protected void progressChanged(final float percent, final String timer, long millis) {
       // Log.d("StateListener", "progressChanged: playback percent: " + percent + "%, timer: " + timer);
    }

    protected void stateChanged(final boolean play) {
       // Log.d("StateListener", "stateChanged: " + " (" + id + ") " + ((play ? "REPRODUZINDO" : "PAUSADO")));
    }

    protected void onPlayerStop() {
       // Log.d("StateListener", " (" + id + ") " + "onPlayerStop: STOPPED");
    }

    private void releasing() {

       // Log.d("StateListener", "releasing: CALLBACK UNREGISTERED");
    }


    public final String getId() {
        return id;
    }
}
