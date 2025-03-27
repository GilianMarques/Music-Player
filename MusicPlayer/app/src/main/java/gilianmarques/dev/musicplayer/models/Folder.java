package gilianmarques.dev.musicplayer.models;

import java.io.Serializable;
import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.annotations.RealmClass;

/**
 * Criado por Gilian Marques
 * Quarta-feira, 18 de Julho de 2018  as 18:50:50.
 *
 * @Since 0.1 Beta
 */
@RealmClass
public class Folder implements Serializable, RealmModel {

    private String name, info;
    private RealmList<Track> tracks;
    private String path;

    public Folder() {
    }

    public Folder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Track> getTracks() {
        return new ArrayList<Track>(tracks);
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = new RealmList<>();
        this.tracks.addAll(tracks);
    }

    public String getFormatedDuration() {

        long durr = 0;

        for (Track mTrack : tracks) {
            durr += mTrack.getDurationMillis();
        }
        return Utils.millisToFormattedString(durr);

    }

    public String getInfo() {
        String duration = getFormatedDuration();
        if (info == null || info.isEmpty())
            info = Utils.format(Utils.toPlural(tracks.size(), R.plurals.faixas), tracks.size())
                    .concat(" | ")
                    .concat(duration);
        return info;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void remove(long id) {
        tracks.remove(id);
    }
}
