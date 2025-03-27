package gilianmarques.dev.musicplayer.tag_editor;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeAlbuns;
import gilianmarques.dev.musicplayer.spotify.helpers.AppTokenHelper;
import gilianmarques.dev.musicplayer.spotify.objects.AlbunsResult;
import gilianmarques.dev.musicplayer.spotify.web_api_wrapper.WebSpotify;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;

public class TaggerAlbuns extends MyActivity {
    private EditText edtAlbum, edtArtist, edtYear;
    private RecyclerView rvSuggestions;
    private ImageView ivArt;
    private SuggestionsAdapter suggestionsAdapter;
    private Album mAlbum;
    boolean webResultsIsShown;
    private float initYresults;
    private String userPath;
    private final FastOutSlowInInterpolator interpolator = new FastOutSlowInInterpolator();
    private ArrayList<Suggestion> suggestions = new ArrayList<>();
    private MediaScannerConnection scanner;
    private View rootView;
    private ArrayList<String> fileRemoved = new ArrayList<>();
    private MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagger_album);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setTitle("");
        rootView = findViewById(android.R.id.content);
       // rootView.setVisibility(View.INVISIBLE);
        mAlbum = new NativeAlbuns().getAlbumByID(getIntent().getLongExtra("id", 0));

        if (!Utils.canConnect()) {
            Toasty.error(this, getString(R.string.Conectese_a_internet), 3500).show();
            finish();
            return;
        }

        if (AppTokenHelper.mAppAuth == null ) {
            Toasty.error(this, getString(R.string.Houve_um_erro_ao_carregar), 3500).show();
            AppTokenHelper.refreshToken(null);
            finish();  return;
        }


        init();


    }


    private void init() {

        ivArt = findViewById(R.id.ivArt);
        edtYear = findViewById(R.id.edtYear);
        edtAlbum = findViewById(R.id.edtAlbum);
        edtArtist = findViewById(R.id.edtArtist);
        rvSuggestions = findViewById(R.id.rvSuggestions);

        suggestionsAdapter = new SuggestionsAdapter(this);


        rvSuggestions.post(new Runnable() {
            @Override public void run() {
                initYresults = rvSuggestions.getY();
                rvSuggestions.setY(Utils.screenHeight);
            }
        });
        //
        rvSuggestions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSuggestions.setAdapter(suggestionsAdapter);

        //
        suggestionsAdapter.setCallback(new SuggestionsAdapter.Callback() {
            @Override public void onSugestionClicked(Suggestion suggestion) {
                toogleWebResults(false);
                edtAlbum.setText(suggestion.album);
                edtYear.setText(suggestion.year + "");
                if (suggestion.artist != null) edtArtist.setText(suggestion.artist);
                Picasso.get().load(suggestion.image).into(ivArt);
            }
        });
        //

        findViewById(R.id.fabDone).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (edtArtist.getText().length() == 0) {
                    Toasty.info(App.binder.get(), App.binder.get().getString(R.string.Preencha_o_campo_artista), 3000).show();
                } else
                    updateTracks();
            }
        });
        //
        findViewById(R.id.fabSearch).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!Utils.canConnect()) {
                    Toasty.info(TaggerAlbuns.this, TaggerAlbuns.this.getString(R.string.Nao_ha_conexao_com_a_internet), 5000).show();
                }
                if (edtArtist.getText().length() == 0 && edtAlbum.getText().length() == 0) {
                    Toasty.info(App.binder.get(), App.binder.get().getString(R.string.Preencha_um_campo), 3000).show();
                } else fetchDataFromCloud();
            }
        });


        //


        if (mAlbum != null) {

            edtAlbum.setText(mAlbum.getName());
            edtArtist.setText(mAlbum.getArtist());
            Picasso.get().load(mAlbum.getURI()).error(R.drawable.no_art_background).into(ivArt);
        }

        ivArt.post(new Runnable() {
            @Override public void run() {
                CollapsingToolbarLayout.LayoutParams p = (CollapsingToolbarLayout.LayoutParams) ivArt.getLayoutParams();
                p.height = ivArt.getMeasuredWidth();
                ivArt.setLayoutParams(p);
                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        AlphaAnimation anim = new AlphaAnimation(0, 1);
                        anim.setDuration(300);
                        anim.setFillAfter(true);
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override public void onAnimationStart(Animation animation) {
                                rootView.setVisibility(View.VISIBLE);
                            }

                            @Override public void onAnimationEnd(Animation animation) {

                            }

                            @Override public void onAnimationRepeat(Animation animation) {

                            }
                        });
                       // rootView.startAnimation(anim);
                    }
                };
                new Handler().postDelayed(mRunnable,600 );

            }
        });
        userPath = Prefs.getString(c.manage_library_user_dir, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        ((TextView) findViewById(R.id.tvPath)).setText(userPath + "/nome_artista/nome_album/");
    }

    private void fetchDataFromCloud() {
        //trying to load query from cache
        // TODO: 01/06/2019 use cache to save queries, remove spotufy wrapper library


        final String query = edtArtist.getText().toString() + " - " + edtAlbum.getText().toString();

        final AlbunsResult albumResult = new Gson().fromJson(Prefs.getString(query, null), AlbunsResult.class);

        if (albumResult != null) {
            consumeResults(albumResult);
            return;
        } else suggestions.clear();

        new WebSpotify().search(WebSpotify.QueryType.album, query, new WebSpotify.WebCallback() {
            @Override public void albunsRes(@Nullable final AlbunsResult result, boolean success) {
                if (!success || result == null)
                    App.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toasty.error(App.binder.get(), App.binder.get().getString(R.string.Erro), 2000).show();

                        }
                    });
                else {
                    //caching results
                    App.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            consumeResults(result);
                        }
                    });
                }
            }
        });
    }

    private void consumeResults(AlbunsResult albumResult) {
        for (AlbunsResult.Item albumSample : albumResult.albums.items) {
            Suggestion suggestion = new Suggestion();
            String rlsDate = albumSample.release_date;

            suggestion.album = albumSample.name;
            suggestion.image = albumSample.images.get(0).url;
            suggestion.artist = albumSample.artists.get(0).name;
            if (rlsDate != null && rlsDate.length() > 4) {
                rlsDate = rlsDate.substring(0, 4);//year
                suggestion.year = Integer.parseInt(rlsDate);
            }
            suggestions.add(suggestion);
        }


        suggestionsAdapter.update(suggestions);
        suggestionsAdapter.notifyDataSetChanged();

        if (suggestions.size() == 0)
            Toasty.info(App.binder.get(), "Nenhum resultado encontrado.", 5000).show();
        else toogleWebResults(true);

    }

    private void updateTracks() {
        dialog = new MaterialDialog.Builder(TaggerAlbuns.this).title(R.string.Por_favor_aguarde).progress(false, mAlbum.getTracks().size()).cancelable(false).build();

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                try {

                    for (Track track : mAlbum.getTracks()) {
                        updateMetadata(track);
                        App.runOnUiThread(new Runnable() {
                            @Override public void run() {
                                dialog.setProgress(dialog.getCurrentProgress() + 1);
                            }
                        });
                    }

                    App.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            scanner = new MediaScannerConnection(getApplicationContext(), new MediaScannerConnection.MediaScannerConnectionClient() {

                                public void onScanCompleted(String path, Uri uri) {
                                    if (path.equals(fileRemoved.get(fileRemoved.size() - 1)))
                                        scanner.disconnect();
                                }

                                public void onMediaScannerConnected() {

                                    for (String file : fileRemoved)
                                        scanner.scanFile(file, "audio/*");
                                }
                            });
                            scanner.connect();
                            dialog.dismiss();

                            Runnable mRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.success(App.binder.get(), getString(R.string.Sucesso), 2000).show();
                                }
                            };
                            new Handler().postDelayed(mRunnable, 200);
                            finish();
                        }
                    });


                } catch (IOException | NotSupportedException | UnsupportedTagException | InvalidDataException e) {
                    e.printStackTrace();
                    App.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toasty.error(TaggerAlbuns.this, TaggerAlbuns.this.getString(R.string.Erro_lendo_arquivo), 5000).show();
                            dialog.dismiss();
                        }
                    });
                }
            }
        };
        dialog.show();
        new Thread(mRunnable).start();


    }


    private void updateMetadata(Track track) throws IOException, NotSupportedException, InvalidDataException, UnsupportedTagException {

        String _new = "." + new LocalDateTime().toDate().getTime(),
                ext = track.getFilePath().substring(track.getFilePath().length() - 4);

        Mp3File songFile;

        try {
            songFile = new Mp3File(track.getFilePath(), true);
        } catch (Exception e) {
            e.printStackTrace();
            songFile = new Mp3File(track.getFilePath(), false);
        }

        String album = edtAlbum.getText().toString();
        String artist = edtArtist.getText().toString();
        String year = edtYear.getText().toString();

        if (songFile.hasId3v1Tag()) songFile.removeId3v1Tag();

        if (songFile.hasCustomTag()) songFile.removeCustomTag();

        if (!songFile.hasId3v2Tag()) songFile.setId3v2Tag(new ID3v24Tag());

        ID3v2 tagV2 = songFile.getId3v2Tag();
        tagV2.setAlbum(album);
        tagV2.setArtist(artist);
        if (year.length() > 3) tagV2.setYear(year);
        tagV2.setAlbumArtist(artist);
        Bitmap mBitmap = Utils.getBitmap(ivArt);
        if (mBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            tagV2.setAlbumImage(byteArray, "image/bmp");
        }


        String path = userPath.concat(File.separator)
                .concat(edtArtist.getText().toString())
                .concat(File.separator).concat(edtAlbum.getText().toString())
                .concat(File.separator);

        //used to create folders
        File mFile = new File(path);
        if (!mFile.exists()) mFile.mkdirs();
        // now insert the filename.ext
        path = path.concat(track.getTitle()).concat(ext + _new);// get extension from track


        songFile.save(path);


        final File origTrack = new File(track.getFilePath());

        if (origTrack.delete()) {
            fileRemoved.add(origTrack.getPath());
        }

        File updatedTrack = new File(path);
        path = path.replace(_new, "");

        if (updatedTrack.renameTo(new File(path)))
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));

    }


    private void toogleWebResults(boolean b) {
        webResultsIsShown = b;
        final ObjectAnimator anim;
        if (b)
            anim = ObjectAnimator.ofFloat(rvSuggestions, "y", Utils.screenHeight, initYresults);
        else
            anim = ObjectAnimator.ofFloat(rvSuggestions, "y", rvSuggestions.getY(), Utils.screenHeight);

        int animDurr = 350;
        anim.setDuration(animDurr);


        anim.setInterpolator(interpolator);
        anim.start();


    }


    @Override public void onBackPressed() {
        if (webResultsIsShown) toogleWebResults(false);
        else super.onBackPressed();
    }


}
