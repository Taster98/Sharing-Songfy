package ml.luiggi.geosongfy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import ml.luiggi.geosongfy.fragments.FragmentHome;
import ml.luiggi.geosongfy.fragments.PlaylistFragment;

/*
 * Questa classe rappresenta l'activity principale dell'app. Al suo interno è presente un BottomNavigationView che consente di navigare tra Fragment.*/
public class MainPageActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView mBottomNavView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        //carico il fragment principale (quello contenente la lista delle canzoni nel server)
        loadFragment(new FragmentHome());

        //Inizializzo il bottom nav menu
        initBottomView();
    }
    public void initBottomView(){
        mBottomNavView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNavView.setOnNavigationItemSelectedListener(this);
        mBottomNavView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

            }
        });
    }
    public void changeFocus(int id){
        mBottomNavView.setSelectedItemId(id);
    }
    //funzione per caricare un fragment specifico
    public boolean loadFragment(Fragment mFragment) {
        if (mFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mFragment)
                    .commit();
            return true;
        }
        return false;
    }

    //funzione per fare lo switch tra i fragment qualora uno di essi viene selezionato dal BottomNavigationView
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment mFragment = null;
        switch (item.getItemId()) {
            case R.id.home:
                mFragment = new FragmentHome();
                break;
            case R.id.fragment_tue_playlist:
                mFragment = new PlaylistFragment();
                break;
        }
        return loadFragment(mFragment);
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }
}