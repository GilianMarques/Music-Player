package gilianmarques.dev.musicplayer.persistence.native_database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Domingo, 28 de Outubro de 2018  as 12:12:53.
 *
 * @Since 1.0
 */
public class NativeAlbuns {

    private ContentResolver resolver;
    private Context mContext;

    public NativeAlbuns() {
        mContext = App.binder.get();
        this.resolver = mContext.getContentResolver();
    }

    private String[] albumProjection = {
            MediaStore.Audio.Albums._ID, // unique id
            MediaStore.Audio.Albums.ALBUM, // title
            MediaStore.Audio.Albums.ARTIST, // artist
            MediaStore.Audio.Albums.NUMBER_OF_SONGS // nbr of tracks
    };

    private Cursor getAllAlbunsCursor() {
        return resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                              albumProjection, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
    }

    private Cursor getAlbumByTrackCursor(String albumName, String albumArtist) {


        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.AlbumColumns.ALBUM + " = '" + SQL.scape(albumName) + "'");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.ARTIST + " = '" + SQL.scape(albumArtist) + "'");

        return resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                              albumProjection, selection.toString(), null, null);

    }

    /**
     * @param query      can be a full name or just part of it, like a search
     * @param maxResults max results
     * @return the albuns corresponding
     */
    public ArrayList<Album> getAlbumByName(String query, int maxResults) {

        return getAlbums(resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumProjection, (MediaStore.Audio.AlbumColumns.ALBUM + " LIKE '%" + query + "%'"), null, null),
                         maxResults);

    }

    private Cursor getAlbumByArtistCursor(final Long artistId) {
        return resolver.query(MediaStore.Audio.Artists.Albums.getContentUri("external", artistId),
                              albumProjection, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
    }

    //


    public ArrayList<Album> getAlbumByArtist(final Long artistId) {
        return getAlbums(getAlbumByArtistCursor(artistId), -1);
    }

    public Album getAlbumByID(final Long id) {

        Cursor c = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                  albumProjection,
                                  (MediaStore.Audio.Albums._ID + " = '") + id + "'",
                                  null, null);
        ArrayList<Album> albuns = getAlbums(c, -1);
        if (albuns.size() == 0) return null;
        else return albuns.get(0);
    }

    public ArrayList<Album> getAllAlbums() {
        return getAlbums(getAllAlbunsCursor(), -1);

    }


    private ArrayList<Album> getAlbums(final Cursor cur, int maxResults) {

        ArrayList<Album> albums = new ArrayList<>();
        if (cur != null && cur.moveToFirst()) {

            int _idColumn = cur.getColumnIndex(MediaStore.Audio.Albums._ID);
            int albumColumn = cur.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int artistColumn = cur.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
            int numOfSongsColumn = cur.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);

            do {
                long _id = cur.getInt(_idColumn);
                String title = cur.getString(albumColumn);
                String artist = cur.getString(artistColumn);
                int numOfSongs = cur.getInt(numOfSongsColumn);

                Album mAlbum = new Album(_id);
                mAlbum.setArtist(artist);
                mAlbum.setName(title);
                mAlbum.setNumberOfTracks(numOfSongs);
                //mAlbum.updateFromDatabase();// load local stats

                albums.add(mAlbum);
                if (maxResults != -1 && albums.size() == maxResults) break;
            } while (cur.moveToNext());
        }
        if (cur != null) {
            cur.close();
        }
        return albums;
    }

}
