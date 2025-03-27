package gilianmarques.dev.musicplayer.sorting.utils;

/**
 * Criado por Gilian Marques
 * Domingo, 26 de Maio de 2019  as 12:42:43.
 */

public enum SortTypes {
    TITLE(1), ALBUM(2), ARTIST(3),
    POSITION(4), TEMP_POSITION(5),
    DATE(6), FOLDER(7), FAVORITS(8), DEFAULT_(TITLE.value);

    public final int value;

    SortTypes(int value) {
        this.value = value;
    }

    public static SortTypes toSortingType(int st) {
        if (st == TITLE.value) return TITLE;
        else if (st == ALBUM.value) return ALBUM;
        else if (st == ARTIST.value) return ARTIST;
        else if (st == POSITION.value) return POSITION;
        else if (st == TEMP_POSITION.value) return TEMP_POSITION;
        else if (st == DATE.value) return DATE;
        else if (st == FOLDER.value) return FOLDER;
        else if (st == FAVORITS.value) return FAVORITS;
        else return DEFAULT_;
    }

    public String getValue() {
        if (value == TITLE.value) return "title";
        else if (value == ALBUM.value) return "album";
        else if (value == ARTIST.value) return "artist";
        else if (value == POSITION.value) return "position";
        else if (value == TEMP_POSITION.value) return "temp_pos";
        else if (value == DATE.value) return "date";
        else if (value == FOLDER.value) return "folder";
        else if (value == FAVORITS.value) return "favorites";

        else return "?";
    }


}