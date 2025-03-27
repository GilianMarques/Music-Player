package gilianmarques.dev.musicplayer.persistence;

import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import gilianmarques.dev.musicplayer.utils.App;
import io.realm.Realm;

/**
 * Criado por Gilian Marques
 * Domingo, 26 de Maio de 2019  as 01:24:08.
 * <p>
 * Handle the THREAD 'question'
 */
public class UIRealm {
    private static final UIRealm inst = new UIRealm();
    private Realm realm;

    public static UIRealm get() {
        return inst;
    }

    /**
     * @param callback Use this callback to receive {@link Realm} instance on UiThread
     * @return a {@link Realm} instance on current thread
     */
    public static Realm getRealm(@Nullable final Callback callback) {

       if (callback!=null)App.runOnUiThread(new Runnable() {
            @Override public void run() {
                callback.doUIStuff(inst.realm);
            }
        });
        //this instance is returned on tcurrent thread
        return inst.realm;
    }

    private UIRealm() {
        App.runOnUiThread(new Runnable() {
            @Override public void run() {
                realm = Realm.getDefaultInstance();
            }
        });
    }

    @UiThread
    public static void init(App app) {
        /*call this methid will force the UIRealm instance to be initiated*/
        Realm.init(app.getApplicationContext());
    }

    public void executeTransaction(final Realm.Transaction transaction) {
        App.runOnUiThread(new Runnable() {
            @Override public void run() {
                realm.executeTransaction(transaction);
            }
        });
    }

    public void executeTransactionAsync(final Realm.Transaction transaction) {
        App.runOnUiThread(new Runnable() {
            @Override public void run() {
                realm.executeTransactionAsync(transaction);
            }
        });
    }

    public interface Callback {
        void doUIStuff(Realm realm);
    }

}
