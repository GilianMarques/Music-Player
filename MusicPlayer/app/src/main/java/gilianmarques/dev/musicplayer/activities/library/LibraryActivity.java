package gilianmarques.dev.musicplayer.activities.library;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.activities.album_details.AlbumDetailsActivity;
import gilianmarques.dev.musicplayer.activities.artist_details.ArtistDetailsActivity;
import gilianmarques.dev.musicplayer.activities.library.fragment_albuns.AlbunsFragment;
import gilianmarques.dev.musicplayer.activities.library.fragment_artists.ArtistsFragment;
import gilianmarques.dev.musicplayer.activities.library.fragment_playlists.PlaylistsFragment;
import gilianmarques.dev.musicplayer.activities.library.fragment_tracks.Menu;
import gilianmarques.dev.musicplayer.activities.library.fragment_tracks.TracksFragment;
import gilianmarques.dev.musicplayer.customs.MyFragment;
import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.mediaplayer.structure.MusicPlayer;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Artist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeAlbuns;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeArtists;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.sorting.SortDialog;
import gilianmarques.dev.musicplayer.sorting.utils.SortTypes;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.MenuActions;
import gilianmarques.dev.musicplayer.utils.Utils;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import me.tankery.lib.circularseekbar.CircularSeekBar;

import static android.content.Intent.ACTION_MAIN;

/**
 * Criado por Gilian Marques em 01/05/2018 as 17:19:38.
 */

public class LibraryActivity extends MyActivity implements Toolbar.OnMenuItemClickListener, SplashDialog.Callback {
    private AHBottomNavigation bottomNavigation;
    private ViewPager mViewPager;
    private AppBarLayout appBar;
    private View playingNowView;
    private TabsAdapter mAdapter;
    private Runnable rotateButton;
    private SplashDialog splashDialog;
    private ViewsVisibilityControl mVisibilityControl;
    private FloatingSearchView mSearchView;
    private SlidingRootNav menu;
    private CardView serchViewCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        splashDialog = new SplashDialog(this, this);
        splashDialog.show(ACTION_MAIN.equals(getIntent().getAction()));
    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            mWindow.setStatusBarColor(0);
        }
    }

    // TODO: 24/05/2019
    //fix  navbar problem


    /**
     * Called from @{@link SplashDialog} when user grants Storage R/W rights
     */
    @Override public void permissionGranted() {

        mSearchView = findViewById(R.id.floating_search_view);
        mVisibilityControl = new ViewsVisibilityControl(findViewById(R.id.include), mSearchView);
        //  mVisibilityControl.hidePlayingNowView();
        menu = new SlidingRootNav(LibraryActivity.this, mSearchView);

        appBar = findViewById(R.id.appbar);
        appBar.setExpanded(false);

        mViewPager = findViewById(R.id.view_pager);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        playingNowView = findViewById(R.id.PlayingNowView);
        playingNowView.setElevation(Utils.toPX(35));
        initViewPager();

        bottomNavigation.post(new Runnable() {
            @Override public void run() {
                playingNowView.post(new Runnable() {
                    @Override public void run() {
                        initBottomBar();
                        initPlayingView();
                        initSeacrhView();
                        initAppBar();

                        splashDialog.continueToLoading();
                    }
                });
            }
        });
        findViewById(R.id.appbar).setElevation(0f);


    }

    private void initAppBar() {

        appBar.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
            int maxOffset = -1;
            boolean scaledDown = false, scaledUp = true;

            @Override public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                if (i < 0) i = i * -1;
                if (i != 0) {
                    mSearchView.setElevation(1f);
                } else {
                    mSearchView.setElevation(50f);
                }

                if (i >= 0) {
                    if (maxOffset == -1) maxOffset = i;
                    int lastOffset = maxOffset - i;
                    //    Log.d(App.myFuckingUniqueTAG + "LibraryActivity", "onOffsetChanged:  " + maxOffset + " last: " + lastOffset);

                    if (lastOffset <= (maxOffset / 4) * 3 && !scaledDown) {
                        scaledDown = true;
                        scaledUp = false;
                        animate(1.0f, 0.93f);
                    } else if (lastOffset > (maxOffset / 4) * 3 && !scaledUp) {
                        scaledDown = false;
                        scaledUp = true;
                        animate(0.93f, 1.0f);
                    }

                }

            }

            private void animate(float start, float end) {
                //   Log.d(App.myFuckingUniqueTAG + "LibraryActivity", "animate: " + start + " -> " + end);
                Animation anim = new ScaleAnimation(
                        start, end, // Start and end values for the X axis scaling
                        start, end, // Start and end values for the Y axis scaling
                        Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                        Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
                anim.setFillAfter(true); // Needed to keep the result of the animation
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.setDuration(150);
                serchViewCard.startAnimation(anim);
            }
        });
        appBar.setExpanded(true);

    }

    private void initSeacrhView() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Utils.applyPadding(mSearchView, true, false);

        final int primaryText = Utils.fetchColorFromReference(R.attr.app_textColorPrimary);

        LinearLayout l = (LinearLayout) mSearchView.getChildAt(0);
        FrameLayout ll = (FrameLayout) l.getChildAt(0);
        FrameLayout lll = (FrameLayout) ll.getChildAt(0);
        serchViewCard = (CardView) lll.getChildAt(0);
        serchViewCard.setCardElevation(2);


        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.sort) {
                    showSortDialog();
                }
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                final Suggestion mSuggestion = (Suggestion) searchSuggestion;
                int pos = mViewPager.getCurrentItem();
                if (pos == 0) {
                    //tracks

                    final Runnable mRunnable1 = new Runnable() {
                        @Override
                        public void run() {
                            App.binder.get().goToPlayingNow(LibraryActivity.this);
                        }
                    };
                    Runnable mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<Track> tracks =   ((TracksFragment)mAdapter.getItem(0)).getTracks();
                            int index = 0;
                            for (Track track : tracks)
                                if (track.getId() == mSuggestion.getTrack().getId())
                                    index = tracks.indexOf(track);

                            MusicService.binder.getPlayer().initFromAllTracks(index, tracks);
                            runOnUiThread(mRunnable1);
                        }
                    };
                    new Thread(mRunnable).start();
                } else if (pos == 1) {
                    //albums

                    Intent mIntent = new Intent(LibraryActivity.this, AlbumDetailsActivity.class).putExtra("album", mSuggestion.getAlbum().getId());
                    startActivity(mIntent);


                } else if (pos == 2) {
                    //artists
                    Intent mIntent = new Intent(LibraryActivity.this, ArtistDetailsActivity.class);
                    mIntent.putExtra("a_id", mSuggestion.getArtist().getId());
                    startActivity(mIntent);

                }
                mSearchView.clearQuery();
                mSearchView.clearSearchFocus();

            }

            @Override public void onSearchAction(String currentQuery) {

            }
        });
        mSearchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override public void onMenuOpened() {
                menu.openMenu();
            }

            @Override public void onMenuClosed() {
                menu.closeMenu();
            }
        });

        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, final ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {


                int pos = mViewPager.getCurrentItem();
                final Suggestion mSuggestion = (Suggestion) item;

                leftIcon.setPadding(0, 0, 0, 0);
                textView.setTextSize(16);
                textView.setTextColor(primaryText);
                if (pos == 0) {
                    //tracks
                    textView.setText(mSuggestion.getTrack().getTitle());
                    Picasso.get().load(mSuggestion.getTrack().getAlbum().getURI()).resize(200, 200)
                            .placeholder(R.drawable.no_art_background).error(R.drawable.no_art_background).transform(new RoundedCornersTransformation(10, 10)).into(leftIcon);

                    // right icon (menu)
                    LinearLayout p = (LinearLayout) leftIcon.getParent();
                    p.removeViewAt(2);
                    ImageView menu = new ImageView(LibraryActivity.this);
                    menu.setImageResource(R.drawable.vec_menu);

                    menu.setElevation(10f);
                    int padding = (int) Utils.toPX(16);
                    menu.setPadding(padding,padding,padding,padding);
                    menu.setOnClickListener(new Menu(mSuggestion.getTrack(), leftIcon).setCallback(new MenuActions.ActionsCallback() {
                        @Override public void trackRemoved(Track mTrack) {
                            mSearchView.clearSuggestions();
                        }
                    }));
                    p.addView(menu, 2);

                } else if (pos == 1) {
                    //albums
                    textView.setText(mSuggestion.getAlbum().getName());
                    Picasso.get().load(mSuggestion.getAlbum().getURI()).resize(200, 200)
                            .placeholder(R.drawable.no_art_background).error(R.drawable.no_art_background).transform(new RoundedCornersTransformation(10, 10)).into(leftIcon);

                } else if (pos == 2) {
                    //artists
                    textView.setText(mSuggestion.getArtist().getName());
                    Picasso.get().load(mSuggestion.getArtist().getUrl()).resize(200, 200)
                            .placeholder(R.drawable.no_art_background).error(R.drawable.no_art_background).transform(new RoundedCornersTransformation(10, 10)).into(leftIcon);

                }
            }

        });


        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                //  if (newQuery.length() <= 2) return;

                int pos = mViewPager.getCurrentItem();
                MyFragment frag = (MyFragment) mAdapter.getItem(pos);

                if (pos == 0) {
                    //tracks
                    ArrayList<Track> tracks = new NativeTracks(false).getTracksByName(newQuery, 5);
                    List<SearchSuggestion> suggestions = new ArrayList<>();
                    for (Track track : tracks) {
                        suggestions.add(new Suggestion(track));
                    }
                    mSearchView.swapSuggestions(suggestions);
                } else if (pos == 1) {
                    //albums
                    ArrayList<Album> albums = new NativeAlbuns().getAlbumByName(newQuery, 5);
                    List<SearchSuggestion> suggestions = new ArrayList<>();
                    for (Album album : albums) {
                        suggestions.add(new Suggestion(album));
                    }
                    mSearchView.swapSuggestions(suggestions);
                } else if (pos == 2) {
                    //artists
                    ArrayList<Artist> artists = new NativeArtists().getArtistsByName(newQuery, 5);
                    List<SearchSuggestion> suggestions = new ArrayList<>();
                    for (Artist artist : artists) {
                        artist.addLocalInfo();
                        suggestions.add(new Suggestion(artist));
                    }
                    mSearchView.swapSuggestions(suggestions);
                }

            }
        });
    }

    private void initPlayingView() {
        final MusicPlayer player = MusicService.binder.getPlayer();
        final CircularSeekBar pBar = findViewById(R.id.pBar);
        final ImageView mImageView = findViewById(R.id.ivArt);
        final ImageButton btnPlay = findViewById(R.id.btnPlay);
        ImageButton btnNext = findViewById(R.id.btnNext);
        ImageButton btnPrevious = findViewById(R.id.btnPrevious);
        final TextView tvArtist = findViewById(R.id.tvArtist);
        final TextView tvTitle = findViewById(R.id.tvTitle);

        CircularSeekBar.OnCircularSeekBarChangeListener seekListener = new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                if (fromUser)
                    player.seekToPosition(progress);

            }

            @Override public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        };

        PlayerProgressListener mlistener = new PlayerProgressListener(getClass().getSimpleName()) {
            @Override protected void trackChanged(final Track newTrack) {
                pBar.setProgress(0f);

                ImageView menuArt = findViewById(R.id.menuArt);
                if (menuArt != null) {
                    Artist artist = newTrack.getArtist();
                    artist.addLocalInfo();
                    Picasso.get().load(artist.getUrl())
                            .placeholder(R.drawable.no_art_artist_dark)
                            .error(R.drawable.no_art_artist_dark)
                            .into(menuArt);
                }

                Picasso.get().load(newTrack.getAlbum().getURI()).resize(200, 200)
                        .placeholder(R.drawable.no_art_background).error(R.drawable.no_art_background)
                        .transform(new RoundedCornersTransformation(100, 0))
                        .into(mImageView);

                tvArtist.setText(newTrack.getArtistName());
                tvTitle.setText(newTrack.getTitle());

                //  Log.d("LibraryActivity", "initPlayingView:  Album art loaded for: " + newTrack.getTitle() + " Id: " + newTrack.getId() + " album: " + newTrack.getAlbumName());

                super.trackChanged(newTrack);
            }

            @Override protected void progressChanged(float percent, String timer, long millis) {
                pBar.setProgress(percent);


                super.progressChanged(percent, timer, millis);
            }

            @Override protected void playPauseChanged(boolean play) {
                btnPlay.setSelected(play);
                rotateButton.run();
                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mVisibilityControl.showPlayingNowView();
                    }
                };
                new Handler().postDelayed(mRunnable, 500);

                super.playPauseChanged(play);
            }
        };


        rotateButton = new Runnable() {
            @Override
            public void run() {
                int from, to;
                if (MusicService.binder.getPlayer().isPlaying()) {
                    from = 0;
                    to = 90;
                } else {
                    from = 90;
                    to = 0;
                }
                // avoid re-rotating the button
                if (Objects.equals(to, btnPlay.getTag())) return;

                RotateAnimation rotate = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(280);
                rotate.setFillAfter(true);
                rotate.setInterpolator(new FastOutSlowInInterpolator());
                btnPlay.startAnimation(rotate);
                btnPlay.setTag(to);
            }
        };


        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.btnPlay:
                        player.toogle();
                        break;
                    case R.id.btnPrevious:
                        player.previousTrack();
                        break;
                    case R.id.btnNext:
                        player.nextTrack(true);
                        break;
                    case R.id.PlayingNowView:
                        App.binder.get().goToPlayingNow(LibraryActivity.this);
                        break;
                }
            }
        };
// TODO: 28/04/2019 dar um jeito nesse listener
        btnNext.setOnClickListener(clickListener);
        btnPrevious.setOnClickListener(clickListener);
        btnPlay.setOnClickListener(clickListener);
        playingNowView.setOnClickListener(clickListener);
        playingNowView.setY(bottomNavigation.getY() - playingNowView.getMeasuredHeight());
        pBar.setOnSeekBarChangeListener(seekListener);
        mlistener.startReceiving();

    }

    private void initViewPager() {

        mAdapter = new TabsAdapter(getSupportFragmentManager());

        mAdapter.add(new TracksFragment(), "0");
        mAdapter.add(new AlbunsFragment(), "1");
        mAdapter.add(new ArtistsFragment(), "2");
        mAdapter.add(new PlaylistsFragment(), "3");
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(vpPageChangerListener);
        mViewPager.setCurrentItem(Prefs.getInt(c.pager_pos, 0));


    }

    private void initBottomBar() {


// Create AlbumSample
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.faixas, R.drawable.vec_music, R.color.transparent_100);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.albuns, R.drawable.vec_album, R.color.transparent_100);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.artistas, R.drawable.vec_artist, R.color.transparent_100);
        AHBottomNavigationItem menu = new AHBottomNavigationItem(R.string.Playlists, R.drawable.vec_playlist_white, R.color.transparent_100);

// Add AlbumSample
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(menu);


// Disable the translation inside the CoordinatorLayout
        bottomNavigation.setBehaviorTranslationEnabled(true);

// Enable the translation of the FloatingActionButton
        //    bottomNavigation.manageFloatingActionButtonBehavior(floatingActionButton);

// Change colors
        //   bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
        //   bottomNavigation.setInactiveColor(Color.parseColor("#747474"));
        bottomNavigation.setDefaultBackgroundColor(Utils.fetchColorFromReference(R.attr.app_card_background));

// Force to tint the drawable (useful for font with icon for example)
        bottomNavigation.setForceTint(true);

// Display color under navigation bar (API 21+)
// Don't forget these lines in your style-v21
// <item name="android:windowTranslucentNavigation">true</item>
// <item name="android:fitsSystemWindows">true</item>

        //   bottomNavigation.setTranslucentNavigationEnabled(Utils.isOrientationPortrait() &&!Utils.isTablet());

// Manage titles
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);

// Use colored navigation with circle reveal effect
        bottomNavigation.setColored(false);


// Customize notification (title, background, typeface)
        bottomNavigation.setNotificationBackgroundColor(Color.parseColor("#F63D2B"));


// Enable / disable item & set disable color
        //  bottomNavigation.enableItemAtPosition(2);
        // bottomNavigation.disableItemAtPosition(2);
        bottomNavigation.setInactiveColor(Utils.fetchColorFromReference(R.attr.app_textColorSecondary));


        bottomNavigation.setAccentColor(Utils.fetchColorFromReference(R.attr.colorAccent));

// Set listeners
        bottomNavigation.setOnTabSelectedListener(barTabClick);
        bottomNavigation.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
            @Override
            public void onPositionChange(int y) {
                // Manage the new y position
            }
        });

// Set current item programmatically
        bottomNavigation.setCurrentItem(mViewPager.getCurrentItem());

    }


    private final ViewPager.OnPageChangeListener vpPageChangerListener = new ViewPager.OnPageChangeListener() {


        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            bottomNavigation.setCurrentItem(position);
            if (position < 3) Prefs.putInt(c.pager_pos, position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private final AHBottomNavigation.OnTabSelectedListener barTabClick = new AHBottomNavigation.OnTabSelectedListener() {
        @Override
        public boolean onTabSelected(int position, boolean wasSelected) {
            mViewPager.setCurrentItem(position);
            if (position == 3) {
                mVisibilityControl.hidePlayingNowView();
                appBar.setExpanded(false, true);
                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        appBar.setVisibility(View.GONE);
                    }
                };
                new Handler().postDelayed(mRunnable, 350);
            } else {
                appBar.setVisibility(View.VISIBLE);
                mVisibilityControl.showPlayingNowView();

            }
            return true;
        }
    };

    public final RecyclerView.OnScrollListener fragmentsRvScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (recyclerView.canScrollVertically(1)) mVisibilityControl.showPlayingNowView();
            else mVisibilityControl.hidePlayingNowView();

            if (dy > 0) mVisibilityControl.hideBottomBar();
            else if (mViewPager.getCurrentItem() != 3) mVisibilityControl.showBottomBar();

        }


    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 100) {
            //going back fromplaylistsViewActivity
            if (mAdapter != null)
                mAdapter.getItem(3).onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 3) {
            mViewPager.setCurrentItem(2);
        } else super.onBackPressed();
    }

    @Override public boolean onMenuItemClick(MenuItem item) {


        return false;
    }


    private void showSortDialog() {
        final int pos = mViewPager.getCurrentItem();
        if (pos == 0) new SortDialog(this, new SortDialog.Callback() {
            @Override public void selected(SortTypes type) {

                if (pos == 0) {
                    Prefs.putInt(c.sorting_tracks_fragment, type.value);
                } else if (pos == 1) {
                    Prefs.putInt(c.sorting_albuns_fragment, type.value);
                } else if (pos == 2) {
                    Prefs.putInt(c.sorting_artists_fragment, type.value);
                }
                ((MyFragment) mAdapter.getItem(pos)).sort(type);

            }
        }).show();

    }

}
