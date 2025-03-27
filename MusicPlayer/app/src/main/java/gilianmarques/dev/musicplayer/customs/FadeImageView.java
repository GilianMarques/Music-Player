package gilianmarques.dev.musicplayer.customs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wonderkiln.blurkit.BlurKit;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques em 26/05/2018 as 06:13:50.
 */
public class FadeImageView extends FrameLayout {
    private boolean blur;
    private int radius, duration;
    private ImageView ivOne, ivTwo;

    public FadeImageView(Context context) {
        super(context);
    }

    public FadeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }


    public void setImageBitmap(Bitmap map) {
        if (map == null) {
            Log.d(App.myFuckingUniqueTAG + "FadeImageView", "setImageBitmap: Bitmap nulo");
            ivOne.setImageBitmap(null);
            ivTwo.setImageBitmap(null);
            return;
        }
        final Bitmap nBitmap = map.copy(map.getConfig(), true);
        if (blur) BlurKit.getInstance().blur(nBitmap, radius);


        AlphaAnimation anim = new AlphaAnimation(1, 0);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        anim.setDuration(duration);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ivTwo.setImageBitmap(nBitmap);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivOne.setImageBitmap(nBitmap);
                ivOne.setAlpha(1f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        ivOne.startAnimation(anim);
    }


    private void init(AttributeSet attrs, Context context) {
        TypedArray styledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.fiv);
        blur = styledAttributes.getBoolean(R.styleable.fiv_blur, false);
        radius = styledAttributes.getInt(R.styleable.fiv_radius, 8);
        duration = styledAttributes.getInt(R.styleable.fiv_anim_duration, 500);
        styledAttributes.recycle();

        ivOne = new ImageView(context);
        ivOne.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //
        ivTwo = new ImageView(context);
        ivTwo.setScaleType(ImageView.ScaleType.CENTER_CROP);


        addView(ivTwo);
        addView(ivOne);

    }


    public void setBlur(boolean blur) {
        this.blur = blur;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
