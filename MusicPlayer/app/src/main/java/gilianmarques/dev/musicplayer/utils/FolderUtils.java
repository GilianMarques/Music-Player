package gilianmarques.dev.musicplayer.utils;

import android.support.annotation.WorkerThread;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import gilianmarques.dev.musicplayer.models.Folder;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.sorting.utils.Sort;
import gilianmarques.dev.musicplayer.sorting.utils.SortTypes;

/**
 * Criado por Gilian Marques
 * Quarta-feira, 18 de Julho de 2018  as 18:50:22.
 *
 * @Since 0.1 Beta
 */
public class FolderUtils {

    @WorkerThread
    public static ArrayList<Folder> createFolders() {
        ArrayList<Track> allTracks = new NativeTracks(false).getAllTracks();

        allTracks= Sort.Tracks.sort(SortTypes.FOLDER, allTracks);
        ArrayList<Folder> folders = new ArrayList<>();

        ArrayList<String> folderPaths = new ArrayList<>();

        for (Track mTrack : allTracks) {
            if (!folderPaths.contains(mTrack.getFilePathNoName()))
                folderPaths.add(mTrack.getFilePathNoName());
        }

        for (String folderPath : folderPaths) {
            String[] folderNameSpliter = folderPath.split(File.separator);
            String folderName = folderNameSpliter[folderNameSpliter.length - 1];
            Folder folder = new Folder(folderName);

            ArrayList<Track> tracksFromThisFolder = new ArrayList<Track>();

            // to separate tracks from this folder
            for (Track mTrack : allTracks) {
                if (mTrack.getFilePathNoName().equals(folderPath)) tracksFromThisFolder.add(mTrack);
            }

            // for reduce amount of tracks
            for (Track track : tracksFromThisFolder) {
                allTracks.remove(track.getId());
            }

            folder.setTracks(tracksFromThisFolder);
            folder.setPath(folderPath);
            folders.add(folder);
        }

        Collections.sort(folders, new Comparator<Folder>() {
            @Override public int compare(Folder o1, Folder o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return folders;

    }
}
