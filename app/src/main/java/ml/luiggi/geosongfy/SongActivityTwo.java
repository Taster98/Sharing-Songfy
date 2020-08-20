package ml.luiggi.geosongfy;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SongActivityTwo  extends AppCompatActivity{
    ImageButton play;
    TextView title;

    List<Song> tracks;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_play_layout);


    }
}
