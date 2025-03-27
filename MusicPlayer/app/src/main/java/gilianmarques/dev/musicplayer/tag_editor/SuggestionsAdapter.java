package gilianmarques.dev.musicplayer.tag_editor;

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
import gilianmarques.dev.musicplayer.utils.AdapterUtils;


class SuggestionsAdapter extends AnimatedRvAdapter {
    private ArrayList<Suggestion> mSugestions;
    private Callback callback;
    private LayoutInflater inflater;

    SuggestionsAdapter(Activity mActivity) {
        inflater = mActivity.getLayoutInflater();
        mSugestions = new ArrayList<Suggestion>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(inflater.inflate(R.layout.view_suggestion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        final MyViewHolder mHolder = (MyViewHolder) holder;
        final Suggestion mSugestion = mSugestions.get(position);

        String title = mSugestion.title;
        String album = mSugestion.album;
        String artist = mSugestion.artist;

        String text = (title==null ? "" : title + "\n") + (album==null ? "" : album + "\n") + (artist==null ? "" : artist);

        mHolder.tvInfo.setText(text);
        Picasso.get().load(mSugestion.image).placeholder(R.drawable.no_art_background).into(mHolder.ivAlbumArt);

        mHolder.ivAlbumArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onSugestionClicked(mSugestion);
            }
        });


        AdapterUtils.changeBackground(mHolder.itemView, position);

        super.onBindViewHolder(mHolder, position);
    }


    @Override
    public int getItemCount() {
        return mSugestions.size();
    }

    public void update(ArrayList<Suggestion> tracks) {
        mSugestions.clear();
        mSugestions.addAll(tracks);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivAlbumArt;
        final TextView tvInfo;

        MyViewHolder(View itemView) {
            super(itemView);
            ivAlbumArt = itemView.findViewById(R.id.iv_art);
            tvInfo = itemView.findViewById(R.id.tv_name);

        }
    }


    /**
     * CallbackI pra notificar a activity  das ações feitas nas view do recyclerView
     */
    public static class Callback {

        public void onSugestionClicked(Suggestion suggestion) {
        }


    }


}
