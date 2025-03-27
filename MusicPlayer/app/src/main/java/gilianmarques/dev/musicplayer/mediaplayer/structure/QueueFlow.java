package gilianmarques.dev.musicplayer.mediaplayer.structure;

import android.support.annotation.WorkerThread;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.models.TrackRef;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Domingo, 26 de Maio de 2019  as 12:53:36.
 * <p>
 * Created to control the flow of queue after TrackArray class were being deleted
 */
public class QueueFlow {
    private int index;
    private TrackRef curr;
    private boolean shuffle;
    RepeatMode repeatMode = RepeatMode.repeat_disabled;

    private ArrayList<TrackRef> currentQueue = new ArrayList<>();
    private ArrayList<Integer> playedOnes = new ArrayList<>();


    private MusicPlayer player;
    private int queue_max = 2000;

    QueueFlow(MusicPlayer player) {
        this.player = player;
    }

    /**
     * @return the curr position of array
     */
    int getIndex() {
        return index;
    }

    public void reset() {
        // Log.d(App.myFuckingUniqueTAG + "QueueFlow", "reset: played sz " + playedOnes.size() + " queue " + currentQueue.size());
        index = -1;
        playedOnes.clear();
    }

    public boolean isEmpty() {
        return currentQueue.size() == 0;
    }

    public boolean isLastIndex() {
        // Log.d(App.myFuckingUniqueTAG + "QueueFlow", "isLastIndex: " + (playedOnes.size() == currentQueue.size()) + " played sz " + playedOnes.size() + " queue " + currentQueue.size() + " played content: " + playedOnes.toString());
        if (shuffle) return playedOnes.size() == currentQueue.size();
        return index == currentQueue.size() - 1;
    }

    public int randomIndex() {
        return new Random().nextInt(currentQueue.size());
    }

    /**
     * @param fromUser if fromUser & shuffle == true i'll get next index in playedOnes array else i'll get an random next index     * @return the next or current track in case of current index is the last one in queue
     */
    public Track getNext(boolean fromUser) {

        if (shuffle) {
            index = randomIndex();
        } else if (index + 1 < currentQueue.size()) index++;


        if (playedOnes.contains(index)) return getNext(true);
        playedOnes.add(index);
        curr = currentQueue.get(index); // next or current Track;
        // Log.d(App.myFuckingUniqueTAG + "QueueFlow", "getNext: " + index + " played " + playedOnes.size() + " currQueue " + currentQueue.size() + " played? " + playedOnes.contains(index) + " >" + playedOnes.toString());
        return curr.getTrack();
    }

    /**
     * @return the previous or the current track in case the current index is 0
     */
    public Track getPrevious() {


        if (shuffle) {
            int indexPosOnArray = playedOnes.indexOf(index);// get pos of index variable on array
            if (indexPosOnArray > 0)
                index = playedOnes.get(indexPosOnArray - 1);// get the value of index before the actual
        } else if (index - 1 >= 0) index--;
        else if (index < 0) index = 0; // index =-1 when user turn off shuffle

        curr = currentQueue.get(index);// next or current Track
        return curr.getTrack();

    }

    /**
     * @param allTracks t
     *                  <p>
     *                  Clear all references before add the now ones
     *                  clear entity reference when called
     */
    public void populate(ArrayList<Track> allTracks) {

        reset();
        currentQueue.clear();
        for (Track track : allTracks)
            if (currentQueue.size() > queue_max)
                App.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Toasty.error(App.binder.get(), App.binder.get().getString(R.string.Nao_e_possivel_adicionar_mais_de_200_musicas), 5000).show();

                    }
                });
            else currentQueue.add(new TrackRef(currentQueue.size(), track));


    }

    public Track currentTrack() {
        if (curr == null) return null;
        return curr.getTrack();
    }


    public Track getTrackAsCurrent(int position) {
        index = position;
        curr = currentQueue.get(index);
        playedOnes.add(position);
        return curr.getTrack();
    }


    public int addTrack(Track mTrack) {
        if (currentQueue.size() > queue_max) {
            App.runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toasty.error(App.binder.get(), App.binder.get().getString(R.string.Nao_e_possivel_adicionar_mais_de_200_musicas), 5000).show();
                }
            });
            return -1;
        }
        return currentQueue.add(new TrackRef(currentQueue.size(), mTrack)) ? 1 : -1;
    }

    public ArrayList<TrackRef> getQueue() {
        return currentQueue;
    }

    public void shuffle(boolean turnShuffleOn) {
        if (!turnShuffleOn) index = -1;// to play all tracks skipped while in shuffle
        shuffle = turnShuffleOn;
    }

    public boolean isShuffling() {
        return shuffle;
    }

    @WorkerThread
    public void saveState() {

        //saving only index and id of track to avoid save all track and album/artist objects inside track
        ArrayList<Long> refs = new ArrayList<>();
        for (TrackRef ref : getQueue())
            refs.add(ref.getId());// save only id because the position of each track is kept by its order in array

        Prefs.putString(c.current_queue, new Gson().toJson(refs));
        Prefs.putString(c.played_ones, new Gson().toJson(playedOnes));
        Prefs.putInt(c.player_index, index);

        Prefs.putString(c.current_track_ref, new Gson().toJson(curr));
        Prefs.putInt(c.current_track_millis, player.getCurrentPosition());

        Prefs.putString(c.player_repeat_mode, new Gson().toJson(repeatMode));
        Prefs.putBoolean(c.player_shffle, shuffle);


    }

    @WorkerThread
    void restoreState() {


        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                ArrayList<Long> refs = new Gson().fromJson(Prefs.getString(c.current_queue, null), new TypeToken<ArrayList<Long>>() {
                }.getType());

                if (refs != null) for (long id : refs) {
                    Track t = new NativeTracks(true).getTrackById(id);
                    if (t != null) addTrack(t);
                }

                //
                playedOnes = new Gson().fromJson(Prefs.getString(c.played_ones, null), new TypeToken<ArrayList<Integer>>() {
                }.getType());
                if (playedOnes == null) playedOnes = new ArrayList<>();
            }
        };
        new Thread(mRunnable).start();
        //
        index = Prefs.getInt(c.player_index, 0);
        //
        curr = new Gson().fromJson(Prefs.getString(c.current_track_ref, null), TrackRef.class);
        //
        repeatMode = new Gson().fromJson(Prefs.getString(c.player_repeat_mode, null), RepeatMode.class);
        if (repeatMode == null) repeatMode = RepeatMode.repeat_disabled;
        shuffle = Prefs.getBoolean(c.player_shffle, false);
        //


        if (curr != null) {
            player.setVolume(0);
            player.play(); // seto no player com o setDataSource() e executo
            player.seekTo(Prefs.getInt(c.current_track_millis, 0));
            player.pause(); // pauso a musica
            player.setVolume(100);
            player.mNotifyer.notifyProgress();
        }
    }

}
