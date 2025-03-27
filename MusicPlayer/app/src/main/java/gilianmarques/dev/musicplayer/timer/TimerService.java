package gilianmarques.dev.musicplayer.timer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.BuildConfig;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Usada para salvar o estado do player e fecha-lo em seguida
 */
public class TimerService extends Service {

    private NotificationManager notificationManager;
    private final String NAME = Utils.getString(R.string.Notificacao_do_servico_de_auto_desligamento);
    private final String CHANNEL_ID = BuildConfig.APPLICATION_ID.concat(NAME);
    private final String CANCEL_ACTION = BuildConfig.APPLICATION_ID.concat(NAME).concat("cancel");
    private int minutesOrTracksUntilShutdown, timer;
    private boolean timerBasedOnMinutes, serviceStopped;
    public static Binder binder;

    public TimerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timerBasedOnMinutes = intent.getBooleanExtra("based_on_minutes", true);
        timer = intent.getIntExtra("timer", 100);
        minutesOrTracksUntilShutdown = timer;

        if (timerBasedOnMinutes) {
            addNotification();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override public void run() {
                    if (serviceStopped || !timerBasedOnMinutes) cancel();
                    else {
                        minutesOrTracksUntilShutdown--;
                        addNotification();
                    }
                }
            }, 0, 60000);
        } else {
            addNotification();
            mPlayerProgressListener.startReceiving();
        }

        binder = new Binder() {
            @Override public TimerService get() {
                return TimerService.this;
            }
        };

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        super.onCreate();
    }

    public void cancel() {
        stopForeground(true);
        stopSelf();
    }

    @Override public void onDestroy() {
        serviceStopped = true;
        binder = null;
        super.onDestroy();
    }

    private void addNotification() {

        Intent cancelIntent = new Intent(this, NotificationActionService.class);
        cancelIntent.setAction(CANCEL_ACTION);
        PendingIntent cancelPendingIntent = PendingIntent.getService(this, 815614190, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        String msg;
        if (timerBasedOnMinutes) {
            msg = Utils.format(Utils.toPlural(minutesOrTracksUntilShutdown, R.plurals.Minutos_ate_desligar), minutesOrTracksUntilShutdown);
        } else
            msg = Utils.format(Utils.toPlural(minutesOrTracksUntilShutdown, R.plurals.Faixas_ate_desligar), minutesOrTracksUntilShutdown);


//Set notification information:
        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder.setOngoing(true)
                .setContentTitle(getString(R.string.Auto_desligamento_iniciado))
                .setContentText(msg)
                .setProgress(100, getProgress(), false)
                .setDefaults(0)
                .setSmallIcon(R.drawable.notification_icon)
                .setVibrate(new long[]{0L}); // Passing null here silently fails


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager == null) return;

            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, NAME, importance);
            mChannel.enableVibration(false);
            mChannel.enableLights(false);
            mChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(mChannel);
            notificationBuilder.setChannelId(CHANNEL_ID);
        }

        //HERE ARE YOUR BUTTONS
        notificationBuilder.addAction(R.drawable.vec_cancel, getString(R.string.Cancelar), cancelPendingIntent);
        // Apply the media style template
        //       .setStyle(new Notification.MediaStyle().setShowActionsInCompactView(0));


        Notification notification = notificationBuilder.build();
        int notificationId = 46153;
        startForeground(notificationId, notification);
        if (timerBasedOnMinutes) {
            if (minutesOrTracksUntilShutdown < 0) shutdown();
        } else if (minutesOrTracksUntilShutdown == 0) shutdown();

    }

    private void shutdown() {
        Log.d("TimerService", "shutdown: Shuting down");
        MusicService.BindService binder = MusicService.binder;
        if (binder != null) binder.getService().stopSelf();
        stopSelf();
    }


    public int getProgress() {
        return new BigDecimal(minutesOrTracksUntilShutdown).divide(new BigDecimal(timer), 2, RoundingMode.HALF_EVEN).multiply(new BigDecimal(100)).intValue();

    }

    PlayerProgressListener mPlayerProgressListener = new PlayerProgressListener(getClass().getSimpleName()) {
        @Override protected void trackEnded() {
            if (serviceStopped || timerBasedOnMinutes) stopReceiving();
            else {
                minutesOrTracksUntilShutdown--;
                addNotification();
            }
            super.trackEnded();
        }
    };

    public interface Binder {
        TimerService get();
    }


    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            TimerService.binder.get().cancel();
            TimerService.binder = null;
            Toasty.success(App.binder.get(), this.getString(R.string.Timer_cancelado)).show();
        }
    }
}
