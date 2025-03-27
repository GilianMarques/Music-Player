package gilianmarques.dev.musicplayer.adapters.dynamic;

import android.support.v7.widget.RecyclerView;

/**
 * Criado por Gilian Marques
 * Domingo, 21 de Abril de 2019  as 17:31:08.
 */
public interface MovementsCallback {
    void onMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);

}
