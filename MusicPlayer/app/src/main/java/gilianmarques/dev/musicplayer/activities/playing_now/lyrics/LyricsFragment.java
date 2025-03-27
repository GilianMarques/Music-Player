package gilianmarques.dev.musicplayer.activities.playing_now.lyrics;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.sync_lyrics.SyncLyric;
import gilianmarques.dev.musicplayer.adapters.AnimatedRvAdapter;
import gilianmarques.dev.musicplayer.customs.MyFragment;
import gilianmarques.dev.musicplayer.lyrics.models.Lyric;
import gilianmarques.dev.musicplayer.lyrics.models.Phrase;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 *
 * @Since 0.1 Beta
 */
public class LyricsFragment extends MyFragment {

    private RecyclerView mRecyclerView;

    private Lyric mLyric;

    private Typeface font;
    private TextView tvLrc;
    private int centerX, centerY;

    private boolean showingTranslation;


    public LyricsFragment() {
    }

    public static LyricsFragment newInstance(Activity mActivity) {
        LyricsFragment frag = new LyricsFragment();
        frag.font = Typeface.createFromAsset(mActivity.getAssets(), "fonts/Product Sans Bold.ttf");
        frag.showingTranslation = Prefs.getBoolean(c.show_translation, false);

        Log.d("LyricsFragment", "newInstance: New  Instance CREATED");
        return frag;
    }

    //
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lyrics, container, false);
    }

    @Override protected void init() {
        mRecyclerView = findViewById(R.id.scroll);
        mRecyclerView.post(new Runnable() {
            @Override public void run() {
                centerX = mRecyclerView.getMeasuredWidth() / 2;
                centerY = mRecyclerView.getMeasuredHeight() / 2;
            }
        });
        mRecyclerView.addOnScrollListener(new MyCustomScrollListener());
        mRecyclerView.setLayoutManager(new MyLayoutManager(mActivity));
        mRecyclerView.setHasFixedSize(true);
        tvLrc = findViewById(R.id.tvLrc);
        ImageView ivSync = findViewById(R.id.ivSync);
        ivSync.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(mActivity, SyncLyric.class));
            }
        });
    }

    public void setLyric(Lyric mLyric) {
        this.mLyric = mLyric;
        if (mActivity == null || mLyric == null) {

            if (showingTranslation) tvLrc.setText(getString(R.string.Traducao_indisponivel_para));
            else tvLrc.setText(getString(R.string.Letra_indisponivel_para));


            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            tvLrc.setText("");
            loadViews();
        }
    }


    private void loadViews() {
        if (centerX == 0) {
            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                    loadViews();
                }
            };
            new Handler().postDelayed(mRunnable, 500);
            return;
        }
        mRecyclerView.setVisibility(View.INVISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        Adapter mAdapter = new Adapter((showingTranslation ? mLyric.getTranslation() : mLyric.getOriginal()));
        mRecyclerView.setAdapter(mAdapter);

        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);

    }

    @Override public void onDetach() {

        super.onDetach();
    }


    public View getRootView() {
        return rootView;
    }


    class Adapter extends AnimatedRvAdapter {
        final ArrayList<Phrase> mPhrases = new ArrayList<>();
        final int padding = (int) Utils.toPX(24);
        final int paddingTop = (centerY / 10) * 12;
        final int paddingBottom = centerY;

        Adapter(ArrayList<Phrase> p) {
            mPhrases.clear();
            mPhrases.addAll(p);
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView mTextView = new TextView(mActivity);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mTextView.setLayoutParams(lp);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setLineSpacing(1.3f, 1.3f);
            mTextView.setPadding(padding, padding / 2, padding, padding / 2);
            mTextView.setTypeface(font);
            return new MyViewHolder(mTextView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder mHolder, int position) {

            TextView mTextView = ((MyViewHolder) mHolder).mTextView;
            Phrase mPhrase = mPhrases.get(position);
            mTextView.setTag(mPhrase.getStart());

            mTextView.setText(mPhrase.getText());

            if (mPhrase.getText().isEmpty()) mTextView.setVisibility(View.GONE);
            else mTextView.setVisibility(View.VISIBLE);


            if (position == 0) {
                mTextView.setPadding(padding, paddingTop, padding, padding);
            } else if (position == mPhrases.size() - 1) {
                mTextView.setPadding(padding, padding, padding, paddingBottom);
            } else if (mTextView.getPaddingTop() == paddingTop || mTextView.getPaddingBottom() == paddingBottom)
                mTextView.setPadding(padding, padding / 2, padding, padding / 2);

            mTextView.setTextColor(Color.WHITE);
            super.onBindViewHolder(mHolder, position);
        }

        @Override public int getItemCount() {
            return mPhrases.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            final TextView mTextView;

            MyViewHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView;
            }
        }
    }


}
