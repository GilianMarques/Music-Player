package gilianmarques.dev.musicplayer.activities.library.fragment_playlists;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.customs.FadeImageView;
import gilianmarques.dev.musicplayer.customs.MyFragment;
import gilianmarques.dev.musicplayer.models.Playlist;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.PaletteUtils;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistsFragment extends MyFragment {
    private View  colorView;
    private PlaylistsAdapter mAdapter;
    private FadeImageView fiv;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager lManager;
    private boolean visible;
    private LottieComposition anim;
    private LottieAnimationView animView;
    TextView tvHint;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlists, container, false);
    }



    @Override public void setUserVisibleHint(boolean isVisibleToUser) {
        visible = isVisibleToUser;
        if (isVisibleToUser) {

            if (strongRefPlaylists != null) {
                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        toogleAnim(strongRefPlaylists.size() == 0);
                    }
                };
                new Handler().postDelayed(mRunnable, 10);
            }


        } else {
            if (animView != null && anim != null) {
     Runnable mRunnable = new Runnable() {
                 @Override
                 public void run() {
                     animView.setProgress(0);

                 }
             };
     new Handler().postDelayed(mRunnable,500);              }

        }

        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override protected void init() {

        tvHint = findViewById(R.id.tvHint);

        colorView = findViewById(R.id.colorView);

        mAdapter = new PlaylistsAdapter(mActivity);
        mAdapter.setCallback(callback);

        lManager = new LinearLayoutManager(App.binder.get(), LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView = findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(true);
        animView = findViewById(R.id.animation_view);

        SnapHelper snapHelper = new PagerSnapHelper(); // makes scroll like viewpager
        mRecyclerView.setLayoutManager(lManager);
        snapHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);


        fiv = findViewById(R.id.fiv);
        fiv.setBlur(true);


        // initializing and loading mAdapter in background


        LottieComposition.Factory.fromAssetFileName(mActivity, "anim/no_playlist.json", new OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(@Nullable final LottieComposition composition) {
                anim = composition;
                animView.setComposition(anim);
                animView.setSpeed(1.5f);
                updateFragment();
            }
        });

    }

    /**
     * @param map quando a primeira view do Rv é carregada por algum motivo nao dava pra obter a arte do imageview no viewholder, sempre retornava a arte padrao que
     *            é usada como placeholder pelo picasso, mesmo com delay de 300mls a arte nao carregava e isso era so com a primeira. O callback do RV avisa qd a
     *            primeira arte é carregada e passa ela pra cá pra atualizar o fundo, resolvendo a treta!
     */
    private void changeArt(@NonNull Bitmap map) {
        fiv.setImageBitmap(map);


        PaletteUtils.colorFrom(map, new PaletteUtils.PaletteCallback() {
            @Override public void result(int primary, int secundary, int background) {
                colorView.setBackgroundColor(Utils.changeAlpha(background, 0.75f));

            }
        });
    }

    RealmResults<Playlist> strongRefPlaylists;

    private void updateFragment() {

        RealmQuery<Playlist> query = UIRealm.getRealm(null).where(Playlist.class);
        strongRefPlaylists = query.findAll();
        toogleAnim(strongRefPlaylists.size() == 0);
        mAdapter.update(new ArrayList<>(strongRefPlaylists));
        strongRefPlaylists.addChangeListener(realmCallback);

    }

    OrderedRealmCollectionChangeListener<RealmResults<Playlist>> realmCallback = new OrderedRealmCollectionChangeListener<RealmResults<Playlist>>() {
        @Override
        public void onChange(@NonNull RealmResults<Playlist> playlists, @NonNull OrderedCollectionChangeSet changeSet) {
            if (mAdapter != null) {
                mAdapter.update(new ArrayList<>(strongRefPlaylists));
                if (strongRefPlaylists.size() > 0) toogleAnim(false);
                else {
                    toogleAnim(true);
                }
            } else strongRefPlaylists.removeAllChangeListeners();

        }
    };
    PlaylistsAdapter.Callback callback = new PlaylistsAdapter.Callback() {
        @Override public void firstArtLoaded(Bitmap art) {
            changeArt(art);
            super.firstArtLoaded(art);
        }


        @Override public void onClick(final PlaylistsAdapter.MyViewHolder holder) {
            final Intent mIntent = new Intent(getActivity(), ViewPlaylistActivity.class);

            Playlist playlist = mAdapter.getList(holder.getAdapterPosition());


            CardView cv = holder.itemView.findViewById(R.id.cv);

            mIntent.putExtra("art_w", cv.getMeasuredWidth());
            mIntent.putExtra("art_h", cv.getMeasuredHeight());
            mIntent.putExtra("pl_id", playlist.getId());

            mActivity.startActivityForResult(mIntent, 100);


        }
    };

    private void toogleAnim(boolean show) {

        if (show) {
            animView.setVisibility(View.VISIBLE);
            tvHint.setVisibility(View.VISIBLE);
            fiv.setVisibility(View.INVISIBLE);
            colorView.setBackgroundColor(0);
            animView.playAnimation();

        } else {
            animView.cancelAnimation();
            animView.setVisibility(View.INVISIBLE);
            tvHint.setVisibility(View.INVISIBLE);
            fiv.setVisibility(View.VISIBLE);
        }

    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }
}
