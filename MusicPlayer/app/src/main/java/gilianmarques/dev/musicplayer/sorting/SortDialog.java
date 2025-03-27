package gilianmarques.dev.musicplayer.sorting;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.AlphaAnimation;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.sorting.utils.SortTypes;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques
 * Sábado, 11 de Maio de 2019  as 19:49:52.
 */
public class SortDialog extends AlertDialog implements DialogInterface.OnShowListener {

    private View parentLayout, alphaLayout;
    private Activity mActivity;
    private View rootView;
    private Callback callback;
    private int sbc;


    public SortDialog(@NonNull Activity mActivity, @NonNull Callback callback) {
        super(mActivity, R.style.AppThemeLight_Translucent_Full);
        this.mActivity = mActivity;

        rootView = mActivity.getLayoutInflater().inflate(R.layout.sort_layout, null);
        this.callback = callback;
        parentLayout = rootView.findViewById(R.id.parentLayout);
        alphaLayout = rootView.findViewById(R.id.alpha);

        alphaLayout.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dismiss();
            }
        });
        setView(rootView);
        setCancelable(true);
        setOnShowListener(this);
    }


    @Override public void onShow(final DialogInterface dialog) {
        int color = ContextCompat.getColor(mActivity, R.color.text_primary_light);
        color = Utils.changeAlpha(color, 0.4f);

        alphaLayout.setBackgroundColor(color);
        ObjectAnimator y = ObjectAnimator.ofFloat(parentLayout, "y", parentLayout.getBottom(), parentLayout.getY());
        y.setInterpolator(new FastOutSlowInInterpolator());
        y.setDuration(350);
        y.start();

        AlphaAnimation anim = new AlphaAnimation(0, 1f);
        anim.setDuration(200);
        anim.setFillAfter(true);
        alphaLayout.startAnimation(anim);

        sbc = mActivity.getWindow().getStatusBarColor();
        mActivity.getWindow().setStatusBarColor(color);
        setclicks();
    }

    @Override public void dismiss() {

        ObjectAnimator y = ObjectAnimator.ofFloat(parentLayout, "y", parentLayout.getY(), parentLayout.getBottom());
        y.setInterpolator(new FastOutSlowInInterpolator());
        y.setDuration(350);
        y.start();

        AlphaAnimation anim = new AlphaAnimation(1f, 0);
        anim.setDuration(200);
        anim.setFillAfter(true);
        alphaLayout.startAnimation(anim);
        mActivity.getWindow().setStatusBarColor(sbc);

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                SortDialog.super.dismiss();
            }
        };
        new Handler().postDelayed(mRunnable, 350);

    }

    private void setclicks() {
        rootView.findViewById(R.id.llAlbum).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.selected(SortTypes.ALBUM);
                dismiss();
            }
        });
        rootView.findViewById(R.id.llArtist).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.selected(SortTypes.ARTIST);
                dismiss();
            }
        });
        rootView.findViewById(R.id.llFavourites).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.selected(SortTypes.FAVORITS);
                dismiss();
            }
        });
        rootView.findViewById(R.id.llTitle).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.selected(SortTypes.TITLE);
                dismiss();
            }
        });
        rootView.findViewById(R.id.llFolder).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.selected(SortTypes.FOLDER);
                dismiss();
            }
        });
        rootView.findViewById(R.id.llPosition).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.selected(SortTypes.POSITION);
                dismiss();
            }
        });
    }

    public interface Callback {
        void selected(SortTypes type);
    }

}
