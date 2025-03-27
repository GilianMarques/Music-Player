package gilianmarques.dev.musicplayer.activities.library.fragment_artists;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.library.LibraryActivity;
import gilianmarques.dev.musicplayer.customs.MyFragment;
import gilianmarques.dev.musicplayer.customs.MyGridLayoutManager;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeArtists;
import gilianmarques.dev.musicplayer.utils.AdapterUtils;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistsFragment extends MyFragment {

    private RecyclerView mRecyclerView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.fragment_artists, container, false);
    }



    @Override
    protected void init() {

        mRecyclerView = findViewById(R.id.rv_artists);
        mRecyclerView.setHasFixedSize(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Utils.applyPadding(mRecyclerView, true, false);
        if (mRecyclerView.getMeasuredWidth() > 0) finishSettingUp(mRecyclerView);
        else mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView.getMeasuredWidth() > 0) finishSettingUp(mRecyclerView);
                else mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                finishSettingUp(mRecyclerView);
                            }
                        });

            }
        });

        mRecyclerView.addOnScrollListener(((LibraryActivity) mActivity).fragmentsRvScrollListener);

    }

    private void finishSettingUp(RecyclerView mRecyclerView) {

        MyGridLayoutManager lManager = new MyGridLayoutManager(AdapterUtils.RV_ARTIST_VIEW_SIZE);

        final ArtistsAdapter mAdapter = new ArtistsAdapter(new NativeArtists().getAllArtists(), mActivity, lManager.getSpanCount());
        mRecyclerView.setLayoutManager(lManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
