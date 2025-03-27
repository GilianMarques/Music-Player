package gilianmarques.dev.musicplayer.models;

import java.io.Serializable;
import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Criado por Gilian Marques em 23/05/2018 as 18:24:57.
 */
@RealmClass
public class Artist implements Serializable, RealmModel {
    @Ignore
    private String artistName;
    @Ignore
    private boolean localDataLoaded;
    @Ignore
    private int songCount, albumCount;
    private String url;
    private boolean infoFetched;
    private long lastFetch;
    @PrimaryKey
    private long id;


    public Artist(long id) {
        this.id = id;
    }

    public Artist() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        if (url == null || url.isEmpty()) return "http://anyurl.com";
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    private int getTracksAmount() {
        return songCount;
    }

    private int getAlbunsAmount() {
        return albumCount;
    }

    public String getInfo() {
        return Utils.format(Utils.toPlural(getAlbunsAmount(), R.plurals.album), getAlbunsAmount())
                .concat(" | ")
                .concat(Utils.format(Utils.toPlural(getTracksAmount(), R.plurals.faixas), getTracksAmount()));

    }

    public ArrayList<Track> getTracks() {
        return new NativeTracks(false).getTracksByArtist(getName());
    }

    public void setnumberOfTracks(int songCount) {
        this.songCount = songCount;
    }

    public void setnumberOfAlbuns(int albumCount) {
        this.albumCount = albumCount;
    }


    public boolean isInfoFetched() {
        return infoFetched;
    }

    public void setInfoFetched(boolean infoFetched) {
        this.infoFetched = infoFetched;
    }

    /**
     * @return true se ja fazem +7 dias desde que os dados foram baixados da nuvem
     */
    public boolean shouldRefetch() {
        return System.currentTimeMillis() - lastFetch == ((24 * 60 * 60 * 1000) * 7/*7 dias*/);
    }

    public void setLastFetch(long currentTimeMillis) {
        lastFetch = currentTimeMillis;
    }

    public void addLocalInfo() {
        if (localDataLoaded) return;
        localDataLoaded = true;
        Artist localArtist = UIRealm.getRealm(null).where(Artist.class).equalTo("id", getId()).findFirst();
        if (localArtist != null) {
            setLastFetch(localArtist.lastFetch);
            setInfoFetched(localArtist.infoFetched);
            setUrl(localArtist.url);
        }
    }

}
