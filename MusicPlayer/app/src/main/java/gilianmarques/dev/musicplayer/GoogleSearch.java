package gilianmarques.dev.musicplayer;

import android.os.Handler;

import okhttp3.Callback;
import okhttp3.OkHttpClient;

/**
 * Criado por Gilian Marques
 * Quarta-feira, 22 de Maio de 2019  as 20:38:02.
 */
public class GoogleSearch {
    public GoogleSearch() {
        Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {

                    }
                };
        new Handler().postDelayed(mRunnable, 2000);
    }

    private final OkHttpClient client = new OkHttpClient();

    public void search(String url, Callback callback) {

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url).get()
                .build();

        client.newCall(request).enqueue(callback);
    }


    // AIzaSyAx_RLe0_sO6Cf1PNRg_aKpwLLK5O7tr3Y



    /*public GoogleSearch() {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                search("https://www.googleapis.com/customsearch/v1?key=AIzaSyAx_RLe0_sO6Cf1PNRg_aKpwLLK5O7tr3Y&cx=017576662512468239146:omuauf_lfve&q=As i lay dying&searchType=image", new Callback() {
                    @Override public void onFailure(Call call, IOException e) {
                        Log.d(App.myFuckingUniqueTAG + "GoogleSearch", "onFailure: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d(App.myFuckingUniqueTAG + "GoogleSearch", "onResponse: \n" + response.body().string());
                    }
                });
            }
        };
        new Thread(mRunnable).start();
    }

    private final OkHttpClient client = new OkHttpClient();

    public void search(String url, Callback callback) {

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }*/
}
