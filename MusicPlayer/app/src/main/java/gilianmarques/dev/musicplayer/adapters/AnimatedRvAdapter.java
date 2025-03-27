package gilianmarques.dev.musicplayer.adapters;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * Criado por Gilian Marques
 * Terça-feira, 31 de Julho de 2018  as 13:18:06.
 *
 * @Since 0.3 Beta
 */
public class AnimatedRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int lasPosition;

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    @CallSuper
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder mHolder, final int position) {

        //descendo
        if (position > lasPosition) {
            YoYo.with(Techniques.FadeInUp).duration(300).playOn(mHolder.itemView);
        } else {
            YoYo.with(Techniques.FadeInDown).duration(300).playOn(mHolder.itemView);
        }
        lasPosition = position;
    }

    @Override public int getItemCount() {
        return 0;
    }


}
