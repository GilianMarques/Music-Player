package gilianmarques.dev.musicplayer.activities.playing_now;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.oze.music.musicbar.FixedMusicBar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.activities.playing_now.lyrics.LyricsController;
import gilianmarques.dev.musicplayer.customs.MyFragment;
import gilianmarques.dev.musicplayer.customs.PlayPauseView;
import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.mediaplayer.structure.MusicPlayer;
import gilianmarques.dev.musicplayer.mediaplayer.structure.RepeatMode;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.PaletteUtils;
import gilianmarques.dev.musicplayer.utils.Utils;
import me.grantland.widget.AutofitHelper;

public class PlayingNowFragment extends MyFragment implements View.OnClickListener {
    private PlayPauseView mPlayButton;
    private FixedMusicBar musicBar;
    private SeekBar mSeekBar;
    private ImageView trackArt;
    private TextView tvTrackArtist, tvTrackTitle, tvTrackTimer, trackDurr;
    private ImageButton ibRepeat, ibShuffle, ibNext, ibPrevious;
    private MusicPlayer musicPlayer;
    private MyActivity mActivity;
    private boolean showLyrics;
    private View gradient1, gradient2;
    private ConstraintLayout lrcParent;
    // Lyrics
    private LyricsController lyricsController;
    // End lyrics

    @Override
    protected void init() {

        mActivity = (MyActivity) App.binder.get().getActivity();
        musicPlayer = MusicService.binder.getPlayer();

        mPlayButton = findViewById(R.id.btn_play_pause);
        gradient1 = findViewById(R.id.gradient1);
        gradient2 = findViewById(R.id.gradient2);

        musicBar = findViewById(R.id.pBar);
        mSeekBar = findViewById(R.id.seekBar);
        trackDurr = findViewById(R.id.tv_track_durr);
        trackArt = findViewById(R.id.iv_art);
        tvTrackArtist = findViewById(R.id.tv_track_artist);
        tvTrackTitle = findViewById(R.id.tv_track_title);
        tvTrackTimer = findViewById(R.id.tv_track_timer);
        ibNext = findViewById(R.id.btn_next);
        ibPrevious = findViewById(R.id.btn_previous);
        ibRepeat = findViewById(R.id.btn_repeat);
        ibShuffle = findViewById(R.id.btn_shuffle);
        lrcParent = findViewById(R.id.lrcParent);
        AlphaAnimation a = new AlphaAnimation(1,0);
        a.setFillAfter(true);
        a.setDuration(0);
        lrcParent.startAnimation(a);
        final Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mActivity.setSupportActionBar(mToolbar);
        ActionBar mActionBar = mActivity.getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }


        mPlayButton.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        ibRepeat.setOnClickListener(this);
        ibShuffle.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    musicPlayer.seekToPosition(progress + 0.00001f);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        trackArt.post(new Runnable() {
            @Override public void run() {
                CardView parent = ((CardView) trackArt.getParent());

                ConstraintLayout.LayoutParams artParams = (ConstraintLayout.LayoutParams) ((CardView) trackArt.getParent()).getLayoutParams();
                artParams.height = trackArt.getMeasuredWidth();

                artParams.height = Math.min(trackArt.getMeasuredHeight(), trackArt.getMeasuredWidth());
                //noinspection SuspiciousNameCombination
                artParams.width = artParams.height; //to make sure it will be square
                parent.setLayoutParams(artParams);
                parent.setY(mToolbar.getY() + mToolbar.getMeasuredHeight());

            }
        });
        Utils.applyPadding(findViewById(R.id.marginParent), true, false);

        // here is applyed the bottom margin if needed
        if (Utils.hasNavBar) {
            ConstraintLayout.LayoutParams playButtonsParams = (ConstraintLayout.LayoutParams) mPlayButton.getLayoutParams();
            playButtonsParams.bottomMargin = playButtonsParams.bottomMargin + MyActivity.navigationHeight;
            mPlayButton.setLayoutParams(playButtonsParams);
        }


        TextView tvLrc = findViewById(R.id.tvLrc);
        AutofitHelper.create(tvLrc);


        lyricsController = new LyricsController(tvLrc, this, mActivity);
        if (musicPlayer != null) {
            mListener.startReceiving();
            repeat(true);
            shuffle(true);
        }

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_play_pause:
                musicPlayer.toogle();
                break;
            case R.id.btn_next:
                musicPlayer.nextTrack(true);
                break;
            case R.id.btn_previous:
                musicPlayer.previousTrack();
                break;
            case R.id.btn_repeat:
                repeat(false);
                break;
            case R.id.btn_shuffle:
                shuffle(false);
                break;
        }

    }

    private void repeat(boolean asGetter) {
        RepeatMode state = musicPlayer.repeat(asGetter);
        switch (state) {
            case repeat_disabled:
                //repeat off
                ibRepeat.setActivated(false);
                ibRepeat.setSelected(false);
                break;
            case repeat_one:
                //repeat 1
                ibRepeat.setSelected(true);
                break;
            case repeat_all:
                //repeat all
                ibRepeat.setActivated(true);
                break;
        }

    }

    private void shuffle(boolean asGetter) {

        if (!asGetter) musicPlayer.toogleShuffle(!musicPlayer.isShuffling());

        boolean shuffle = musicPlayer.isShuffling();
        if (shuffle) ibShuffle.setActivated(true);
        else ibShuffle.setActivated(false);
    }


    private final PlayerProgressListener mListener = new PlayerProgressListener(getClass().getSimpleName()) {

        @Override
        public void trackChanged(final Track newTrack) {
            musicBar.loadFrom(newTrack.getFilePath(), (int) newTrack.getDurationMillis());
            trackDurr.setText(Utils.millisToFormattedString(newTrack.getDurationMillis()));
            //    musicBar.setProgress(0);
            Log.d(App.myFuckingUniqueTAG + "PlayingNowFragment", "trackChanged: ");
            tvTrackTitle.setText(newTrack.getTitle());
            tvTrackArtist.setText(newTrack.getArtistName() + " - " + newTrack.getAlbumName());
            final Album mAlbum = newTrack.getAlbum();
            Picasso.get().load(mAlbum.getURI()).noPlaceholder()
                    .error(R.drawable.no_art_background)
                    .into(trackArt, new Callback() {
                        @Override public void onSuccess() {
                            apllyGradient(Utils.getBitmap(trackArt));
                        }

                        @Override public void onError(Exception e) {
                            apllyGradient(Utils.getBitmap(trackArt));
                        }
                    });


        }


        @Override
        public void progressChanged(final float percent, final String timer, final long millis) {
            tvTrackTimer.setText(timer);
            musicBar.setProgress((int) millis);
            mSeekBar.setProgress((int) percent);
        }


        @Override
        public void playPauseChanged(final boolean play) {
            mPlayButton.change(!play, true);

        }


    };

    private void apllyGradient(Bitmap bitmap) {

        PaletteUtils.gradientColorFrom(bitmap, new PaletteUtils.GradientCallback() {
            @Override public void result(int c1, int c2) {

                int[] colors = new int[]{c1, c2, c1};

                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BL_TR, colors);

                gradient1.setBackground(gradient2.getBackground());
                gradient2.setBackground(gd);

                AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setDuration(500);
                fadeOut.setFillAfter(true);
                gradient1.startAnimation(fadeOut);


            }
        });


        //gradient1


    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playing_now, container, false);
    }


    @Override public void onDetach() {
        mListener.stopReceiving();
        super.onDetach();
    }

    public void toogleLrcs(boolean animateHide) {
        showLyrics = !showLyrics;
        lrcParent.setKeepScreenOn(showLyrics);
        if (showLyrics) {
            lyricsController.start();

            AlphaAnimation a = new AlphaAnimation(0, 1);
            a.setDuration(300);
            a.setInterpolator(new FastOutSlowInInterpolator());
            a.setFillAfter(true);
            lrcParent.startAnimation(a);

            lyricsController.ivFullscreenLrc.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    ((PlayingNowActivity) mActivity).showFullLyrics();
                }
            });


        } else {

            lyricsController.stop();
            AlphaAnimation a = new AlphaAnimation(1, 0);
            a.setDuration(300);
            a.setInterpolator(new FastOutSlowInInterpolator());
            a.setFillAfter(true);
            lrcParent.startAnimation(a);
            lyricsController.ivFullscreenLrc.setOnClickListener(null);
        }
    }

    public boolean isShowLyrics() {
        return showLyrics;
    }

    public void showPlaylist() {
        startActivity(new Intent(mActivity, PlayingNowQueue.class));
    }
}
