package gilianmarques.dev.musicplayer.sorting.utils;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Sextafeira, 24 de Maio de 2019  as 16:13:11.
 */
public class Sort {


    public static String key_to_sort = "";


    public static class Tracks {


        @WorkerThread
        public static ArrayList<Track> sort(String prefsKey, ArrayList<Track> t) {
            SortTypes type = SortTypes.toSortingType(Prefs.getInt(prefsKey, SortTypes.DEFAULT_.value));
            return sort(type, t);

        }
        /*
        *
        * 1 - animal i have...
        * 2 - never too late
        * 3 - pain
        * 4 - time of dying
        * ----------------------134
        * */

        @WorkerThread
        public static ArrayList<Track> sort(@Nullable SortTypes type, ArrayList<Track> t) {
            //porrrrrrra de buggggggggggggggggggggg
            Collections.sort(t, compareTempPosition);
            ArrayList<Track> tracks = new ArrayList<>(t);

            if (type == null) type = SortTypes.DEFAULT_;

            Log.d(App.myFuckingUniqueTAG + "ArrayList<Track>", "sort_: Ordenando musicas: " + type.getValue());

            if (type == SortTypes.ALBUM) {
                Collections.sort(tracks, compareAlbum);
            } else if (type == SortTypes.TITLE) {
                Collections.sort(tracks, compareTitle);
            } else if (type == SortTypes.ARTIST) {
                Collections.sort(tracks, compareArtist);
            } else if (type == SortTypes.TEMP_POSITION) {
                Collections.sort(tracks, compareTempPosition);
            } else if (type == SortTypes.DATE) {
                Collections.sort(tracks, comparelastReproductionDate);
            } else if (type == SortTypes.FOLDER) {
                Collections.sort(tracks, compareFolder);
            } else if (type == SortTypes.FAVORITS) {
                Collections.sort(tracks, compareFavoritism);
            }

            return tracks;

        }


        public static final Comparator<Track> compareTitle = new Comparator<Track>() {
            @Override
            public int compare(Track mTrack1, Track mTrack2) {
                return mTrack1.getTitle().compareTo(mTrack2.getTitle());
            }
        };

        public static final Comparator<Track> compareFolder = new Comparator<Track>() {
            @Override
            public int compare(Track mTrack1, Track mTrack2) {
                return mTrack1.getFilePath().compareTo(mTrack2.getFilePath());
            }
        };

        public static final Comparator<Track> compareAlbum = new Comparator<Track>() {
            @Override
            public int compare(Track mTrack1, Track mTrack2) {
                return mTrack1.getAlbumName().compareTo(mTrack2.getAlbumName());
            }
        };

        public static final Comparator<Track> compareArtist = new Comparator<Track>() {
            @Override
            public int compare(Track mTrack1, Track mTrack2) {
                return mTrack1.getArtistName().compareTo(mTrack2.getArtistName());
            }
        };

        public static final Comparator<Track> compareFavoritism = new Comparator<Track>() {
            @Override
            public int compare(Track mTrack1, Track mTrack2) {
                //is in crescent 0->1->2, tested 29/06/18
                return (int) (mTrack2.getPlayedTime() - mTrack1.getPlayedTime());
            }
        };

        public static final Comparator<Track> comparelastReproductionDate = new Comparator<Track>() {
            @Override
            public int compare(Track mTrack1, Track mTrack2) {
                return (int) (mTrack2.getLastReproductionDate() - mTrack1.getLastReproductionDate());
            }
        };


        public static final Comparator<Track> compareTempPosition = new Comparator<Track>() {
            @Override
            public int compare(Track mTrack1, Track mTrack2) {
                return mTrack1.getIndex() - mTrack2.getIndex();
            }
        };
    }
}
