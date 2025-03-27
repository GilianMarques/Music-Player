package gilianmarques.dev.musicplayer.mediaplayer;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.persistence.native_database.NativeTracks;
import gilianmarques.dev.musicplayer.utils.EasyAsynk;

/**
 * Criado por Gilian Marques
 * Terça-feira, 31 de Dezembro de 2019  as 20:13:57.
 */
@SuppressLint("UseSparseArrays")
public class Queue {


    private HashMap<Long, Integer> positions = new HashMap<>();
    private ArrayList<Track> queue = new ArrayList<>();// do not save this field, save queueIds instead
    private ArrayList<Long> queueIds = new ArrayList<>();


    public Track get(int index) {
        return queue.get(index);
    }

    public int size() {
        return queue.size();
    }

    public void set(ArrayList<Track> itens) {
        positions.clear();
        queue.clear();
        if (itens.size() > 1000) {
            while (itens.size() > 0) itens.remove(itens.size() - 1);
        }
        queue.addAll(itens);
    }

    //----------------------------------------------------------------------------
    @SuppressWarnings("ConstantConditions")
    public void shuffle(final boolean shouldShuffle) {

        new EasyAsynk(new EasyAsynk.Actions() {
            @Override
            public int doInBackground() {
                if (shouldShuffle) {

                    for (int i = 0; i < queue.size(); i++) {
                        Track track = queue.get(i);
                        positions.put(track.getId(), i);
                    }
                    Collections.shuffle(queue);


                    // ao iniciar uma nova fila de faixas, se o shuffle estiver ligado o player desliga
                    // ao fazer isso a fila tenta desembaralhar as faixas mas como a fila recem adicionada nao
                    // estava embaralhada o hashmap  positions esta vazio e isso joga uma exception. Por isso
                    // verifico o tamanho.
                } else if (positions.size() > 0) {
                    Collections.sort(queue, new Comparator<Track>() {
                        @Override
                        public int compare(Track t1, Track t2) {
                            return positions.get(t1.getId()) - positions.get(t2.getId());
                        }
                    });


                    // manter as posiçoes salvas ja nao é mais necessario
                    positions.clear();


                }
                return super.doInBackground();
            }
        }).executeAsync();
    }

    /**
     * @param track x
     * @return a posiçao da faixa no array (sempre é adicionada na ultima posiçao)
     */
    public int add(Track track) {
        queue.add(track);

        //salvo a posiçao da faixa caso o shuffle esteja on
        // buscar a posiçao de uma faixa no hashmap vai retonar null caso ela n estja registrada la
        if (MusicPlayer.getInstance().isShuffling())
            positions.put(track.getId(), queue.size() - 1);

        return queue.size() - 1;
    }


    public HashMap<Long, Integer> getPositions() {
        return positions;
    }

    //7,7 - 8,2 segs lendo to.do db pra cada faixa com  497 faixas
    //0.132 - 0.624 segs caregando 1000 faixas num array e lendo dele. tempo usando metodo de cima: 17.976 segs

    public ArrayList<Long> getQueueIds() {
        queueIds.clear();
        for (Track track : queue) queueIds.add(track.getId());
        return queueIds;
    }


    public void restore(HashMap<Long, Integer> positions, ArrayList<Long> queueIds) {
        if (positions != null) this.positions = positions;
        if (queueIds != null) this.queueIds = queueIds;

        // long t = System.currentTimeMillis();

        if (queueIds != null) for (Long id : queueIds) {
            Track track = findTrack(id);
            if (track != null) queue.add(track);
        }
        allTracks = null;
        //  Log.d(App.myFuckingUniqueTAG + "Queue", "restore: restore time: " + (System.currentTimeMillis() - t) + " millis. Songs: " + queue.size());
    }

    private ArrayList<Track> allTracks = null;

    private Track findTrack(long id) {
        if (allTracks == null) allTracks = new NativeTracks(true).getAllTracks();

        for (Track track : allTracks) {
            if (track.getId() == id) return track;
        }
        return null;
    }

    public ArrayList<Track> getTracks() {
        return allTracks;
    }
}
