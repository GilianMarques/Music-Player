package gilianmarques.dev.musicplayer.lyrics.models;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Criado por Gilian Marques
 * Domingo, 01 de Julho de 2018  as 20:36:10.
 *
 * @Since 0.1 Beta
 */
public class Phrase {
    private final String text;
    private long start, end;

    public Phrase(String text, long start, long end) {

        this.text = text;
        this.start = start;
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        Log.d("Phrase", "setStart: setting start(" + start + ") for text: \"" + getText() + "\"");
        this.start = start;
    }

    public Phrase(String text, long start) {
        this.text = text;
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @NonNull @Override public String toString() {
        return "\n>" + getText() + "< start: " + getStart() + " end: " + getEnd();
    }
}
