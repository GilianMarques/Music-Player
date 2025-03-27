package gilianmarques.dev.musicplayer.activities.playing_now.lyrics;


import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques
 * Sábado, 14 de Julho de 2018  as 10:51:41.
 *
 * @Since 0.1 Beta
 */
public class MyLayoutManager extends LinearLayoutManager {
    private final Context mContext;
    private static  float millisByInch = 250f;

    public MyLayoutManager(Context context) {
        super(context);
        mContext = context;
    }

    public MyLayoutManager(Context context, float millisByInch) {
        super(context);
        mContext = context;
        this.millisByInch = millisByInch;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {

        //Create your RecyclerView.SmoothScroller instance? Check.
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(mContext) {

            //Automatically implements this method on instantiation.
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                //What is PointF? A class that just holds two float coordinates.
                //Accepts a (x , y)
                //for y: use -1 for up direction, 1 for down direction.
                //for x (did not test): use -1 for left direction, 1 for right
                //direction.
                //We let our custom LinearLayoutManager calculate PointF for us
                return MyLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override public int calculateDyToMakeVisible(View view, int snapPreference) {
                Log.d("MyLayoutManager", "calculateDyToMakeVisible: " + snapPreference);
                int offset = (int) ((int) (view.getY() + (view.getMeasuredHeight() / 2)) - (Utils.screenHeight / 2));
                if (offset > 0)
                    return super.calculateDyToMakeVisible(view, snapPreference) - offset;
                else return super.calculateDyToMakeVisible(view, snapPreference) + (offset * -1);
            }

            //The holy grail of smooth scrolling
            //returns the milliseconds it takes to scroll one pixel.
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return millisByInch / displayMetrics.densityDpi;
            }

        };

        //Docs do not tell us anything about this,
        //but we need to set the position we want to scroll to.
        smoothScroller.setTargetPosition(position);

        //Call startSmoothScroll(SmoothScroller)? Check.
        startSmoothScroll(smoothScroller);
    }


}
