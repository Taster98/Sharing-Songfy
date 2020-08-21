package ml.luiggi.geosongfy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ml.luiggi.geosongfy.fragments.FragmentHome;
import ml.luiggi.geosongfy.fragments.FragmentPeople;
import ml.luiggi.geosongfy.utils.DrawerLocker;

/*
 * Questa classe rappresenta l'activity principale dell'app. Al suo interno è presente un BottomNavigationView che consente di navigare tra Fragment.*/
public class MainPageActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, DrawerLocker {
    private BottomNavigationView mBottomNavView;
    private DrawerLayout drawer;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        //Imposto la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //carico il fragment principale (quello contenente la lista delle canzoni nel server)
        loadFragment(new FragmentHome());

        //Inizializzo il bottom nav menu
        mBottomNavView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNavView.setOnNavigationItemSelectedListener(this);
        mBottomNavView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.people) {
                }
            }
        });
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
            case R.id.people:
                mFragment = new FragmentPeople();
                break;
        }
        return loadFragment(mFragment);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void setDrawerEnabled(boolean en) {
        int lockMode;
        if (en) {
            lockMode = DrawerLayout.LOCK_MODE_UNLOCKED;
        } else {
            lockMode = DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        }
        drawer.setDrawerLockMode(lockMode);
        toggle.setDrawerIndicatorEnabled(en);
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }
}