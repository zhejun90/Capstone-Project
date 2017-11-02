package com.example.android.capstone_project.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.capstone_project.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DataInterface{

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.listView) ListView listView;
    @Nullable
    @BindView(R.id.spinner) Spinner spinner;

    private String[] spinnerItems = new String[] {"All", "Business", "Entertainment", "Gaming", "General",
            "Music", "Politics","Science-and-Nature", "Sport", "Technology"};

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private String spinnerSelection = "all";
    private String source_item = "";
    private boolean itemSelected = false;

    private static final int ID_TOP_ARTICLES_LOADER = 156;
    private static final int ID_LATEST_ARTICLES_LOADER = 249;

    private int SPINNER_BUSINESS = 1;
    private int SPINNER_ENTERTAINMENT = 2;
    private int SPINNER_GAMING = 3;
    private int SPINNER_GENERAL = 4;
    private int SPINNER_MUSIC = 5;
    private int SPINNER_POLITICS = 6;
    private int SPINNER_SCIENCE_NATURE = 7;
    private int SPINNER_SPORT = 8;
    private int SPINNER_TECHNOLOGY = 9;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        tabLayout.addTab(tabLayout.newTab().setText("Top"));
        tabLayout.addTab(tabLayout.newTab().setText("Latest"));

        mPagerAdapter = new MyPagerAdapter(getFragmentManager(), tabLayout.getTabCount());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_activated_1, spinnerItems);

        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if(itemSelected) {
                    spinnerSelection = adapterView.getItemAtPosition(pos).toString().toLowerCase();
                    Toast.makeText(MainActivity.this, spinnerSelection, Toast.LENGTH_SHORT).show();
                    updateFragments(source_item);
                    source_item = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                updateFragments(source_item);
            }
        });
    }

    @Override
    public void onSourceItemClicked(String source, String category) {
        source_item = source;
        if(spinnerSelection.equals(category)){

            // Fragment update cannot occur if category of selected source is same as shown in
            // spinner item. Therefore, do a manual update.
            updateFragments(source);
            source_item = "";
        } else {

            // If source category is different from selected category from spinner, update via
            // spinner itemSelected listener.
            switch (category) {
                case "business":
                    spinner.setSelection(SPINNER_BUSINESS);
                    break;
                case "entertainment":
                    spinner.setSelection(SPINNER_ENTERTAINMENT);
                    break;
                case "gaming":
                    spinner.setSelection(SPINNER_GAMING);
                    break;
                case "general":
                    spinner.setSelection(SPINNER_GENERAL);
                    break;
                case "music":
                    spinner.setSelection(SPINNER_MUSIC);
                    break;
                case "politics":
                    spinner.setSelection(SPINNER_POLITICS);
                    break;
                case "science-and-nature":
                    spinner.setSelection(SPINNER_SCIENCE_NATURE);
                    break;
                case "sport":
                    spinner.setSelection(SPINNER_SPORT);
                    break;
                case "technology":
                    spinner.setSelection(SPINNER_TECHNOLOGY);
                    break;
            }
        }
        closeDrawer();
    }

    @Override
    public void updateFragments(String source) {
        Fragment fragment_1 = mPagerAdapter.getRegisteredFragment(0);
        Fragment fragment_2 = mPagerAdapter.getRegisteredFragment(1);
        if (fragment_1 instanceof MainActivityFragment) {
            ((MainActivityFragment) fragment_1).setSource(source);
            ((MainActivityFragment) fragment_1).restartLoader(ID_TOP_ARTICLES_LOADER, spinnerSelection);
        }
        if(fragment_2 instanceof MainActivityFragment) {
            ((MainActivityFragment) fragment_2).setSource(source);
            ((MainActivityFragment) fragment_2).restartLoader(ID_LATEST_ARTICLES_LOADER, spinnerSelection);
        }
    }

    @Override
    public ListView getListView() {
        return listView;
    }

    public void setItemSelectedTrue(){
        itemSelected = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public String getSpinnerSelection(){
        return spinnerSelection;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.search){
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void closeDrawer() {
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        // Register and store fragments for access later on
        private SparseArray<Fragment> registeredFragments = new SparseArray<>();

        int tabCount;

        public MyPagerAdapter(FragmentManager fm, int mTabCount) {
            super(fm);
            this.tabCount = mTabCount;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = MainActivityFragment.newInstance(position);
            return fragment;
        }

        @Override
        public int getCount() {
            return tabLayout.getTabCount();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        public Fragment getRegisteredFragment(int position){
            return registeredFragments.get(position);
        }
    }
}