package gilianmarques.dev.musicplayer.adapters.dynamic;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Criado por Gilian Marques em 31/05/2018 as 14:26:56.
 */
public class RecyclerViewDragEventsHelper extends ItemTouchHelper.Callback {

    private final MovementsCallback callback;
    private final boolean vertMoves;
    private final boolean horMoves;

    public RecyclerViewDragEventsHelper(MovementsCallback callback, boolean vertMoves, boolean horMoves) {
        this.callback = callback;
        this.vertMoves = vertMoves;
        this.horMoves = horMoves;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.END | ItemTouchHelper.START;

        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        callback.onMove(viewHolder, target);

        return true;
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return vertMoves;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return horMoves;
    }




}

