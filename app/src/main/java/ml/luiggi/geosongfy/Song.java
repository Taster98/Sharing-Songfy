package ml.luiggi.geosongfy;

import java.util.ArrayList;

public class Song {
    private String title, cover;
    private ArrayList<String> authors, feats;

    public Song(String title, String cover, ArrayList<String> authors, ArrayList<String> feats){
        this.title=title;
        this.authors=authors;
        this.cover=cover;
        this.feats=feats;
    }

    public String getCover() {
        return cover;
    }

    public ArrayList<String> getFeats() {
        return feats;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }

    public String getTitle() {
        return title;
    }
}
