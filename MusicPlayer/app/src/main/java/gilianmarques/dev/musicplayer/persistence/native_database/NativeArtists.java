package gilianmarques.dev.musicplayer.persistence.native_database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.models.Artist;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Domingo, 28 de Outubro de 2018  as 17:35:53.
 *
 * @Since 1.0
 */

public class NativeArtists {


    private ContentResolver resolver;

    public NativeArtists() {
        Context mContext = App.binder.get();
        this.resolver = mContext.getContentResolver();
    }

    private String[] artistsProjection = new String[]{
            /* 0 */ BaseColumns._ID,
            /* 1 */ MediaStore.Audio.ArtistColumns.ARTIST,
            /* 2 */ MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
            /* 3 */ MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
    };


    /**
     * @param name       can be the full name or just part of it. Cases aren't sensitive.
     * @param maxResults maxResults
     * @return r
     */
    public ArrayList<Artist> getArtistsByName(String name, int maxResults) {


        Cursor cursor = resolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                                       artistsProjection,
                                       (MediaStore.Audio.Artists.ARTIST + " LIKE '%") + SQL.scape(name) + "%'",
                                       null, null);
        return getArtists(cursor, maxResults);
    }

    public Artist getArtistsByID(long uid) {


        Cursor cursor = resolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                                       artistsProjection,
                                       (MediaStore.Audio.Artists._ID + " = '") + uid + "'",
                                       null, null);
        ArrayList<Artist> artists = getArtists(cursor, -1);
        if (artists.size() == 0) return null;
        else return artists.get(0);
    }


    public ArrayList<Artist> getAllArtists() {
        Cursor cursor = resolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                                       artistsProjection, null, null, null);
        return getArtists(cursor, -1);
    }


    private ArrayList<Artist> getArtists(Cursor cursor, int maxResults) {

        final ArrayList<Artist> artists = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                final long id = (cursor.getLong(0));
                final String artistName = cursor.getString(1);
                final int albumCount = cursor.getInt(2);
                final int songCount = cursor.getInt(3);

                final Artist artist = new Artist(id);

                artist.setArtistName(artistName);
                artist.setnumberOfTracks(songCount);
                artist.setnumberOfAlbuns(albumCount);
                artists.add(artist);
                if (maxResults != -1 && artists.size() == maxResults) break;

            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        return artists;
    }


}
