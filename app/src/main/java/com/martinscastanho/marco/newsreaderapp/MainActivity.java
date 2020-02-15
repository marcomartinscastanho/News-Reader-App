package com.martinscastanho.marco.newsreaderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static SQLiteDatabase sqLiteDatabase;
    static ListOfArticles articles;
    static ArrayList<Integer> articleIds;
    static ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        articles = new ListOfArticles();
        articleIds = new ArrayList<>();
        initListView();
        initDb();
        initIdsList();
    }

    public void initDb(){
        sqLiteDatabase = this.openOrCreateDatabase("News Feed", MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER, title VARCHAR, url VARCHAR)");
    }

    public void initIdsList(){
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM articles", null);
        boolean hasCursorData = cursor.moveToFirst();
        if(!hasCursorData){
            cursor.close();
            // get data from web
            Log.i("LoadingData", "from Web");
            getArticlesListFromWeb();
        }
        else{
            int idIndex = cursor.getColumnIndex("id");
            int titleIndex = cursor.getColumnIndex("title");
            int urlIndex = cursor.getColumnIndex("url");

            Log.i("LoadingData", "from DataBase");

            while(hasCursorData){
                addArticle(cursor.getInt(idIndex), cursor.getString(titleIndex), cursor.getString(urlIndex));

                hasCursorData = cursor.moveToNext();
            }
        }
    }

    public void getArticlesListFromWeb(){
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
    }

    public static void getArticleFromWeb(Integer id){
        Log.i("Downloading Article", "id: " + id);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute("https://hacker-news.firebaseio.com/v0/item/"+ id + ".json?print=pretty");
    }

    public static void addArticle(Integer id, String title, String url){
        articles.add(id, title, url);
        arrayAdapter.notifyDataSetChanged();
    }

    public static class DownloadTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... urls) {
            Log.i("doInBackground", "do http request");
            String result="";
            URL url;
            HttpURLConnection urlConnection;
            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){
                    char c = (char) data;
                    result += c;
                    data = reader.read();
                }
                return result;
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("onPostExecute", "decoding the response");
            try {
                // check if array,
                JSONArray jsonArray = new JSONArray(s);
                Log.i("onPostExecute", "response is a JSON array of length " + jsonArray.length());
                for(int i=0; i<20; i++){
                    MainActivity.getArticleFromWeb(jsonArray.getInt(i));
                }

            } catch (JSONException e) {
                // otherwise might be an object
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Log.i("onPostExecute", "response is a JSON object");
                    Integer id = jsonObject.getInt("id");
                    String title = jsonObject.getString("title");
                    String url = jsonObject.getString("url");
                    MainActivity.addArticle(id, title, url);
                    MainActivity.sqLiteDatabase.execSQL("INSERT INTO articles VALUES (?, ?, ?)", new Object[]{id, title, url});
                } catch (JSONException ex) {
                    Log.e("onPostExecute", "couldn't decode the response");
                    ex.printStackTrace();
                }
            }
        }
    }

    public void initListView(){
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, articles.getTitles());
        ListView listView = findViewById(R.id.newsListView);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                String url = articles.getArticleUrl(position);
                if(url == null){
                    Toast.makeText(MainActivity.this, "Couldn't get page!", Toast.LENGTH_SHORT).show();
                }
                else {
                    intent.putExtra("url", url);
                    startActivity(intent);
                }
            }
        });

    }

}
