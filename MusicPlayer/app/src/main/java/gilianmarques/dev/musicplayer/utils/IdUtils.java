package gilianmarques.dev.musicplayer.utils;

import android.util.Base64;

import java.util.ArrayList;
import java.util.Random;

/**
 * Criado por Gilian Marques em 12/04/2017.
 */

public class IdUtils {


    public static String createIdForTrack(String path) {
        String decoded = Base64.encodeToString(path.getBytes(), Base64.DEFAULT);
        return decoded.replaceAll("[^A-Za-z0-9]", "");
    }

    public static String removeAllButLetterAndNumber(String text) {
        return text.replace(" ","SppAaCEe").replaceAll("[^A-Za-z0-9]", "").replace("SppAaCEe"," ");
    }


    public static long createObjectId() {

  return getLongRandom(99999L, 999999999999999999L);

    }
    public static String createStringObjectId() {

        ArrayList<Integer> numeros = new ArrayList<>();
        while (numeros.size() < 2) {
            numeros.add(getRandom(1000, 9999));
        }

        String strNumeros = numeros.toString().concat(String.valueOf(System.currentTimeMillis()));

        String[] id = removerCaracteresProibidos(strNumeros).replaceAll("[^0-9]", "").split("");

        String strId = getLetraAleatoria();

        int letra = 5;
        for (int i = 0; i < id.length; i++) {

            if (i > 1 && i % letra == 0) {
                strId = strId.concat(getLetraAleatoria() + id[i]);
                letra = getRandom(2, 5);
            } else strId = strId.concat(id[i]);
        }


        return strId;

    }

    private static int getRandom(int minimo, int maximo) {
        int r = new Random().nextInt(maximo + 1);
        return r >= minimo ? r : getRandom(minimo, maximo);
    }

    private static long getLongRandom(long minimo, long maximo) {
        long r = new Random().nextLong();
        return (r < minimo || r > maximo) ? getLongRandom(minimo, maximo) : r;
    }

    private static String getLetraAleatoria() {
        boolean maiuscula = (getRandom(1, 2) % 2 == 0);
        ArrayList<String> letras = new ArrayList<>();
        letras.add("a");

        letras.add("b");
        letras.add("c");
        letras.add("d");
        letras.add("e");
        letras.add("f");

        letras.add("g");
        letras.add("h");
        letras.add("i");
        letras.add("j");
        letras.add("k");

        letras.add("l");
        letras.add("m");
        letras.add("n");
        letras.add("o");
        letras.add("p");

        letras.add("q");
        letras.add("r");
        letras.add("s");
        letras.add("t");
        letras.add("u");

        letras.add("v");
        letras.add("w");
        letras.add("x");
        letras.add("y");
        letras.add("z");
        String c = letras.get(new Random().nextInt(26));
        return maiuscula ? c.toUpperCase() : c;

    }

    private static String removerCaracteresProibidos(String nome) {
        if (nome == null || nome.isEmpty()) return nome;
        return nome.replace(".", "-")
                .replace("#", "")
                .replace("$", "")
                .replace("[", "")
                .replace("]", "")
                .replace("/", "");
    }
}
