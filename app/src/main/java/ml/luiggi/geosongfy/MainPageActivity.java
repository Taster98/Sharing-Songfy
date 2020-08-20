package ml.luiggi.geosongfy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
/*
* Questa classe rappresenta l'activity principale dell'app. Al suo interno è presente un BottomNavigationView che consente di navigare tra Fragment.*/
public class MainPageActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    private BottomNavigationView mBottomNavView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Nascondo l'actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //carico il fragment principale (quello contenente la lista delle canzoni nel server)
        loadFragment(new FragmentHome());

        //Inizializzo il bottom nav menu
        mBottomNavView =  (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNavView.setOnNavigationItemSelectedListener(this);
    }

    //funzione per caricare un fragment specifico
    public boolean loadFragment(Fragment mFragment){
        if(mFragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,mFragment)
                    .commit();
            return true;
        }
        return false;
    }
    //funzione per fare lo switch tra i fragment qualora uno di essi viene selezionato dal BottomNavigationView
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment mFragment = null;
        switch (item.getItemId()){
            case R.id.home:
                mFragment = new FragmentHome();
                break;
            case R.id.people:
                mFragment = new FragmentPeople();
                break;
        }
        return loadFragment(mFragment);
    }
}