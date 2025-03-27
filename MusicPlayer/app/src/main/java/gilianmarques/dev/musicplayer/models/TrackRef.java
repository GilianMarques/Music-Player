package gilianmarques.dev.musicplayer.models;

import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Criado por Gilian Marques
 * Domingo, 26 de Maio de 2019  as 11:46:08.
 */
public class TrackRef extends RealmObject {

    private int index;
    private
    @PrimaryKey
    long id;
    @Ignore
    private Track ref;

    public TrackRef() {
    }

    public TrackRef(int index, long id) {
        this.index = index;
        this.id = id;
    }

    public TrackRef(int index, Track track) {
        this.index = index;
        this.id = track.getId();
        this.ref = track;
    }

    public int getIndex() {
        return index;
    }

    public long getId() {
        return id;
    }

    public Track getTrack() {
        if (ref == null) {
            ref = new NativeTracks(true).getTrackById(id);
            ref.setIndex(getIndex());
        }
        return ref;
    }

    public void setIndex(int index) {
        this.index = index;
        getTrack().setIndex(index);
    }

    @Override public String toString() {
        return getIndex() + " id: " + getId() + " title: " + getTrack().getTitle();
    }
}
