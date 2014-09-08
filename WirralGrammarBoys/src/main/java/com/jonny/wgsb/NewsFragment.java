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
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NewsFragment extends Fragment {
    private static final String AllNewsItemsURL = "http://app.wirralgrammarboys.com/get_news.php";
    private static final String TAG_SUCCESS = "success";
    private static final String NEWS = "news";
    private static final String TITLE = "title";
    private static final String STORY = "story";
    private static final String IMAGE_SRC = "imageSrc";
    private static final String DATE = "date";
    private static final String ERROR_URL = "http://www.wirralgrammarboys.comhttp://wirralgrammarboys.com";
    private static final String ERROR_FIX_URL = "http://wirralgrammarboys.com";
    private static final String NEWS_DIR_URL = "http://wirralgrammarboys.com/news/";
    private static final String IMAGE_DIR_URL = "http://wirralgrammarboys.com/images/newsPics/";
    private static final String ADMINIMAGES_DIR_URL = "http://wirralgrammarboys.com/admin/images/";
    private static final String NUNTIUS_DIR_URL = "http://wirralgrammarboys.com/general/nuntius";
    private static final String UPLOADS_DIR_URL = "http://wirralgrammarboys.com/uploads/";
    JSONParser jParser = new JSONParser();
    JSONArray newsItems = null;
    ProgressDialog mProgress;
    ListView newsListView;
    ScrollView newsScrollView;
    TextView titleTextView, storyTextView, dateTextView;
    ImageView storyImageView;
    DatabaseHandler dbhandler;
    ConnectionDetector cd;
    AsyncTask<Void, Integer, Void> mLoadNewsTask;
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
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.news_drawer_layout);
        newsListView = (ListView) view.findViewById(R.id.news_list);
        newsScrollView = (ScrollView) view.findViewById(R.id.news_scroll_view);
        titleTextView = (TextView) view.findViewById(R.id.titleArticleNews);
        storyImageView = (ImageView) view.findViewById(R.id.storyNewsImage);
        storyTextView = (TextView) view.findViewById(R.id.storyArticleNews);
        dateTextView = (TextView) view.findViewById(R.id.dateArticleNews);
        mDrawerLayout.setDrawerListener(new DrawerListener());
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mActionBar = createActionBarHelper();
        mActionBar.init();
        mDrawerToggle = new ActionBarDrawerToggle(this.getActivity(), mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name);
        mDrawerLayout.openDrawer(newsListView);
        mDrawerToggle.syncState();
        mDrawerLayout.setFocusableInTouchMode(false);
        if (dbhandler.getNewsCount() > 0) {
            getNewsList();
        } else {
            if (cd.isConnectingToInternet()) {
                loadNews();
            } else {
                internetDialogue(getResources().getString(R.string.no_internet));
            }
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionBar = createActionBarHelper();
        mActionBar.init();
        if (contentAvailable == 0) {
            if (cd.isConnectingToInternet()) {
                loadNews();
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
        if (mLoadNewsTask != null && mLoadNewsTask.getStatus() == AsyncTask.Status.PENDING) {
            FlagCancelled = true;
            mLoadNewsTask.cancel(true);
            mLoadNewsTask = null;
        } else if (mLoadNewsTask != null && mLoadNewsTask.getStatus() == AsyncTask.Status.RUNNING) {
            FlagCancelled = true;
            mLoadNewsTask.cancel(true);
            mLoadNewsTask = null;
        } else if (mLoadNewsTask != null && mLoadNewsTask.getStatus() == AsyncTask.Status.FINISHED) {
            FlagCancelled = true;
            mLoadNewsTask = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDrawerToggle.setDrawerIndicatorEnabled(false);
    }

    boolean onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(newsListView)) {
            return true;
        } else {
            mDrawerLayout.openDrawer(newsListView);
            return false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.news, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.newsRefresh) {
            if (cd.isConnectingToInternet()) {
                loadNews();
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

    private void getNewsList() {
        List<HashMap<String, String>> newsListItems = new ArrayList<HashMap<String, String>>();
        List<News> news = dbhandler.getAllNews();
        for (News n : news) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("listID", n.getID().toString());
            map.put("listTitle", n.getTitle());
            map.put("listDate", n.getDate());
            newsListItems.add(map);
        }
        ListAdapter newsListAdapter = new SimpleAdapter(getActivity(), newsListItems, R.layout.list_news_nav_drawer,
                new String[]{"listID", "listTitle", "listDate"}, new int[]{R.id.newsId, R.id.titleNews, R.id.dateNews});
        newsListView.setAdapter(newsListAdapter);
        newsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView idText = (TextView) view.findViewById(R.id.newsId);
                Integer newsId = Integer.parseInt(idText.getText().toString());
                News articleNews = dbhandler.getNews(newsId);
                String articleTitle = articleNews.getTitle();
                String articleStory = articleNews.getStory();
                String articleDate = articleNews.getDate();
                String imageUrl = articleNews.getImageSrc();
                Spanned htmlSpan;
                mActionBar.setTitle(articleTitle);
                titleTextView.setText(articleTitle);
                dateTextView.setText(articleDate);
                htmlSpan = Html.fromHtml(articleStory);
                storyTextView.setText(htmlSpan);
                storyTextView.setMovementMethod(LinkMovementMethod.getInstance());
                newsScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        newsScrollView.fullScroll(ScrollView.FOCUS_UP);
                    }
                }, 5);
                mDrawerLayout.closeDrawer(newsListView);
                Picasso.with(getActivity()).load(imageUrl).resize(770, 550).into(storyImageView);
            }
        });
    }

    private void loadNews() {
        mLoadNewsTask = new AsyncTask<Void, Integer, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgress = new ProgressDialog(getActivity());
                mProgress.setMessage("Loading news, Please wait...");
                mProgress.setIndeterminate(false);
                mProgress.setCancelable(true);
                mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgress.show();
            }

            @Override
            protected Void doInBackground(Void... args) {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                try {
                    JSONObject json = jParser.makeHttpRequest(AllNewsItemsURL, params);
                    int success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        newsItems = json.getJSONArray(NEWS);
                        mProgress.setMax(newsItems.length());
                        int progressCount = 0;
                        for (int i = 0; i < newsItems.length(); i++) {
                            JSONObject obj = newsItems.getJSONObject(i);
                            Integer id = i + 1;
                            String title = obj.getString(TITLE);
                            String story = obj.getString(STORY);
                            String imageSrc = IMAGE_DIR_URL + obj.getString(IMAGE_SRC).replaceAll(" ", "%20");
                            String date = obj.getString(DATE).replaceAll(" ", "");
                            story = replace(story);
                            date = buildDate(date);
                            if (id > dbhandler.getNewsCount()) {
                                dbhandler.addNews(new News(id, title, story, imageSrc, date));
                            } else {
                                dbhandler.updateNews(new News(id, title, story, imageSrc, date));
                            }
                            if (cd.isWiFiConnected())
                                Picasso.with(getActivity()).load(imageSrc).resize(770, 550).fetch();
                            if (isCancelled() || FlagCancelled) break;
                            progressCount++;
                            publishProgress((int) (progressCount * 100 / newsItems.length()));
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
                mLoadNewsTask = null;
                mProgress.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        getNewsList();
                        if (!taskSuccess)
                            internetDialogue(getResources().getString(R.string.no_working_connection));
                    }
                });
            }
        };
        mLoadNewsTask.execute(null, null, null);
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
        if (story.contains("<!--<img")) {
            story = story.replaceAll("<!--<img [^>]*>-->", "");
        } else {
            story = story.replaceAll("<img [^>]*>", "");
        }
        return story;
    }

    private String buildDate(String date) {
        String[] splitDate = splitStringEvery(date, 2);
        Integer dayDateInt = Integer.parseInt(splitDate[0]);
        String dayDateString = (new StringBuilder(dayDateInt + getDayNumberSuffix(Integer.parseInt(splitDate[0])))).toString();
        String monthDateString = getMonthName(Integer.parseInt(splitDate[1]));
        String yearDateString = splitDate[2] + splitDate[3];
        date = dayDateString + " " + monthDateString + " " + yearDateString;
        return date;
    }

    private String[] splitStringEvery(String s, int interval) {
        int arrayLength = (int) Math.ceil(((s.length() / (double) interval)));
        String[] result = new String[arrayLength];
        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = s.substring(j, j + interval);
            j += interval;
        }
        result[lastIndex] = s.substring(j);
        return result;
    }

    private String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    private String getMonthName(int monthNo) {
        switch (monthNo) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            default:
                return "December";
        }
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
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setIcon(R.drawable.banner);
            v = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.actionbar_title, null);
            /*v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerLayout.isDrawerOpen(newsListView)) {
                        mDrawerLayout.closeDrawer(newsListView);
                    } else {
                        mDrawerLayout.openDrawer(newsListView);
                    }
                }
            });*/
            mActionTitle = ((TextView) v.findViewById(R.id.title));
            mActionTitle.setText(R.string.news);
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
            mActionTitle.setText(R.string.news);
        }

        private void setTitle(CharSequence title) {
            mTitle = title;
        }
    }
}