package com.example.android_final.ui;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_final.R;
import com.example.android_final.model.News;
import com.example.android_final.util.LRUBitmapCache;


public class MainActivity extends AppCompatActivity {

    private ListView newsListView;
    private LinearLayout noDataView;
    private SearchView searchView;

    private SearchAdapter listAdapter;
    private List<News> news;

    private LRUBitmapCache lruBitmapCache;

    public static String DETAIL_INTENT_KEY ="DETAIL_INTENT_KEY";

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lruBitmapCache = new LRUBitmapCache();

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listAdapter.getFilter().filter(newText);
                return false;
            }
        });

        initData();
        setViews();
        setAdapters();
        getNews();
    }

    private void initData() {
        news = new ArrayList<News>();
    }

    public InputStream getInputStream(URL url) throws IOException {
        return  url.openConnection().getInputStream();
    }

    private void getNews() {
        new NewsAsyncTask().execute();
    }

    private void setAdapters() {
        listAdapter = new SearchAdapter(this, news);

        newsListView.setAdapter(listAdapter);
    }

    public class SearchAdapter extends BaseAdapter implements Filterable {

        private Context context;
        private List<News> newsList;
        private List<News> newsListForSearch;
        private LRUBitmapCache bitmapCache = new LRUBitmapCache();


        public SearchAdapter(Context context, List<News> news) {
            if (context == null){
                throw new IllegalArgumentException("Context can not be null!");
            }
            this.context = context;
            this.newsList = news;
            this.newsListForSearch = news;
        }

        @Override
        public int getCount() {
            if (newsListForSearch == null) return 0;
            return newsListForSearch.size();
        }

        @Override
        public Object getItem(int position) {
            if (newsListForSearch == null) return 0;
            return newsListForSearch.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.layout_list_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();

            }
            News myNew = newsListForSearch.get(position);
            viewHolder.title.setText(myNew.getTitle());
            viewHolder.date.setText(myNew.getDate());

            if(myNew.getImageUrl() != null){
                try {
                    URL imageURL = new URL(myNew.getImageUrl());
                    bitmapCache.loadBitmap(imageURL, viewHolder.backgroundImageView);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
            return convertView;
        }

        @Override
        public Filter getFilter() {
            Filter filter= new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();

                    if(constraint == null || constraint.length() == 0){
                        filterResults.count = newsList.size();
                        filterResults.values = newsList;

                    }
                    else{
                        String searchString = constraint.toString().toLowerCase();
                        List<News> resultList = new ArrayList<>();

                        for(News myNews: newsList ){
                            if(myNews.getTitle().contains(searchString)){
                                resultList.add(myNews);
                            }

                            filterResults.count = resultList.size();
                            filterResults.values = resultList;
                        }

                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    newsListForSearch = (List<News>) results.values;
                    notifyDataSetChanged();
                }
            };
            return filter;
        }


        private class ViewHolder {
            private TextView title;
            private TextView date;
            private ImageView backgroundImageView;

            public ViewHolder(View convertView) {
                title = convertView.findViewById(R.id.titleTextView);
                date = convertView.findViewById(R.id.date_text);
                backgroundImageView = convertView.findViewById(R.id.backgroundImageView);
            }
        }

    }

    private void setViews() {
        newsListView = findViewById(R.id.newsListView);
        noDataView = findViewById(R.id.no_data_view);
        newsListView.setEmptyView(noDataView);


        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Intent detailIntent = new Intent(MainActivity.this,DetailActivity.class);
                detailIntent.putExtra(DETAIL_INTENT_KEY,news.get(position).getLink());
                startActivity(detailIntent);
            }
        });
    }

    public class NewsAsyncTask extends AsyncTask<Void, Void, Exception> {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("News fetching...");
            progressDialog.show();

        }

        @Override
        protected Exception doInBackground(Void... voids) {

            try {
                URL newsUrl = new URL("https://www.aa.com.tr/tr/rss/default?cat=guncel");

                XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                xmlPullParserFactory.setNamespaceAware(true); // Default false
                XmlPullParser parser = xmlPullParserFactory.newPullParser();
                parser.setInput(getInputStream(newsUrl),"UTF_8");

                boolean inItemTag = false;

                int eventType = parser.getEventType();
                News newsObject = null;
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    if (eventType == XmlPullParser.START_TAG) {
                        if (parser.getName().equalsIgnoreCase("item")) {
                            inItemTag = true;
                            newsObject = new News();
                        } else if (parser.getName().equalsIgnoreCase("guid")) {
                            if (inItemTag) {
                                newsObject.setGuid(parser.nextText());
                            }
                        } else if (parser.getName().equalsIgnoreCase("title")) {
                            if (inItemTag) {
                                newsObject.setTitle(parser.nextText());
                            }
                        } else if (parser.getName().equalsIgnoreCase("link")) {
                            if (inItemTag) {
                                newsObject.setLink(parser.nextText());
                            }
                        } else if (parser.getName().equalsIgnoreCase("description")) {
                            if (inItemTag) {
                                newsObject.setDescription(parser.nextText());
                            }
                        }else if (parser.getName().equalsIgnoreCase("pubDate")) {
                            if (inItemTag) {
                                newsObject.setDate(parser.nextText());
                            }
                        }else if (parser.getName().equalsIgnoreCase("image")) {
                            if (inItemTag) {
                                newsObject.setImageUrl(parser.nextText());
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                        news.add(newsObject);
                        inItemTag = false;
                    }
                    eventType = parser.next();
                }
            } catch (MalformedURLException e) {
                exception = e;
            } catch ( XmlPullParserException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            }
            return exception;
        }

        @Override
        protected void onPostExecute(Exception ex) {
            super.onPostExecute(ex);

            if (ex != null) {
                Toast.makeText(MainActivity.this, "While fetching, an error is occurred. Please try again." + ex.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                listAdapter.notifyDataSetChanged();
            }

            progressDialog.dismiss();
        }
    }
}