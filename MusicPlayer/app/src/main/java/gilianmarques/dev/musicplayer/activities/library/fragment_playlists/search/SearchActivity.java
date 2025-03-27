package gilianmarques.dev.musicplayer.activities.library.fragment_playlists.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.afollestad.materialdialogs.MaterialDialog;
import com.andremion.counterfab.CounterFab;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.models.Playlist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.sorting.utils.Sort;
import gilianmarques.dev.musicplayer.sorting.utils.SortTypes;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.Realm;


/**
 * Criado por Gilian Marques
 * Quinta-feira, 25 de Abril de 2019  as 19:10:35.
 */
public class SearchActivity extends MyActivity {

    private RecyclerView rvTracks;
    private FloatingSearchView searchBar;
    private CounterFab counterFab;
    private SearchTracksAdapter tracksAdapter;
    private Playlist playlist;
    SortTypes sType;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        String playlistId = getIntent().getStringExtra("pl_id");
        playlist = UIRealm.getRealm(null).where(Playlist.class).equalTo("id", playlistId).findFirst();
        if (playlist == null) {
            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            };
            new Handler().postDelayed(mRunnable, 300);
        }
        sType = SortTypes.toSortingType(Prefs.getInt(c.sorting_view_playlists, SortTypes.DEFAULT_.value));
        rvTracks = findViewById(R.id.rvTracks);
        searchBar = findViewById(R.id.floating_search_view);
        counterFab = findViewById(R.id.counterFab);

        tracksAdapter = new SearchTracksAdapter(this);

        rvTracks.setLayoutManager(new LinearLayoutManager(this));
        rvTracks.setAdapter(tracksAdapter);


        tracksAdapter.setCallback(new SearchTracksAdapter.Callback() {
            @Override public void onTrackSelected(int totalSelection) {
                super.onTrackSelected(totalSelection);
                counterFab.setCount(totalSelection);
            }
        });
        initSeacrhView();
        counterFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ArrayList<Long> selection = tracksAdapter.getSelection();
                new Task(selection, new finishCallback() {
                    @Override public void done() {
                        finish();
                    }
                }).execute();
            }
        });
    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    private void initSeacrhView() {
        searchBar.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override public void onSearchTextChanged(String oldQuery, final String newQuery) {
                Runnable mRunnable = new Runnable() {

                    @Override
                    public void run() {
                        final ArrayList<Track> tracks = Sort.Tracks.sort(sType, new NativeTracks(false).getTracksByName(newQuery, -1));

                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                tracksAdapter.update(tracks);

                            }
                        });
                    }
                };
                new Thread(mRunnable).start();
            }
        });

        new Thread(new Runnable() {
            @Override public void run() {
                final ArrayList<Track> tracks;
                tracks = Sort.Tracks.sort(sType, new NativeTracks(false).getAllTracks());
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        tracksAdapter.update(tracks);

                    }
                });
            }
        }).start();
    }


    @SuppressLint("StaticFieldLeak")
    private class Task extends AsyncTask<Void, Integer, Void> {
        private final ArrayList<Long> selection;
        private int tracksAdded, progress;
        final MaterialDialog dialog;
        private final finishCallback callback;

        Task(ArrayList<Long> selection, finishCallback callback) {
            this.selection = selection;
            dialog = new MaterialDialog.Builder(App.binder.get().getActivity())
                    .progress(false, selection.size(), true).title(R.string.Adicionando_faixas).cancelable(false).build();
            this.callback = callback;
        }

        @Override protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
            super.onProgressUpdate(values);
        }

        @Override protected void onPreExecute() {
            dialog.show();
            super.onPreExecute();
        }

        @Override protected Void doInBackground(Void... voids) {

            for (long trackId : selection) {
                final Track track = new NativeTracks(false).getTrackById(trackId);
                Log.d(App.myFuckingUniqueTAG + "Task", "doInBackground: " + trackId + "  " + track.getTitle());

                App.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        UIRealm.get().executeTransaction(new Realm.Transaction() {
                            @Override public void execute(@NonNull Realm realm) {
                                if (playlist.addTrack(track)) tracksAdded++;
                                publishProgress(progress++);
                            }
                        });
                    }
                });
            }
            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            Context context = App.binder.get();
            Toasty.success(context, Utils.format(Utils.toPlural(tracksAdded, R.plurals.Faixas_adicionadas), String.valueOf(tracksAdded))).show();
            dialog.dismiss();
            callback.done();
            super.onPostExecute(aVoid);
        }


    }

    interface finishCallback {
        void done();
    }
}
