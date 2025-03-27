package gilianmarques.dev.musicplayer.activities.library.fragment_playlists.search;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.AdapterUtils;

/**
 * Criado por Gilian Marques em 08/05/2018 as 21:03:30. reutilizado em 25/04/2019
 */
class SearchTracksAdapter extends AnimatedRvAdapter {
    private ArrayList<Track> mTracks;
    private Callback callback;
    private LayoutInflater inflater;
    private ArrayList<Long> selected;
    private LottieComposition animation;

    SearchTracksAdapter(Activity mActivity) {
        selected = new ArrayList<>();
        inflater = mActivity.getLayoutInflater();
        mTracks = new ArrayList<Track>();
        LottieComposition.Factory.fromAssetFileName(mActivity, "anim/selected.json", new OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(@Nullable final LottieComposition composition) {
                animation = composition;
            }
        });
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
        Album mAlbum = mTrack.getAlbum();
        mHolder.tvTrackArtist.setText(mAlbum.getName());
        Picasso.get().load(mAlbum.getURI()).resize(200, 200).placeholder(R.drawable.no_art_background).into(mHolder.ivAlbumArt);

        mHolder.setSelected(selected.contains(mTrack.getId()), false, false);

        mHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (selected.contains(mTrack.getId())) {
                    selected.remove(mTrack.getId());
                    mHolder.setSelected(false, true, true);

                } else {
                    selected.add(mTrack.getId());
                    mHolder.setSelected(true, true, true);

                }
                callback.onTrackSelected(selected.size());
            }
        });


        AdapterUtils.changeBackground(mHolder.itemView, position);
        super.onBindViewHolder(mHolder, position);
    }


    @Override
    public int getItemCount() {
        return mTracks.size();
    }

    public void update(ArrayList<Track> tracks) {
        mTracks.clear();
        mTracks.addAll(tracks);
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public ArrayList<Long> getSelection() {
        return selected;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivAlbumArt;
        private final ImageView ivMenu;
        final TextView tvTrackTitle;
        final TextView tvTrackArtist;
        final LottieAnimationView animView;
        final RelativeLayout selectContainer;

        MyViewHolder(View itemView) {
            super(itemView);
            selectContainer = itemView.findViewById(R.id.selectContainer);
            animView = itemView.findViewById(R.id.animation_view);
            ivAlbumArt = itemView.findViewById(R.id.iv_art);
            ivMenu = itemView.findViewById(R.id.iv_menu);
            tvTrackTitle = itemView.findViewById(R.id.tv_track_title);
            tvTrackArtist = itemView.findViewById(R.id.tv_track_artist);
            ivMenu.post(new Runnable() {
                @Override public void run() {
                    ivMenu.setVisibility(View.INVISIBLE);
                }
            });

        }


        void setSelected(boolean selected, boolean animateDesselect, boolean animateSelect) {

            animView.setMinAndMaxProgress(0.1f, 0.7f);

            animView.setComposition(animation);


            if (selected) {
                if (animateSelect) {
                    animView.setSpeed(1.5f);
                    YoYo.with(Techniques.FadeIn).duration(150).playOn(selectContainer);
                } else {
                    YoYo.with(Techniques.FadeIn).duration(1).playOn(selectContainer);
                    animView.setSpeed(10);
                }

                animView.playAnimation();

            } else {

                if (animateDesselect) {
                    animView.setSpeed(-3);
                    animView.playAnimation();
                    YoYo.with(Techniques.FadeOut).delay(400).duration(150).playOn(selectContainer);

                } else {
                    YoYo.with(Techniques.FadeOut).duration(1).playOn(selectContainer);
                }

            }

        }
    }


    /**
     * CallbackI pra notificar a activity  das ações feitas nas view do recyclerView
     */
    public static class Callback {

        public void onTrackSelected(int totalSelection) {
        }


    }
}