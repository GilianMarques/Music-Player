package gilianmarques.dev.musicplayer.activities.playing_now.lyrics;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Objects;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.activities.edit_lyrics.EditLyrics;
import gilianmarques.dev.musicplayer.activities.playing_now.PlayingNowFragment;
import gilianmarques.dev.musicplayer.lyrics.LyricsUtils;
import gilianmarques.dev.musicplayer.lyrics.models.Lyric;
import gilianmarques.dev.musicplayer.lyrics.models.Phrase;
import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques
 * Quarta-feira, 01 de Maio de 2019  as 17:36:08.
 */
public class LyricsController {
    private TextView target;
    private PlayingNowFragment mNowFragment;
    private MyActivity mActivity;
    private Lyric currLyric;
    private boolean canShowLyric;
    private boolean translation;
    public ImageView ivFullscreenLrc;
    private ImageView ivTranslate;
    private ImageView ivEdit;
    private Track currTrack;

    public LyricsController(TextView target, final PlayingNowFragment mNowFragment, MyActivity mActivity) {
        this.target = target;
        this.mNowFragment = mNowFragment;
        this.mActivity = mActivity;
        translation = Prefs.getBoolean(c.show_translation, false);
        ivFullscreenLrc = mNowFragment.findViewById(R.id.ivOpenActivity);
        ivTranslate = mNowFragment.findViewById(R.id.ivTranslate);
        ivEdit = mNowFragment.findViewById(R.id.ivEdit);

        ivTranslate.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                toogleTranslation();
            }
        });
        ivEdit.setOnClickListener(editLrcClickListener);
    }

    View.OnClickListener editLrcClickListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            // TODO: 05/05/2019 change way as id is send to activity if begin to save name of track as lrc name
            mNowFragment.startActivity(new Intent(App.binder.get(), EditLyrics.class).putExtra("lrc", currTrack.getId()));
        }
    };
    View.OnClickListener addLrcClickListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            String url = currTrack.getArtistName() + " " + currTrack.getTitle() + " letra e tradução";
            url = "https://www.google.com/search?q=" + (url.replaceAll("[^a-zA-Z0-9 ]", ""));

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse((url)));
            Log.d(App.myFuckingUniqueTAG + "LyricsController", "onClick: " + Uri.parse((url)).toString());

            mNowFragment.startActivity(new Intent(App.binder.get(), EditLyrics.class).putExtra("lrc", currTrack.getId()));
            mNowFragment.startActivity(i);

        }
    };

    public void start() {
        target.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.toSp(16));
        target.setText(R.string.Letra_indisponivel_para);

        mProgressListener.startReceiving();
    }

    public void stop() {
        mProgressListener.stopReceiving();
    }

    private PlayerProgressListener mProgressListener = new PlayerProgressListener(getClass().getSimpleName()) {
        @Override protected void trackChanged(Track newTrack) {
            currTrack = newTrack;
            LyricsUtils.getLyric(currTrack, mActivity, new LyricsUtils.Callback() {
                @Override public void done(Lyric lyric) {
                    currLyric = lyric;
                    updateUI();
                }
            });
        }

        @Override
        protected void progressChanged(float percent, String timer, long millis) {
            if (canShowLyric) {

                Phrase phrase = currLyric.getPhraseByPeriod(millis, translation);
                if (translation) {
                    Phrase phrase2 = currLyric.getPhraseByPeriod(millis, false);
                    if (!Objects.equals(phrase2.getText(), phrase.getText()))
                        phrase = new Phrase(phrase2.getText() + "\n------\n" + phrase.getText(), phrase.getStart(), phrase.getEnd());
                }

                if (!phrase.getText().equals(target.getText().toString())) {
                    YoYo.with(Techniques.FadeIn).duration(130).playOn(target);
                    target.setText(phrase.getText());
                }
            }
        }

        @Override protected void playPauseChanged(boolean play) {
        }
    };


    private void updateUI() {
        target.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.toSp(23));

        if (currLyric == null) {
            canShowLyric = false;
            target.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.toSp(16));
            if (translation) target.setText(R.string.Traducao_indisponivel_para);
            else target.setText(R.string.Letra_indisponivel_para);
            ivEdit.setOnClickListener(addLrcClickListener);
            ivEdit.setImageResource(R.drawable.vec_plus_white);
        } else {
            ivEdit.setImageResource(R.drawable.vec_edit_white);
            ivEdit.setOnClickListener(editLrcClickListener);

            //used to remove older msgs from screen if player is paused and user toogle between original and translated mode
            target.setText("");

            if (currLyric.isSynced()) {
                canShowLyric = true;
            } else {
                canShowLyric = false;

                if (translation) target.setText(R.string.Esta_traducao_nao_esta_sincronizada);
                else target.setText(R.string.Esta_letra_nao_esta_sincronizada_clique_no_icone);

            }

        }


    }

    public void toogleTranslation() {
        translation = !translation;
        Prefs.putBoolean(c.show_translation, translation);
        updateUI();

    }
}
