package gilianmarques.dev.musicplayer.activities.artist_details;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.customs.MyFragment;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.models.Artist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistDetailsFragment extends MyFragment {


    private Artist mArtist;
    private ArrayList<Track> tracks;

    public static ArtistDetailsFragment newInstance(Artist mArtist) {
        ArtistDetailsFragment inst = new ArtistDetailsFragment();
        inst.mArtist = mArtist;
        return inst;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist_details, container, false);
    }


    @Override protected void init() {
        if (mArtist == null) return;
        ArtistsFragmentTracksAdapter mAdapter = new ArtistsFragmentTracksAdapter(mActivity);
        mAdapter.setCallback(new ArtistsFragmentTracksAdapter.Callback() {
            @Override public void onTrackClicked(int position) {
                MusicService.binder.getPlayer().initFromArtist(mArtist, position, false);
                App.binder.get().goToPlayingNow(mActivity);

            }
        });

        tracks = new NativeTracks(false).getTracksByArtist(mArtist.getName());
        mAdapter.update(tracks);

        final RecyclerView rvTracks = findViewById(R.id.rv_tracks);
        rvTracks.setHasFixedSize(true);
        rvTracks.setAdapter(mAdapter);
        rvTracks.setLayoutManager(new LinearLayoutManager(mActivity));

    }


    public View getView() {
        return rootView;
    }


}

