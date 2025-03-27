package gilianmarques.dev.musicplayer.spotify.objects;

import io.realm.RealmObject;

/**
 * Criado por Gilian Marques
 * Quinta-feira, 06 de Junho de 2019  as 20:16:29.
 */
public class WebSpotifyCache extends RealmObject {

    public WebSpotifyCache(String url, String json) {
        this.url = url;
        this.json = json;//_____________________________D___H____M____S____MLS__
        this.expiriesAt = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000);
    }

    public WebSpotifyCache() {
    }

    private String url, json;
    private long expiriesAt;

    public String getUrl() {
        return url;
    }

    public String getJson() {
        return json;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiriesAt;
    }
}
