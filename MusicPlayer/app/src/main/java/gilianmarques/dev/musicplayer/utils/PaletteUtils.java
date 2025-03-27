package gilianmarques.dev.musicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

import gilianmarques.dev.musicplayer.R;

/**
 * Criado por Gilian Marques em 31/05/2018 as 13:13:03.
 */
public class PaletteUtils {


    public static void colorFrom(Bitmap map, final PaletteCallback callback) {

        if (map == null) throw new IllegalArgumentException("Bitmap nulo");
        if (callback == null)
            throw new IllegalArgumentException("callbak n pode ser nulo");


        new Palette.Builder(map).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@NonNull Palette palette) {
                final int primaryText, secundaryText, background;


                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    primaryText = vibrantSwatch.getTitleTextColor();
                    secundaryText = vibrantSwatch.getBodyTextColor();
                    background = vibrantSwatch.getRgb();

                } else {

                    Palette.Swatch mutedSwatch = palette.getMutedSwatch();
                    if (mutedSwatch != null) {
                        primaryText = mutedSwatch.getTitleTextColor();
                        secundaryText = mutedSwatch.getBodyTextColor();
                        background = mutedSwatch.getRgb();

                    } else {

                        primaryText = Utils.fetchColorFromReference(R.attr.app_textColorPrimary);
                        secundaryText = Utils.fetchColorFromReference(R.attr.app_textColorSecondary);
                        background = Utils.fetchColorFromReference(R.attr.app_card_background);
                    }
                }


                callback.result(primaryText, secundaryText, background);

            }
        });


    }

    public static void gradientColorFrom(Bitmap map, final GradientCallback callback) {

        if (map == null) throw new IllegalArgumentException("Bitmap nulo");
        if (callback == null)
            throw new IllegalArgumentException("callbak n pode ser nulo");


        new Palette.Builder(map).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@NonNull Palette palette) {
                int c1 = Color.MAGENTA;

                Palette.Swatch vibrantSwatch = palette.getDarkVibrantSwatch();
                Palette.Swatch mutedSwatch = palette.getDarkMutedSwatch();

                if (vibrantSwatch != null) c1 = vibrantSwatch.getRgb();
                else if (mutedSwatch != null) c1 = mutedSwatch.getRgb();


                callback.result(c1, manipulateColor(c1, 1.9f));

            }
        });


    }

    /**
     * @param color  to manipulate
     * @param factor 1.0f nothing changes <1.0f darker >1.0f lighter
     * @return manipulated color
     */
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                          Math.min(r, 255),
                          Math.min(g, 255),
                          Math.min(b, 255));
    }

    public interface PaletteCallback {

        void result(@ColorInt int primary, @ColorInt int secundary, @ColorInt int background);


    }

    public interface GradientCallback {

        void result(@ColorInt int c1, @ColorInt int c2);


    }

}
