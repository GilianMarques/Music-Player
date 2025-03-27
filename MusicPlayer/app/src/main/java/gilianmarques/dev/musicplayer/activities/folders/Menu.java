package gilianmarques.dev.musicplayer.activities.folders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.MenuActions;

/**
 * Criado por Gilian Marques
 * Sábado, 16 de Junho de 2018  as 17:18:10.
 */
public class Menu implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private final Track mTrack;
    private final ImageView ivArt;
    private final Activity mActivity;

    private MenuActions.ActionsCallback callback;

    Menu(Track mTrack, ImageView ivArt) {
        this.mTrack = mTrack;
        this.ivArt = ivArt;
        mActivity = App.binder.get().getActivity();
    }

    public Menu setCallback(MenuActions.ActionsCallback callback) {
        this.callback = callback;
        return this;
    }


    @Override
    @SuppressLint("RestrictedApi")
    public void onClick(View view) {


        PopupMenu menu = new PopupMenu(mActivity, view);
        menu.inflate(R.menu.popup_tracks);
        menu.setOnMenuItemClickListener(this);
        MenuPopupHelper menuHelper = new MenuPopupHelper(mActivity, (MenuBuilder) menu.getMenu(), view);
        menuHelper.setForceShowIcon(true);

        menu.getMenu().removeItem(R.id.remove_from_playlist);
        menu.getMenu().removeItem(R.id.remove_from_queue);

        menuHelper.show();


    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.popup_song_goto_album:
                new MenuActions(mActivity, mTrack).gotoAlbum(ivArt);
                break;
            case R.id.popup_song_goto_artist:
                new MenuActions(mActivity, mTrack).gotoArtist();
                break;
            case R.id.popup_song_addto_queue:
                new MenuActions(mActivity, mTrack).addToQueue();
                break;
            case R.id.popup_song_addto_playlist:
                new MenuActions(mActivity, mTrack).addToPlaylist();
                break;
            case R.id.popup_song_share:
                new MenuActions(mActivity, mTrack).shareTrack();
                break;
            case R.id.editTag:
                new MenuActions(mActivity, mTrack).editTags();
                break;
            case R.id.popup_song_delete:
                new MenuActions(mActivity, mTrack).deleteTrack(callback);
                break;

        }
        return false;
    }


}
