package com.example.android.capstone_project.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.android.capstone_project.R;
import com.example.android.capstone_project.data.ArticleContract;
import com.example.android.capstone_project.data.ArticleDbHelper;
import com.example.android.capstone_project.data.ArticleQuery;
import com.example.android.capstone_project.data.DbUtils;
import com.example.android.capstone_project.http.GetArticlesListService;
import com.example.android.capstone_project.http.apimodel.Article;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivityFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>,
    NavigationAdapter.OnClickHandler{



    public final int TOP_ARTICLES = 0;
    public final int LATEST_ARTICLES = 1;
    private int MAIN_ACTIVITY = 0;
    private final String TAG = "MainActivityFragment";

    private static final int ID_TOP_ARTICLES_LOADER = 156;
    private static final int ID_LATEST_ARTICLES_LOADER = 249;

    public ArrayList<String> articleSourcesList;
    private DataInterface mCallback;

    private static String source_item = "";
    private Cursor sourceCursor;

    private int category_id;
    private SearchArticlesAdapter mAdapter;

    boolean top_articles_loaded = false;
    boolean latest_articles_loaded = false;

    private ArticleDbHelper helper;
    private String spinnerSelection = "all";
    private DbUtils utils;

    private String[] spinnerItems = new String[] {"all", "business", "entertainment", "gaming", "general",
            "music", "politics", "science-and-nature", "sport", "technology"};

    @Nullable
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    public MainActivityFragment() {
    }

    public static MainActivityFragment newInstance(int id) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putInt("Category_Id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSourceItemClicked(String source, String category) {
        source_item = source;
        mCallback.onSourceItemClicked(source, category);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallback = (DataInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                + " must implement DataInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = new ArticleDbHelper(getActivity());
        utils = new DbUtils(helper);

        if (getArguments() != null) {
            category_id = getArguments().getInt("Category_Id");
        }

        if (savedInstanceState == null) {

            IntentFilter topArticlesIntentFilter = new IntentFilter(getString(R.string.get_top_articles));
            MyResponseReceiver responseReceiver = new MyResponseReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(responseReceiver, topArticlesIntentFilter);

            IntentFilter latestArticlesIntentFilter = new IntentFilter(getString(R.string.get_latest_articles));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(responseReceiver, latestArticlesIntentFilter);

            switch (category_id){
                case TOP_ARTICLES:
                    GetArticlesListService.getTopArticles(getActivity());
                    break;
                case LATEST_ARTICLES:
                    GetArticlesListService.getLatestArticles(getActivity());
                    break;
            }

        } else {

            switch(category_id){
                case TOP_ARTICLES:
                    getLoaderManager().initLoader(ID_TOP_ARTICLES_LOADER, null, this);
                    break;
                case LATEST_ARTICLES:
                    getLoaderManager().initLoader(ID_LATEST_ARTICLES_LOADER, null, this);
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main_activity, container, false);
        ButterKnife.bind(this, mRootView);

        if(getActivity() instanceof MainActivity){
            spinnerSelection = ((MainActivity) getActivity()).getSpinnerSelection();
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(10);
        mRecyclerView.setDrawingCacheEnabled(true);
        return mRootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SQLiteDatabase readDb = helper.getReadableDatabase();
        spinnerSelection = ((MainActivity) getActivity()).getSpinnerSelection();

        String[] sourceSelection = new String[] {source_item};
        String[] selectionArgs = new String[] {spinnerSelection};

        Uri top_articles_uri = ArticleContract.ArticleEntry.TOP_URI;
        Uri latest_articles_uri = ArticleContract.ArticleEntry.LATEST_URI;
        switch(category_id){
            case TOP_ARTICLES:

                if(spinnerSelection.equals("all")){
                    source_item = "";
                    return new CursorLoader(getActivity(), top_articles_uri, ArticleQuery.PROJECTION,
                            null, null, null);

                } else {

                    if (source_item.equals("")) {

                        // Account for categories available only in Top but not in Latest. If Latest
                        // tab is missing in info, duplicate info from Top tab.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, "Category=?", selectionArgs, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), top_articles_uri,
                                    ArticleQuery.PROJECTION, "Category=?", selectionArgs, null);
                        } else return new CursorLoader(getActivity(), latest_articles_uri,
                                ArticleQuery.PROJECTION, "Category=?", selectionArgs, null);

                    } else {

                        // If source selected from navigation drawer, set spinner selection to category,
                        // then query info based on source name provided.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, "Source=?", sourceSelection, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), top_articles_uri,
                                    ArticleQuery.PROJECTION, "Source=?", sourceSelection, null);
                        } else return new CursorLoader(getActivity(), latest_articles_uri,
                                ArticleQuery.PROJECTION, "Source=?", sourceSelection, null);
                    }

                }

            case LATEST_ARTICLES:

                if(spinnerSelection.equals("all")){
                    source_item = "";
                    return new CursorLoader(getActivity(), latest_articles_uri,
                            ArticleQuery.PROJECTION, null, null, null);

                } else {

                    if (source_item.equals("")) {

                        // Account for categories available only in Latest but not in Top. If Top
                        // tab is missing in info, duplicate info from Latest tab.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, "Category=?", selectionArgs, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), latest_articles_uri,
                                    ArticleQuery.PROJECTION, "Category=?", selectionArgs, null);
                        } else return new CursorLoader(getActivity(), top_articles_uri,
                                ArticleQuery.PROJECTION, "Category=?", selectionArgs, null);
                    } else {

                        // If source selected from navigation drawer, set spinner selection to category,
                        // then query info based on source name provided.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, "Source=?", sourceSelection, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), latest_articles_uri,
                                    ArticleQuery.PROJECTION, "Source=?", sourceSelection, null);
                        } else return new CursorLoader(getActivity(), top_articles_uri,
                                ArticleQuery.PROJECTION, "Source=?", sourceSelection, null);
                    }
                }
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).setItemSelectedTrue();

        source_item = "";
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new SearchArticlesAdapter(getActivity(), MAIN_ACTIVITY);
        mAdapter.setCursor(cursor);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

    private void updateNavAdapter(){
        ListView listView = mCallback.getListView();

        if(sourceCursor!=null){
            sourceCursor.close();
        }

        if(articleSourcesList != null) {
            sourceCursor = utils.querySources();
            NavigationAdapter navAdapter = new NavigationAdapter(getActivity(), this);
            navAdapter.setCursor(sourceCursor);
            listView.setAdapter(navAdapter);
        }
    }

    public void setSource(String source){
        source_item = source;
    }

    public void startLoader(int loaderID){
        getLoaderManager().initLoader(loaderID, null, this);
    }

    public void restartLoader(int loaderID, String itemSelected){
        spinnerSelection = itemSelected;
        getLoaderManager().restartLoader(loaderID, null, this);
        mAdapter.notifyDataSetChanged();
    }

    private class MyResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(category_id){
                case TOP_ARTICLES:
                    ArrayList<Article> s = intent.getParcelableArrayListExtra("GET_TOP_ARTICLES");
                    articleSourcesList = intent.getStringArrayListExtra("GET_TOP_ARTICLES_SOURCES");
                    if (s!=null) {
                        utils.insertIntoDb(s, "top");
                        updateNavAdapter();
                    }
                    if(!top_articles_loaded) {
                        startLoader(ID_TOP_ARTICLES_LOADER);
                        top_articles_loaded = true;
                    }
                    break;
                case LATEST_ARTICLES:
                    s = intent.getParcelableArrayListExtra("GET_LATEST_ARTICLES");
                    articleSourcesList = intent.getStringArrayListExtra("GET_LATEST_ARTICLES_SOURCES");
                    if (s!=null) {
                        utils.insertIntoDb(s, "latest");
                        updateNavAdapter();
                    }
                    if(!latest_articles_loaded) {
                        startLoader(ID_LATEST_ARTICLES_LOADER);
                        latest_articles_loaded = true;
                    }
                    break;
            }
        }
    }
}