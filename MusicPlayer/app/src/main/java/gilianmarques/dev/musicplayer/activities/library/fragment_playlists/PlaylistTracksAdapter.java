package gilianmarques.dev.musicplayer.activities.library.fragment_playlists;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.adapters.dynamic.MovementsCallback;
import gilianmarques.dev.musicplayer.adapters.dynamic.RecyclerViewDragEventsHelper;
import gilianmarques.dev.musicplayer.models.Playlist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.utils.MenuActions;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.Realm;

/**
 * Criado por Gilian Marques em 08/05/2018 as 21:03:30.
 */
public class PlaylistTracksAdapter extends AnimatedRvAdapter {
    private final ArrayList<Track> mTracks = new ArrayList<Track>();
    private Activity mActivity;
    private Callback callback;
    private ItemTouchHelper itemTouchHelper;
    private int textPrimary, textSecundary;
    private Playlist playlist;

    public PlaylistTracksAdapter(Activity mActivity, Playlist playlist, RecyclerView mRecyclerView, Callback callback) {
        this.mActivity = mActivity;
        this.playlist = playlist;
        this.callback = callback;
        itemTouchHelper = new ItemTouchHelper(new RecyclerViewDragEventsHelper(dynamicCallback, false, false));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(this);
        textPrimary = ContextCompat.getColor(mActivity, R.color.text_primary_dark);
        textSecundary = textPrimary;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mActivity.getLayoutInflater().inflate(R.layout.view_music_with_label, parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        final MyViewHolder mHolder = (MyViewHolder) holder;
        final Track mTrack = mTracks.get(position);
        Picasso.get().load(mTrack.getAlbum().getURI())
                .resize(200, 200)
                .placeholder(R.drawable.no_art_background).into(mHolder.ivAlbumArt);

        mHolder.tvTrackArtist.setText(mTrack.getAlbumName());
        mHolder.tvTrackTitle.setText(mTrack.getTitle());
        mHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onTrackClicked(holder.getAdapterPosition());
            }
        });
        mHolder.ivLabel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                itemTouchHelper.startDrag(mHolder);
                return true;
            }
        });

        mHolder.ivMenu.setOnClickListener(new PlaylistTracksMenu(mTrack, mHolder.ivAlbumArt, playlist, new MenuActions.ActionsCallback() {
            @Override public void trackRemovedFromPlaylist(Track mTrack) {
                int indexToRemove = 0;

                for (int i = 0; i < mTracks.size(); i++) {
                    if (mTracks.get(i).getId() == (mTrack.getId())) {
                        indexToRemove = i;
                        break;
                    }
                }

                mTracks.remove(indexToRemove);
                notifyItemRemoved(indexToRemove);
                super.trackRemovedFromPlaylist(mTrack);
            }
        }));

        super.onBindViewHolder(mHolder, position);
    }

    @Override
    public int getItemCount() {
        return mTracks.size();
    }


    @SuppressWarnings("FieldCanBeLocal")
    private final MovementsCallback dynamicCallback = new MovementsCallback() {
        @Override
        public void onMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            move(viewHolder, target);
        }

        void move(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

            final int initialPosition = viewHolder.getAdapterPosition();
            final int finalPosition = target.getAdapterPosition();


            if (initialPosition < mTracks.size() && finalPosition < mTracks.size()) {
                if (initialPosition < finalPosition) {
                    for (int i = initialPosition; i < finalPosition; i++) {
                        Collections.swap(mTracks, i, i + 1);
                    }
                } else {
                    for (int i = initialPosition; i > finalPosition; i--) {
                        Collections.swap(mTracks, i, i - 1);
                    }
                }

                notifyItemMoved(initialPosition, finalPosition);

                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        final Track mTrack = mTracks.get(finalPosition);
                        final Track mTrack2 = mTracks.get(initialPosition);
                        mTrack.setIndex(finalPosition);
                        mTrack2.setIndex(initialPosition);
                        UIRealm.get().executeTransaction(new Realm.Transaction() {
                            @Override public void execute(Realm realm) {
                                playlist.updateReferences(mTrack, mTrack2);
                            }
                        });
                    }
                };
                new Thread(mRunnable).start();

            }
        }
    };

    public void update(ArrayList<Track> update) {
        mTracks.clear();
        mTracks.addAll(update);
    }

    public ArrayList<Track> getItens() {
        return mTracks;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivAlbumArt, ivMenu;
        final ImageView ivLabel;
        final TextView tvTrackTitle;
        final TextView tvTrackArtist;

        MyViewHolder(View itemView) {
            super(itemView);
            ivMenu = itemView.findViewById(R.id.iv_menu);
            ivAlbumArt = itemView.findViewById(R.id.iv_art);
            ivLabel = itemView.findViewById(R.id.ivLabel);
            tvTrackTitle = itemView.findViewById(R.id.tv_track_title);
            tvTrackArtist = itemView.findViewById(R.id.tv_track_artist);

            tvTrackTitle.setTextColor(textPrimary);
            tvTrackArtist.setTextColor(textSecundary);
            Utils.applyColorOnDrawable(textSecundary, ivLabel.getDrawable());
            Utils.applyColorOnDrawable(textSecundary, ivMenu.getDrawable());

        }
    }


    /**
     * CallbackI pra notificar a activity  das ações feitas nas view do recyclerView
     */
    public interface Callback {
        void onTrackClicked(int position);
    }


}
