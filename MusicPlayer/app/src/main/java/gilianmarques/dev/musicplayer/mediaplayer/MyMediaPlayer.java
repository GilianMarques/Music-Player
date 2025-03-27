package gilianmarques.dev.musicplayer.mediaplayer;

import java.io.IOException;

/**
 * Criado por Gilian Marques
 * Quinta-feira, 02 de Janeiro de 2020  as 20:57:08.
 */
public class MyMediaPlayer extends android.media.MediaPlayer {


    private boolean playing = false;
    private int currPosition = 0;
    private int duration = 0;
    private int seek = 0;

    @Override
    public void start() throws IllegalStateException {
        super.start();
        if (seek > 0) {
            super.seekTo(seek);
            seek = 0;
        }
        playing = true;
    }


    @Override
    public void stop() throws IllegalStateException {
        playing = false;
        super.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        playing = false;
        super.pause();
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        playing = false;
        super.prepare();

    }

    @Override
    public void reset() {
        playing = false;
        super.reset();
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }


    @Override
    public int getCurrentPosition() {

        if (isPlaying()) currPosition = super.getCurrentPosition();
        return currPosition;
    }

    @Override
    public int getDuration() {
        if (isPlaying()) duration = super.getDuration();
        return duration;
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        if (isPlaying()) super.seekTo(msec);
        else seek = msec;// to seek as soon start() is called
    }

}
