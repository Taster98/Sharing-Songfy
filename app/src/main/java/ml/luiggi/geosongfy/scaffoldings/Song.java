package ml.luiggi.geosongfy.scaffoldings;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Objects;

/*
 *Questa classe serve per contenere invece in un oggetto i dati parsati dal file Json. Deve implementare Serializable per poter essere passata come extra all'interno di un Intent.
 */
public class Song implements Serializable {
    String title,
            author,
            feat,
            url,
            cover;

    public Song(String title, String cover, String author, String feat, String url) {
        this.title = title;
        this.author = author;
        this.cover = cover;
        this.feat = feat;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getCover() {
        return cover;
    }

    public String getAuthors() {
        return author;
    }

    public String getFeats() {
        return feat;
    }

    public String getTitle() {
        return title;
    }


    //sovrascrivo equals e hashCode per poter comparare oggetti di tipo Song (mi servono per trovare la loro posizione in un'ArrayList):
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return title.equals(song.title) &&
                author.equals(song.author) &&
                Objects.equals(feat, song.feat) &&
                url.equals(song.url) &&
                cover.equals(song.cover);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(title, author, feat, url, cover);
    }
}
