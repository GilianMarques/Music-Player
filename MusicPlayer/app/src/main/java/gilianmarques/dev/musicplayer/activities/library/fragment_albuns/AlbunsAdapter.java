package gilianmarques.dev.musicplayer.activities.library.fragment_albuns;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.activities.album_details.AlbumDetailsActivity;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.utils.AdapterUtils;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.PaletteUtils;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques em 05/05/2018 as 20:08:39.
 */
public class AlbunsAdapter extends AnimatedRvAdapter {
    private final ArrayList<Album> mAlbums;
    private final Activity mActivity;

    public AlbunsAdapter(ArrayList<Album> mAlbums, Activity mActivity) {
        this.mAlbums = mAlbums;
        this.mActivity = mActivity;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mActivity.getLayoutInflater().inflate(R.layout.view_album, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final MyViewHolder mHolder = (MyViewHolder) holder;
        final Album mAlbum = mAlbums.get(holder.getAdapterPosition());

        mHolder.p = holder.getAdapterPosition();

        Picasso.get().load(mAlbum.getURI())
                .resize(250, 250)
                .placeholder(R.drawable.no_art_background)
                .error(R.drawable.no_art_background)
                .into(mHolder.ivAlbumArt, new Callback() {
                    @Override public void onSuccess() {
                        AlphaAnimation a = new AlphaAnimation(0,1);
                        a.setDuration(200);
                        mHolder.ivAlbumArt.startAnimation(a);
                        PaletteUtils.colorFrom(Utils.getBitmap(mHolder.ivAlbumArt), new PaletteUtils.PaletteCallback() {
                            @Override
                            public void result(int primary, int secundary, int background) {

                                Utils.applyColorOnDrawable(background, MyActivity.darkTheme ? mHolder.ivPlay.getDrawable() : mHolder.ivPlay.getBackground());


                            }
                        });
                    }

                    @Override public void onError(Exception e) {
                        onSuccess();
                    }
                });

        mHolder.tvArtist.setText(mAlbum.getArtist());
        mAlbum.getDuration(mHolder.tvDurr, mActivity);
        mHolder.tvName.setText(mAlbum.getName());

        mHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(mAlbum, mHolder);
            }
        });

        mHolder.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                MusicService.binder.getPlayer().initFromAlbum(mAlbum, 0, false);
                App.binder.get().goToPlayingNow(mActivity);
            }
        });

        super.onBindViewHolder(mHolder, position);
    }


    @Override
    public int getItemCount() {
        return mAlbums.size();
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivAlbumArt, ivPlay;
        final TextView tvArtist, tvDurr;
        final TextView tvName;
        final CardView cv;
        int p;

        MyViewHolder(View itemView) {
            super(itemView);
            tvDurr = itemView.findViewById(R.id.tvDurr);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            cv = itemView.findViewById(R.id.cv);
            ivAlbumArt = itemView.findViewById(R.id.iv_art);
            tvArtist = itemView.findViewById(R.id.tv_artist);
            tvName = itemView.findViewById(R.id.tv_name);
            AdapterUtils.adaptAlbumView(itemView.findViewById(R.id.adaptableView));
        }


    }

    private void startActivity(Album mAlbum, MyViewHolder mHolder) {
        Intent mIntent = new Intent(mActivity, AlbumDetailsActivity.class);
        mIntent.putExtra("album", mAlbum.getId());
        mActivity.startActivity(mIntent);
    }

}
