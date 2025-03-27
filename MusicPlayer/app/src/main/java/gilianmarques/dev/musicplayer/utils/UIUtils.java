package gilianmarques.dev.musicplayer.utils;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;

import java.math.BigDecimal;
import java.util.Locale;

import gilianmarques.dev.musicplayer.R;

/**
 * Criado por Gilian Marques
 * Domingo, 29 de Dezembro de 2019  as 14:01:12.
 */
public class UIUtils {
    public static Drawable applyThemeToDrawable(Drawable image, int cor) {
        if (image == null) return null;
        image.mutate();
        PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(cor, PorterDuff.Mode.SRC_ATOP);
        image.setColorFilter(porterDuffColorFilter);
        return image;

    }
// TODO: 16/02/2020 passar todos os metodos de Utils para ca
    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public static String getProgressTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString;

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);


        if (seconds < 10) secondsString = "0" + seconds;
        else secondsString = "" + seconds;

        if (hours > 0) finalTimerString += hours + ":";

        finalTimerString += minutes + ":";
        finalTimerString += secondsString;

        return finalTimerString;
    }


    /**
     * Function to get Progress percentage
     *
     * @param currentDuration c
     */
    public static float getProgressPercent(long currentDuration, int totalDuration) {
        float percentage;

        percentage = ((((float) currentDuration) / totalDuration) * 100);
        return percentage;
    }

    /**
     * Function to change progress to timer
     *
     * @param progress -
     */
    public static int progressToMillis(int progress, int totalDuration) {

        int currentDuration;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;

    }

    /**
     * @param px amount to convert
     * @return the value received in dp
     */
    public static float dp(final float px) {
        return new BigDecimal(App.binder.get().getResources().getDimension(R.dimen.dp_base)).multiply(new BigDecimal(px)).floatValue();

    }

    /**
     * @param px amount to convert
     * @return the value received in sp
     */
    public static float sp(final float px) {
        return new BigDecimal(App.binder.get().getResources().getDimension(R.dimen.dp_base)).multiply(new BigDecimal(px)).floatValue();

    }

    public static Interpolator interpolate(double v, double v1, double v2, double v3) {

        return PathInterpolatorCompat.create((float) v, (float) v1, (float) v2, (float) v3);
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
}
