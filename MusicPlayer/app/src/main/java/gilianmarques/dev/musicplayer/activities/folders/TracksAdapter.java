package gilianmarques.dev.musicplayer.activities.folders;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.AdapterUtils;
import gilianmarques.dev.musicplayer.utils.EasyAsynk;
import gilianmarques.dev.musicplayer.utils.MenuActions;

/**
 * Criado por Gilian Marques em 08/05/2018 as 21:03:30. reutilizado em 14/04/2019
 */
class TracksAdapter extends AnimatedRvAdapter {
    private ArrayList<Track> mTracks;
    private Callback callback;
    private LayoutInflater inflater;

    TracksAdapter(Activity mActivity) {
        inflater = mActivity.getLayoutInflater();
        mTracks = new ArrayList<Track>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(inflater.inflate(R.layout.view_music, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        final MyViewHolder mHolder = (MyViewHolder) holder;
        final Track mTrack = mTracks.get(position);

        mHolder.tvTrackTitle.setText(mTrack.getTitle());

        new EasyAsynk(new EasyAsynk.Actions() {
            private Album mAlbum;

            @Override public int doInBackground() {
                mAlbum = mTrack.getAlbum();
                return super.doInBackground();
            }

            @Override public void onPostExecute() {
                mHolder.tvTrackArtist.setText(mAlbum.getName());
                Picasso.get().load(mAlbum.getURI()).resize(200, 200).placeholder(R.drawable.no_art_background).into(mHolder.ivAlbumArt);
                super.onPostExecute();
            }
        }).execute();

        mHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onTrackClicked(holder.getAdapterPosition());
            }
        });

        mHolder.ivMenu.setOnClickListener(
                new Menu(mTrack, mHolder.ivAlbumArt).setCallback(menuCallback));

        AdapterUtils.changeBackground(mHolder.itemView, position);

        super.onBindViewHolder(mHolder, position);
    }

    private MenuActions.ActionsCallback menuCallback = new MenuActions.ActionsCallback() {
        @Override public void trackRemoved(Track mTrack) {

            int indexToRemove = 0;
            for (int i = 0; i < mTracks.size(); i++) {
                if (mTracks.get(i).getId() == (mTrack.getId())) {
                    indexToRemove = i;
                    break;
                }
            }
            mTracks.remove(indexToRemove);
            notifyItemRemoved(indexToRemove);
            if (callback != null) callback.trackRemoved(mTrack);
            super.trackRemoved(mTrack);

        }
    };

    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    public void update(ArrayList<Track> tracks) {
        mTracks.clear();
        mTracks.addAll(tracks);
        notifyDataSetChanged();
    }

    public TracksAdapter setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivAlbumArt;
        final ImageView ivMenu;
        final TextView tvTrackTitle;
        final TextView tvTrackArtist;

        MyViewHolder(View itemView) {
            super(itemView);
            ivAlbumArt = itemView.findViewById(R.id.iv_art);
            ivMenu = itemView.findViewById(R.id.iv_menu);
            tvTrackTitle = itemView.findViewById(R.id.tv_track_title);
            tvTrackArtist = itemView.findViewById(R.id.tv_track_artist);

        }
    }


    /**
     * CallbackI pra notificar a activity  das ações feitas nas view do recyclerView
     */
    public static class Callback {

        public void onTrackClicked(int position) {
        }

        public void trackRemoved(Track mTrack) {

        }
    }


}
