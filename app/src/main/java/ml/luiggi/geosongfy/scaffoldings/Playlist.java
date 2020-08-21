package ml.luiggi.geosongfy.scaffoldings;

import java.util.List;

public class Playlist {
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
}
