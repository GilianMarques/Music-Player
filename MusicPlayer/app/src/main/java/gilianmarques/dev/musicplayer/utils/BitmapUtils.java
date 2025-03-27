package gilianmarques.dev.musicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.io.IOException;

import gilianmarques.dev.musicplayer.R;

/**
 * Criado por Gilian Marques
 * Domingo, 28 de Outubro de 2018  as 11:55:42.
 *
 * @Since 1.0
 */
@SuppressWarnings("WeakerAccess")
public class BitmapUtils {
    // there's a copy of this image in the drawable folder
    public static final String defArtAddress = "assets://png/no_art_background.png";
    public static final Bitmap defArt = BitmapFactory.decodeResource(App.binder.get().getResources(), R.drawable.no_art_background);




    // fetchArtURL art in the UI thread
    public static @Nullable Bitmap loadArtWithIdOnUIThread(Uri uri) {

        try {
            return MediaStore.Images.Media.getBitmap(App.binder.get().getContentResolver(), uri);
        } catch (IOException e) {
            return null;
        }

    }



    // apply masks

    public static Bitmap roundBitmap(Bitmap bitmap) {

        Bitmap mResultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mResultBitmap);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return mResultBitmap;
    }

    public static class Callback {
        public void loadDone(Bitmap loadedImage) {
        }

    }
}
