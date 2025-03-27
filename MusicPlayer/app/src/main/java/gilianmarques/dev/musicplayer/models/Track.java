package gilianmarques.dev.musicplayer.models;

import java.io.Serializable;

import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeAlbuns;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeArtists;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Criado por Gilian Marques em 01/05/2018 as 17:19:38.
 */
@RealmClass
public class Track implements Serializable, RealmModel {
// TODO: 24/05/2019 save artist name and album name to sorting porpuses

    @PrimaryKey
    private long id;

    @Ignore
    private String filePath;
    @Ignore
    private String title;
    @Ignore
    private String filePathNoName;
    @Ignore
    private long durationMillis;
    @Ignore
    private long mAlbum;
    @Ignore
    private long mArtist;

    @Ignore
    private Album mAlbumObj;
    @Ignore
    private boolean localDataLoaded;
    @Ignore
    private String artistName;
    @Ignore
    private int index;
    @Ignore
    private String albumName;

    private int playedTimes;
    private long lastReproductionDate;


    public Track(long id) {

        this.id = id;
    }

    public Track() {
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }


    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(int durationMillis) {
        this.durationMillis = durationMillis;
    }


    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }


    public long getLastReproductionDate() {
        return lastReproductionDate == 0 ? 1 : lastReproductionDate;
    }

    public void setLastReproductionDate(long lastReproductionDate) {
        this.lastReproductionDate = lastReproductionDate;
    }


    public String getFilePathNoName() {
        return filePathNoName;
    }

    public void setFilePathNoName(String filePathNoName) {
        this.filePathNoName = filePathNoName;
    }


    /**
     * @return  how many times track was played
     */
    public int getPlayedTime() {
        return playedTimes;
    }

    public void setplayedTime(int times) {
        this.playedTimes = times;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public Album getAlbum() {
        if (mAlbumObj == null) mAlbumObj = new NativeAlbuns().getAlbumByID(mAlbum);
        return mAlbumObj;
    }

    public Artist getArtist() {
        return new NativeArtists().getArtistsByID(mArtist);

    }

    public void setAlbum(long mAlbum) {
        this.mAlbum = mAlbum;
    }

    public void setArtist(long mArtist) {
        this.mArtist = mArtist;
    }

    public void addLocalInfo() {

        if (localDataLoaded) return;
        localDataLoaded = true;
        UIRealm.getRealm(new UIRealm.Callback() {
            @Override public void doUIStuff(Realm realm) {
                Track localTrack = realm.where(Track.class).equalTo("id", getId()).findFirst();
                if (localTrack != null) {
                    setplayedTime(localTrack.getPlayedTime());
                    setLastReproductionDate(localTrack.getLastReproductionDate());
                }
            }
        });

    }

    public void reloadLocalInfo() {
        localDataLoaded = false;
        addLocalInfo();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}


