package gilianmarques.dev.musicplayer.activities.artist_details;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.models.Artist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeArtists;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;

public class ArtistDetailsActivity extends MyActivity {
    private Artist mArtist;
    private Bitmap map;
    private LinearLayout container;
    private CollapsingToolbarLayout cltb;
    private RecyclerView rvAlbuns;
    private String duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_details);
        long id = getIntent().getLongExtra("a_id", 0);
        mArtist = new NativeArtists().getArtistsByID(id);
        if (mArtist == null) {
            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                    Toasty.info(App.binder.get(), App.binder.get().getString(R.string.Artista_nao_encontrado), 3000).show();
                }
            };
            new Handler().postDelayed(mRunnable, 150);
            finish();
            return;
        }
        mArtist.addLocalInfo();
        // loading duration
        ArrayList<Track> tracks = mArtist.getTracks();
        long t = 0;
        for (Track track : tracks) {
            t += track.getDurationMillis();
        }
        duration = Utils.millisToFormattedString(t);
        // end loading duration
        ArtistDetailsFragment fragment = ArtistDetailsFragment.newInstance(mArtist);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        loadRvAlbuns();
        init();


    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mWindow.setStatusBarColor(0);
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    }

    private void loadRvAlbuns() {

        AlbunsAdapter albunsAdapter = new AlbunsAdapter(mArtist, this);

        LinearLayoutManager lManager = new LinearLayoutManager(this);
        lManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        rvAlbuns = findViewById(R.id.rv_albuns);


        rvAlbuns.setHasFixedSize(true);
        rvAlbuns.setAdapter(albunsAdapter);
        rvAlbuns.setLayoutManager(lManager);
        View parent = findViewById(R.id.rvParent);
        rvAlbuns.setPadding(0, 0, 0, MyActivity.statusBarHeight);
        parent.setPadding(0, MyActivity.statusBarHeight, 0, 0);


    }


    private void init() {
        cltb = findViewById(R.id.cltb);

        ImageView ivProfArt = findViewById(R.id.iv_prof_art);
        ImageView art = findViewById(R.id.art);
        TextView tvArtist = findViewById(R.id.tv_artist_name);
        TextView tvInfo = findViewById(R.id.tv_artist_info);


        container = findViewById(R.id.container);

        tvArtist.setText(mArtist.getName());
        findViewById(R.id.appbar).setElevation(0f);
        tvInfo.setText(mArtist.getInfo() + "\n" + duration);

        art.setTransitionName(getIntent().getStringExtra("tName"));

        ivProfArt.setImageBitmap(map);
        final FloatingActionButton fabShuffle = findViewById(R.id.fabShuffle);
        fabShuffle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                App.binder.get().goToPlayingNow(ArtistDetailsActivity.this);
                MusicService.binder.getPlayer().initFromArtist(mArtist, 0, true);
            }
        });

        cltb.post(new Runnable() {
            @Override public void run() {
                ViewGroup.LayoutParams params = cltb.getLayoutParams();
                params.height = (int) ((Utils.screenHeight - (rvAlbuns.getMeasuredHeight() * 2)));
                cltb.setLayoutParams(params);
            }
        });

        Picasso.get().load(mArtist.getUrl()).error(R.drawable.no_art_background)
                .placeholder(R.drawable.no_art_background).into(art);

        Picasso.get().load(mArtist.getUrl()).error(R.drawable.no_art_background)
                .placeholder(R.drawable.no_art_background).into(ivProfArt);

    }

    @Override public void onBackPressed() {
        super.onBackPressed();
    }
}
