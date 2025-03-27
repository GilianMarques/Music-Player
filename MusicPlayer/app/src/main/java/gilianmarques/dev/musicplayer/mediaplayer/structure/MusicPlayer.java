package gilianmarques.dev.musicplayer.mediaplayer.structure;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.SongStatistscs;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicServiceStarter;
import gilianmarques.dev.musicplayer.models.Album;
import gilianmarques.dev.musicplayer.models.Artist;
import gilianmarques.dev.musicplayer.models.Playlist;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.models.TrackRef;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.sorting.utils.Sort;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques em 5/05/2018 as 13:03:38.
 */
public class MusicPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {


    public MusicPlayer(MusicService musicService) {
        mSongStatistscs = new SongStatistscs(this);
        mSongStatistscs.listen();
        mNotifyer = new Notifyer(this);
        flow = new QueueFlow(this);
        setOnCompletionListener(this);
        setOnErrorListener(this);
        setAudioStreamType(AudioManager.STREAM_MUSIC);
        setAudioAttributes(new AudioAttributes.Builder()
                                   .setUsage(AudioAttributes.USAGE_MEDIA)
                                   .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                   .build());

        if (musicService == null || MusicService.binder == null)
            throw new RuntimeException("Player started from a not allowed class");
        Log.d("MusicPlayer", "MusicPlayer: novo player aberto");

    }

    private boolean wasPlaying;
    private boolean canResume;
    boolean listeningProgress;
    boolean preparing;
    private int lastPosition, lastDuration;
    boolean released;
    @SuppressWarnings("FieldCanBeLocal")
    private final SongStatistscs mSongStatistscs;
    final Notifyer mNotifyer;
    public QueueFlow flow;

    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        preparing = true;
        super.setDataSource(fd);
    }

    @Override
    public int getDuration() {
        if (!preparing && isPlaying()) lastDuration = super.getDuration();
        return lastDuration;
    }

    @Override
    public int getCurrentPosition() {
        if (!released && !preparing && isPlaying()) lastPosition = super.getCurrentPosition();
        return lastPosition;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        preparing = false;
    }

    void play() {
        if (preparing) {
            Log.e("MusicPlayer", "play: PREPARING! rescheduling...");
            Runnable mRunnable = new Runnable() {
                @Override
                public void run() {
                    play();
                }
            };
            new Handler().postDelayed(mRunnable, 100);
        }
        canResume = false;
        try {
            reset();

            FileInputStream inputStream = new FileInputStream(flow.currentTrack().getFilePath());
            setDataSource(inputStream.getFD());
            prepare();
            start();
            wasPlaying = true;
            mNotifyer.notifyListener.onReceive(null, null);
        } catch (Exception e) {
            e.printStackTrace();

            final String msg = String.format(Locale.getDefault(), App.binder.get().getString(R.string.Erro_ao_reproduzir_X), flow.currentTrack().getTitle());
            App.binder.get().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.error(App.binder.get(), msg, 3800).show();
                }
            });

            if (!new File(flow.currentTrack().getFilePath()).exists()) {
                mNotifyer.notifyTrackNotFound();
            }
        }
    }


    @Override
    public void pause() throws IllegalStateException {
        if (!isPlaying()) return;
        wasPlaying = false;
        canResume = true;
        super.pause();
        mNotifyer.notifyPlayPause(null);
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                flow.saveState();
            }
        };
        new Thread(mRunnable).start();
    }

    @Override
    public void stop() {
        Log.d("MusicPlayer", "stop: PLAYER PARADO!");
        super.stop();
        canResume = false;
        wasPlaying = false;
        flow.reset();
        mNotifyer.notifyPlayPause(null);
        mNotifyer.notifyTrackChanged(null);
        mNotifyer.stop();
    }

    public void nextTrack(boolean fromUser) {
        /*se clicar nas setas de next e previous sem sem ter iniciado o player da nullPointer*/
        if (flow.isEmpty()) return;

        /*repetindo a mesma musica*/
        if (flow.repeatMode == RepeatMode.repeat_one) {
            play();
            return;
        }
        // esta na ultima musica da playlist
        else if (flow.isLastIndex()) {

            if (flow.repeatMode == RepeatMode.repeat_all) {
                //reseto pra começar a tocar as musicas de 0 am diante
                flow.reset();
            } else {
                // dps de tocar a ultima musica da ultima playlist a menos que seja pra repetir eu paro a reprodução
                play();//to update metadata on UI like set progressbars to 0
                stop();
            }
        }

        canResume = false;
        //Log.d(App.myFuckingUniqueTAG + "MusicPlayer", "nextTrack: about to change track: " + flow.currentTrack().getTitle() + " " + flow.getIndex());
        flow.getNext(fromUser);
        //Log.d(App.myFuckingUniqueTAG + "MusicPlayer", "nextTrack:track changed: " + flow.currentTrack().getTitle() + " " + flow.getIndex());
        if (wasPlaying) play();
        mNotifyer.notifyTrackChanged(null);
    }


    public void previousTrack() {
        /*se clicar nas setas de next e previous sem sem ter iniciado o player da nullPointer*/
        if (flow.isEmpty()) return;

        canResume = false;
        flow.getPrevious();
        if (wasPlaying) play();
        mNotifyer.notifyTrackChanged(null);
    }


    public RepeatMode repeat(boolean asGetter) {
        if (!asGetter) {
            switch (flow.repeatMode) {
                case repeat_disabled:
                    flow.repeatMode = RepeatMode.repeat_all;
                    break;
                case repeat_all:
                    flow.repeatMode = RepeatMode.repeat_one;
                    break;
                case repeat_one:
                    flow.repeatMode = RepeatMode.repeat_disabled;
                    break;
            }
        }
        //Log.d(App.myFuckingUniqueTAG + "MusicPlayer", "repeat: MusicPlayer repeat mode: " + flow.repeatMode.getValue());
        return flow.repeatMode;

    }

    public void toogleShuffle(boolean turnShuffleOn) {

        flow.shuffle(turnShuffleOn);

// TODO: 25/05/2019 implement
        if (wasPlaying && !isPlaying()) play();

        Log.d("MusicPlayer", "toogleShuffle: " + (flow.isShuffling() ? "shuffling" : "not shuffling"));
    }

    public boolean isShuffling() {
        return flow.isShuffling();
    }
    //


    public void seekToPosition(float progress) {
        seekTo(progressToMillis((int) progress));
        /*currentMusicPosition = progressToMillis((int) progress);
        seekToPosition(currentMusicPosition);*/
    }

    public void toogle() {

        if (flow.isEmpty()) {
            Log.d("MusicPlayer", "toogle: PLAYLIST VAZIA");
            ArrayList<Track> t = new NativeTracks(false).getAllTracks();
            Sort.Tracks.sort(c.sorting_tracks_fragment, t);
            flow.populate(t);
            if (flow.isEmpty()) return;
        }

        if (isPlaying()) {
            pause();
        } else {

            if (flow.currentTrack() == null) {
                flow.getNext(true);
                mNotifyer.notifyTrackChanged(null);
                play();
            }

            if (canResume) {
                canResume = false;
                wasPlaying = true;
                start();
                mNotifyer.notifyPlayPause(null);
                mNotifyer.notifyProgress();
            } else play();

        }

        Log.d("MusicPlayer", "toogle:  Player está  " + ((isPlaying() ? "REPRODUZINDO" : "PAUSADO")));
    }


    /**
     * @param mp mp
     *           <p>
     *           CallbackI setado no {@link MediaPlayer} original
     *           avisa quando a musica acaba
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(App.myFuckingUniqueTAG + "MusicPlayer", "onCompletion: " + flow.currentTrack().getTitle() + " is over.");
        nextTrack(false);
        mNotifyer.notifyTrackEnded();
    }


    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    String getProgressTimer() {
        String finalTimerString = "";
        String secondsString;
        long milliseconds = getCurrentPosition();

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);


        if (seconds < 10) secondsString = "0" + seconds;
        else secondsString = "" + seconds;

        if (hours > 0) finalTimerString += hours + ":";

        finalTimerString += minutes + ":";
        finalTimerString += secondsString;

        return finalTimerString;
    }


    /**
     * Function to get Progress percentage
     *
     * @param currentDuration c
     */
    float getProgressPercent(long currentDuration) {
        float percentage;
        int totalDuration = getDuration();


        percentage = ((((float) currentDuration) / totalDuration) * 100);
        return percentage;
    }

    /**
     * Function to change progress to timer
     *
     * @param progress -
     */
    private int progressToMillis(int progress) {

        int currentDuration;
        int totalDuration = getDuration() / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("MusicPlayer", "onError: Player found an error, reseting and playing! (" + what + ", " + extra + ")");
        reset();
        if (wasPlaying) play();
        return true;
    }

    public void initFromArtist(Artist mArtist, int position, boolean turnShuffleOn) {

        toogleShuffle(turnShuffleOn);

        // TODO: 26/05/2019 sort if necessary
        ArrayList<Track> p = new NativeTracks(false).getTracksByArtist(mArtist.getName());
        flow.populate(p);
        flow.getTrackAsCurrent(turnShuffleOn ? flow.randomIndex() : position);
        play();

    }

    public void initFromAlbum(Album mAlbum, int position, boolean turnShuffleOn) {
        /*ordenar as musicas antes de adiciona-las ao flow vai fazer que ao ordenar as musicas por ordem de adição
         * que é padrão ao desligar os toogleShuffle, as musicas automaticamente se organizem n só por ordem de adição mas consequentemente
         * pela posição definida pelo usuario com drag'n drop uma vez que ao ser adicionadas na playlist as musicas receberam uma
         * posição temporaria referente a sua posição inicial na playlist que sempre vai bater com a posição definida pelo ultimo modelo de
         * ordenação, nesse caso, posição.*/

        toogleShuffle(turnShuffleOn);

        // TODO: 26/05/2019 sort if necessary
        ArrayList<Track> p = (new NativeTracks(false).getTracksByAlbum(mAlbum));
        flow.populate(p);
        flow.getTrackAsCurrent(turnShuffleOn ? flow.randomIndex() : position);
        play();
    }

    public void initFromAllTracks(int position, ArrayList<Track> mTracks) {
        mTracks = Sort.Tracks.sort(c.sorting_tracks_fragment, mTracks);

        flow.populate(mTracks);
        flow.getTrackAsCurrent(position);
        play();
    }

    public void initFromPlaylist(Playlist playlist, int position, boolean turnShuffleOn) {

        toogleShuffle(turnShuffleOn);

        ArrayList<Track> p = playlist.getTracks(true);
        flow.populate(p);
        flow.getTrackAsCurrent(turnShuffleOn ? flow.randomIndex() : position);
        play();
    }

    public void initFromFolder(ArrayList<Track> tracks, int position, boolean turnShuffleOn) {
        toogleShuffle(turnShuffleOn);
        flow.populate(tracks);
        flow.getTrackAsCurrent(turnShuffleOn ? flow.randomIndex() : position);
        play();
    }

    public int addToQueue(Track mTrack) {
        if (mTrack != null) {
            return flow.addTrack(mTrack);
        }
        return 0;
    }

    public Track getCurrentTrack() {
        return flow.currentTrack();
    }


    public void changeFocus(boolean hasFocus) {
        if (!hasFocus) {
            /*O usuario pode estar em uma chamada então se o app perder o foco de audio devo pausar o player*/
            if (isPlaying()) toogle();

        } else if (!isPlaying()) toogle();

    }


    public void setVolume(float vol) {
        if (vol < 0 || vol > 100)
            throw new RuntimeException("O volume deve ser >=0&&<=100. Volume passado: " + vol);

        vol = vol / 100;
        setVolume(vol, vol);
    }

    /**
     * Deve ser chamado atravez do serviço {@link PlayerStateService}
     */
    void saveState() {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                flow.saveState();
            }
        };
        new Thread(mRunnable).start();

    }

    public void restoreState() {
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                flow.restoreState();
                LocalBroadcastManager.getInstance(App.binder.get()).sendBroadcast(new Intent(MusicServiceStarter.SERVICE_STARTED));
                mNotifyer.notifyListener.onReceive(null, null);
            }
        };
        new Thread(mRunnable).start();
    }

    /**
     * @return DO NOT EDIT THIS LIST
     */
    public ArrayList<TrackRef> getQueue() {
        return flow.getQueue();
    }

    public void restartTrack() {
        play();
    }

    public void playFrom(int position) {
        flow.getTrackAsCurrent(position);
        play();

    }


    void release(PlayerStateService mPlayerStateService) {
        if (mPlayerStateService == null) return;
        mNotifyer.release();
        released = true;
        super.release();
    }

    @Override
    public void release() {
        throw new RuntimeException("Use release(PlayerStateService killer).");
        /*inutilizando o metodo para só o release(PlayerStateService killer) ser usado*/
    }


}



