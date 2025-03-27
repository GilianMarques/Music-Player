package gilianmarques.dev.musicplayer.activities.edit_lyrics;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.customs.MyFragment;
import gilianmarques.dev.musicplayer.lyrics.models.Phrase;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("SuspiciousMethodCalls")
public class EditLyricFragment extends MyFragment {
    LinearLayout container;
    ArrayList<Phrase> phrases;
    HashMap<Long, PhraseViewHolder> holders = new HashMap<>();
    private String copiedText = "";
    private NestedScrollView scroll;
    private EditLyrics mActivity;

    public static EditLyricFragment newInstance(ArrayList<Phrase> phrases) {
        EditLyricFragment fragment = new EditLyricFragment();
        fragment.phrases = phrases;
        return fragment;

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (EditLyrics) getActivity();
        return inflater.inflate(R.layout.fragment_edit_lyric, container, false);
    }

    @Override protected void init() {

        if (phrases == null) return;
        scroll = findViewById(R.id.scrollM);
        container = findViewById(R.id.container);

        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) container.getLayoutParams();
        p.bottomMargin = (int) (Utils.screenHeight / 2);
        container.setLayoutParams(p);

        for (Phrase phrase : phrases) {
            PhraseViewHolder holder = createView(phrase.getText());
            holder.indexOnLayout = container.getChildCount();
            holder.phraseStart = phrase.getStart();
            holder.phraseEnd = phrase.getEnd();
            container.addView(holder.rootView);
            holder.edt.requestFocus();
            holder.edt.setSelection(holder.edt.getText().toString().length());


        }
        scroll.post(new Runnable() {
            @Override public void run() {
                scroll.setFocusableInTouchMode(true);
                scroll.fullScroll(ScrollView.FOCUS_UP);
            }
        });

    }

    private PhraseViewHolder createView(final String phrase) {
        int index = container.getChildCount();
        final PhraseViewHolder holder = new PhraseViewHolder(mActivity, index + new LocalDateTime().toDate().getTime());
        holder.edt.setText(phrase);

        holders.put(holder.id, holder);

        holder.setCallback(new PhraseViewHolder.Callback() {

            @Override public void addOrPaste(long id) {
                AddOrPaste(holders.get(id));
                fixIndex();
            }

            @Override public void remove(View rootView, long id) {
                holders.remove(id);
                container.removeView(rootView);
                fixIndex();
            }

            @Override public void copy(String text) {

                copiedText += text;
                Toasty.success(mActivity, copiedText, 500).show();
                forcePasteAction();
            }

            @Override public void insert(long id, String newText) {
                PhraseViewHolder from = holders.get(id);
                PhraseViewHolder holder = createView(newText);
                container.addView(holder.rootView, from.indexOnLayout + 1);
                holder.edt.requestFocus();
                holder.edt.setSelection(holder.edt.getText().toString().length());

                fixIndex();

            }

        });

        return holder;
    }

    private void AddOrPaste(PhraseViewHolder phraseViewHolder) {
        PhraseViewHolder holder;

        //paste
        if (!copiedText.isEmpty()) {
            holder = createView(copiedText);

            copiedText = "";
            //add
        } else holder = createView("");

        container.addView(holder.rootView, phraseViewHolder.indexOnLayout + 1);
        holder.edt.requestFocus();
        holder.edt.setSelection(holder.edt.getText().toString().length());

        forceAddAction();

    }


    /**
     * make views show a 'AddOrPaste' icon around them
     */
    private void forcePasteAction() {
        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            PhraseViewHolder holder = holders.get(view.getTag());// tag contains holder id
            if (holder != null) holder.actionPaste();

        }
    }

    /**
     * make views show a "plus" icon around them
     */
    @SuppressWarnings("ConstantConditions")
    private void forceAddAction() {
        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            PhraseViewHolder holder = holders.get(view.getTag());// tag contains holder id
            holder.actionAdd();
        }
    }

    /**
     * save the correct view's posotion on layout
     */
    @SuppressWarnings("ConstantConditions")
    private void fixIndex() {
        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);
            PhraseViewHolder holder = holders.get(view.getTag());// tag contains holder id
            holder.indexOnLayout = i;

        }
    }

    ArrayList<Phrase> finish() {

        ArrayList<Phrase> phrases = new ArrayList<>();

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            PhraseViewHolder holder = holders.get(v.getTag());

            String text = holder.edt.getText().toString();
            if (text.isEmpty()) continue;
            // remove double spaces and space at start/end
            text = text.trim().replaceAll("[ ]{2,}", " ");
            // remove double linebreakers (\n)
            text = text.trim().replaceAll("[\r\n]+", "\n");
            //
            Phrase phrase = new Phrase(text, holder.phraseStart, holder.phraseEnd);
            phrases.add(phrase);
        }
        return phrases;

    }


}
