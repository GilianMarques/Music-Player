package gilianmarques.dev.musicplayer.models;

import android.util.Log;

import com.pixplicity.easyprefs.library.Prefs;

import java.io.Serializable;
import java.util.ArrayList;

import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.sorting.utils.Sort;
import gilianmarques.dev.musicplayer.sorting.utils.SortTypes;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

/**
 * Criado por Gilian Marques
 * Segunda-feira, 04 de Junho de 2018  as 19:48:40.
 */
@RealmClass
public class Playlist implements Serializable, RealmModel {

    public Playlist(String id) {
        this.id = id;
    }

    public Playlist() {
    }


    @Ignore
    private ArrayList<Track> cacheTracks = new ArrayList<Track>();

    private int playedTimes;
    private long duration;
    private String name;
    private RealmList<TrackRef> tracksRef = new RealmList<>();
    private String id;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param mTrack t
     *               <p>
     *               Are not allowed repeated tracks
     */
    public boolean addTrack(final Track mTrack) {

        if (repeated(mTrack.getId())) return false;
        TrackRef ref = new TrackRef(tracksRef.size(), mTrack.getId());
        mTrack.setIndex(tracksRef.size());
        tracksRef.add(ref);
        cacheTracks.add(mTrack);
        duration += mTrack.getDurationMillis();

        return true;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private boolean repeated(long id) {

        for (TrackRef id2 : tracksRef) {
            if (id2.getId() == id) return true;
        }
        return false;
    }


    public int size() {
        return tracksRef.size();
    }

    public ArrayList<Track> getTracks(boolean sort) {
        //clone to avoid modify this cache from outside of this object
        ArrayList<Track> tracks = new ArrayList<Track>(cacheTracks);

        if (tracks.size() == 0) {

            for (TrackRef ref : tracksRef) {
                Track mTrack = ref.getTrack();
                if (mTrack != null) cacheTracks.add(mTrack);
            }

            tracks.addAll(cacheTracks);
        }

        if (sort) {
            Sort.key_to_sort = getId();
            int i = Prefs.getInt(c.sorting_view_playlists, SortTypes.DEFAULT_.value);
            tracks=   Sort.Tracks.sort(SortTypes.toSortingType(i), tracks);
            Log.d(App.myFuckingUniqueTAG + "Playlist", "getTracks: " + SortTypes.toSortingType(i).getValue());
        }
        return tracks;

    }

    public String getFormatedDuration() {

        return Utils.millisToFormattedString(duration);

    }

    public Track getFirstTrack() {
        return getTracks(true).get(0);
    }

    public int getPlayedTimes() {
        return playedTimes;
    }

    public void setPlayedTimes(int playedTimes) {
        this.playedTimes = playedTimes;
    }


    public boolean removeTrack(Track mTrack) {

        for (TrackRef ref : tracksRef) {
            if (ref.getId() == mTrack.getId()) {
                tracksRef.remove(ref);
                break;
            }
        }


        for (Track cacheTrack : cacheTracks) {
            if (cacheTrack.getId() == mTrack.getId()) {
                duration -= mTrack.getDurationMillis();
                return cacheTracks.remove(cacheTrack);
            }
        }

        return false;

    }

    public boolean livePlaylist() {
        return false;

    }

    public void updateReferences(final Track... mTracks) {

        for (final Track track : mTracks)
            for (final TrackRef tReference : tracksRef)

                if (tReference.getId() == (track.getId())) {
                    tReference.setIndex(track.getIndex());

                    for (Track cacheTrack : cacheTracks)
                        if (cacheTrack.getId() == (tReference.getId())) {
                            cacheTrack.setIndex(tReference.getIndex());
                            break;
                        }
                    break;
                }


    }
}
