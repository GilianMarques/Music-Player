package gilianmarques.dev.musicplayer.spotify.objects;

import org.joda.time.LocalDateTime;

import gilianmarques.dev.musicplayer.utils.DontObfuscate;

/**
 * Criado por Gilian Marques
 * Sábado, 18 de Maio de 2019  as 17:46:22.
 */
@SuppressWarnings("unused")
@DontObfuscate

public class AppAuth {
    @SuppressWarnings("unused")

    private String access_token, token_type;
    private long expires_in;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }


    public long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
    }

    /**
     * @return true if It's been 1 hour since token was gettered from spotify
     *  
     */
    public boolean isExpired() {
         return System.currentTimeMillis() > getExpires_in();
    }

    @Override public String toString() {
        return "\naccess_token: " + getAccess_token() + "\n" +
                "token_type: " + getToken_type() + "\n" +
                "expires_in: " + getExpires_in() + " (" + new LocalDateTime(getExpires_in()) + ")+\n";

    }
}
