package gilianmarques.dev.musicplayer.models;

import android.app.Activity;
import android.content.ContentUris;
import android.net.Uri;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.RealmClass;

/**
 * Criado por Gilian Marques em 05/05/2018 as 20:10:11.
 */
@RealmClass
public class Album implements Serializable, RealmModel {

    private String name, artist;
    @Ignore
    private String duration;
    private int numberOfTracks;

    long id;

    public Album() {
    }

    public Album(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Uri getURI() {
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), getId());
        //  Log.d(App.myFuckingUniqueTAG + "Album", "getURI: artPath " + getName() + ": " + uri.toString());
        return uri;
    }

    public ArrayList<Track> getTracks() {
        return new NativeTracks(false).getTracksByAlbum(this);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }


    public void setNumberOfTracks(int numOfSongs) {

        numberOfTracks = numOfSongs;
    }

    public int getNumberOfTracks() {
        return numberOfTracks;
    }

    public void getDuration(final TextView target, final Activity mActivity) {

        if (duration == null) {
            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                    ArrayList<Track> res = new NativeTracks(false).getTracksByAlbum(Album.this);
                    long dur = 0;
                    for (Track track : res) {
                        dur += track.getDurationMillis();
                    }
                    duration = Utils.millisToFormattedString(dur);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            target.setText(duration);

                        }
                    });
                }
            };
            new Thread(mRunnable).start();
        } else target.setText(duration);
    }

}
