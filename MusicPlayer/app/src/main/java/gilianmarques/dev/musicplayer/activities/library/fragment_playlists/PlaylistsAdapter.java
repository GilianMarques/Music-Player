package gilianmarques.dev.musicplayer.activities.library.fragment_playlists;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Playlist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques em 08/05/2018 as 21:03:30. reutilizado em 14/04/2019
 */
class PlaylistsAdapter extends AnimatedRvAdapter {
    private ArrayList<Playlist> playlists;
    private Callback callback;
    private LayoutInflater inflater;

    PlaylistsAdapter(Activity mActivity) {
        inflater = mActivity.getLayoutInflater();
        playlists = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(inflater.inflate(R.layout.view_rv_item_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        final MyViewHolder mHolder = (MyViewHolder) holder;
        final Playlist pList = playlists.get(position);

        String msg = Utils.format(Utils.toPlural(pList.size(), R.plurals.Faixas), String.valueOf(pList.size())).concat(" | ").concat(pList.getFormatedDuration());

        mHolder.tvName.setText(pList.getName());
        mHolder.tvTotalTracks.setText(msg);

        if (pList.livePlaylist()) mHolder.ivLive.setVisibility(View.VISIBLE);
        else mHolder.ivLive.setVisibility(View.INVISIBLE);
        Track mTrack = pList.getFirstTrack();
        if (mTrack != null) mHolder.uri = mTrack.getAlbum().getURI();
        else mHolder.uri = null;
        mHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.onClick(mHolder);
            }
        });
        super.onBindViewHolder(mHolder, position);

    }

    @Override public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder rvHolder) {
        final MyViewHolder holder = (MyViewHolder) rvHolder;
        //  Log.d(App.myFuckingUniqueTAG + "PlaylistsAdapter", "onViewAttachedToWindow: " + holder.tvName.getText().toString());
        Picasso.get().load((holder.uri != null ? holder.uri.toString() : "empity_uri_to_load_def_art")).resize(320, 320).placeholder(R.drawable.no_art_background).error(R.drawable.no_art_background).into(holder.ivArt, new com.squareup.picasso.Callback() {
            @Override public void onSuccess() {
                callback.firstArtLoaded(Utils.getBitmap(holder.ivArt));

            }

            @Override public void onError(Exception e) {
                onSuccess();
            }
        });

        //super.onViewAttachedToWindow(rvHolder);
    }

    @Override public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder rvHolder) {
        final MyViewHolder holder = (MyViewHolder) rvHolder;
        // Log.d(App.myFuckingUniqueTAG + "PlaylistsAdapter", "onViewAttachedToWindow: " + holder.tvName.getText().toString());
        //  super.onViewDetachedFromWindow(holder);
    }


    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void update(ArrayList<Playlist> tracks) {
        playlists.clear();
        playlists.addAll(tracks);
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Playlist getList(int adapterPosition) {
        return playlists.get(adapterPosition);
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivArt;
        final ImageView ivLive;
        final RelativeLayout container;
        final TextView tvName;
        final TextView tvTotalTracks;
        public Uri uri;
        final CardView cv;

        MyViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            ivArt = itemView.findViewById(R.id.iv_art);
            ivLive = itemView.findViewById(R.id.ivLive);
            container = itemView.findViewById(R.id.container);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTotalTracks = itemView.findViewById(R.id.tv_total_trakcs);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cv.getLayoutParams();
            params.width = (int) (Utils.screenWidth / 6 * 5);
            params.height = (int) (Utils.screenHeight / 4 * 3);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            cv.setLayoutParams(params);
        }
    }


    /**
     * CallbackI pra notificar a activity  das ações feitas nas view do recyclerView
     */
    public static class Callback {

        public void onClick(MyViewHolder position) {
        }

        public void onItemChanged(Playlist list, Album mAlbum) {

        }

        public void firstArtLoaded(Bitmap art) {

        }
    }


}




































