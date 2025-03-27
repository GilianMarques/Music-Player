package gilianmarques.dev.musicplayer.spotify.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.pixplicity.easyprefs.library.Prefs;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.spotify.helpers.UserTokenHelper;
import gilianmarques.dev.musicplayer.utils.App;

public class RedirectActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (System.currentTimeMillis() - Prefs.getLong(c.spotify_req_stamp, 0) > 120 * 1000/*2 min*/) {
            Toasty.info(App.binder.get(), getString(R.string.Solicitacao_expirou), 3000).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_my_spotify_auth);
        Intent i = getIntent();

        // the activity is called from browser  after result of calling makeRequest method with auth code
        // in case user autorizes access
        if ("android.intent.action.VIEW".equals(i.getAction())) {
            // returning with usert approval or denial about access
            String code = i.getData().getQueryParameter("code");

            if (code == null || code.isEmpty()) {
                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(App.binder.get(), "Permisão negada!", 3000).show();
                    }
                };
                new Handler().postDelayed(mRunnable, 600);

            } else UserTokenHelper.fetchTokens(code, new UserTokenHelper.CallbackI() {
                @Override public void done(final boolean success) {
                    final Runnable mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (success)
                                Toasty.success(App.binder.get(), getString(R.string.Sucesso), 3000).show();
                            else
                                Toasty.error(App.binder.get(), getString(R.string.Erro), 3000).show();
                        }
                    };
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            new Handler().postDelayed(mRunnable, 200);
                        }
                    });
                }
            });

        }
        finish();


    }


}
