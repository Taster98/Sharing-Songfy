package ml.luiggi.geosongfy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MusicListActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    private BottomNavigationView mBottomNavView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //carico i fragment
        loadFragment(new FragmentHome());

        //Inizializzo il bottom nav menu
        mBottomNavView =  (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNavView.setOnNavigationItemSelectedListener(this);
    }

    //funzione per caricare i vari fragment
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
    //funzione per fare lo switch tra i fragment qualora uno di essi viene selezionato
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