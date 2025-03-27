package gilianmarques.dev.musicplayer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

/**
 * Criado por Gilian Marques
 * Domingo, 28 de Outubro de 2018  as 18:23:47.
 *
 * @Since 1.0
 */
public class NetworkUtils {
    public static final int WIFI = 0b1;
    public static final int MOBILE = 0b10;
    public static final int OFFLINE = 0b11;


    public static int checkConnection() {
        final ConnectivityManager connMgr = (ConnectivityManager) App.binder.get().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr == null) return OFFLINE;

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnectedOrConnecting()) return WIFI;
        else if (mobile.isConnectedOrConnecting()) return MOBILE;
        else return OFFLINE;

    }
}
