package gilianmarques.dev.musicplayer.customs;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import gilianmarques.dev.musicplayer.sorting.utils.SortTypes;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {
    protected View rootView;
    protected Activity mActivity;

    public MyFragment() {
        // Required empty public constructor
    }


    @CallSuper
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rootView = view;
        mActivity = getActivity();
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    public final <T extends View> T findViewById(@IdRes int id) {
        return this.rootView.findViewById(id);
    }

    protected void init() {

    }


    public void sort(SortTypes type) {

    }
}
