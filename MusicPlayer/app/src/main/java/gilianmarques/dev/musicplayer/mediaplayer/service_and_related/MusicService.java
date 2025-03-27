package gilianmarques.dev.musicplayer.mediaplayer.service_and_related;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;
import android.view.KeyEvent;

import gilianmarques.dev.musicplayer.BuildConfig;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.mediaplayer.structure.MusicPlayer;
import gilianmarques.dev.musicplayer.mediaplayer.structure.PlayerStateService;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.timer.TimerService;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;


/**
 * Criado por Gilian Marques em 13/05/2018 as 17:01:05.
 */

@SuppressLint("NewApi")
public class MusicService extends Service {

    private MusicPlayer mPlayer;
    private MyMediaSession mediaSession;
    private final Context mContext = App.binder.get().getApplicationContext();
    private boolean onForeground;
    public static BindService binder;

    private final CharSequence NAME = Utils.getString(R.string.titulo_do_canal_denotificacao);
    private final String CHANNEL_ID = BuildConfig.APPLICATION_ID.concat(String.valueOf(NAME));

    private NotificationManager notificationManager;
    public static final int KILL_APP_IF_DISMISS_NOTIFICATION = 965832174;
    private static final int notificationId = 1246197259;
    private final boolean API26orAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    private boolean API24orAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(App.myFuckingUniqueTAG + "MusicService", "onStartCommand: ");
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(App.myFuckingUniqueTAG + "MusicService", "onCreate: ");
        Utils.putOnForeground(this, notificationId);
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        binder = mBindService;
        mPlayer = new MusicPlayer(this);
        mediaSession = new MyMediaSession(MusicService.this, mPlayer);
        mPlayer.restoreState();
        mListener.startReceiving();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        App.binder.get().finishActivities();
        if (onForeground) toBackground(true);
        mediaSession.release();
        Utils.startService(this, PlayerStateService.class);//save MusicPlayer state and kill it
        if (TimerService.binder != null) TimerService.binder.get().stopSelf();
        Log.d("MusicService", "onDestroy: destruindo serviço!!");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private final BindService mBindService = new BindService() {
        @Override
        public MusicService getService() {
            return MusicService.this;
        }

        @Override
        public MusicPlayer getPlayer() {
            return mPlayer;
        }
    };

    public void handleMediButton(Intent mIntent) {
        MediaButtonReceiver.handleIntent(mediaSession, mIntent);
    }


    public interface BindService {
        MusicService getService();

        MusicPlayer getPlayer();
    }

    // RELACIONADOS A NOTIFICAÇÂO

    /**
     * Usada para atualizar a notificação
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final PlayerProgressListener mListener = new PlayerProgressListener(getClass().getSimpleName()) {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                showNotification();
            }
        };

        @Override
        public void trackChanged(Track newTrack) {
            new Handler().postDelayed(mRunnable, 150);

        }

        @Override
        public void playPauseChanged(boolean play) {

            new Handler().postDelayed(mRunnable, 150);

        }

        @Override
        public void onPlayerStop() {
            new Handler().postDelayed(mRunnable, 150);

        }
    };

    /**
     * @return uma notificação prontinha
     * <p>
     * Cria a notificação com as ações titulo, capa do album e etc...
     */
    private NotificationCompat.Builder buildNotification() {
        // caso esta instancia seja nula, tento iniciala, talvez seja nula pq quenao tentei inicia-la o systema teve que negar
        if (notificationManager == null)
            notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        MediaControllerCompat mediaController = mediaSession.getController();
        MediaMetadataCompat metadata = mediaController.getMetadata();
        MediaDescriptionCompat description = metadata.getDescription();

        int color = ContextCompat.getColor(mContext, R.color.colorPrimary_Dark);

        String titulo, sub, desc;

        // as coisas mudam de ordem no lollipop
        if (API24orAbove) {
            color = ContextCompat.getColor(mContext, R.color.text_secondary_dark);
            titulo = String.valueOf(description.getTitle());
            desc = String.valueOf(description.getSubtitle());
            sub = mContext.getString(R.string.Tocando_agora);
        } else {
            desc = String.valueOf(description.getDescription());
            sub = String.valueOf(description.getSubtitle());
            titulo = String.valueOf(description.getTitle());
        }

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(desc)
                .setOnlyAlertOnce(true)
                .setSubText(sub)
                .setLargeIcon(description.getIconBitmap())
                .setContentIntent(mediaController.getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(getActionIntent(mContext, KILL_APP_IF_DISMISS_NOTIFICATION))
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(color)
                .addAction(new NotificationCompat.Action(R.drawable.vec_previous, "previous", getActionIntent(mContext, KeyEvent.KEYCODE_MEDIA_PREVIOUS)))
                .addAction(new NotificationCompat.Action(mPlayer.isPlaying() ? R.drawable.vec_pause : R.drawable.vec_play, "play/pause", getActionIntent(mContext, mPlayer.isPlaying() ? KeyEvent.KEYCODE_MEDIA_PAUSE : KeyEvent.KEYCODE_MEDIA_PLAY)))
                .addAction(new NotificationCompat.Action(R.drawable.vec_next, "next", getActionIntent(mContext, KeyEvent.KEYCODE_MEDIA_NEXT)))
                .addAction(new NotificationCompat.Action(R.drawable.vec_stop, "close app", getActionIntent(mContext, KeyEvent.KEYCODE_MEDIA_STOP)))
                .setDefaults(0)
                .setVibrate(new long[]{0L}); // Passing null here silently fails

        // problemas com imports
        android.support.v4.media.app.NotificationCompat.MediaStyle style = new android.support.v4.media.app.NotificationCompat.MediaStyle();
        style.setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSession.getSessionToken())
                .setShowCancelButton(true);


        if (API26orAbove) {

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, NAME, importance);
            mChannel.enableVibration(false);
            mChannel.enableLights(false);
            mChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(mChannel);
            nBuilder.setChannelId(CHANNEL_ID);
        }


        nBuilder.setStyle(style);
        return nBuilder;
    }

    private void showNotification() {

        Notification mNotification = buildNotification().build();

        if (mPlayer.isPlaying()) {
            if (onForeground)
                notificationManager.notify(notificationId, mNotification);
            else {
                startForeground(notificationId, mNotification);
                onForeground = true;
            }

        } else {
            if (onForeground) {
                // TODO: 27/04/2019 check if service stops dying while is on background (paused) toBackground(false);
            }
            notificationManager.notify(notificationId, mNotification);
        }
    }

    private void toBackground(boolean b) {
        stopForeground(b);
        onForeground = false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(App.myFuckingUniqueTAG + "MusicService", "onTaskRemoved: REMOVEEEEEEEEEEED");
        super.onTaskRemoved(rootIntent);
    }

    /**
     * @param mContext c
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
}
