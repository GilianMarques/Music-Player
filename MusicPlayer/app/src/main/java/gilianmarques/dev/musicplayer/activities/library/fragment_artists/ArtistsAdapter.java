package gilianmarques.dev.musicplayer.activities.library.fragment_artists;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.artist_details.ArtistDetailsActivity;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.models.Artist;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.spotify.fetcher.ArtistFetcher;
import gilianmarques.dev.musicplayer.utils.AdapterUtils;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.Realm;

/**
 * Criado por Gilian Marques em 05/05/2018 as 20:08:39.
 */
public class ArtistsAdapter extends AnimatedRvAdapter {
    private final ArrayList<Artist> mArtists;
    private final Activity mActivity;


    public ArtistsAdapter(ArrayList<Artist> mArtists, Activity mActivity, int spanCount) {
        this.mArtists = mArtists;
        this.mActivity = mActivity;
        int spanCount1 = spanCount;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(mActivity.getLayoutInflater().inflate(R.layout.view_artist, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        final MyViewHolder mHolder = (MyViewHolder) holder;
        final Artist mArtist = mArtists.get(position);

        mHolder.p = holder.getAdapterPosition();
        mArtist.addLocalInfo();

        //will check if local info must be downloaded or  updated

        if (!mArtist.shouldRefetch() && mArtist.isInfoFetched()) loadArt(mHolder, mArtist);
        else if (Utils.canConnect()) new ArtistFetcher()
                .fetchArtURL(mArtist.getName(), new ArtistFetcher.Callback() {
                    @Override public void onFetch(String url, boolean fetchSuccess) {
                        if (!url.isEmpty()) {
                            mArtist.setInfoFetched(true);
                            mArtist.setLastFetch(System.currentTimeMillis());
                            mArtist.setUrl(url);
                            UIRealm.get().executeTransaction(new Realm.Transaction() {
                                @Override public void execute(@NonNull Realm realm) {
                                    realm.insertOrUpdate(mArtist);
                                }
                            });

                        }

                        loadArt(mHolder, mArtist);
                    }
                });
        else loadArt(mHolder, mArtist);


        mHolder.tvName.setText(mArtist.getName());
        mHolder.tvTags.setText(mArtist.getInfo());

        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mIntent = new Intent(mActivity, ArtistDetailsActivity.class);
                mIntent.putExtra("a_id", mArtists.get(mHolder.getAdapterPosition()).getId());
                mActivity.startActivity(mIntent);

            }
        };


        mHolder.cv.setOnClickListener(mOnClickListener);

        super.onBindViewHolder(holder, position);

    }

    private void loadArt(MyViewHolder mHolder, Artist mArtist) {
        Picasso.get().load(mArtist.getUrl())
                .resize(450, 450)
                .placeholder(R.drawable.no_art_artist_dark)
                .error(R.drawable.no_art_artist_dark)
                .into(mHolder.ivArtistArt);
    }


    @Override
    public int getItemCount() {
        return mArtists.size();
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivArtistArt;
        final TextView tvTags;
        final TextView tvName;
        final CardView cv;
        int p;

        MyViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            ivArtistArt = itemView.findViewById(R.id.iv_art);
            tvTags = itemView.findViewById(R.id.tv_tags);
            tvName = itemView.findViewById(R.id.tv_name);
            AdapterUtils.adaptArtistView(itemView.findViewById(R.id.adaptView));
        }

    }

}
