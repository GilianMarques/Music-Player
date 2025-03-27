

package gilianmarques.dev.musicplayer.spotify.objects;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.DontObfuscate;


@DontObfuscate
public class AlbunsResult {

    public Albums albums;

    @Override public String toString() {
        return albums.toString();
    }

    public class Albums {

        public List<Item> items = new ArrayList<Item>();
        public int limit;
        public String next;
        public int offset;
        public Object previous;
        public int total;

        @Override public String toString() {

            String rt = "";
            for (Item sample : items) {

                Log.d(App.myFuckingUniqueTAG + "AlbunsResult", " name: " + sample.name + " release: " + sample.release_date);
                for (Artist artist : sample.artists) {
                    Log.d(App.myFuckingUniqueTAG + "AlbunsResult", " artists: " + artist.name);
                }
                for (Image albumImage : sample.images) {
                    Log.d(App.myFuckingUniqueTAG + "AlbunsResult", "images: " + albumImage.width + "x" + albumImage.height + " url: " + albumImage.url);
                }
            }
            return rt;

        }
    }

    public class Artist {

        public String name;

    }


    public class Image {

        public int height;
        public String url;
        public int width;

    }


    public class Item {

        public List<Artist> artists = new ArrayList<Artist>();
        public String href;
        public List<Image> images = new ArrayList<Image>();
        public String name;
        public String release_date;

    }

}


