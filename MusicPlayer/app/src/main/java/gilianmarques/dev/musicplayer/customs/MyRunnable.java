package gilianmarques.dev.musicplayer.customs;

import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Sábado, 01 de Junho de 2019  as 12:07:06.
 */
public abstract class MyRunnable implements Runnable {

    @Override public void run() {
        workerThread();
        App.runOnUiThread(new Runnable() {
            @Override public void run() {
                UIThread();
            }
        });
    }

    protected abstract void workerThread();

    public abstract void UIThread();

}
