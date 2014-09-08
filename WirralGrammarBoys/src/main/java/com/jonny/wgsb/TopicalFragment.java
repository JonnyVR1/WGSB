package com.jonny.wgsb;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TopicalFragment extends Fragment {
    private static final String AllTopicalItemsURL = "http://app.wirralgrammarboys.com/get_topical.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TOPICAL = "topical";
    private static final String TITLE = "title";
    private static final String STORY = "story";
    private static final String STAFF = "staff";
    private static final String SHOW = "show";
    private static final String RED = "red";
    private static final String ERROR_URL = "http://www.wirralgrammarboys.comhttp://wirralgrammarboys.com";
    private static final String ERROR_FIX_URL = "http://wirralgrammarboys.com";
    private static final String NEWS_DIR_URL = "http://wirralgrammarboys.com/news/";
    private static final String IMAGE_DIR_URL = "http://wirralgrammarboys.com/images/newsPics/";
    private static final String ADMINIMAGES_DIR_URL = "http://wirralgrammarboys.com/admin/images/";
    private static final String NUNTIUS_DIR_URL = "http://wirralgrammarboys.com/general/nuntius";
    private static final String UPLOADS_DIR_URL = "http://wirralgrammarboys.com/uploads/";
    JSONParser jParser = new JSONParser();
    JSONArray topicalItems = null;
    ProgressDialog mProgress;
    ListView topicalListView;
    ScrollView topicalScrollView;
    TextView titleTextView, storyTextView;
    DatabaseHandler dbhandler;
    ConnectionDetector cd;
    AsyncTask<Void, Integer, Void> mLoadTopicalTask;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ActionBarHelper mActionBar;
    private Integer contentAvailable = 1;
    private Boolean FlagCancelled = false, taskSuccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        cd = new ConnectionDetector(this.getActivity().getApplicationContext());
        dbhandler = DatabaseHandler.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topical, container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.topical_drawer_layout);
        topicalListView = (ListView) view.findViewById(R.id.topical_list);
        topicalScrollView = (ScrollView) view.findViewById(R.id.topical_scroll_view);
        titleTextView = (TextView) view.findViewById(R.id.titleArticleTopical);
        storyTextView = (TextView) view.findViewById(R.id.storyArticleTopical);
        mDrawerLayout.setDrawerListener(new DrawerListener());
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mActionBar = createActionBarHelper();
        mActionBar.init();
        mDrawerToggle = new ActionBarDrawerToggle(this.getActivity(), mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name);
        mDrawerLayout.openDrawer(topicalListView);
        mDrawerToggle.syncState();
        mDrawerLayout.setFocusableInTouchMode(false);
        if (dbhandler.getTopicalCount() > 0) {
            getTopicalList();
        } else {
            if (cd.isConnectingToInternet()) {
                loadTopical();
            } else {
                internetDialogue(getResources().getString(R.string.no_internet));
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (contentAvailable == 0) {
            if (cd.isConnectingToInternet()) {
                loadTopical();
            } else {
                internetDialogue(getResources().getString(R.string.no_internet));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dbhandler.getNewsCount() == 0) {
            contentAvailable = 0;
        } else {
            contentAvailable = 1;
        }
        if (mLoadTopicalTask != null && mLoadTopicalTask.getStatus() == AsyncTask.Status.PENDING) {
            FlagCancelled = true;
            mLoadTopicalTask.cancel(true);
            mLoadTopicalTask = null;
        } else if (mLoadTopicalTask != null && mLoadTopicalTask.getStatus() == AsyncTask.Status.RUNNING) {
            FlagCancelled = true;
            mLoadTopicalTask.cancel(true);
            mLoadTopicalTask = null;
        } else if (mLoadTopicalTask != null && mLoadTopicalTask.getStatus() == AsyncTask.Status.FINISHED) {
            FlagCancelled = true;
            mLoadTopicalTask = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(topicalListView)) {
            return true;
        } else {
            mDrawerLayout.openDrawer(topicalListView);
            return false;
        }
    }

    private void internetDialogue(String string) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(this.getActivity());
        alertBox.setIcon(R.drawable.fail);
        alertBox.setMessage(string);
        alertBox.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings", "com.android.settings.Settings");
                startActivity(intent);
            }
        });
        alertBox.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        alertBox.show();
    }

    private void loadTopical() {
        mLoadTopicalTask = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress = new ProgressDialog(getActivity());
                mProgress.setMessage("Loading Topical Information, Please wait...");
                mProgress.setIndeterminate(false);
                mProgress.setCancelable(true);
                mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgress.show();
            }

            @Override
            protected Void doInBackground(Void... args) {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                try {
                    JSONObject json = jParser.makeHttpRequest(AllTopicalItemsURL, params);
                    int success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        dbhandler.deleteAllTopical();
                        topicalItems = json.getJSONArray(TOPICAL);
                        mProgress.setMax(topicalItems.length());
                        int progressCount = 0;
                        for (int i = 0; i < topicalItems.length(); i++) {
                            JSONObject obj = topicalItems.getJSONObject(i);
                            String title = obj.getString(TITLE);
                            String story = obj.getString(STORY);
                            int staff = obj.getInt(STAFF);
                            int show = obj.getInt(SHOW);
                            int red = obj.getInt(RED);
                            story = replace(story);
                            Integer id = getId(i);
                            if (staff == 0 && show == 1) {
                                if (id > dbhandler.getTopicalCount()) {
                                    dbhandler.addTopical(new Topical(id, title, story, red));
                                } else {
                                    dbhandler.updateTopical(new Topical(id, title, story, red));
                                }
                            }
                            if (isCancelled() || FlagCancelled) break;
                            progressCount++;
                            publishProgress((int) (progressCount * 100 / topicalItems.length()));
                        }
                        taskSuccess = true;
                    } else {
                        Log.e("JSON Response", "success == 0");
                        taskSuccess = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    taskSuccess = false;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);
                mProgress.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(Void result) {
                mLoadTopicalTask = null;
                mProgress.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        getTopicalList();
                        if (!taskSuccess)
                            internetDialogue(getResources().getString(R.string.no_working_connection));
                    }
                });
            }
        };
        mLoadTopicalTask.execute(null, null, null);
    }

    private void getTopicalList() {
        List<HashMap<String, String>> topicalListItems = new ArrayList<HashMap<String, String>>();
        List<Topical> topical = dbhandler.getAllTopical();
        for (Topical t : topical) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("listID", t.getID().toString());
            map.put("listTitle", t.getTitle());
            topicalListItems.add(map);
        }
        ListAdapter topicalListAdapter = new SimpleAdapter(getActivity(), topicalListItems, R.layout.list_topical_nav_drawer,
                new String[]{"listID", "listTitle"}, new int[]{R.id.topicalId, R.id.titleTopical});
        topicalListView.setAdapter(topicalListAdapter);
        topicalListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView idText = (TextView) view.findViewById(R.id.topicalId);
                Integer topicalId = Integer.parseInt(idText.getText().toString());
                Topical articleTopical = dbhandler.getTopical(topicalId);
                String articleTitle = articleTopical.getTitle();
                String articleStory = articleTopical.getStory();
                Spanned htmlSpan;
                htmlSpan = Html.fromHtml(articleStory);
                mActionBar.setTitle(articleTitle);
                titleTextView.setText(articleTitle);
                storyTextView.setText(htmlSpan);
                storyTextView.setMovementMethod(LinkMovementMethod.getInstance());
                topicalScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        topicalScrollView.fullScroll(ScrollView.FOCUS_UP);
                    }
                }, 5);
                mDrawerLayout.closeDrawer(topicalListView);
            }
        });
    }

    private String replace(String story) {
        if (story.contains("/news/") && !story.contains(NEWS_DIR_URL)) {
            story = story.replaceAll("/news/", NEWS_DIR_URL);
        }
        if (story.contains("/images/newsPics/") && !story.contains(IMAGE_DIR_URL)) {
            story = story.replaceAll("/images/newsPics/", IMAGE_DIR_URL);
        }
        if (story.contains("/admin/images/") && !story.contains(ADMINIMAGES_DIR_URL)) {
            story = story.replaceAll("/admin/images/", ADMINIMAGES_DIR_URL);
        }
        if (story.contains("/general/nuntius") && !story.contains(NUNTIUS_DIR_URL)) {
            story = story.replaceAll("/general/nuntius", NUNTIUS_DIR_URL);
        }
        if (story.contains("/uploads/") && !story.contains(UPLOADS_DIR_URL)) {
            story = story.replaceAll("/uploads/", UPLOADS_DIR_URL);
        }
        if (story.contains(ERROR_URL)) {
            story = story.replaceAll(ERROR_URL, ERROR_FIX_URL);
        }
        return story;
    }

    private Integer getId(Integer i) {
        Integer id;
        if (dbhandler.getTopical(i) == null && i > 1) {
            id = i;
        } else if (dbhandler.getTopical(i - 1) == null && i > 1) {
            id = i - 1;
        } else if (dbhandler.getTopical(i - 2) == null && i > 2) {
            id = i - 2;
        } else if (dbhandler.getTopical(i - 3) == null && i > 3) {
            id = i - 3;
        } else {
            id = i + 1;
        }
        return id;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.topical, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.topicalRefresh) {
            if (cd.isConnectingToInternet()) {
                loadTopical();
            } else {
                internetDialogue(getResources().getString(R.string.no_internet_refresh));
            }
        } else if (item.getItemId() == R.id.settings) {
            SettingsFragment settingsFragment = new SettingsFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.zoom_enter, 0, 0, R.anim.zoom_exit)
                    .replace(R.id.fragment_container, settingsFragment, "SETTINGS_FRAGMENT").addToBackStack(null).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private ActionBarHelper createActionBarHelper() {
        return new ActionBarHelper();
    }

    public class DrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {
            mDrawerToggle.onDrawerOpened(drawerView);
            mActionBar.onDrawerOpened();
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            mDrawerToggle.onDrawerClosed(drawerView);
            mActionBar.onDrawerClosed();
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            mDrawerToggle.onDrawerStateChanged(newState);
        }
    }

    public class ActionBarHelper {
        private final ActionBar mActionBar;
        LayoutInflater inflater;
        TextView mActionTitle;
        View v;
        private CharSequence mTitle;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private ActionBarHelper() {
            mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            mActionBar.setIcon(R.drawable.banner);
        }

        private void init() {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setIcon(R.drawable.banner);
            v = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.actionbar_title, null);
           /*v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerLayout.isDrawerOpen(topicalListView)) {
                        mDrawerLayout.closeDrawer(topicalListView);
                    } else {
                        mDrawerLayout.openDrawer(topicalListView);
                    }
                }
            });*/
            mActionTitle = ((TextView) v.findViewById(R.id.title));
            mActionTitle.setText(R.string.topical_information);
            mActionTitle.setMarqueeRepeatLimit(255);
            mActionTitle.setFocusable(true);
            mActionTitle.setSingleLine(true);
            mActionTitle.setFocusableInTouchMode(true);
            mActionTitle.requestFocus();
            mActionBar.setCustomView(v);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setTranslucentStatus(true);
                SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
                tintManager.setStatusBarTintEnabled(true);
                tintManager.setStatusBarTintColor(Color.parseColor("#FF004890"));
            }
        }

        @TargetApi(19)
        private void setTranslucentStatus(boolean on) {
            Window win = getActivity().getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }

        private void onDrawerClosed() {
            if (mTitle != null) mActionTitle.setText(mTitle);
        }

        private void onDrawerOpened() {
            mActionTitle.setText(R.string.topical_information);
        }

        private void setTitle(CharSequence title) {
            mTitle = title;
        }
    }
}