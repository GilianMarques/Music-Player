package gilianmarques.dev.musicplayer.utils;

import android.os.AsyncTask;


/**
 * Criado por Gilian Marques
 * Domingo, 24 de Junho de 2018  as 11:19:13.
 *
 * @Since 0.1 Beta
 */
public class EasyAsynk extends AsyncTask<Void, Integer, Void> {
    private final Actions callback;

    public EasyAsynk(Actions callback) {
        this.callback = callback;
    }

    @Override protected void onPreExecute() {
        callback.onPreExecute();
        super.onPreExecute();
    }

    @Override protected void onPostExecute(Void aVoid) {
        callback.onPostExecute();
        super.onPostExecute(aVoid);
    }

    @Override protected void onProgressUpdate(Integer... values) {
        callback.onProgressUpdate(values);
        super.onProgressUpdate(values);
    }

    @Override protected Void doInBackground(Void... voids) {
        publishProgress(callback.doInBackground());
        return null;
    }

    public void executeAsync() {
        this.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    public static class Actions {
        public void onPreExecute() {
        }

        public int doInBackground() {
            return 0;
        }

        public void onPostExecute() {
        }

        public void onProgressUpdate(Integer[] values) {
        }

    }
}
