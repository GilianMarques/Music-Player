package gilianmarques.dev.musicplayer.mediaplayer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import gilianmarques.dev.musicplayer.BuildConfig;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.library.LibraryActivity;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.UIUtils;

/**
 * Criado por Gilian Marques em 20/05/2018 as 13:31:30.
 * <p>
 */
@SuppressLint("NewApi")
public
class MyMediaSession extends MediaSessionCompat {


    private MusicPlayer mPlayer;
    private Context mContext = App.binder.get();
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private AudioManager audioManager;
    private  Binder iBinder;


    private NotificationManager notificationManager;


    public MyMediaSession() {
        super(App.binder.get(), MyMediaSession.class.getName());

        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mContext.registerReceiver(mNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        mPlayer = MusicPlayer.getInstance();

        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        Intent playingNowIntent = new Intent(mContext, LibraryActivity.class).setAction("showPlayingNow");//.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent PlayerUiPintent = PendingIntent.getActivity(mContext, 0, playingNowIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(mContext, BroadcastController.class);
        PendingIntent mediaButtonPIntent = PendingIntent.getBroadcast(mContext, 0, mediaButtonIntent, 0);


        setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionActivity(PlayerUiPintent);
        setMediaButtonReceiver(mediaButtonPIntent);
        setCallback(new MyMediaSession.MediaButtonsCallback());
        setPlaybackState(createState(PlaybackState.STATE_PAUSED));



        setActive(true);
        requestAudioFocus();
        requestAudioFocus();
        initPlayerListener();
        iBinder = new Binder() {
            @Override
            public MyMediaSession getInstance() {
                return MyMediaSession.this;
            }
        };
    }


    private void initPlayerListener() {
        //  if is paused the playPauseChanged method will call updateMetadataAndNotify.
        StateListener mListener = new StateListener(getClass().getSimpleName()) {
            @Override
            public void trackChanged(Track newTrack) {
                // TODO: 02/01/2020 remover     updateMetadataAndNotify();
            }


            @Override
            protected void stateChanged(boolean play) {
                if (play) setPlaybackState(createState(PlaybackStateCompat.STATE_PLAYING));
                else setPlaybackState(createState(PlaybackStateCompat.STATE_PAUSED));

                updateMetadataAndNotify();

                super.stateChanged(play);
            }

            @Override
            public void onPlayerStop() {
                setPlaybackState(createState(PlaybackStateCompat.STATE_STOPPED));
            }
        };

        MusicPlayer.getInstance().registerListener(mListener);

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
        mContext.unregisterReceiver(mNoisyReceiver);
        notificationManager.cancel(notificationId);
        setActive(false);

        super.release();
    }

    private boolean lostFocusMakePlayerPause;
    private boolean lostFocusMakePlayerPauseTemporary;
    private boolean lostFocusMakePlayerLowerVolume;

    private void requestAudioFocus() {
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {

                if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    if (lostFocusMakePlayerPause) {
                        lostFocusMakePlayerPause = false;
                        mPlayer.resume();
                    }
                    if (lostFocusMakePlayerPauseTemporary) {
                        lostFocusMakePlayerPauseTemporary = false;
                        mPlayer.resume();
                    }
                    if (lostFocusMakePlayerLowerVolume) {
                        lostFocusMakePlayerLowerVolume = false;
                        mPlayer.setVolume(100);
                    }

                    // Stop playback, because you lost the Audio Focus.
                    // MovementsCallback.e. the user started some other playback app
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    if (mPlayer.isPlaying()) lostFocusMakePlayerPause = true;
                    mPlayer.pause();

                    /*  um outro app pode estar pedindo para que eu pause a reproduççao
                    temporariamente até ganhar o foco novamente*/
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    if (mPlayer.isPlaying()) {
                        lostFocusMakePlayerPauseTemporary = true;
                        mPlayer.pause();
                    }

                    /*Neste caso devo abaixar o volume até que ganhe foco novamente*/
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    mPlayer.setVolume(15);
                    if (mPlayer.isPlaying()) lostFocusMakePlayerLowerVolume = true;

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
            //      Log.d("MediaButtonsCallback", "onPlay: ");

            if (!mPlayer.isPlaying()) {
                mPlayer.resume();
            }

        }

        @Override
        public void onPause() {
            super.onPause();
//            Log.d("MediaButtonsCallback", "onPause: ");
            if (mPlayer.isPlaying()) mPlayer.pause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            mPlayer.next();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            mPlayer.next();
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


    private final BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPlayer.isPlaying()) mPlayer.toggle();
        }
    };


    /**
     * Adiciona a metadata da track em reprodução na mediasession para ser usada na notificação e chama showNotification
     */
    private void updateMetadataAndNotify() {

        final Track mTrack = mPlayer.getCurrentSong();
        Album album = mTrack.getAlbum();

        Picasso.get().load(album.getURI()).resize(350, 350).into(new Target() {


            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                MediaMetadataCompat metadataCompat = new MediaMetadataCompat.Builder()
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mTrack.getTitle())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mTrack.getAlbumName())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mTrack.getArtistName())
                        .build();
                setMetadata(metadataCompat);
                displayNotification();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                MediaMetadataCompat metadataCompat = new MediaMetadataCompat.Builder()
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.no_art_artist_dark))// TODO: 16/02/2020 por icone certo
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mTrack.getTitle())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mTrack.getAlbumName())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mTrack.getArtistName())
                        .build();
                setMetadata(metadataCompat);
                displayNotification();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });


    }


    public static final int KILL_APP_IF_DISMISS_NOTIFICATION = 12;
    public static final int notificationId = 123;
    private final CharSequence NAME = UIUtils.getString(R.string.titulo_do_canal_denotificacao);
    private final String CHANNEL_ID = BuildConfig.APPLICATION_ID.concat(String.valueOf(NAME));
    public Notification mNotification;

    /**
     * @return uma notificação prontinha
     * <p>
     * Cria a notificação com as ações titulo, capa do album e etc...
     */
    private void buildNotification() {
        // caso esta instancia seja nula, tento iniciala, talvez seja nula pq quenao tentei inicia-la o systema teve que negar
        if (notificationManager == null)
            notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        MediaControllerCompat mediaController = getController();
        MediaMetadataCompat metadata = mediaController.getMetadata();
        MediaDescriptionCompat description = metadata.getDescription();

        int color = ContextCompat.getColor(mContext, R.color.colorPrimary_Dark); // TODO: 16/02/2020 ajustar de acorodo com o tema

        String titulo, sub, desc;

        //  color = ContextCompat.getColor(mContext, R.color.text_secondary_dark);
        titulo = String.valueOf(description.getTitle());
        desc = String.valueOf(description.getSubtitle());
        sub = mContext.getString(R.string.Tocando_agora);


        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(desc)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSubText(sub)
                .setLargeIcon(description.getIconBitmap())
                .setContentIntent(mediaController.getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(getActionIntent(mContext, KILL_APP_IF_DISMISS_NOTIFICATION))
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setSmallIcon(R.drawable.no_art_background)// TODO: 02/01/2020 por icone certo
                .setColor(color)
                .addAction(new NotificationCompat.Action(R.drawable.vec_previous, "previous", getActionIntent(mContext, KeyEvent.KEYCODE_MEDIA_PREVIOUS)))
                .addAction(new NotificationCompat.Action(MusicPlayer.getInstance().isPlaying() ? R.drawable.vec_pause : R.drawable.vec_play, "play/pause",
                        getActionIntent(mContext, MusicPlayer.getInstance().isPlaying() ? KeyEvent.KEYCODE_MEDIA_PAUSE : KeyEvent.KEYCODE_MEDIA_PLAY)))
                .addAction(new NotificationCompat.Action(R.drawable.vec_next, "next", getActionIntent(mContext, KeyEvent.KEYCODE_MEDIA_NEXT)))
                .addAction(new NotificationCompat.Action(R.drawable.vec_stop, "close app", getActionIntent(mContext, KeyEvent.KEYCODE_MEDIA_STOP)))
                .setDefaults(0)
                .setVibrate(new long[]{0L}); // Passing null here silently fails

        // problemas com imports
        // TODO: 16/02/2020 migrar app pro androidX
        android.support.v4.media.app.NotificationCompat.MediaStyle style = new android.support.v4.media.app.NotificationCompat.MediaStyle();
        style.setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(getSessionToken())
                .setShowCancelButton(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, NAME, importance);
            mChannel.enableVibration(false);
            mChannel.enableLights(false);
            mChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(mChannel);
            mNotificationBuilder.setChannelId(CHANNEL_ID);
        }


        mNotificationBuilder.setStyle(style);

        mNotification = mNotificationBuilder.build();
    }


    private void displayNotification() {
        buildNotification();
        notificationManager.notify(notificationId, mNotification);

        if (!PlayerService.serviceIsRunning)
            mContext.startForegroundService(new Intent(mContext, PlayerService.class));
    }


    /**
     * @param mContext Keys
     * @param keyEvent o evento a ser disparado
     * @return uma {@link PendingIntent}
     * <p>
     * Cria uma {@link PendingIntent} com um {@link KeyEvent} para ser colocao nas actions da notificação
     * que serão executadas quando o usuario clicar no botao play da notificação, por exemplo.
     */
    private PendingIntent getActionIntent(Context mContext, int keyEvent) {
        Intent mIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mIntent.setPackage(mContext.getPackageName());
        mIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));
        return PendingIntent.getBroadcast(mContext, keyEvent, mIntent, 0);

    }

    public interface Binder {
        MyMediaSession getInstance();
    }

}
