package com.learninga_z.myfirstapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.fragments.ConversationListFragment;
import com.learninga_z.myfirstapp.fragments.SettingsFragment;
import com.learninga_z.myfirstapp.fragments.UsersFragment;
import com.learninga_z.myfirstapp.models.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        ConversationListFragment.OnFragmentInteractionListener,
        UsersFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    private static final String FRAGMENT_TAG_CONVO_LIST = "conversation_list";
    private static final String FRAGMENT_TAG_USERS = "users";
    private static final String FRAGMENT_TAG_SETTINGS = "settings";

    private static final String DEFAULT_FRAGMENT_TAG = FRAGMENT_TAG_CONVO_LIST;

    private String currentFragmentTag = DEFAULT_FRAGMENT_TAG;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgProfile;
    private TextView nameTextView, emailTextView;
    private Toolbar toolbar;

    private View progressOverlayView;

    private String[] activityTitles;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    private boolean shouldLoadHomeFragmOnBackPress = true;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        handler = new Handler();

        progressOverlayView = findViewById(R.id.progress_overlay);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Setup Drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        nameTextView = (TextView) navHeader.findViewById(R.id.nav_header_name);
        emailTextView = (TextView) navHeader.findViewById(R.id.nav_header_email);
        imgProfile = (ImageView) navHeader.findViewById(R.id.nav_header_image);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        // load nav menu header data
        loadNavHeader();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            currentFragmentTag = DEFAULT_FRAGMENT_TAG;
        }

        loadSelectedFragment();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.nav_item_conversation_list:
                navItemIndex = 0;
                currentFragmentTag = FRAGMENT_TAG_CONVO_LIST;
                break;
            case R.id.nav_item_users:
                navItemIndex = 1;
                currentFragmentTag = FRAGMENT_TAG_USERS;
                break;
            case R.id.nav_item_settings:
                navItemIndex = 2;
                currentFragmentTag = FRAGMENT_TAG_SETTINGS;
                break;
            case R.id.nav_item_logout:
                logout();
                break;
        }

        loadSelectedFragment();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadSelectedFragment() {
        selectNavMenuItem();
        setToolbarTitle();

        // if user selects the current navigation menu
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(currentFragmentTag) != null) {
            drawer.closeDrawers();
            return;
        }

        Fragment fragment = getSelectedFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, currentFragmentTag);
        fragmentTransaction.commit();

        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private void setToolbarTitle() {
        String title = activityTitles[navItemIndex];
        getSupportActionBar().setTitle(title);
    }

    private void selectNavMenuItem() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private Fragment getSelectedFragment() {
        switch (currentFragmentTag) {
            case FRAGMENT_TAG_CONVO_LIST:
                return new ConversationListFragment();
            case FRAGMENT_TAG_USERS:
                return new UsersFragment();
            case FRAGMENT_TAG_SETTINGS:
                return new SettingsFragment();
            default:
                return new ConversationListFragment();
        }
    }

    private void loadUserInfoToDrawer() {
        db.collection("users").document(currentUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot snap = task.getResult();
                    User user = snap.toObject(User.class);
                    if(user != null) {
                        nameTextView.setText(user.getUsername());
                        emailTextView.setText(user.getEmail());
                    }
                }
            }
        });
    }

    private void loadNavHeader() {
        loadUserInfoToDrawer();
    }

    private void logout() {
        Intent i = new Intent(MainActivity.this, LauncherActivity.class);
        i.putExtra("logout", true);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction() {

    }
}
