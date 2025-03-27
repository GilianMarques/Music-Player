package gilianmarques.dev.musicplayer.activities.library.fragment_tracks;


import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Objects;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.library.LibraryActivity;
import gilianmarques.dev.musicplayer.customs.MyFragment;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.sorting.utils.Sort;
import gilianmarques.dev.musicplayer.sorting.utils.SortTypes;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.EasyAsynk;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class TracksFragment extends MyFragment {
    private Adapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracks, container, false);
    }


    @Override protected void init() {

        final RecyclerView mRecyclerView = findViewById(R.id.rv_tracks);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(((LibraryActivity) Objects.requireNonNull(getActivity())).fragmentsRvScrollListener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(App.binder.get()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Utils.applyPadding(mRecyclerView, true, false);
        // initializing and loading mAdapter in background
        new EasyAsynk(new EasyAsynk.Actions() {

            ArrayList<Track> mTracks;
            Adapter.Callback callback;

            @Override public int doInBackground() {
                mAdapter = new Adapter(getActivity());
                mTracks = new NativeTracks(true).getAllTracks();

                mTracks = Sort.Tracks.sort(c.sorting_tracks_fragment, mTracks);
                callback = new Adapter.Callback() {
                    @Override
                    public void onTrackClicked(final int position) {
                        App.binder.get().goToPlayingNow(mActivity);
                        Runnable mRunnable = new Runnable() {
                            @Override
                            public void run() {
                                MusicService.binder.getPlayer().initFromAllTracks(position, mTracks);
                            }
                        };
                        new Handler().postDelayed(mRunnable, 500);
                    }
                };
                return super.doInBackground();
            }

            @Override public void onPostExecute() {
                mAdapter.setCallback(callback);
                mAdapter.update(mTracks);
                mRecyclerView.setAdapter(mAdapter);

                super.onPostExecute();
            }
        }).executeAsync();


    }

    @Override public void sort(final SortTypes type) {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                for (Track track : mAdapter.getItens()) {
                    track.reloadLocalInfo();
                }
                mAdapter.update(Sort.Tracks.sort(type, mAdapter.getItens()));

                App.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        if (mAdapter != null) new Thread(mRunnable).start();
        super.sort(type);
    }

    public ArrayList<Track> getTracks() {
        return mAdapter.getItens();
    }
}
