package gilianmarques.dev.musicplayer.activities.library.fragment_playlists;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.pixplicity.easyprefs.library.Prefs;
import com.wonderkiln.blurkit.BlurKit;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.activities.library.fragment_playlists.search.SearchActivity;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.models.Playlist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.sorting.SortDialog;
import gilianmarques.dev.musicplayer.sorting.utils.Sort;
import gilianmarques.dev.musicplayer.sorting.utils.SortTypes;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.BitmapUtils;
import gilianmarques.dev.musicplayer.utils.PaletteUtils;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

public class ViewPlaylistActivity extends MyActivity {
    private Bitmap lastArtBlur, lastArt;
    private Toolbar mToolbar;
    private PlaylistTracksAdapter adapter;
    private Runnable finishTransition;
    private int animDur = 250;
    private View parent;
    private ObjectAnimator rvEnterAnim;
    Playlist playlist;
    RealmResults<Playlist> result;
    private ImageView ivArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        int statusBarColor = getIntent().getIntExtra("color", Color.CYAN);
        getWindow().setNavigationBarColor(statusBarColor);

        setContentView(R.layout.activity_view_playlist);

        String pId = getIntent().getStringExtra("pl_id");

        result = UIRealm.getRealm(null).where(Playlist.class).equalTo("id", pId).findAll();
        playlist = result.first();
        result.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Playlist>>() {
            @Override
            public void onChange(@NonNull RealmResults<Playlist> playlists, @NonNull OrderedCollectionChangeSet changeSet) {
                if (adapter != null) {
                    if (playlists.size() > 0) playlist = playlists.first();
                } else result.removeAllChangeListeners();

            }
        });
        Track track = playlist.getFirstTrack();
        if (track != null)
            lastArtBlur = BitmapUtils.loadArtWithIdOnUIThread(track.getAlbum().getURI());

        if (lastArtBlur == null) lastArtBlur = BitmapUtils.defArt;
        lastArt = lastArtBlur.copy(lastArtBlur.getConfig(), true);

        init();

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                initTransition();
            }
        };
        new Handler().postDelayed(mRunnable, 100);
    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_playlists, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.add_tracks:
                if (playlist.livePlaylist()) {
                    Toasty.error(this, getString(R.string.Voce_nao_pode_editar_essa), Toast.LENGTH_LONG).show();
                } else
                    startActivity(new Intent(this, SearchActivity.class).putExtra("pl_id", playlist.getId()));
                break;
            case R.id.remove_playlist:
                if (playlist.livePlaylist()) {
                    Toasty.error(ViewPlaylistActivity.this, getString(R.string.Voce_nao_pode_remover), Toast.LENGTH_LONG).show();
                } else confirmRemotion();
                break;
            case R.id.edit_playlist:
                editPlaylist();
                break;
            case R.id.sort:
                new SortDialog(ViewPlaylistActivity.this, new SortDialog.Callback() {
                    @Override public void selected(final SortTypes type) {
                        Runnable mRunnable = new Runnable() {
                            @Override
                            public void run() {
                                Prefs.putInt(c.sorting_view_playlists, type.value);

                                if (type.value == SortTypes.FAVORITS.value)
                                    for (Track track : adapter.getItens()) track.reloadLocalInfo();

                                adapter.update(Sort.Tracks.sort(type, adapter.getItens()));

                                runOnUiThread(new Runnable() {
                                    @Override public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        };
                        new Thread(mRunnable).start();
                    }
                }).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void editPlaylist() {
        if (playlist.livePlaylist()) {
            Toasty.error(this, getString(R.string.Voce_nap_pode_renomear), Toast.LENGTH_LONG).show();
            return;
        }
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.Renomear_playlist)
                .input(getString(R.string.Nome_da_playlist), playlist.getName(), false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, final CharSequence input) {
                        UIRealm.get().executeTransaction(new Realm.Transaction() {
                            @Override public void execute(Realm realm) {
                                playlist.setName(input.toString());
                            }
                        });
                        mToolbar.setTitle(playlist.getName());
                    }
                }).inputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE).positiveText(R.string.Concluir)
                .build();
        dialog.show();
    }

    private void confirmRemotion() {
        new MaterialDialog.Builder(this)
                .title(R.string.Deseja_memo_remover_esta).content(R.string.Esta_acao_nao_podera)
                .positiveText(getString(R.string.Remover))
                .negativeText(R.string.Cancelar)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        UIRealm.get().executeTransaction(new Realm.Transaction() {
                            @Override public void execute(@NonNull Realm realm) {
                                realm.where(Playlist.class).equalTo("id", playlist.getId()).findAll().deleteAllFromRealm();
                                Toasty.success(ViewPlaylistActivity.this, getString(R.string.Playlist_removida_com), Toast.LENGTH_LONG).show();
                                Runnable mRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                };
                                new Handler().postDelayed(mRunnable, 800);
                            }
                        });
                    }
                }).build().show();
    }

    private void initTransition() {
        final CardView cardArt = findViewById(R.id.cv);
        final ConstraintLayout cardParent = (ConstraintLayout) cardArt.getParent();
        ivArt = findViewById(R.id.iv_art);
        final float cornerRadius = cardArt.getRadius();
        final int artWidth = getIntent().getIntExtra("art_w", 20);
        final int artHeight = getIntent().getIntExtra("art_h", 20);
        final int y = getIntent().getIntExtra("y", 20);
        final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cardArt.getLayoutParams();

        cardArt.setY(cardArt.getY() - MyActivity.statusBarHeight / 2);
        ivArt.setImageBitmap(lastArtBlur);
        params.width = artWidth;
        params.height = artHeight;
        cardArt.setLayoutParams(params);
        //cardParent.setY(cardParent.getY() - (int)(MyActivity.statusBarHeight/2));
        //  Log.d(App.myFuckingUniqueTAG + "ViewPlaylistActivity", "initTransition: p_W: " + parent.getMeasuredWidth() + " p_H: " + parent.getMeasuredHeight() + " artW " + artWidth + " artH " + artHeight);

        Runnable startTransition = new Runnable() {
            @Override public void run() {

                ValueAnimator animWidth = ValueAnimator.ofInt(artWidth, cardParent.getMeasuredWidth());
                animWidth.setDuration(animDur);
                animWidth.setInterpolator(new FastOutSlowInInterpolator());
                animWidth.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animation) {
                        params.width = (int) animation.getAnimatedValue();
                        cardArt.setLayoutParams(params);
                    }
                });

                //

                ValueAnimator animHeight = ValueAnimator.ofInt(artHeight, cardParent.getMeasuredHeight() + MyActivity.statusBarHeight);
                animHeight.setDuration(animDur);
                animHeight.setInterpolator(new FastOutSlowInInterpolator());
                animHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animation) {
                        params.height = (int) animation.getAnimatedValue();
                        cardArt.setLayoutParams(params);
                    }
                });
                animHeight.addListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        rvEnterAnim.start();
                        BlurKit.getInstance().blur(lastArtBlur, 12);
                        super.onAnimationEnd(animation);
                    }
                });
                animHeight.setStartDelay(animDur / 10 * 9);


                //

                ValueAnimator animRadius = ValueAnimator.ofFloat(cornerRadius, 0f);
                animRadius.setDuration(animDur);
                animRadius.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animation) {
                        cardArt.setRadius((Float) animation.getAnimatedValue());
                    }
                });

                animWidth.start();
                animHeight.start();
                animRadius.start();

            }
        };


        startTransition.run();
        findViewById(R.id.include).setVisibility(View.VISIBLE);

        //-----------------------------------------------------------------------------------------
        finishTransition = new Runnable() {
            @Override public void run() {
                animDur += animDur / 2;

                ValueAnimator animWidth = ValueAnimator.ofInt(cardArt.getMeasuredWidth(), artWidth);
                animWidth.setDuration(animDur);
                animWidth.setInterpolator(new FastOutSlowInInterpolator());
                animWidth.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animation) {
                        params.width = (int) animation.getAnimatedValue();
                        cardArt.setLayoutParams(params);
                    }
                });

                //

                ValueAnimator animHeight = ValueAnimator.ofInt(cardArt.getMeasuredHeight(), artHeight);
                animHeight.setDuration(animDur);
                animHeight.setInterpolator(new FastOutSlowInInterpolator());
                animHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animation) {
                        params.height = (int) animation.getAnimatedValue();
                        cardArt.setLayoutParams(params);
                    }
                });

                animHeight.setStartDelay(animDur / 2);

                //

                ValueAnimator animRadius = ValueAnimator.ofFloat(0f, cornerRadius);
                animRadius.setDuration(animDur);
                animRadius.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animation) {
                        cardArt.setRadius((Float) animation.getAnimatedValue());
                    }
                });

                AlphaAnimation anim = new AlphaAnimation(1, 0);
                anim.setDuration(animDur / 2);
                anim.setFillAfter(true);


                parent.startAnimation(anim);
                animWidth.start();
                animHeight.start();
                animRadius.start();
                ivArt.setImageBitmap(lastArt);


            }
        };

    }

    private void init() {

        final RecyclerView mRecyclerView = findViewById(R.id.rv);
        mToolbar = findViewById(R.id.tb);
        parent = findViewById(R.id.parentLayout);

        mToolbar.setTitle(playlist.getName());

        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(App.binder.get()));

        adapter = new PlaylistTracksAdapter(this, playlist, mRecyclerView, new PlaylistTracksAdapter.Callback() {
            @Override
            public void onTrackClicked(int position) {
                MusicService.binder.getPlayer().initFromPlaylist(playlist, position, false);
                onBackPressed();
            }


        });

// updated on onResume        adapter.update((playlist.getTracks(true)));

        PaletteUtils.colorFrom(lastArtBlur, new PaletteUtils.PaletteCallback() {
            @Override public void result(int primary, int secundary, int background) {
                int color = Utils.changeAlpha(background, 0.4f);
                parent.setBackgroundColor(color);
                getWindow().setStatusBarColor(color);
                getWindow().setNavigationBarColor(color);

            }
        });

        rvEnterAnim = ObjectAnimator.ofFloat(mRecyclerView, "y", Utils.screenHeight * 2, mRecyclerView.getY());
        rvEnterAnim.setDuration(450).setInterpolator(new FastOutSlowInInterpolator());
        rvEnterAnim.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationStart(Animator animation) {
                YoYo.with(Techniques.FadeIn).duration(300).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        parent.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).playOn(parent);
                super.onAnimationStart(animation);
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {

        finishTransition.run();

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                ViewPlaylistActivity.super.onBackPressed();
            }
        };
        new Handler().postDelayed(mRunnable, animDur + (animDur / 3));

    }


    @Override protected void onResume() {
        if (adapter != null) {
            adapter.update(playlist.getTracks(true));
            adapter.notifyDataSetChanged();
        }
        super.onResume();
    }
}
