package gilianmarques.dev.musicplayer.activities.album_details;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeAlbuns;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.tag_editor.TaggerAlbuns;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.BitmapUtils;
import gilianmarques.dev.musicplayer.utils.EasyAsynk;
import gilianmarques.dev.musicplayer.utils.PaletteUtils;
import gilianmarques.dev.musicplayer.utils.Utils;

public class AlbumDetailsActivity extends MyActivity implements View.OnClickListener {
    private Album mAlbum;
    private RecyclerView rv;
    private FloatingActionButton fabShuffle, fabEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbum = new NativeAlbuns().getAlbumByID(getIntent().getLongExtra("album", 0));
        if (mAlbum == null) {
            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                    Toasty.info(App.binder.get(), App.binder.get().getString(R.string.Album_nao_encontrado), 3000).show();
                }
            };
            new Handler().postDelayed(mRunnable, 150);
            finish();
        } else {
            setContentView(R.layout.activity_album_details);
            init();

        }


    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mWindow.setStatusBarColor(0);
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        //status bar must have white icons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

    }

    private void init() {
        //1
        rv = findViewById(R.id.rv);
        final TextView tvArtistName = findViewById(R.id.tvArtistName);
        TextView tvAlbumName = findViewById(R.id.tvAlbumName);
        ImageView ivAlbumArt = findViewById(R.id.iv_art);
        ImageView ivProfArt = findViewById(R.id.ivProfArt);
        fabShuffle = findViewById(R.id.fab_play_random);
        fabEdit = findViewById(R.id.fabEdit);
        fabShuffle.setOnClickListener(AlbumDetailsActivity.this);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AlbumDetailsActivity.this.startActivity(new Intent(AlbumDetailsActivity.this, TaggerAlbuns.class).putExtra("id", mAlbum.getId()));
            }
        });

        Utils.applyPadding(rv, true, false);

        tvAlbumName.setText(mAlbum.getName());

        tvArtistName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override public void afterTextChanged(Editable s) {
                tvArtistName.removeTextChangedListener(this);
                tvArtistName.setText(mAlbum.getArtist() + "\n" + tvArtistName.getText());
            }
        });
        // when this call put info in the TextView i'll complete it with the TextChangedListener
        mAlbum.getDuration(tvArtistName, this);

        new EasyAsynk(new EasyAsynk.Actions() {
            private ArrayList<Track> mTracks;

            @Override public int doInBackground() {
                mTracks = new NativeTracks(false).getTracksByAlbum(mAlbum);
                return 0;
            }

            @Override public void onPostExecute() {
                rv.setHasFixedSize(true);
                rv.setLayoutManager(new LinearLayoutManager(AlbumDetailsActivity.this));


                TracksAdapter mAdapter = new TracksAdapter(AlbumDetailsActivity.this);
                mAdapter.setCallback(new TracksAdapter.Callback() {
                    @Override public void onTrackClicked(int position) {
                        MusicService.binder.getPlayer().initFromAlbum(mAlbum, position, false);
                        App.binder.get().goToPlayingNow(AlbumDetailsActivity.this);
                        super.onTrackClicked(position);
                    }
                });
                mAdapter.update(mTracks);
                rv.setAdapter(mAdapter);
            }


        }).executeAsync();

        CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) ivAlbumArt.getLayoutParams();
        //noinspection SuspiciousNameCombination
        params.height = getResources().getDisplayMetrics().widthPixels;
        ivAlbumArt.setLayoutParams(params);

        Bitmap albumArt = BitmapUtils.loadArtWithIdOnUIThread(mAlbum.getURI());
        if (albumArt == null) albumArt = BitmapUtils.defArt;
        ivAlbumArt.setImageBitmap(albumArt);
        ivProfArt.setImageBitmap(albumArt);
        ivAlbumArt.setTransitionName(getIntent().getStringExtra("ivAlbumArt_transName"));


        PaletteUtils.colorFrom(albumArt, new PaletteUtils.PaletteCallback() {
            @Override public void result(int primary, int secundary, int background) {
                fabShuffle.setBackgroundTintList(ColorStateList.valueOf(background));
                fabEdit.setBackgroundTintList(ColorStateList.valueOf(background));
            }
        });

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.fab_play_random:
                initFromAlbum();
                break;
        }
    }

    private void initFromAlbum() {
        MusicService.binder.getPlayer().initFromAlbum(mAlbum, 0, true);
        App.binder.get().goToPlayingNow(this);
    }

    @Override
    public void onBackPressed() {

        ScaleAnimation sAnim = new ScaleAnimation(1, 0
                , 1, 0
                , Animation.RELATIVE_TO_SELF, 0.5f
                , Animation.RELATIVE_TO_SELF, 0.5f);
        sAnim.setDuration(350);
        sAnim.setFillAfter(true);
        sAnim.setInterpolator(new FastOutSlowInInterpolator());
        fabShuffle.startAnimation(sAnim);


        int cHeight = (int) Utils.screenHeight;
        int cY = (int) rv.getY();
        ObjectAnimator anim = ObjectAnimator.ofFloat(rv, "y", cY, cHeight);
        anim.setDuration(400).setInterpolator(new LinearOutSlowInInterpolator());
        anim.start();


        Runnable closeRunnable = new Runnable() {
            @Override
            public void run() {
                AlbumDetailsActivity.super.onBackPressed();
            }
        };


        if (!Utils.isOrientationPortrait()) {
            ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(true);
            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                    AlbumDetailsActivity.super.onBackPressed();
                }


            };
            new Handler().postDelayed(mRunnable, 420);

        } else new Handler().postDelayed(closeRunnable, 420);


    }


}
