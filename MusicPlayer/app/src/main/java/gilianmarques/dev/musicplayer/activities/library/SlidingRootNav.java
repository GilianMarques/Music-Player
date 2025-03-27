package gilianmarques.dev.musicplayer.activities.library;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.pixplicity.easyprefs.library.Prefs;
import com.yarolegovich.slidingrootnav.SlideGravity;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;
import com.yarolegovich.slidingrootnav.SlidingRootNavLayout;
import com.yarolegovich.slidingrootnav.callback.DragListener;
import com.yarolegovich.slidingrootnav.callback.DragStateListener;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.SettingsActivity;
import gilianmarques.dev.musicplayer.activities.folders.FolderActivity;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.timer.TimerDialog;
import gilianmarques.dev.musicplayer.timer.TimerService;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;

import static gilianmarques.dev.musicplayer.utils.Utils.getString;

/**
 * Criado por Gilian Marques em 02/06/2018 as 18:56:13.
 */
class SlidingRootNav implements com.yarolegovich.slidingrootnav.SlidingRootNav, DragListener, DragStateListener {
    private com.yarolegovich.slidingrootnav.SlidingRootNav drawer;
    private final Activity mActivity;
    private final ArrayList<ItemHolder> itens = new ArrayList<>();
    private LinearLayout container;
    private FloatingSearchView mSearchView;


    public SlidingRootNav(Activity mActivity, FloatingSearchView mSearchView) {
        this.mActivity = mActivity;
        this.mSearchView = mSearchView;

        init();
    }

    private void init() {
//https://github.com/yarolegovich/SlidingRootNav
        drawer = new SlidingRootNavBuilder(mActivity)
                .withMenuLayout(R.layout.menu_left_drawer)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(true)
                .withGravity(SlideGravity.LEFT)
                .withDragDistance(280) //Horizontal translation of a view. Default == 180dp
                .withRootViewScale(1f) //Content view's scale will be interpolated between 1f and 0.7f. Default == 0.65f;
                .withRootViewElevation(20) //Content view's elevation will be interpolated between 0 and 10dp. Default == 8.
                .withRootViewYTranslation(2) //Content view's translationY will be interpolated between 0 and 4. Default == 0
                .addDragListener(this)
                .addDragStateListener(this)
                .inject();
        container = getLayout().findViewById(R.id.menu_container);
        container.setAlpha(0f);
//container.setBackgroundColor(Utils.fetchColorFromReference(R.attr.app_card_background));

        Utils.applyPadding(container, true, true);
        populate();
        showItens();


    }

    private void showItens() {
        for (ItemHolder item : itens) {
            View mView = mActivity.getLayoutInflater().inflate(R.layout.view_side_menu_item, null);

            Drawable d = ContextCompat.getDrawable(mActivity, item.getImage());
            d = Utils.applyColorOnDrawable(Color.WHITE, d);
            ((ImageView) mView.findViewById(R.id.iv_ic)).setImageDrawable(d);
            ((TextView) mView.findViewById(R.id.tv_title)).setText(item.getText());
            mView.setOnClickListener(item.getClickListener());
            container.addView(mView);

        }
        closeMenu();
    }

    private void populate() {


        itens.add(new ItemHolder(R.drawable.vec_folder_theme, R.string.Pastas, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.startActivity(new Intent(mActivity, FolderActivity.class));
            }
        }));

        itens.add(new ItemHolder(R.drawable.vec_timer_theme, R.string.Definir_timer, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimerDialog();

            }
        }));

        itens.add(new ItemHolder(R.drawable.vec_play_theme_text, R.string.Tocando_agora, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.binder.get().goToPlayingNow(mActivity);

            }
        }));

        itens.add(new ItemHolder(R.drawable.vec_settings_theme, R.string.Configuracoes, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent playingNowIntent = new Intent(mActivity, SettingsActivity.class);
                mActivity.startActivity(playingNowIntent);

            }
        }));
        itens.add(new ItemHolder(R.drawable.vec_feedback_theme, R.string.FeedBack, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(mActivity).title(R.string.Caro_usuario)
                        .content(R.string.Use_este_canal_para).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"+"br.feedback.us@gmail.com"));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback sobre  "+getString(R.string.app_name));
                        intent.putExtra(Intent.EXTRA_TEXT, "");
                        mActivity.   startActivity(intent);


                    }
                }).positiveText(R.string.Continuar)
                        .cancelable(false).show();

            }
        }));

    }


    @Override
    public boolean isMenuClosed() {
        return drawer.isMenuClosed();
    }

    @Override
    public boolean isMenuOpened() {
        return drawer.isMenuOpened();
    }

    @Override
    public boolean isMenuLocked() {
        return drawer.isMenuLocked();
    }

    @Override
    public void closeMenu() {
        drawer.closeMenu(true);
    }

    @Override
    public void closeMenu(boolean animated) {
        drawer.closeMenu(animated);

    }

    @Override
    public void openMenu() {
        drawer.openMenu();

    }

    @Override
    public void openMenu(boolean animated) {
        drawer.openMenu(animated);

    }

    @Override
    public void setMenuLocked(boolean locked) {
        drawer.setMenuLocked(locked);

    }

    @Override
    public SlidingRootNavLayout getLayout() {
        return drawer.getLayout();
    }

    @Override
    public void onDrag(float progress) {
        container.setAlpha(progress);
    }

    @Override public void onDragStart() {
    }

    @Override public void onDragEnd(boolean isMenuOpened) {
        mSearchView.setLeftMenuOpen(isMenuOpened);
    }


    private class ItemHolder {
        private final int image;
        private final int text;
        private final View.OnClickListener clickListener;

        private ItemHolder(int image, int text, View.OnClickListener clickListener) {
            this.image = image;
            this.text = text;
            this.clickListener = clickListener;
        }


        int getImage() {
            return image;
        }

        int getText() {
            return text;
        }

        View.OnClickListener getClickListener() {
            return clickListener;
        }
    }


    private void showTimerDialog() {
        if (TimerService.binder != null) {
            Toasty.info(mActivity, getString(R.string.O_timer_ja_esta_rodando)).show();

            if (!Prefs.getBoolean(c.hint_cancel_timer_displayed, false)) {
                Toasty.info(mActivity, getString(R.string.Voce_pode_cancelar_o_timer_expandindo), Toast.LENGTH_LONG).show();
                Prefs.putBoolean(c.hint_cancel_timer_displayed, true);
            }


        } else new TimerDialog(mActivity).show();

    }
}
