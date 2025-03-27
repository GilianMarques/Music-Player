package gilianmarques.dev.musicplayer.mediaplayer.service_and_related;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.mediaplayer.structure.MusicPlayer;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.activities.playing_now.PlayingNowActivity;
import gilianmarques.dev.musicplayer.utils.BitmapUtils;

/**
 * Criado por Gilian Marques em 20/05/2018 as 13:31:30.
 * <p>
 */
@SuppressLint("NewApi")
class MyMediaSession extends MediaSessionCompat {


    private boolean MEDIA_SESSION_EXISTS;
    private boolean lostFocusMakePlayerPause, lostFocusMakePlayerLowerVolume;


    private MusicService mService;
    private MusicPlayer mPlayer;
    private Context mContext;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private AudioManager audioManager;


    /**
     * @param mService o serviço ( {@link MusicService}) que vai chamar esse contrutor
     *                 <p>
     *                 Deve ser chamado apenas pelo service para inicializar
     * @param mPlayer  p
     */
    MyMediaSession(MusicService mService, MusicPlayer mPlayer) {

        super(mService, "MyMediaSession", new ComponentName(mService, MyReceiver.class), null);

        if (MEDIA_SESSION_EXISTS) {
            throw new RuntimeException("Apenas uma instancia é permitida!");
        } else {
            MEDIA_SESSION_EXISTS = true;
            Log.d("MyMediaSession", "MyMediaSession: nova sesao aberta");
        }

        this.mService = mService;
        this.mContext = mService;
        this.mPlayer = mPlayer;

        audioManager = (AudioManager) mService.getSystemService(Context.AUDIO_SERVICE);

        Intent playingNowIntent = new Intent(this.mService, PlayingNowActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent PlayerUiPintent = PendingIntent.getActivity(this.mService, 0, playingNowIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(mService, MyReceiver.class);
        PendingIntent mediaButtonPIntent = PendingIntent.getBroadcast(mService, 0, mediaButtonIntent, 0);


        setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionActivity(PlayerUiPintent);
        setMediaButtonReceiver(mediaButtonPIntent);
        setCallback(new MyMediaSession.MediaButtonsCallback());
        setPlaybackState(createState(PlaybackState.STATE_PAUSED));

        requestAudioFocus();

        mListener.startReceiving();

        mContext.registerReceiver(mNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        setActive(true);

    }


    private PlaybackStateCompat createState(int state) {
        return new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
                                    | PlaybackStateCompat.ACTION_PLAY
                                    | PlaybackStateCompat.ACTION_PAUSE
                                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                    | PlaybackStateCompat.ACTION_STOP)
                .setState(state, 0, 1, SystemClock.elapsedRealtime())
                .build();
    }

    @Override
    public void release() {
        audioManager.abandonAudioFocus(audioFocusChangeListener);
        audioFocusChangeListener = null;
        mContext.unregisterReceiver(mNoisyReceiver);
        mPlayer = null;
        mService = null;
        audioManager = null;
        mContext = null;
        super.release();
    }


    private void requestAudioFocus() {
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {

                    case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                        setActive(true);
                        if (lostFocusMakePlayerPause) {
                            lostFocusMakePlayerPause = false;
                            mPlayer.changeFocus(true);
                        }
                        if (lostFocusMakePlayerLowerVolume) {
                            lostFocusMakePlayerLowerVolume = false;
                        }

                        mPlayer.setVolume(100);
                        break;

                    // Stop playback, because you lost the Audio Focus.
                    // MovementsCallback.e. the user started some other playback app
                    case AudioManager.AUDIOFOCUS_LOSS:
                        if (mPlayer.isPlaying()) lostFocusMakePlayerPause = true;
                        mPlayer.changeFocus(false);
                        break;


           /*Nesse caso (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)  um outro app pode estar pedindo para que eu pause a reproduççao temporariamente então pauso e qd voltar a ter foco o
            audioFocusChangeListener vai mudar o foco no plçayer dando play */
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        if (mPlayer.isPlaying()) lostFocusMakePlayerPause = true;
                        mPlayer.changeFocus(false);
                        break;


                    /*Neste caso devo abaixar o volume até que ganhe foco novamente*/
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        mPlayer.setVolume(3);
                        if (mPlayer.isPlaying()) lostFocusMakePlayerLowerVolume = true;
                        break;

                    default:
                        if (mPlayer.isPlaying()) lostFocusMakePlayerPause = true;
                        mPlayer.changeFocus(false);
                        break;
                }


            }

        };


        audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

    }


    /**
     * Este é o CallbackI do controlador de musica do MediaSession os metodos dentro dessa classe são chamados
     * apartir das notificações, android wear, assistant, UI deste app e etc.
     * <p>
     * os metodos dessa classe fazem interação direta com o player, delegando a ação necessaria a ele e atualizando o
     * estado do midiaSession. em dois casos outras atualizações do estado do MediaSession sao feitos no CallbackI do MusicPlayer
     * nessa classe
     */
    private class MediaButtonsCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            super.onPlay();
            Log.d("MediaButtonsCallback", "onPlay: ");

            if (!mPlayer.isPlaying()) {
                mPlayer.toogle();
            }

        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d("MediaButtonsCallback", "onPause: ");
            if (mPlayer.isPlaying()) mPlayer.toogle();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            /* n tem lugar no player pra esse estado. mas nem faz mta diferença pq trocar de musica leva alguns mls*/
            setPlaybackState(createState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT));
            mPlayer.nextTrack(true);
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            /* n tem lugar no player pra esse estado. mas nem faz mta diferença pq trocar de musica leva alguns mls*/
            setPlaybackState(createState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS));
            mPlayer.previousTrack();
        }

        @Override
        public void onStop() {
            super.onStop();
            mPlayer.stop();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
        }
    }

    /**
     * Usada para receber os eventos do player e atualizar a sessao de midia.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final PlayerProgressListener mListener = new PlayerProgressListener(getClass().getSimpleName()) {
        @Override
        public void trackChanged(Track newTrack) {
                updateMetadata(); //  if is paused the playPauseChanged method will call updateMetadata.
        }

        @Override
        public void playPauseChanged(boolean play) {
            if (mPlayer.isPlaying()) {
                setPlaybackState(createState(PlaybackStateCompat.STATE_PLAYING));
                updateMetadata();
            } else {
                setPlaybackState(createState(PlaybackStateCompat.STATE_PAUSED));
            }
        }

        @Override
        public void onPlayerStop() {
            setPlaybackState(createState(PlaybackStateCompat.STATE_STOPPED));
        }
    };


    private final BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPlayer.isPlaying()) mPlayer.toogle();
        }
    };


    /**
     * Adiciona a metadata da track em reprodução na mediasession para ser usada na notificação e chama showNotification
     */
    private void updateMetadata() {

        Track mTrack = mPlayer.getCurrentTrack();

        Bitmap art = BitmapUtils.loadArtWithIdOnUIThread(mTrack.getAlbum().getURI());
        if (art == null) art = BitmapUtils.defArt;

        MediaMetadataCompat metadataCompat = new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, art)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mTrack.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mTrack.getAlbumName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mTrack.getArtistName())
                .build();
        setMetadata(metadataCompat);
    }


}
