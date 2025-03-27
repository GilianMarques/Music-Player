package gilianmarques.dev.musicplayer.lyrics.models;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Criado por Gilian Marques
 * Domingo, 01 de Julho de 2018  as 20:35:41.
 *
 * @Since 0.1 Beta
 */
public class Lyric {
    public static final int FONT_VAGALUME = -15;
    private int font;
    private String URL;
    private boolean originalSynced;
    private ArrayList<Phrase> original = new ArrayList<>();
    private ArrayList<Phrase> translation = new ArrayList<>();

    public Lyric(ArrayList<Phrase> original, ArrayList<Phrase> translation) {
        this.original = original;
        this.translation = translation;
    }

    private Lyric() {
    }

    private String getURL() {
        return URL;
    }

    public ArrayList<Phrase> getOriginal() {
        return original;
    }

    public ArrayList<Phrase> getTranslation() {
        return translation;
    }


    private int getFont() {
        return font;
    }

    public void setFont(int font) {
        this.font = font;
    }

    private void setURL(String URL) {
        this.URL = URL;
    }

    public boolean isSynced() {
        return originalSynced;
    }

    public void setOriginalSynced(boolean originalSynced) {
        this.originalSynced = originalSynced;
    }

    public static Lyric from(String url, String original, String translated) {
        if (url == null || url.isEmpty()) return null;
        if (original == null || original.isEmpty()) return null;

        ArrayList<Phrase> translation = new ArrayList<>();
        ArrayList<Phrase> originals = new ArrayList<>();

        // usually translations came with [name of the music translated] at begginnig of text
        if (translated.startsWith("["))
            translated = translated.substring(translated.indexOf("]") + 1);

        // remove double spaces and space at start/end
        original = original.trim().replaceAll("[ ]{2,}", " ");
        translated = translated.trim().replaceAll("[ ]{2,}", " ");

        // remove double linebreakers (\n)
        original = original.trim().replaceAll("[\r\n]+", "\n");
        translated = translated.trim().replaceAll("[\r\n]+", "\n");


        //-------------------Translation first!

        String[] t = translated.split("\n");
        for (String period : t) translation.add(new Phrase(period, 0));


        //-------------------Original Begins!

        String[] o = original.split("\n");
        for (String period : o) originals.add(new Phrase(period, 0));


        Lyric mLyric = new Lyric();
        mLyric.URL = url;
        mLyric.original = originals;
        if (translation.size() > 0) mLyric.translation = translation;
        return mLyric;

    }

    public String asString() {
        return new Gson().toJson(this);
    }

    public static Lyric fromString(String strLyric) {
        return new Gson().fromJson(strLyric, Lyric.class);
    }

    @NonNull
    public Phrase getPhraseByPeriod(long period, boolean fromTranslation) {
        Phrase bestChoice = new Phrase("♩♪♪", 0, 0);

        long lastPhraseEnd = -1, nextPhraseStart = -1;


        for (Phrase phrase : getOriginal()) {

            if (phrase.getEnd() <= period) lastPhraseEnd = phrase.getEnd();
            if (nextPhraseStart == -1 && phrase.getStart() > period)
                nextPhraseStart = phrase.getStart();
            if (phrase.getStart() <= period && phrase.getEnd() >= period) {
                bestChoice = phrase;
                break;
            }
        }

        // I won't return an phrase with text if no phrase was found to the specified period
        // and if the interval between the last phrase and the next one is smaller than 1 sec
        if (bestChoice.getStart() == 0 && bestChoice.getEnd() == 0) {
            if (nextPhraseStart - lastPhraseEnd < 1000) bestChoice = new Phrase("", 0, 0);

        }
        // Log.d(App.myFuckingUniqueTAG + "Lyric", "getPhraseByPeriod:  interval: " + (nextPhraseStart - lastPhraseEnd) + " lastPE: " + lastPhraseEnd + " nextPS: " + nextPhraseStart + " returning: " + bestChoice.getText());

        if (fromTranslation) {
            int index = getOriginal().indexOf(bestChoice);
            if (index > 0 && index < getOriginal().size())
                return getTranslation().get(index);
        }

        return bestChoice;
    }

    public boolean hasTranslation() {
        return getTranslation().size() > 0;
    }

    public void setOriginal(ArrayList<Phrase> original) {
        this.original = original;
    }

    public void setTranslation(ArrayList<Phrase> translation) {
        this.translation = translation;
    }
}
