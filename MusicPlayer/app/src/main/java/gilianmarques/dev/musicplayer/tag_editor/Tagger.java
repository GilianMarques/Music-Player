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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
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
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.spotify.helpers.AppTokenHelper;
import gilianmarques.dev.musicplayer.spotify.objects.TracksResult;
import gilianmarques.dev.musicplayer.spotify.web_api_wrapper.WebSpotify;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;

public class Tagger extends MyActivity implements FolderChooserDialog.FolderCallback {
    private EditText edtTitle, edtAlbum, edtArtist, edtNumber, edtYear;
    private TextView tvPath;
    private RecyclerView rvSuggestions;
    private ImageView ivArt;
    private SuggestionsAdapter suggestionsAdapter;
    private Track track;
    private Switch swLibrary, swDelete;
    boolean webResultsIsShown;
    private float initYresults;
    private String userPath;
    private final FastOutSlowInInterpolator interpolator = new FastOutSlowInInterpolator();
    private ArrayList<Suggestion> suggestions = new ArrayList<>();
    private MediaScannerConnection scanner;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagger);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setTitle("");

        track = new NativeTracks(true).getTrackById(getIntent().getLongExtra("id", 0));
        rootView = findViewById(android.R.id.content);
        rootView.setVisibility(View.INVISIBLE);

        if (!Utils.canConnect()) {
            Toasty.error(this, getString(R.string.Conectese_a_internet), 3500).show();
            finish();
            return;
        }

        if (AppTokenHelper.mAppAuth == null) {
            Toasty.error(this, getString(R.string.Houve_um_erro_ao_carregar), 3500).show();
            AppTokenHelper.refreshToken(null);
            finish();
            return;
        }


        init();


    }

    @Override protected void applyTheme(Window mWindow, View decorView) {

        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);

        mWindow.setStatusBarColor(0);
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        //status bar must have white icons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

    }

    private void initLibraryManager() {
        tvPath.setText(track.getFilePathNoName() + "titulo_musica.mp3");
        swLibrary.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (swLibrary.isChecked())
                    new FolderChooserDialog.Builder(Tagger.this).allowNewFolder(true, R.string.Nova_pasta).goUpLabel("Voltar").cancelButton(R.string.Cancelar).chooseButton(R.string.Selecionar).show(getSupportFragmentManager());
            }
        });
        swLibrary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvPath.setText(userPath + "/nome_artista/nome_album/titulo_musica.mp3");

                } else {
                    tvPath.setText(track.getFilePathNoName() + "titulo_musica.mp3");
                }
                Prefs.putBoolean(c.manage_library, isChecked);
            }
        });
        swDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Prefs.putBoolean(c.manage_library_delete_orig_file, isChecked);
            }
        });
        userPath = Prefs.getString(c.manage_library_user_dir, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        swLibrary.setChecked(Prefs.getBoolean(c.manage_library, false));
        swDelete.setChecked(Prefs.getBoolean(c.manage_library_delete_orig_file, false));

    }

    private void init() {

        tvPath = findViewById(R.id.tvPath);
        swLibrary = findViewById(R.id.swLibrary);
        swDelete = findViewById(R.id.swDelete);
        ivArt = findViewById(R.id.ivArt);
        edtTitle = findViewById(R.id.edtTitle);
        edtAlbum = findViewById(R.id.edtAlbum);
        edtArtist = findViewById(R.id.edtArtist);
        edtNumber = findViewById(R.id.edtNumber);
        edtYear = findViewById(R.id.edtYear);
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
                edtTitle.setText(suggestion.title);
                edtAlbum.setText(suggestion.album);
                edtArtist.setText(suggestion.artist);
                edtNumber.setText("" + suggestion.number);
                edtYear.setText("" + suggestion.year);
                Picasso.get().load(suggestion.image).into(ivArt);
            }
        });
        //

        findViewById(R.id.fabDone).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                if (edtArtist.getText().length() == 0 || edtAlbum.getText().length() == 0 || edtTitle.getText().length() == 0) {
                    Toasty.info(App.binder.get(), App.binder.get().getString(R.string.Verifique_os_campos), 3000).show();
                } else try {
                    updateMetadata();
                } catch (IOException | NotSupportedException | UnsupportedTagException | InvalidDataException e) {
                    e.printStackTrace();
                    Toasty.error(Tagger.this, Tagger.this.getString(R.string.Erro_lendo_arquivo), 5000).show();
                }
            }
        });
        //
        findViewById(R.id.fabSearch).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!Utils.canConnect()) {
                    Toasty.info(Tagger.this, Tagger.this.getString(R.string.Nao_ha_conexao_com_a_internet), 5000).show();
                }

                {
                    String title = edtTitle.getText().toString();

                    if (title.length() == 0) {
                        Toasty.info(App.binder.get(), App.binder.get().getString(R.string.Preencha_o_campo_titulo), 3000).show();
                    } else
                        fetchDataFromCloud(edtArtist.getText().toString() + " - " + title);
                }
            }
        });
        //


        //


        if (track != null) {

            edtTitle.setText(track.getTitle());
            edtAlbum.setText(track.getAlbumName());
            edtArtist.setText(track.getArtistName());
            Log.d(App.myFuckingUniqueTAG + "Tagger", "init: " + track.getAlbum().getURI());
            Picasso.get().load(track.getAlbum().getURI()).error(R.drawable.no_art_background).placeholder(R.drawable.no_art_background).into(ivArt);
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
                                    rootView.startAnimation(anim);
                                }
                            };
                    new Handler().postDelayed(mRunnable,600 );
            }
        });
        userPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();

        initLibraryManager();
    }

    private void fetchDataFromCloud(final String query) {

        new WebSpotify().search(WebSpotify.QueryType.track, query, new WebSpotify.WebCallback() {
            @Override
            public void tracksRes(@Nullable final TracksResult result, final boolean success) {
                suggestions.clear();
                App.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (success) consumeResults(result);
                        else
                            Toasty.error(Tagger.this, Tagger.this.getString(R.string.Erro), 1500).show();

                    }
                });

            }
        });

    }

    private void consumeResults(TracksResult result) {
        for (TracksResult.Items track : result.tracks.items) {
            Suggestion suggestion = new Suggestion();
            TracksResult.Album album = track.album;
            TracksResult.Artists artist = track.artists.get(0);

            suggestion.title = track.name;
            suggestion.number = track.track_number;
            suggestion.artist = artist.name;
            suggestion.album = album.name;
            suggestion.image = album.images.get(0).url;

            String y = track.album.release_date;
            y = y == null ? "" : (y.length() < 4 ? y : (y.substring(0, 4)));
            suggestion.year = Integer.parseInt(y);

            suggestions.add(suggestion);
        }
        suggestionsAdapter.update(suggestions);
        suggestionsAdapter.notifyDataSetChanged();

        if (suggestions.size() == 0)
            Toasty.info(App.binder.get(), "Nenhum resultado encontrado.", 5000).show();
        else toogleWebResults(true);

    }

    private void updateMetadata() throws IOException, NotSupportedException, InvalidDataException, UnsupportedTagException {

        String _old = "." + new LocalDateTime().getMillisOfDay(), _new = "." + new LocalDateTime().toDate().getTime(),
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
        String title = edtTitle.getText().toString();
        String number = edtNumber.getText().toString();
        String year = edtYear.getText().toString();

        if (songFile.hasId3v1Tag()) songFile.removeId3v1Tag();

        if (songFile.hasCustomTag()) songFile.removeCustomTag();

        if (!songFile.hasId3v2Tag()) songFile.setId3v2Tag(new ID3v24Tag());

        ID3v2 tagV2 = songFile.getId3v2Tag();
        tagV2.setAlbum(album);
        tagV2.setArtist(artist);
        tagV2.setTitle(title);
        tagV2.setAlbumArtist(artist);

        if (!number.isEmpty()) tagV2.setTrack(number);
        if (!year.isEmpty() && year.length() > 3) tagV2.setYear(year);

        Bitmap mBitmap = Utils.getBitmap(ivArt);
        if (mBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            tagV2.setAlbumImage(byteArray, "image/bmp");
        }


        String path;
        if (swLibrary.isChecked()) {

            path = userPath.concat(File.separator).concat(edtArtist.getText().toString()).concat(File.separator).concat(edtAlbum.getText().toString()).concat(File.separator);

            //used to create folders
            File mFile = new File(path);
            if (!mFile.exists()) mFile.mkdirs();
            // now insert the filename.ext
            path = path.concat(edtTitle.getText().toString()).concat(ext + _new);// get extension from track

        } else
            path = track.getFilePathNoName() + File.separator + tagV2.getTitle().concat(ext + _new);// get extension from track


        songFile.save(path);


        final File origTrack = new File(track.getFilePath());

        if (swDelete.isChecked()) {
            if (origTrack.delete()) {
                scanner = new MediaScannerConnection(getApplicationContext(),
                                                     new MediaScannerConnection.MediaScannerConnectionClient() {

                                                         public void onScanCompleted(String path, Uri uri) {
                                                             scanner.disconnect();
                                                         }

                                                         public void onMediaScannerConnected() {
                                                             scanner.scanFile(origTrack.getPath(), "audio/*");
                                                         }
                                                     });

                scanner.connect();
            } else
                Log.d(App.myFuckingUniqueTAG + "Tagger", "updateMetadata: couldn't delete file " + origTrack.getPath());

        } else if (origTrack.renameTo(new File(track.getFilePath() + _old + ext))) {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(track.getFilePath() + _old + ext))));
        } else
            Log.d(App.myFuckingUniqueTAG + "Tagger", "updateMetadata: couldn't rename file " + origTrack.getPath());


        File updatedTrack = new File(path);
        path = path.replace(_new, "");

        if (updatedTrack.renameTo(new File(path)))
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));


        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                Toasty.success(App.binder.get(), getString(R.string.Sucesso), 2000).show();
            }
        };
        finish();
    }

    // TODO: 02/06/2019 por dialogs de explicação nos dois tagger pra preencher os campos de artista e faixa
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

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        Prefs.putString(c.manage_library_user_dir, folder.getAbsolutePath());
        userPath = folder.getAbsolutePath();
        tvPath.setText(userPath + "/nome_artista/nome_album/titulo_musica.mp3");
    }

    @Override public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {

    }

}
