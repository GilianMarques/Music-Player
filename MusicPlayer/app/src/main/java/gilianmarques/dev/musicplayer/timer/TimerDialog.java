package gilianmarques.dev.musicplayer.timer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.davidmiguel.numberkeyboard.NumberKeyboard;
import com.davidmiguel.numberkeyboard.NumberKeyboardListener;
import com.pixplicity.easyprefs.library.Prefs;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques
 * Domingo, 01 de Agosto de 2018  as 19:43:13.
 *
 * @Since 0.3 Beta
 */
public class TimerDialog extends AlertDialog implements DialogInterface.OnShowListener {

    private Activity mActivity;
    private View rootView;
    private String timer;
    private boolean basedOnMinutes;
    private Animation fadeIn, fadeOut;

    public TimerDialog(Activity mActivity) {
        super(mActivity, R.style.AppThemeDark_Translucent_Full);
        this.mActivity = mActivity;
        setOnShowListener(this);
        fadeIn = AnimationUtils.loadAnimation(mActivity, R.anim.fade_in_down);
        fadeOut = AnimationUtils.loadAnimation(mActivity, R.anim.fade_out_up);
        init();
    }

    private void init() {
        rootView = mActivity.getLayoutInflater().inflate(R.layout.timer_dialog_view, null, false);
        final RelativeLayout parent = rootView.findViewById(R.id.parentLayout);
        final RelativeLayout parent1 = rootView.findViewById(R.id.parent1);
        final RelativeLayout parent2 = rootView.findViewById(R.id.parent2);

        final TextView rbMin = rootView.findViewById(R.id.rbMin);
        final TextView rbTracks = rootView.findViewById(R.id.rbTracks);
        final NumberKeyboard kbView = rootView.findViewById(R.id.kbView);
        final FloatingActionButton fabDone = rootView.findViewById(R.id.fabDone);
        final TextView tv = rootView.findViewById(R.id.tv);
        timer = Prefs.getString(c.last_timer, "");
        tv.setText(timer);
        kbView.setListener(new NumberKeyboardListener() {
            @Override public void onNumberClicked(int i) {
                if (timer.length() == 0 && i == 0) return;
                if (timer.length() < 3) timer += i;
                tv.setText(timer);

            }

            @Override public void onLeftAuxButtonClicked() {
            }

            @Override public void onRightAuxButtonClicked() {
                if (!timer.isEmpty())
                    timer = timer.substring(0, timer.length() - 1);
                tv.setText(timer);
            }
        });

        rbMin.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                basedOnMinutes = true;
                parent.removeView(parent2);
                parent.addView(parent1);
            }
        });

        rbTracks.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                parent.removeView(parent2);
                parent.addView(parent1);
            }
        });

        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (timer.isEmpty()) return;
                done();

            }
        });

        parent.removeView(parent1);
        Utils.applyPadding(rootView.findViewById(R.id.grandPa), true, true);
        setView(rootView);
    }

    private void done() {
        mActivity.startService(new Intent(mActivity, TimerService.class).putExtra("timer", Integer.parseInt(timer)).putExtra("based_on_minutes", basedOnMinutes));

        Prefs.putString(c.last_timer, timer);
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                dismiss();
                Toasty.success(mActivity, mActivity.getString(R.string.O_timer_esta_rodando_verifique)).show();
            }
        };
        new Handler().postDelayed(mRunnable, 500);
    }


    @Override public void dismiss() {
        rootView.findViewById(R.id.cv).startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {

            }

            @Override public void onAnimationEnd(Animation animation) {
                TimerDialog.super.dismiss();

            }

            @Override public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override public void onShow(DialogInterface dialog) {
        rootView.findViewById(R.id.cv).startAnimation(fadeIn);
    }
}
