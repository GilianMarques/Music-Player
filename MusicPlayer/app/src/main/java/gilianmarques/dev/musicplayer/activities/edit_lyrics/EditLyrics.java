package gilianmarques.dev.musicplayer.activities.edit_lyrics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.activities.library.TabsAdapter;
import gilianmarques.dev.musicplayer.lyrics.LyricsUtils;
import gilianmarques.dev.musicplayer.lyrics.models.Lyric;
import gilianmarques.dev.musicplayer.lyrics.models.Phrase;
import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.mediaplayer.structure.MusicPlayer;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;

public class EditLyrics extends MyActivity {
    public int playerControlHeight;
    Lyric lyric;
    long id;
    TabsAdapter tabsAdapter;
    ImageView ivPlay;
    SeekBar seekB;
    MusicPlayer player;
    PlayerProgressListener receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lyrics);
        id = getIntent().getLongExtra("lrc", 0);
        findViewById(R.id.appbar).setElevation(0f);
        LyricsUtils.getLyric(new NativeTracks(false).getTrackById(id), this, new LyricsUtils.Callback() {
            @Override public void done(Lyric l) {
                lyric = l;
                if (lyric == null) {
                    lyric = new Lyric(new ArrayList<Phrase>(), new ArrayList<Phrase>());
                    showAddLyricDialog();
                } else init();
            }
        });
    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);

    }

    private void showAddLyricDialog() {

        View view = getLayoutInflater().inflate(R.layout.view_get_lyric, null);
        final EditText edtLrc = view.findViewById(R.id.edt1);
        final EditText edtTrans = view.findViewById(R.id.edt2);


        final AlertDialog alertDialog = new AlertDialog.Builder(this,darkTheme?R.style.AppThemeDark:R.style.AppThemeLight ).create();
        alertDialog.setView(view);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Concluir), new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                String original = edtLrc.getText().toString();
                String translated = edtTrans.getText().toString();
                if (original.isEmpty()) {
                    finish();
                    return;
                }
                lyric = Lyric.from("url", original, translated);
                alertDialog.dismiss();
                init();

            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.Cancelar), new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        alertDialog.setCancelable(false);
        alertDialog.show();
    }


    private void init() {
        findViewById(R.id.fabDone).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                saveLrc();
            }
        });
        player = MusicService.binder.getPlayer();

        ivPlay = findViewById(R.id.ivPlay);
        seekB = findViewById(R.id.seekB);

        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                player.toogle();
                if (player.isPlaying()) {
                    ivPlay.setImageResource(R.drawable.vec_pause_accent);
                } else {
                    ivPlay.setImageResource(R.drawable.vec_play_accent);

                }
            }
        });
        receiver = new PlayerProgressListener(getClass().getSimpleName()) {
            @Override protected void playPauseChanged(boolean play) {
                if (play) {
                    ivPlay.setImageResource(R.drawable.vec_pause_accent);
                } else {
                    ivPlay.setImageResource(R.drawable.vec_play_accent);

                }
                super.playPauseChanged(play);
            }

            @Override
            protected void progressChanged(float percent, String timer, long millis) {
                super.progressChanged(percent, timer, millis);
                seekB.setProgress((int) percent);
            }
        };
        seekB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) player.seekToPosition((float) progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        final ViewPager viewPager = findViewById(R.id.viewPager);
        tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(2);
        receiver.startReceiving();

        final View pControl = findViewById(R.id.pControl);

        pControl.post(new Runnable() {
            @Override public void run() {
                playerControlHeight = pControl.getMeasuredHeight();
                tabsAdapter.add(EditLyricFragment.newInstance(lyric.getOriginal()), "Original");
                if (lyric.hasTranslation())
                    tabsAdapter.add(EditLyricFragment.newInstance(lyric.getTranslation()), "Tradução");
                viewPager.setAdapter(tabsAdapter);
            }
        });

    }

    @Override protected void onStop() {
        if (receiver != null) receiver.stopReceiving();
        super.onStop();
    }

    @Override protected void onResume() {
        if (receiver != null) receiver.startReceiving();
        super.onResume();
    }

    private void saveLrc() {
        ArrayList<Phrase> translation = null;
        ArrayList<Phrase> original = ((EditLyricFragment) tabsAdapter.getItem(0)).finish();
        if (tabsAdapter.getCount() == 2)
            translation = ((EditLyricFragment) tabsAdapter.getItem(1)).finish();

        lyric.setOriginal(original);
        if (translation != null) lyric.setTranslation(translation);

        if (new LyricsUtils().writeLyric(id, lyric)) {
            Toasty.success(this, "Letra atualizada com sucesso!", Toasty.LENGTH_LONG).show();
            if (lyric.isSynced()) {
                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Toasty.info(EditLyrics.this, "Pode ser necessário re-sincronizar a letra", Toasty.LENGTH_LONG).show();
                    }

                };
                finish(1);
                new Handler().postDelayed(mRunnable, 2000);
            } else finish(2000);
        } else {
            Toasty.error(this, "Erro atualizando letra", Toasty.LENGTH_LONG).show();
            finish(2000);
        }


    }

    private void finish(int delay) {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
        new Handler().postDelayed(mRunnable, delay);
    }
}
