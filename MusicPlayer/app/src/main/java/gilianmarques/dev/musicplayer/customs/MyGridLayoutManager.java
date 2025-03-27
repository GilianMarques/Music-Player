package gilianmarques.dev.musicplayer.customs;

import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques em 09/05/2018 as 15:13:56.
 */
public class MyGridLayoutManager extends android.support.v7.widget.StaggeredGridLayoutManager {


    public MyGridLayoutManager( int childWidth) {
        super(1, VERTICAL);


        int newSpanCount = (int) Math.floor(Utils.screenWidth / childWidth);
        setSpanCount(newSpanCount);
        requestLayout();


    }

    /*  @Override public void addView(final View child) {


        child.post(new Runnable() {
            @Override public void search() {
                int newSpanCount = (int) Math.floor(getWidth() / (float)child.getMeasuredWidth());
                setSpanCount(newSpanCount);
                requestLayout();
            }
        });
        super.addView(child);
    }*/

}
