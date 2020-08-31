package ml.luiggi.sharingsongfy.scaffoldings;

/*
 * Interfaccia per la rappresentazione degli elementi "riproducibili"
 */
public interface Playable {
    void onTrackPrevious();

    void onTrackPlay();

    void onTrackNext();

    void onTrackPause();
}
