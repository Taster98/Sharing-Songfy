package ml.luiggi.geosongfy;
import com.google.gson.Gson;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/*
* Questa classe serve per parsare un JSON presente in uno specifico URL, che contiene un array chiamato allmusic contenente una lista di tutte le canzoni,
* ciascuna delle quali avrà un titolo, una stringa di autori, una stringa di featuring artists, un url della canzone, un url della copertina dell'album
* */
public class JsonParserUrl {
    //url relativo al file json
    private String url;
    public JsonParserUrl(String url){
        this.url=url;
    }

    //creo una classe statica (in questo caso sarebbe una "struct" come in C) per memorizzare la lista di oggetti di tipo Song
    static class Songs{
        List<Song> allmusic;
    }

    //funzione per leggere json da un link e convertirlo in stringa
    private static String readUrl(String urlString) throws Exception{
        URL url = new URL(urlString);
        Scanner s = new Scanner(url.openStream());
        String result = "";
        while(s.hasNext())
            result += s.nextLine();
        return result;
    }
    //funzione che usa la libreria Gson per parsare automaticamente il file json nella mia classe Song
    public ArrayList<Song> getSongs(){
        Gson gson = new Gson();
        ArrayList<Song> songs = new ArrayList<>();
        Songs result;
        try {
            String json = readUrl(url);
            result = gson.fromJson(json,Songs.class);
            songs.addAll(result.allmusic);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }
}
