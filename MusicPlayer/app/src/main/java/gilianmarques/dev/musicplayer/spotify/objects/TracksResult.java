package gilianmarques.dev.musicplayer.spotify.objects;

import java.util.ArrayList;
import java.util.List;

import gilianmarques.dev.musicplayer.utils.DontObfuscate;

/**
 * Criado por Gilian Marques
 * Quarta-feira, 05 de Junho de 2019  as 18:56:45.
 */
@DontObfuscate

public class TracksResult {
    public Tracks tracks;

    public class Tracks {

        public List<Items> items = new ArrayList<>();

    }

    public class Items {
        public Album album;
        public String type;
        public String uri;
        public List<TracksResult.Artists> artists = new ArrayList<>();
        public String name;
        public int track_number;
        public String id;

    }

    public class Album {
        public List<TracksResult.Images> images = new ArrayList<>();
        public String release_date;
        public String name;


    }

    public class Images {
        public String width;
        public String url;
        public String height;


    }

    public class Artists {
        public String name;

    }

}

