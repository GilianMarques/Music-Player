package gilianmarques.dev.musicplayer.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.BuildConfig;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.c;

/**
 * Essa classe tem como objetivo facilitar o acesso a classe {@link App} que é uma instancia de {@link android.app.Application}
 * onde posso obter uma instancia de {@link android.content.Context} para nao precisar ficar passando ela atraves de metodos por _todo o codigo
 * <p>
 * Criado por Gilian Marques em 01/05/2018 as 17:19:38.
 * <p>
 * <p>
 * OBS: NOMES DE METODOS CURTOS PARA DIMINUIR O TAMANHO DA CHAMADA
 */
public class Utils {
    public static final int TABLET_7_INCH = 7;
    public static final int TABLET_10_INCH = 10;
    public static final int PHONE = 6;


    public static float screenWidth, screenHeight;
    public static boolean hasNavBar;

    public static void hideKeyboardFrom(View view) {
        InputMethodManager imm = (InputMethodManager) App.binder.get().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * @return if device's orientation is oprtrait or not. it ruturns false if device is on landScape or reverseLandScape
     */
    public static boolean isOrientationPortrait() {
        return App.binder.get().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }


    public static boolean isTablet() {
        return App.binder.get().getResources().getBoolean(R.bool.isTablet);
    }

    @SuppressWarnings("unused") public static int deviceType() {
        return App.binder.get().getResources().getInteger(R.integer.device_type);
    }

    /**
     * @param dpToConvertIntoPixels os DPs a converter em PX
     * @return o valor de DP em PX
     * <p>
     * passe um valor em DP para que ele seja convertido e pixels
     */
    public static float toPX(float dpToConvertIntoPixels) {
        return App.binder.get().getResources().getDimension(R.dimen.dp) * dpToConvertIntoPixels;

    }

    public static float toSp(int unitToConvertIntoSp) {
        return App.binder.get().getResources().getDimension(R.dimen.sp) * unitToConvertIntoSp;
    }

    public static float spToPX(int sp) {
        return new BigDecimal(sp).divide(new BigDecimal(App.binder.get().getResources().getDimension(R.dimen.sp)), 2, RoundingMode.HALF_UP).floatValue();
    }


    public static int fetchColorFromReference(int reference) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = App.binder.get().getActivity().getTheme();
        theme.resolveAttribute(reference, typedValue, true);
        return typedValue.data;
    }

    @ColorInt
    public static int fetchColorFromReferenceWithAlpha(int color, float factor) {
        color = fetchColorFromReference(color);
        color = changeAlpha(color, factor);
        return color;
    }

    @ColorInt
    public static int changeAlpha(int color, float factor) {

        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        return Color.argb(1 - alpha, red, green, blue);

    }

    public static Uri idToURI(String albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumId));
    }

    public static Bitmap getBitmap(ImageView imageView) {
        return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
    }


    public static void applyPadding(View mView, boolean statusBar, boolean navBar) {
        if (isOrientationPortrait() || Utils.isTablet()) {
            if (statusBar && navBar)
                mView.setPadding(0, MyActivity.statusBarHeight, 0, MyActivity.navigationHeight);
            else if (statusBar)
                mView.setPadding(0, MyActivity.statusBarHeight, 0, 0);
            else if (navBar)
                mView.setPadding(0, 0, 0, MyActivity.navigationHeight);
        } else {
            //noinspection SuspiciousNameCombination
            if (statusBar && navBar)
                mView.setPadding(0, MyActivity.statusBarHeight, MyActivity.navigationHeight, 0);
            else if (statusBar)
                mView.setPadding(0, MyActivity.statusBarHeight, 0, 0);
            else if (navBar)
                mView.setPadding(0, 0, MyActivity.navigationHeight, 0);


        }
    }


    public static String millisToFormattedString(long durr) {

        String finalTimerString = "";

        int hours = (int) (durr / (1000 * 60 * 60));
        int min = (int) (durr % (1000 * 60 * 60)) / (1000 * 60);
        String seconds = "" + ((int) ((durr % (1000 * 60 * 60)) % (1000 * 60) / 1000));
        String minutes = (min < 10 ? "0" + min : "" + min);

        if (seconds.length() < 2) seconds = "0" + seconds;

        if (hours > 0) finalTimerString = hours + ":";

        finalTimerString += minutes + ":";
        finalTimerString += seconds;

        // return timer string
        return finalTimerString;


    }

    @SuppressWarnings("unused")
    public static void changeBackgroundColor(View imageView, int cor) {

        Drawable background = imageView.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(cor);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(cor);
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(cor);
        }
    }

    @SuppressWarnings("unused")
    public static Drawable applyColorResOnDrawable(Drawable image, @ColorRes int colorRes) {
        return applyColorOnDrawable(ContextCompat.getColor(App.binder.get(), colorRes), image);
    }

    public static Drawable applyColorOnDrawable(int color, Drawable image) {
        if (image == null) return null;
        image.mutate();
        PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        image.setColorFilter(porterDuffColorFilter);
        return image;

    }

    public static String toPlural(int amount, int plural) {
        return App.binder.get().getResources().getQuantityString(plural, amount);
    }

    public static String format(String text, Object... values) {
        return String.format(Locale.getDefault(), text, values);
    }

    public static String getString(@StringRes int stringRes) {
        return App.binder.get().getString(stringRes);
    }

    @SuppressLint("CheckResult") public static void startService(Context mContext, Class service) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(mContext, new Intent(mContext, service));
                //  mContext.startService(new Intent(mContext, service)); // TODO: 04/06/2019 fix notificatons on O
                Log.i("Utils", "startService: INITING service as foreground " + service.getSimpleName());
            } else {
                mContext.startService(new Intent(mContext, service));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toasty.error(mContext, "Android O Sucks!");
        }


    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean removeTrack(Track mTrack, boolean fromStorage) {
        if (fromStorage) new File(mTrack.getFilePath()).delete();

        Uri rootUri = MediaStore.Audio.Media.getContentUriForPath(mTrack.getFilePath());
        App.binder.get().getContentResolver().delete(rootUri, MediaStore.MediaColumns.DATA + "=?", new String[]{mTrack.getFilePath()});


        return true;
    }

    public static boolean canConnect() {

        int networkStatus = NetworkUtils.checkConnection();
        if (networkStatus == NetworkUtils.WIFI) {
            return true;
        } else if (networkStatus == NetworkUtils.MOBILE) {
            return Prefs.getBoolean(c.can_use_mobile_data, false);
        } else return false;


    }

    public static void putOnForeground(Service service, int notificationId) {

        NotificationManager notificationManager = (NotificationManager) App.binder.get().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(App.binder.get(), "" + notificationId)
                .setContentTitle("Foregrounding")
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setSmallIcon(R.drawable.notification_icon)
                .setDefaults(0)
                .setVibrate(new long[]{0L}); // Passing null here silently fails


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel("" + notificationId, "holder while loading noification", importance);
            mChannel.enableVibration(false);
            mChannel.enableLights(false);
            mChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(mChannel);
            nBuilder.setChannelId("" + notificationId);

        }
        service.startForeground(notificationId, nBuilder.build());
        if (BuildConfig.DEBUG)
            Toasty.info(App.binder.get(), service.getClass().getSimpleName() + " on foreground").show();

    }
}