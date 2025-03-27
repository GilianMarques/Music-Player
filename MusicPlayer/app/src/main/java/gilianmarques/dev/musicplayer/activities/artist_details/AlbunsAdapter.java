package gilianmarques.dev.musicplayer.activities.artist_details;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.album_details.AlbumDetailsActivity;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Artist;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeAlbuns;

class AlbunsAdapter extends AnimatedRvAdapter {
    private ArrayList<Album> mAlbums = new ArrayList<>();
    private final Activity mActivity;
    private final Artist mArtist;

    public AlbunsAdapter(Artist mArtist, Activity mActivity) {
        this.mActivity = mActivity;
        this.mArtist = mArtist;
        update();
    }

    private void update() {
        mAlbums = new NativeAlbuns().getAlbumByArtist(mArtist.getId());
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mActivity.getLayoutInflater().inflate(R.layout.view_album_small, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final MyViewHolder mHolder = (MyViewHolder) holder;
        final Album mAlbum = mAlbums.get(position);
        mHolder.tvName.setText(mAlbum.getName());
        Picasso.get().load(mAlbum.getURI()).resize(200, 200).placeholder(R.drawable.no_art_background).into(mHolder.ivAlbumArt);

        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(mActivity, AlbumDetailsActivity.class);

                String ivAlbumArtTrasitionName = "ivAlbumArt_" + holder.getAdapterPosition();

                mHolder.ivAlbumArt.setTransitionName(ivAlbumArtTrasitionName);

                mIntent.putExtra("ivAlbumArt_transName", ivAlbumArtTrasitionName);
                mIntent.putExtra("album", mAlbum.getId());

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                        mActivity,
                        Pair.create(((View) mHolder.ivAlbumArt), ivAlbumArtTrasitionName));


                mActivity.startActivity(mIntent, options.toBundle());

            }
        };


        mHolder.ivAlbumArt.setOnClickListener(mOnClickListener);

        super.onBindViewHolder(mHolder, position);
    }


    @Override
    public int getItemCount() {
        return mAlbums.size();
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivAlbumArt;
        final TextView tvName;


        MyViewHolder(View itemView) {
            super(itemView);
            ivAlbumArt = itemView.findViewById(R.id.iv_art);
            tvName = itemView.findViewById(R.id.tv_name);

        }
    }


}
