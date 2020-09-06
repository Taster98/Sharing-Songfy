package ml.luiggi.sharingsongfy.scaffoldings;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Objects;

//Oggetto che rappresenta una playlist
public class Playlist{
    //ha una lista di canzoni associata a un nome
    private String playlistName;
    private List<Song> songList;

    public Playlist(String playlistName, List<Song> songList) {
        this.playlistName = playlistName;
        this.songList = songList;
    }

    public List<Song> getSongList() {
        return songList;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }

    //Sovrascrivo equals e hashcode per far sì che le uguaglianze siano viste solo per i nomi delle playlist
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(playlistName, playlist.playlistName);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(playlistName);
    }
}
