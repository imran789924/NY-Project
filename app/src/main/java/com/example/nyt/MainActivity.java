package com.example.nyt;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> urls = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    ListView listView;
    SQLiteDatabase sqLiteDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, titles);
        listView.setAdapter(arrayAdapter);


        sqLiteDatabase = this.openOrCreateDatabase("NewsArticles", MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS ARTICLES(ID INTEGER PRIMARY KEY, URL VARCHAR UNIQUE, TITLE VARCHAR)");

        updateListView();




        downLoadTask task = new downLoadTask();

        try{
//            task.execute("https://newsapi.org/v2/top-headlines?country=us&apiKey=4ec56298d9124daab0f81ece9eb232da");
        }catch (Exception e){
            e.printStackTrace();
        }



    }





    public void updateListView(){

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM ARTICLES", null);

        int titleIndex = cursor.getColumnIndex("TITLE");
        int urlIndex = cursor.getColumnIndex("URL");

        if (cursor.moveToFirst()){
            titles.clear();
            urls.clear();
        }

        while (cursor.moveToNext()){
            titles.add(cursor.getString(titleIndex));
            urls.add(cursor.getString(urlIndex));
        }

        arrayAdapter.notifyDataSetChanged();

    }




    public class downLoadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try{
                url = new URL(urls[0]);

//                httpURLConnection = (HttpURLConnection) url.openConnection();
//                InputStream inputStream =  httpURLConnection.getInputStream();

                InputStream inputStream = url.openStream();

//                InputStream inputStream = new BufferedInputStream(new DataInputStream(httpURLConnection.getInputStream()));
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

//                int data = inputStreamReader.read();
//
//                while (data != -1){
//                    char r = (char) data;
//                    result += r;
//                    data = inputStreamReader.read();
//                }

                String line = bufferedReader.readLine();

                while (line != null){
                    result += line;
                    line = bufferedReader.readLine();
                }

//                String lol = "really? " + result.length();
//
//                Log.v("Result length", lol);

//                Printing the JSON from url
                /*
                int maxLogSize = 1300;
                for(int i = 0; i <= result.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i+1) * maxLogSize;
                    end = end > result.length() ? result.length() : end;
                    Log.v("Full JSON", result.substring(start, end));
                }
                */


//                Log.i("All JSONS", result);
//                System.out.println("IMRAN MU MU" + result);
////                inputStreamReader.close();
//                bufferedReader.close();

                return result;

            } catch (Exception e){
                e.printStackTrace();
                Log.i("Exception!", "Caught the exception");
            }



            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            String title;
            String url;

            try {
                JSONObject jsonObject = new JSONObject(result);

                JSONArray jsonArray = jsonObject.getJSONArray("articles");

//                Log.i("JSON ARRAY", jsonArray.toString());



                sqLiteDatabase.execSQL("DELETE FROM ARTICLES");


                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonOfArticles = jsonArray.getJSONObject(i);
                    url = jsonOfArticles.getString("url");
                    title = jsonOfArticles.getString("title");
//                    sqLiteDatabase.execSQL("INSERT OR IGNORE INTO ARTICLES (URL, TITLE) VALUES ('" + url + "', '" + title + "')");
                    String sql = "INSERT OR IGNORE INTO ARTICLES (URL, TITLE) VALUES (?, ?)";

                    SQLiteStatement statement = sqLiteDatabase.compileStatement(sql);

                    statement.bindString(1, url);
                    statement.bindString(2, title);

                    statement.execute();

                }

                updateListView();


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


//    public static void appendLog(String text)
//    {
//        File logFile = new File("sdcard/log.txt");
//        if (!logFile.exists())
//        {
//            try
//            {
//                logFile.createNewFile();
//            } catch (IOException e)
//            {
//                e.printStackTrace();
//            }
//        }
//        try
//        {
//            // BufferedWriter for performance, true to set append to file flag
//            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
//            buf.append(text);
//            buf.newLine();
//            buf.close();
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }
}
