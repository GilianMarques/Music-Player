package gilianmarques.dev.musicplayer.activities.library;

import android.os.Parcel;

import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Artist;
import gilianmarques.dev.musicplayer.models.Track;

/**
 * Criado por Gilian Marques
 * Domingo, 19 de Maio de 2019  as 21:03:00.
 */
public class Suggestion implements com.arlib.floatingsearchview.suggestions.model.SearchSuggestion {

    // parcelable.....
    public static int CREATOR;
    private Track track;
    private Album album;
    private Artist artist;

    public Suggestion(Track track) {
        this.track = track;
    }

    public Suggestion(Album album) {

        this.album = album;
    }

    public Suggestion(Artist artist) {

        this.artist = artist;
    }

    public Track getTrack() {
        return track;
    }

    public Album getAlbum() {
        return album;
    }

    public Artist getArtist() {
        return artist;
    }

    @Override public String getBody() {
        return "must implement!";
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {

    }
}
