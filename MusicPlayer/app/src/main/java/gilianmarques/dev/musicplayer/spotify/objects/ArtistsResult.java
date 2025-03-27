package gilianmarques.dev.musicplayer.spotify.objects;

import java.util.ArrayList;

import gilianmarques.dev.musicplayer.utils.DontObfuscate;

/**
 * Criado por Gilian Marques
 * Quinta-feira, 06 de Junho de 2019  as 19:35:55.
 */
@DontObfuscate

public class ArtistsResult {

    public Artists artists;


    public class Artists {
        public ArrayList<Items> items = new ArrayList<>();
    }


    public class Items {
        public ArrayList<Images> images = new ArrayList<>();
        public String name;
    }


    public class Images {
        public String width;

        public String url;

        public String height;

    }


}
