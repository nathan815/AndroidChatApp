package com.learninga_z.myfirstapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learninga_z.myfirstapp.R;
import com.learninga_z.myfirstapp.fragments.ConversationListFragment;
import com.learninga_z.myfirstapp.fragments.SettingsFragment;
import com.learninga_z.myfirstapp.fragments.UsersFragment;

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
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtWebsite;
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
        txtName = (TextView) navHeader.findViewById(R.id.nav_header_name);
        txtWebsite = (TextView) navHeader.findViewById(R.id.nav_header_email);
//        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
//        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

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
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_action_settings:
                Snackbar.make(getWindow().getDecorView(), "Settings!", Snackbar.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Log.d(TAG, "Clicked " + id);

        switch(id) {
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
        selectNavMenu();
        setToolbarTitle();

        Log.d(TAG, "Loading " + currentFragmentTag);

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(currentFragmentTag) != null) {
            drawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
//        Runnable pendingRunnable = new Runnable() {
//            @Override
//            public void run() {
//                // update the main content by replacing fragments
//                Fragment fragment = getSelectedFragment();
//                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
//                        android.R.anim.fade_out);
//                fragmentTransaction.replace(R.id.content_frame, fragment, currentFragmentTag);
//                fragmentTransaction.commitAllowingStateLoss();
//            }
//        };
//
//        // If pendingRunnable is not null, then add to the message queue
//        if (pendingRunnable != null) {
//            handler.post(pendingRunnable);
//        }
        Fragment fragment = getSelectedFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment, currentFragmentTag);
        fragmentTransaction.commitAllowingStateLoss();

        // Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private void setToolbarTitle() {
        String title = activityTitles[navItemIndex];
        getSupportActionBar().setTitle(title);
    }

    private void selectNavMenu() {
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


    private void loadNavHeader() {
        // name, website
        txtName.setText("Testing");
        txtWebsite.setText("www.learninga-z.com");

//        // loading header background image
//        Glide.with(this).load(urlNavHeaderBg)
//                .crossFade()
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(imgNavHeaderBg);
//
//        // Loading profile image
//        Glide.with(this).load(urlProfileImg)
//                .crossFade()
//                .thumbnail(0.5f)
//                .bitmapTransform(new CircleTransform(this))
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(imgProfile);

        // showing dot next to notifications label
        //navigationView.getMenu().getItem(3).setActionView(R.layout.menu_dot);
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
}
