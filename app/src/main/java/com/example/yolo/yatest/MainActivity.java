package com.example.yolo.yatest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public static DBHelper dbHelper;

    public static boolean internetActive(Context c){
        ConnectivityManager conManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conManager.getActiveNetworkInfo();
        return ( netInfo != null && netInfo.isConnected() );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        dbHelper = new DBHelper(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private Button translate_button;
        private ToggleButton translate_direction_button;
        private ToggleButton history_toggle;
        private ListView history_list;
        private WebView translate_webview;
        private String api_key = "trnsl.1.1.20170419T211235Z.6a560078798c2f09.a9a817d264aa28783ea8216b37d07bb1a3dfa4dd";
        private String translate_direction = "en-ru";
        private EditText translate_text;
        private TextView debug_text;
        public String response_text = "";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                final View rootView = inflater.inflate(R.layout.fragment_translator, container, false);

                // ?????
                translate_button = (Button)rootView.findViewById(R.id.button);
                translate_webview = (WebView)rootView.findViewById(R.id.webview);
                translate_direction_button = (ToggleButton)rootView.findViewById(R.id.toggleButton);
                translate_text = (EditText)rootView.findViewById(R.id.editText);
                debug_text = (TextView)rootView.findViewById(R.id.textView);

                // Направление перевода
                translate_direction_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            translate_direction = "ru-en";
                        } else {
                            translate_direction = "en-ru";
                        }
                    }
                });

                // Кнопка перевести
                translate_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (translate_text.getText().toString().length() == 0) {
                            Toast.makeText(getContext(), getResources().getString(R.string.no_text), Toast.LENGTH_LONG).show();
                        }
                        else
                        if (!internetActive(getContext())) {
                            Toast.makeText(getContext(), getResources().getString(R.string.offline), Toast.LENGTH_LONG).show();
                        } else {
                            String text_to_translate = translate_text.getText().toString();
                            String original_input = text_to_translate;

                            try {
                                text_to_translate = URLEncoder.encode(text_to_translate, "utf-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + api_key + "&text=" + text_to_translate + "&lang=" + translate_direction;
                            //translate_webview.loadUrl(url);
                            //debug_text.setText(url);

                            // Instantiate the RequestQueue.
                            RequestQueue queue = Volley.newRequestQueue(getContext());

                            // Request a string response from the provided URL.
                            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject json = new JSONObject(response);
                                                JSONArray json_array = json.getJSONArray("text");
                                                debug_text.setText(json_array.getString(0));
                                                response_text = json_array.getString(0);
                                            } catch (JSONException e) {
                                                Toast.makeText(getContext(), getResources().getString(R.string.smth_wrong), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.smth_wrong), Toast.LENGTH_LONG).show();
                                }
                            });
                            // Add the request to the RequestQueue.
                            queue.add(stringRequest);

                            // Сохраним обьект в бд
                            // создаем объект для данных
                            ContentValues cv = new ContentValues();
                            cv.put("input", original_input);
                            cv.put("translation", response_text);
                            // подключаемся к БД
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            // вставляем запись и получаем ее ID
                            db.insert("mytable", null, cv);
                            //reload_history();
                        }
                    }
                });
                // ?????

                return rootView;
            } else {
                View rootView = inflater.inflate(R.layout.fragment_history, container, false);

                history_toggle = (ToggleButton)rootView.findViewById(R.id.toggleButton2);
                history_list = (ListView)rootView.findViewById(R.id.listView);

                ArrayList<String> strings = new ArrayList<String>();
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                try {
                    Cursor c = db.rawQuery("SELECT id, input, translation, favourite FROM mytable where 1 = 1 LIMIT 100", null);

                    if (c != null ) {
                        if  (c.moveToFirst()) {
                            do {
                                String input = c.getString(c.getColumnIndex("input"));
                                String translation = c.getString(c.getColumnIndex("translation"));
                                int id = c.getInt(c.getColumnIndex("id"));
                                int fav = c.getInt(c.getColumnIndex("favourite"));
                                strings.add(input + " " + translation);
                            }while (c.moveToNext());
                        }
                    }
                } catch (SQLiteException se ) {
                    Log.e(getClass().getSimpleName(), "Could not create or Open the database");
                }

                // используем адаптер данных
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_list_item_1, strings);

                history_list.setAdapter(adapter);

                // Избранное/история
                history_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            // Избранное
                        } else {
                            // История
                        }
                    }
                });

                return rootView;
//                View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//                TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//                textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
//                return rootView;
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Перевод";
                case 1:
                    return "История";
            }
            return null;
        }
    }

    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "by_database", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table mytable ("
                    + "id integer primary key autoincrement,"
                    + "input text,"
                    + "translation text,"
                    + "favourite integer" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
