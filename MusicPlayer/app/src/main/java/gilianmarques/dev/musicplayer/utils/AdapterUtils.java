package gilianmarques.dev.musicplayer.utils;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Criado por Gilian Marques
 * Terça-feira, 19 de Junho de 2018  as 21:13:31.
 */
public class AdapterUtils {

    public static final int RV_ALBUM_VIEW_SIZE = (int) Utils.toPX(300);
    public static final int RV_ARTIST_VIEW_SIZE = (int) Utils.toPX(300);
    public static final int RV_TRACK_VIEW_SIZE = (int) Utils.toPX(300);
    public static final int RV_FOLDER_VIEW_SIZE = (int) Utils.toPX(100);
    public static final int RV_FOLDER_LIST_VIEW_SIZE = (int) Utils.toPX(300);

    // TODO: 11/05/2019 change this when were possible to user to change themes
    private static int highlightColor = 0;//Utils.fetchColorFromReferenceWithAlpha(R.attr.colorPrimaryDark, (MyActivity.darkTheme ? 0f : 0.965f));

    @SuppressWarnings("SuspiciousNameCombination")
    public static void adaptForDevice(@NonNull View baseView, int wichView, int paddingInUnit, boolean changeHeight) {

        ViewGroup.LayoutParams params = baseView.getLayoutParams();
        if (params == null)
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int viewWidth = viewWidth(wichView, paddingInUnit);

        params.width = viewWidth;
        if (changeHeight) params.height = viewWidth;
        baseView.setLayoutParams(params);
    }


    public static void adaptAlbumView(@NonNull View baseView) {

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int) Utils.toPX(350), (int) Utils.toPX(150));
        params.width = viewWidth(AdapterUtils.RV_ARTIST_VIEW_SIZE, 0);
        baseView.setLayoutParams(params);
    }

    public static void adaptArtistView(@NonNull View baseView) {

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams((int) Utils.toPX(350), (int) Utils.toPX(250));
        params.width = viewWidth(AdapterUtils.RV_ARTIST_VIEW_SIZE, 0);
        baseView.setLayoutParams(params);
    }

    public static void changeBackground(View itemView, int position) {
        if (true) return;
        if (position % 2 == 0) {
            itemView.setBackgroundColor(highlightColor);
        } else {
            itemView.setBackgroundColor(0);
        }
    }

    private static int viewWidth(int viewSize, int padding) {
        int spanCount = (int) (Utils.screenWidth / viewSize);
        return (int) ((Utils.screenWidth / spanCount) - Utils.toPX(padding));
    }


}
