package gilianmarques.dev.musicplayer.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.album_details.AlbumDetailsActivity;
import gilianmarques.dev.musicplayer.activities.artist_details.ArtistDetailsActivity;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.models.Playlist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.spotify.helpers.AppTokenHelper;
import gilianmarques.dev.musicplayer.tag_editor.Tagger;
import io.realm.Realm;

/**
 * Criado por Gilian Marques
 * Domingo, 14 de Abril de 2019  as 21:53:32.
 */
public class MenuActions {
    private Activity mActivity;
    private Track mTrack;

    public MenuActions(Activity mActivity, Track mTrack) {
        this.mActivity = mActivity;
        this.mTrack = mTrack;
    }

    public void shareTrack() {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        File fileWithinMyDir = new File(mTrack.getFilePath());

        if (fileWithinMyDir.exists()) {
            intentShareFile.setType("audio/*");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + mTrack.getFilePath()));
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, mTrack.getTitle());
            intentShareFile.putExtra(Intent.EXTRA_TEXT, mActivity.getString(R.string.app_name));
            mActivity.startActivity(Intent.createChooser(intentShareFile, mTrack.getTitle()));
        }
    }

    public void addToPlaylist() {


        final ArrayList<Playlist> lists = new ArrayList<>(UIRealm.getRealm(null).where(Playlist.class).findAll());
        final ArrayList<String> names = new ArrayList<>();

        for (Playlist playlist : lists) {
            names.add(playlist.getName());
        }


        MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.selecione_a_playlist)
                .items(names)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(final MaterialDialog dialog, View view, final int which, CharSequence text) {
                        UIRealm.get().executeTransaction(new Realm.Transaction() {
                            @Override public void execute(@NonNull Realm realm) {
                                if (lists.get(which).addTrack(mTrack))
                                    Toasty.success(mActivity, Utils.format(Utils.toPlural(1, R.plurals.Faixas_adicionadas), String.valueOf(1))).show();
                                dialog.dismiss();
                            }
                        });
                    }
                }).negativeText(R.string.Cancelar).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).neutralText(R.string.Nova_playlist).onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        createPlaylist();
                    }
                })
                .build();
        dialog.show();

    }

    public void createPlaylist() {
        MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.Nova_playlist)
                .input(mActivity.getString(R.string.Nome_da_playlist), "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Playlist p = new Playlist(IdUtils.createStringObjectId());
                        p.setName(IdUtils.removeAllButLetterAndNumber(input.toString()));
                        p.addTrack(mTrack);

                        Realm realm = UIRealm.getRealm(null);
                        realm.beginTransaction();
                        realm.copyToRealm(p);
                        realm.commitTransaction();


                        //    SQL.playListRW(false).addPlayList(p);
                        Toasty.success(mActivity, Utils.format(Utils.toPlural(1, R.plurals.Faixas_adicionadas), String.valueOf(1))).show();
                        dialog.dismiss();
                    }
                }).inputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE).positiveText(R.string.Concluir)
                .build();
        dialog.show();
    }

    public void gotoAlbum(ImageView ivArt) {
        Intent mIntent = new Intent(mActivity, AlbumDetailsActivity.class);
        String ivAlbumArtTrasitionName = "ivAlbumArt_";
        ivArt.setTransitionName(ivAlbumArtTrasitionName);
        mIntent.putExtra("ivAlbumArt_transName", ivAlbumArtTrasitionName);
        mIntent.putExtra("album", mTrack.getAlbum().getId());
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                mActivity, Pair.create(((View) ivArt), ivAlbumArtTrasitionName));

        mActivity.startActivity(mIntent, options.toBundle());
    }

    public void gotoArtist() {
        Intent mIntent = new Intent(mActivity, ArtistDetailsActivity.class);
        mIntent.putExtra("a_id", mTrack.getArtist().getId());
        mActivity.startActivity(mIntent);
    }

    public void addToQueue() {
        int done = MusicService.binder.getPlayer().addToQueue(mTrack);
        if (done == 1) {
            Toasty.success(mActivity, Utils.format(Utils.getString(R.string.Inserida_na_pos_x), mTrack.getTitle(), "" + MusicService.binder.getPlayer().getQueue().size()), 3800).show();
        } else Toasty.info(mActivity, Utils.getString(R.string.Faixa_ja_esta_na_fila), 3800).show();
    }

    public void deleteTrack(final ActionsCallback callback) {
        final MaterialDialog materialDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.Deseja_remover_essa_Faixa)
                .content(R.string.Essa_faixa_sera_removida_do_seu_dispositivo)
                .positiveText(R.string.Remover)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Runnable mRunnable = new Runnable() {
                            @Override
                            public void run() {
                                Runnable mRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null) callback.trackRemoved(mTrack);
                                        Toasty.success(mActivity, mActivity.getString(R.string.Faixa_removida_com_sucesso), 3500).show();
                                    }
                                };
                                if (Utils.removeTrack(mTrack, true))
                                    mActivity.runOnUiThread(mRunnable);
                            }
                        };
                        new Thread(mRunnable).start();
                    }
                }).negativeText(R.string.Cancelar).cancelable(false).build();
        materialDialog.show();
    }

    public void removeFromPlaylist(final Playlist playlist, final ActionsCallback callback) {
        if (playlist != null) {
            if (playlist.size() == 1)
                Toasty.error(mActivity, mActivity.getString(R.string.Playlists_nao_podem_ficar), Toast.LENGTH_LONG).show();
            else UIRealm.get().executeTransaction(new Realm.Transaction() {
                @Override public void execute(@NonNull Realm realm) {
                    if (playlist.removeTrack(mTrack)) {
                        if (callback != null) callback.trackRemovedFromPlaylist(mTrack);
                        Toasty.success(mActivity, mActivity.getString(R.string.Faixa_removida), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void editTags() {
        if (AppTokenHelper.mAppAuth == null || AppTokenHelper.mAppAuth.getAccess_token() == null || AppTokenHelper.mAppAuth.getAccess_token().isEmpty())
            return;
        mActivity.startActivity(new Intent(mActivity, Tagger.class).putExtra("id", mTrack.getId()));
    }

    public static abstract class ActionsCallback {
        public void trackRemovedFromPlaylist(Track mTrack) {
        }

        public void trackRemovedFromPlayingNow(Track mTrack) {
        }

        public void trackRemoved(Track mTrack) {
        }
    }
}
