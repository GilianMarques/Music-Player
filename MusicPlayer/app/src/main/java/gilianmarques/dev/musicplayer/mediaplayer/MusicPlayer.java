package gilianmarques.dev.musicplayer.mediaplayer;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.EasyAsynk;

/**
 * Criado por Gilian Marques
 * Domingo, 29 de Dezembro de 2019  as 15:46:28.
 */
public class MusicPlayer implements MyMediaPlayer.OnCompletionListener {
    private static MusicPlayer ourInstance = new MusicPlayer();
    private MyMediaPlayer myMediaPlayer;
    private Queue queue;// TODO: 31/12/2019 savestate
    private ListenerHandler listenerHandler;
    private MyMediaSession mSession;
    private int index = 0; // TODO: 31/12/2019 savestate
    private RepeatMode repeatMode = RepeatMode.repeat_disabled;// TODO: 31/12/2019 savestate
    private boolean shuffle;// TODO: 31/12/2019 savestate
    private boolean pauseWhenPrepared;
    private boolean playSongAt0AfterToogleShuffle;

    private boolean autoStart = true;


    public static void recreate() {
        ourInstance = new MusicPlayer();
    }

    public static MusicPlayer getInstance() {
        return ourInstance;
    }

    private MusicPlayer() {
        Log.d(App.myFuckingUniqueTAG + "MusicPlayer", "MusicPlayer: new instance created");
        myMediaPlayer = new MyMediaPlayer();
        queue = new Queue();
        myMediaPlayer.setOnCompletionListener(this);
        listenerHandler = new ListenerHandler();

        restoreState();


    }


    public void playAllSongs(ArrayList<Track> itens, int position) {
        if (isShuffling()) toogleShuffle();
        queue.set(itens);
        index = position;
        autoStart = true;
        play();
    }

    public void playFrom(int position) {
        index = position;
        play();
    }


    //__________________________________________________________________________________ playback

    private void play() {

        final Track nowTrack = queue.get(index);
//        Log.d(App.myFuckingUniqueTAG + "MusicPlayer", "play: " + nowTrack.getTitle() + " index " + index);

        try {

            myMediaPlayer.reset();
            myMediaPlayer.setDataSource(nowTrack.getFilePath());
            myMediaPlayer.prepareAsync();
            myMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (autoStart) mp.start();
                    listenerHandler.notifyStateChanged(isPlaying());
                    listenerHandler.notifyTrackChanged(queue.get(index));
                    Log.d(App.myFuckingUniqueTAG + "MusicPlayer", "onPrepared: " + nowTrack.getTitle() + " index " + index);

                    //usado para quando a fila acaba e o player reseta
                    if (pauseWhenPrepared) {
                        pauseWhenPrepared = false;
                        pause();
                    }
                }
            });
        } catch (IOException e) {
            // TODO: 29/12/2019 por msg aqui
            e.printStackTrace();
        }

    }

    public void pause() {
        if (!myMediaPlayer.isPlaying()) return;
        myMediaPlayer.pause();
        listenerHandler.notifyStateChanged(false);

    }

    public void next() {
        if (index == 0 && queue.size() > 0 && playSongAt0AfterToogleShuffle) {
            // ver o metodo shuffle() pra entender essa varivel
            playSongAt0AfterToogleShuffle = false;
            play();
        } else if (index < queue.size() - 1) {
            index++;
            autoStart = isPlaying();
            play();

        } else
            onCompletion(myMediaPlayer);// onCompletion tem a logica necessario pra qdo a fila de musicas acaba
    }

    public void previous() {
        if (index > 0) index--;
        autoStart = isPlaying();
        play();

    }

    public void resume() {
        if (myMediaPlayer.isPlaying()) return;
        myMediaPlayer.start();
        listenerHandler.notifyStateChanged(true);

    }

    /**
     * Chamar apenas quando for fechar o app de vez
     */
    public void stop() {
        listenerHandler.notifyPlayerStopped();
        App.binder.get().finishActivities();

        saveState();

        listenerHandler.removeAll();
        ourInstance = null;

        myMediaPlayer.stop();
        myMediaPlayer.release();

        mSession.release();

    }


    public void toggle() {
        if (isPlaying()) pause();
        else resume();
    }

    public void seek(int millisToGo) {
        myMediaPlayer.seekTo(millisToGo);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // musica acabou, quando a proxima for carregada o player pode começar a reproduzir
        autoStart = true;

        if (index == 0 && queue.size() > 0 && playSongAt0AfterToogleShuffle) {
            // ver o metodo shuffle() pra entender essa varivel
            playSongAt0AfterToogleShuffle = false;
            play();
        } else if (queue.size() > 0) if (repeatMode == RepeatMode.repeat_all) {
            if (index == queue.size() - 1) {
                index = 0;
                play();
            } else  next();


        } else if (repeatMode == RepeatMode.repeat_one) {
            play();

        } else {
            if (index == queue.size() - 1) {
                index = 0;
                play();// to get and prepare song at position 0
                pauseWhenPrepared = true;// to force player to pause as soon the song is prepared
            } else    next();
        }
    }


    //__________________________________________________________________________________ callbacks

    public void unregisterListener(StateListener stateListener) {
        listenerHandler.removeCallback(stateListener);
    }

    public void registerListener(StateListener stateListener) {
        listenerHandler.registerCallback(stateListener);
    }

    //__________________________________________________________________________________ gettering info

    public ArrayList<Track> getQueue() {
        return queue.getTracks();
    }

    public boolean isPlaying() {
        return myMediaPlayer.isPlaying();
    }

    int getCurrentPosition() {
        return myMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return myMediaPlayer.getDuration();
    }

    public Track getCurrentSong() {
        if (queue.size() == 0) return null;
        return queue.get(index);
    }

    public MyMediaSession getMediaSession() {
        return mSession;
    }

    public void toogleRepeat() {
        if (repeatMode == RepeatMode.repeat_disabled) repeatMode = RepeatMode.repeat_all;
        else if (repeatMode == RepeatMode.repeat_all) repeatMode = RepeatMode.repeat_one;
        else if (repeatMode == RepeatMode.repeat_one) repeatMode = RepeatMode.repeat_disabled;
        //  Log.d(App.myFuckingUniqueTAG + "MusicPlayer", "toogleRepeat: "+repeatMode.getValue());
    }

    /**
     * Quando o shuffle é ligado a lista é memorizada, embaralhada e começa do zero assim que a musica em reproduçao acabar.
     * <p>
     * Se for desligado a lista é restaurada e começa do zero assim que a musica em reproduçao acabar
     */
    public void toogleShuffle() {
        shuffle = !shuffle;
        queue.shuffle(shuffle);
        index = 0;
        // após alternar o shuffle a musica que ja esta tocando vai acabar  e
        // a primeira da fila vai tocar
        playSongAt0AfterToogleShuffle = true;

    }

    public RepeatMode getRepeatingMode() {
        return repeatMode;
    }

    public boolean isShuffling() {
        return shuffle;
    }

    //__________________________________________________________________________________ others

    private void restoreState() {
        new EasyAsynk(new EasyAsynk.Actions() {
            PersistentState state = new PersistentState();

            @Override
            public int doInBackground() {
                state.restore();
                queue.restore( state.positions, state.queueIds);
                return super.doInBackground();
            }

            @Override
            public void onPostExecute() {
                mSession = new MyMediaSession();

                shuffle = state.shuffle;
                repeatMode = state.repeatMode;
                index = state.index;
                if (index >= queue.size())
                    index = 0;// alguma faixa da fila foi apagada do dispositivo,ja que a fila ta menos que antes
                seek((int) state.currentPosition);

                //do this to make player prepare file and notify listeners
                if (queue.size() > 0) {
                    pauseWhenPrepared = true;
                    play();
                }
                // TODO: 03/01/2020 se uma faixa for apagada e estiver na fila vai dar erro por causa do index qdo o estado for restaurado

                super.onPostExecute();
            }
        }).executeAsync();
    }

    private void saveState() {
        PersistentState state = new PersistentState();
        state.positions = queue.getPositions();
        state.queueIds = queue.getQueueIds();
        state.shuffle = shuffle;
        state.repeatMode = repeatMode;
        state.index = index;
        state.currentPosition = getCurrentPosition();
        state.save();

    }

    public void setVolume(int vol) {
        myMediaPlayer.setVolume(vol / 100, vol / 100);
    }

    public void restartSong() {
        play();
    }
}
