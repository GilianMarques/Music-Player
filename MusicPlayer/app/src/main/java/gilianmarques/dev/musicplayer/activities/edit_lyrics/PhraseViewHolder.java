package gilianmarques.dev.musicplayer.activities.edit_lyrics;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import gilianmarques.dev.musicplayer.R;

/**
 * Criado por Gilian Marques
 * Domingo, 05 de Maio de 2019  as 16:28:27.
 */
public class PhraseViewHolder {
    final ImageView ivCopy, ivRemove, ivAddPasteBottom;
    final EditText edt;
    public long phraseEnd;
    public long phraseStart;
    View rootView;
    long id;
    int indexOnLayout;

    public PhraseViewHolder(Activity mActivity, long id) {
        this.id = id;
        rootView = mActivity.getLayoutInflater().inflate(R.layout.view_phrase, null);
        ivAddPasteBottom = rootView.findViewById(R.id.ivAddPasteBottom);
        ivCopy = rootView.findViewById(R.id.ivCopy);
        ivRemove = rootView.findViewById(R.id.ivRemove);
        edt = rootView.findViewById(R.id.edt);
        rootView.setTag(id);

    }


    public void actionPaste() {

        ivAddPasteBottom.setImageResource(R.drawable.vec_paste_theme);

    }

    public void actionAdd() {

        ivAddPasteBottom.setImageResource(R.drawable.vec_plus_theme);

    }


    public void setCallback(final Callback callback) {


        View.OnClickListener addOrPasteListenerBottom = new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.addOrPaste(id);
            }
        };

        ivAddPasteBottom.setOnClickListener(addOrPasteListenerBottom);

        ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.remove(rootView, id);
            }
        });

        ivCopy.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                callback.copy(edt.getText().toString());
            }
        });

        View.OnLongClickListener lClick = new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                return false;
            }
        };

        rootView.setOnLongClickListener(lClick);
        edt.setOnLongClickListener(lClick);

        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override public void afterTextChanged(Editable s) {
                String text = s.toString();

                if (text.contains("\n")) {

                    if (text.endsWith("\n")) {
                        callback.insert(id, "");
                        edt.setText(text.replace("\n", ""));
                    } else if (text.startsWith("\n")) {
                        edt.setText(text.replace("\n", ""));
                    } else {
                        String[] v = text.split("\n");
                        callback.insert(id, v[1]);
                        edt.setText(v[0]);
                        edt.setSelection(v[0].length() );


                    }

                } else if (text.isEmpty()) {
                    callback.remove(rootView, id);
                }
            }
        });

    }


    public abstract static class Callback {

        public abstract void addOrPaste(long id);

        public abstract void remove(View rootView, long id);

        public abstract void copy(String text);


        public abstract void insert(long id, String newText);
    }
}
