package gilianmarques.dev.musicplayer.activities.library.fragment_albuns;


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
import gilianmarques.dev.musicplayer.persistence.native_database.NativeAlbuns;
import gilianmarques.dev.musicplayer.sorting.utils.SortTypes;
import gilianmarques.dev.musicplayer.utils.AdapterUtils;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbunsFragment extends MyFragment {
    private RecyclerView mRecyclerView;
    private AlbunsAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_albuns, container, false);
    }


    @Override
    protected void init() {

        mRecyclerView = findViewById(R.id.rv_albuns);
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


        MyGridLayoutManager lManager = new MyGridLayoutManager(AdapterUtils.RV_ALBUM_VIEW_SIZE);

        mAdapter = new AlbunsAdapter(new NativeAlbuns().getAllAlbums(), mActivity);

        mRecyclerView.setLayoutManager(lManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override public void sort(SortTypes type) {

        super.sort(type);
    }
}
