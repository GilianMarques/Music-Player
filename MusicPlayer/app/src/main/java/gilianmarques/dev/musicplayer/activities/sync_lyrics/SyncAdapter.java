package gilianmarques.dev.musicplayer.activities.sync_lyrics;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.lyrics.models.Phrase;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques
 * Sábado, 04 de Maio de 2019  as 16:01:13.
 */
public class SyncAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<Phrase> mPhrases = new ArrayList<>();
    private final int padding = (int) Utils.toPX(24);
    private final int largePadding = (int) ((Utils.screenHeight / 2));
    private final Activity mActivity;
    private Typeface font;

    SyncAdapter(ArrayList<Phrase> p, Activity mActivity) {
        this.mActivity = mActivity;
        mPhrases.clear();
        mPhrases.add(0,new Phrase("--- intro ---", 0, 0));
        mPhrases.addAll(p);
        font = Typeface.createFromAsset(mActivity.getAssets(), "fonts/Product Sans Bold.ttf");
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

        TextView mTextView = ((SyncAdapter.MyViewHolder) mHolder).mTextView;
        Phrase mPhrase = mPhrases.get(position);
        mTextView.setTag(mPhrase.getStart());

        mTextView.setText(mPhrase.getText());

        if (mPhrase.getText().isEmpty()) mTextView.setVisibility(View.GONE);
        else mTextView.setVisibility(View.VISIBLE);


        if (position == 0) {
            mTextView.setPadding(padding, largePadding, padding, padding);
        } else if (position == mPhrases.size() - 1) {
            mTextView.setPadding(padding, padding, padding, largePadding);
        } else
            mTextView.setPadding(padding, padding , padding, padding );

        mTextView.setTextColor(Color.WHITE);
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

