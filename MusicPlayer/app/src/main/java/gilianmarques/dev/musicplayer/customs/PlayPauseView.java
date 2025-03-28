package gilianmarques.dev.musicplayer.customs;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques em 01/06/2018 as 21:35:39.
 */
public class PlayPauseView extends FrameLayout {

    private static final Property<PlayPauseView, Integer> COLOR =
            new Property<PlayPauseView, Integer>(Integer.class, "color") {
                @Override
                public Integer get(PlayPauseView v) {
                    return v.getColor();
                }

                @Override
                public void set(PlayPauseView v, Integer value) {
                    v.setColor(value);
                }
            };

    private static final long PLAY_PAUSE_ANIMATION_DURATION = 200;

    private final PlayPauseDrawable mDrawable;
    private final Paint mPaint = new Paint();
    private final int mPauseBackgroundColor;
    private final int mPlayBackgroundColor;

    private AnimatorSet mAnimatorSet;
    private int mBackgroundColor;
    private int mWidth;
    private int mHeight;

    public PlayPauseView(Context context) {
        super(context);
        mPlayBackgroundColor = Utils.fetchColorFromReference(R.attr.colorAccent);
        mPauseBackgroundColor = Utils.fetchColorFromReference(R.attr.colorPrimary);
        mDrawable = new PlayPauseDrawable();

        init(context);
    }

    public PlayPauseView(Context context, AttributeSet attrs) {
        super(context, attrs);


        mPlayBackgroundColor = Utils.fetchColorFromReference(R.attr.colorAccent);
        mPauseBackgroundColor = Utils.fetchColorFromReference(R.attr.colorPrimaryDark);
        mDrawable = new PlayPauseDrawable();

        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mDrawable.setCallback(this);

        mBackgroundColor = mPlayBackgroundColor;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.isPlay = mDrawable.isPlay();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        initStatus(ss.isPlay);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        final int size = Math.min(width, height);
        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        );
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawable.setBounds(0, 0, w, h);
        mWidth = w;
        mHeight = h;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            setClipToOutline(true);
        }
    }

    private void setColor(int color) {
        mBackgroundColor = color;
        invalidate();
    }

    private int getColor() {
        return mBackgroundColor;
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == mDrawable || super.verifyDrawable(who);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mBackgroundColor);
        final float radius = Math.min(mWidth, mHeight) / 2f;
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, radius, mPaint);
        mDrawable.draw(canvas);
    }

    /**
     * Change status to play or pause
     *
     * @param isPlay   for playing, false else
     * @param withAnim false to change status without animation
     */
    public void change(boolean isPlay, boolean withAnim) {
        if (mDrawable.isPlay() == isPlay)
            return;
        toggle(withAnim);
    }

    /**
     * Toogle the play/pause status
     *
     * @param withAnim false to change status without animation
     */
    private void toggle(boolean withAnim) {
        if (withAnim) {
            if (mAnimatorSet != null) {
                mAnimatorSet.cancel();
            }

            mAnimatorSet = new AnimatorSet();
            final boolean isPlay = mDrawable.isPlay();
            final ObjectAnimator colorAnim = ObjectAnimator.ofInt(this, COLOR, isPlay ? mPauseBackgroundColor : mPlayBackgroundColor);
            colorAnim.setEvaluator(new ArgbEvaluator());
            final Animator pausePlayAnim = mDrawable.getPausePlayAnimator();
            mAnimatorSet.setInterpolator(new DecelerateInterpolator());
            mAnimatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
            mAnimatorSet.playTogether(colorAnim, pausePlayAnim);
            mAnimatorSet.start();
        } else {
            final boolean isPlay = mDrawable.isPlay();
            initStatus(!isPlay);
            invalidate();
        }
    }

    private void initStatus(boolean isPlay) {
        if (isPlay) {
            mDrawable.setPlay();
            setColor(mPlayBackgroundColor);
        } else {
            mDrawable.setPause();
            setColor(mPauseBackgroundColor);
        }
    }

    private static class SavedState extends BaseSavedState {
        boolean isPlay;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isPlay = in.readByte() > 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte(isPlay ? (byte) 1 : (byte) 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}

