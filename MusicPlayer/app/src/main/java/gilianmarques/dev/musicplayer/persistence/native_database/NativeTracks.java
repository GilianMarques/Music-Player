package gilianmarques.dev.musicplayer.persistence.native_database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Domingo, 28 de Outubro de 2018  as 15:11:47.
 *
 * @Since 1.0
 */
public class NativeTracks {
    private ContentResolver resolver;
    private Context mContext;
    private boolean loadDataFromRealm;


    public NativeTracks(boolean loadDataFromRealm) {
        this.loadDataFromRealm = loadDataFromRealm;
        mContext = App.binder.get();
        this.resolver = mContext.getContentResolver();
    }

    private String[] trackProjection = {
            MediaStore.Audio.Media._ID, // id
            MediaStore.Audio.Media.ARTIST, // built in artist
            MediaStore.Audio.Media.TITLE, // built in title
            MediaStore.Audio.Media.DATA, //path
            MediaStore.Audio.Media.DISPLAY_NAME, // file.mp3
            MediaStore.Audio.Media.DURATION, //in millis
            MediaStore.Audio.Media.ALBUM, // built in album
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
    };


    public ArrayList<Track> getTracksByArtist(String name) {

        String selection = (MediaStore.Audio.Media.IS_MUSIC + " != 0") + " AND " + MediaStore.Audio.AudioColumns.ARTIST + " = '" + SQL.scape(name) + "'";

        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                       trackProjection, selection, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

        return getTracks(cursor, -1);
    }

    public ArrayList<Track> getTracksByAlbum(Album album) {

        String selection = (MediaStore.Audio.Media.IS_MUSIC + " != 0") +
                " AND " + MediaStore.Audio.AudioColumns.ARTIST + " = '" + SQL.scape(album.getArtist()) + "'" +
                " AND " + MediaStore.Audio.AudioColumns.ALBUM + "  = '" + SQL.scape(album.getName()) + "'";

        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                       trackProjection, selection, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

        return getTracks(cursor, -1);
    }

    /**
     * @param name       Can be the full name or just part  of it like a search
     * @param maxResults
     * @return tracks containing the string received in their title
     * NOTE: it seems the cases aren't sensitive so A and a are the same.
     */
    public ArrayList<Track> getTracksByName(String name, int maxResults) {

        String selection = (MediaStore.Audio.Media.IS_MUSIC + " != 0") +
                " AND " + MediaStore.Audio.AudioColumns.TITLE + " LIKE '%" + SQL.scape(name) + "%'";

        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                       trackProjection, selection, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

        return getTracks(cursor, maxResults);
    }

    public ArrayList<Track> getAllTracks() {

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        ContentResolver mResolver = mContext.getContentResolver();

        Cursor cursor = mResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        trackProjection, selection, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

        return getTracks(cursor, -1);
    }

    public Track getTrackById(long id) {

        String selection = (MediaStore.Audio.Media.IS_MUSIC + " != 0") + " AND " + MediaStore.Audio.AudioColumns._ID + " = '" + SQL.scape(String.valueOf(id)) + "'";

        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                       trackProjection, selection, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

        ArrayList<Track> array = getTracks(cursor, 1);
        if (array.size() > 0) return array.get(0);
        else return null;
    }

    //
    //
    //
    private ArrayList<Track> getTracks(final Cursor cursor, int maxResults) {
        ArrayList<Track> songs = new ArrayList<Track>();

        if (cursor == null) {
            return songs;
        }

        while (cursor.moveToNext()) {

            String artistName = cursor.getString(1);
            String albumName = cursor.getString(6);
            String path = cursor.getString(3);
            String displayName = cursor.getString(4);
            String pathNoName = path.replace(displayName, "");
            String title = cursor.getString(2);
            String duration = cursor.getString(5);
            long artistId = cursor.getLong(8);
            long albumId = cursor.getLong(7);


            final Track mTrack = new Track(cursor.getLong(0));
            mTrack.setFilePath(path);
            mTrack.setAlbumName(albumName);
            mTrack.setArtistName(artistName);
            mTrack.setFilePathNoName(pathNoName);
            mTrack.setTitle(title);
            mTrack.setDurationMillis(duration != null ? Integer.parseInt(duration) : 0);
            mTrack.setAlbum(albumId);
            mTrack.setArtist(artistId);

            //if (loadDataFromRealm) mTrack.addLocalInfo();
            mTrack.addLocalInfo();
            songs.add(mTrack);
            if (maxResults != -1 && songs.size() == maxResults) break;
            /**/

        }


        cursor.close();

        return songs;
    }

}
