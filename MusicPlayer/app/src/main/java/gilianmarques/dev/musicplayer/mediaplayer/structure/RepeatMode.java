package gilianmarques.dev.musicplayer.mediaplayer.structure;

/**
 * Criado por Gilian Marques
 * Terça-feira, 28 de Maio de 2019  as 20:43:37.
 */

public enum RepeatMode {

    repeat_disabled(0), repeat_one(1), repeat_all(2);

    final int value;

    RepeatMode(int value) {
        this.value = value;
    }

    public String getValue() {
        if (value == 0) return "not repeating";
        if (value == 1) return "repeat one";
        if (value == 2) return "repeat all";
        return "?";
    }


}
