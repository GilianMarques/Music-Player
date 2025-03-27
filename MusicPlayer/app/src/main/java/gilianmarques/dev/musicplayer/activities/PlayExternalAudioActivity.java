package gilianmarques.dev.musicplayer.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * @Since 0.2 Beta
 */
public class PlayExternalAudioActivity extends MyActivity {
    private MediaPlayer player;
    private Uri pathUri;
    private Runnable mProgressRunnable;
    private boolean playerReleased;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_external_audio);
        if (!Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            finish();
            return;
        }

        pathUri = getIntent().getData();

        if (pathUri == null) {
            Toasty.error(this, getString(R.string.O_caminho_recebido)).show();
            finish();
            return;
        }

        player = MediaPlayer.create(this, pathUri);
        init();
        player.start();
        new Thread(mProgressRunnable).start();
        findViewById(R.id.cv).setOnTouchListener(new LayoutAnimation(findViewById(R.id.cv)));
    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    private Bitmap extractArt() {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(this, pathUri);
            byte[] data = mediaMetadataRetriever.getEmbeddedPicture();
            if (data != null && data.length > 0) {
                Bitmap art = BitmapFactory.decodeByteArray(data, 0, data.length);
                return Bitmap.createScaledBitmap(art, 500, 500, true);
            }
        } catch (Exception ignored) {
        }
        return BitmapFactory.decodeResource(getResources(), R.drawable.no_art_background);
    }

    private void init() {
        final CardView cv = findViewById(R.id.cv);
        final ImageView ivArt = findViewById(R.id.ivArt);
        final ImageView artMini = findViewById(R.id.ivArtMini);
        final SeekBar pBar = findViewById(R.id.pBar);
        final ImageButton btnPlay = findViewById(R.id.btnPlay);
        final TextView tvArtist = findViewById(R.id.tvArtist);
        final TextView tvTitle = findViewById(R.id.tvTitle);
        final TextView tv = findViewById(R.id.tv);

        final int c1 = ContextCompat.getColor(this, R.color.b_bar_1);
        final int c2 = ContextCompat.getColor(this, R.color.b_bar_2);
        final int c3 = ContextCompat.getColor(this, R.color.b_bar_3);
        final MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, pathUri);
        final String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        final String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        final Bitmap art = extractArt();


        SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null)
                    player.seekTo((int) ((player.getDuration() / 100) * progress));

            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        final Runnable rotateButton = new Runnable() {
            @Override
            public void run() {
                int from, to;
                if (player.isPlaying()) {
                    from = 90;
                    to = 0;
                } else {
                    from = 0;
                    to = 180;
                }
                btnPlay.setSelected(!player.isPlaying());

                RotateAnimation rotate = new RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(280);
                rotate.setFillAfter(true);
                rotate.setInterpolator(new FastOutSlowInInterpolator());
                btnPlay.startAnimation(rotate);
            }
        };

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.btnPlay:
                        rotateButton.run();
                        if (player.isPlaying()) player.pause();
                        else player.start();
                        break;

                }
            }
        };


        tvTitle.setText(title);
        tvArtist.setText(artist);

        btnPlay.setOnClickListener(clickListener);
        pBar.setOnSeekBarChangeListener(seekListener);


        ivArt.setImageBitmap(art);
        artMini.setImageBitmap(art);


        Shader textShader = new LinearGradient(0, 0, 100, 20,
                                               new int[]{c1, c2, c3},
                                               new float[]{0, 1, 2}, Shader.TileMode.MIRROR);
        tv.getPaint().setShader(textShader);


        mProgressRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while (player != null && !playerReleased) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                if (player != null && !playerReleased) {
                                    int percent = new BigDecimal(player.getCurrentPosition() + 1 + "").divide(new BigDecimal(player.getDuration() + ""), 2, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).intValue();
                                    pBar.setProgress(percent);
                                    Log.d("PlayExternalAudioAc", "search: " + percent + " " + player.getCurrentPosition() + " " + player.getDuration());
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        btnPlay.setSelected(true);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(MediaPlayer mp) {
                btnPlay.setSelected(false);

            }
        });

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cv.getLayoutParams();
        int base = (int) (Utils.isOrientationPortrait() ? Utils.screenWidth : Utils.screenHeight);
        params.height = (base / 10) * 9;
        params.width = (base / 10) * 7;
        cv.setLayoutParams(params);

        cv.post(new Runnable() {
            @Override public void run() {
                final ValueAnimator anim = ValueAnimator.ofFloat(Utils.screenHeight + MyActivity.navigationHeight, cv.getY());
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override public void onAnimationUpdate(ValueAnimator animation) {
                        cv.setY((Float) animation.getAnimatedValue());
                    }
                });
                anim.setDuration(350);
                anim.setInterpolator(new FastOutSlowInInterpolator());
                anim.start();

            }
        });
    }


    @Override protected void onStop() {
        try {
            if (player != null) {
                if (player.isPlaying()) player.stop();
                player.release();
                playerReleased = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    class LayoutAnimation implements View.OnTouchListener {
        private final long MIN_ANIMATION_TIME_MILLIS = 500;
        private final long MAX_ANIMATION_TIME_MILLIS = 1000;
        private final int PERCENT_TO_MOVE = 120;
        private int pxMovedToLaunch;
        private float initialTouchY;
        private float initialViewY = -1;
        private float viewHeigth;
        private float maxMovimentDown;
        private float maxMovimentUp;
        private boolean viewIsUping;
        private View targetView;
        private TranslateAnimation floatAnim;


        public LayoutAnimation(View viewById) {
            targetView = viewById;
            floatAnim = new TranslateAnimation(0, 0, -(Utils.toPX(1)), (Utils.toPX(1)));
            floatAnim.setRepeatCount(Animation.INFINITE);
            floatAnim.setRepeatMode(Animation.REVERSE);
            floatAnim.setDuration(1000);
            floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            viewById.startAnimation(floatAnim);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.performClick();
                    initialTouchY = event.getY();
                    if (initialViewY == -1) initVars(v);
                    targetView.clearAnimation();
                    break;
                case MotionEvent.ACTION_MOVE:
                    viewIsUping = v.getY() - initialViewY < 0;
                    moveY(event.getY(), v);

                    break;
                case MotionEvent.ACTION_UP:
                    animate(v);
                    break;
            }


            return false;
        }

        private void moveY(float y, View v) {
            float yRunnedByFinger = initialTouchY - y;
            float yToRun = (v.getY() - yRunnedByFinger);

            if (yToRun >= maxMovimentUp && yToRun <= maxMovimentDown) {
                v.setY(yToRun);
            }


        }

        /**
         * @param v Determina se a view foi arrastada na posição correte e pela distância suficiente para ser lançada
         *          e faz as animções necessárias
         */
        private void animate(final View v) {
            float y = v.getY();
            float movimentPercent = getViewMovimentPercent(v);

            long duration = (long) (viewIsUping ? (initialViewY - y) : (y - initialViewY));

            if (duration > MAX_ANIMATION_TIME_MILLIS) duration = MAX_ANIMATION_TIME_MILLIS;
            if (duration < MIN_ANIMATION_TIME_MILLIS) duration = MIN_ANIMATION_TIME_MILLIS;
            if (movimentPercent > 70 && viewIsUping || movimentPercent <= 70 && !viewIsUping)
                duration = ((duration / 10) * 7);

            ValueAnimator moveAnim;

            // view será lançada
            if (y <= pxMovedToLaunch) {
                moveAnim = ValueAnimator.ofFloat(y, -viewHeigth);
                moveAnim.setDuration(duration);
                finish(duration);

            } else {
                moveAnim = ValueAnimator.ofFloat(y, initialViewY);
                /*diminuo em 1/3 a velocidade da animação*/
                duration = (duration / 3) * 2;
                moveAnim.setDuration(duration);

            }


            ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (float) valueAnimator.getAnimatedValue();
                    v.setY(val);
                }
            };


            moveAnim.addUpdateListener(mUpdateListener);


            /*Aplico a tensão corerspondete a distância percorrida pela view.
             * Que a minha professora de física me perdoe!*/
            float tension = movimentPercent / 15;
            tension = tension < 0 ? tension * -1 : tension;
            moveAnim.setInterpolator(new OvershootInterpolator(tension));

            moveAnim.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    targetView.startAnimation(floatAnim);
                    super.onAnimationEnd(animation);
                }
            });
            moveAnim.start();

        }

        private void initVars(View v) {

            initialViewY = v.getY();
            viewHeigth = v.getMeasuredHeight();
            maxMovimentDown = initialViewY + percentValue(v.getMeasuredHeight(), PERCENT_TO_MOVE);
            maxMovimentUp = initialViewY - percentValue(v.getMeasuredHeight(), PERCENT_TO_MOVE);
            pxMovedToLaunch = (int) percentValue((initialViewY - v.getMeasuredHeight()), 20);


        }

        /**
         * @param target t
         * @param value  v
         * @return quantos % value é de target.
         */
        private float percent(float value, float target) {
            return new BigDecimal(value).divide(new BigDecimal(target), 2, RoundingMode.HALF_DOWN).multiply(new BigDecimal(100)).floatValue();
        }

        /**
         * @param v v
         * @return a porcentegem de movimento que a view fez do seu ponto inicial até o ponto maximo de movimento permitido
         */
        private float getViewMovimentPercent(View v) {
            /*ao remover initialViewY de v.gety() descubro o quanto a view andou. Valores >0 indicam que a view subiu
             * ao remover initialViewY de maxMovimentUp descubro quantos px a view tem que andar da sua posição inicial até a posição
             * maxima de animação, assim descubro em quantos % a view andou apartir de seu ponto de origem até o ponto de movimento final
             *
             * A VARIAVEL maxMovimentUp  foi usada como referencia pq reotnou uma porcentagem melhor
             * do que a maxMovimentDown mesmo q na lógica elas tenha um valor bem parecido*/
            return percent(initialViewY - v.getY(), initialViewY - maxMovimentUp);

        }

        /**
         * @param target  t
         * @param percent p
         * @return o qual o valor correspondente a porcentagem recebida
         * <p>
         * EX: target =150 & percentValue = 50 return será 75
         */
        private float percentValue(float target, int percent) {
            return new BigDecimal(percent).multiply(new BigDecimal(target)).divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN).floatValue();
        }

        private void finish(long duration) {
            Runnable mRunnableCallback = new Runnable() {
                @Override
                public void run() {
                    PlayExternalAudioActivity.this.finish();
                }
            };

            new Handler().postDelayed(mRunnableCallback, (duration / 10) * 6);
        }
    }
}
