package com.amitay.arye.songsmash;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //UI members
    private ListView listSongsList;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SwipeLayout swipeLayout;

    //Class members
    private SQLiteDatabase mSongSmashDb;
    private SongsCursorAdapter mCursorAdapter;

    private final int FILE_SELECT_CODE = 1;
    private final String TAG = "SongSmashTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();

        mSongSmashDb = new DBHelper(this).getReadableDatabase();

        mCursorAdapter = new SongsCursorAdapter(this, queryDb(""));

        //SwipeActionAdapter

        listSongsList.setAdapter(mCursorAdapter);

        listSongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = ((TextView) view.findViewById(R.id.txtSongName)).getText().toString();
                Log.d(TAG, "GOTO YouTube with search: [" + songName + "]");

                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("com.google.android.youtube");
                intent.putExtra("query", songName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        checkForIncomingIntent();

        Log.d(TAG, "SongSmash created");
    }

    private void checkForIncomingIntent() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                //handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Log.d(TAG, "Received text: " + sharedText);

            Pattern shazamPattern = Pattern.compile("(I used Shazam to discover)(.*)(https://shz.am/.*)");
            Matcher match = shazamPattern.matcher(sharedText);

            if(match.matches()){
                Log.d(TAG, "Song name is: " + match.group(2));
                insertNewSong(match.group(2));

                mCursorAdapter.changeCursor(queryDb(""));
                //Remove the data to avoid re insertion in next time
                intent.removeExtra(Intent.EXTRA_TEXT);
            }
            else
                Log.d(TAG, "Match not found");
        }
    }

    /*
    * Initialize UI members
    * */
    private void initializeUI() {
        swipeLayout = (SwipeLayout) findViewById(R.id.content_main);

        //set show mode.
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

        //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        //swipeLayout.

        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout swipeLayout) {
                Log.d(TAG, "onStartOpen");
            }

            @Override
            public void onOpen(SwipeLayout swipeLayout) {
                Log.d(TAG, "onOpen");
            }

            @Override
            public void onStartClose(SwipeLayout swipeLayout) {
                Log.d(TAG, "onStartClose");
            }

            @Override
            public void onClose(SwipeLayout swipeLayout) {
                Log.d(TAG, "onClose");
            }

            @Override
            public void onUpdate(SwipeLayout swipeLayout, int i, int i1) {
                Log.d(TAG, "onUpdate");
            }

            @Override
            public void onHandRelease(SwipeLayout swipeLayout, float v, float v1) {
                Log.d(TAG, "onHandRelease");
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawerLayout.setDrawerListener(toggle);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listSongsList = (ListView) findViewById(R.id.listSongsList);
    }

    /*
    * query the DB and get cursor.
    * getting name and phone in order to filter by them (contains),
    * if using an empty string - not filtering
    * */
    private Cursor queryDb(String songName) {

        String where = null;
        String[] whereValues = null;

        if (!songName.isEmpty()) {
            where = DbConstants.Songs.SONG_NAME + " LIKE ?";
            whereValues = new String[]{"%" + songName + "%"};
        }

        return mSongSmashDb.query(
                DbConstants.Songs.TABLE_NAME,
                new String[]{DbConstants.Songs._ID, DbConstants.Songs.SONG_NAME, DbConstants.Songs.LIKED},
                where,
                whereValues,
                null,
                null,
                null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_import) {
            showFileChooser();
        } else if (id == R.id.nav_export) {

        } else if (id == R.id.nav_deleteDb) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.filter_all) {

        } else if (id == R.id.filter_liked) {

        } else if (id == R.id.filter_unliked) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to import"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    importSongsFromIntent(data);
                }
                else{
                    Toast.makeText(this, "No file selected.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void importSongsFromIntent(Intent data){
        // Get the Uri of the selected file
        Uri uri = data.getData();
        Log.d(TAG, "File Uri: " + uri.toString());
        // Get the path
        String path = CsvHelper.getFilePath(uri);

        if(path == null){
            Log.d(TAG, "File Path is null");
            return;
        }
        Log.d(TAG, "File Path: " + path);

        try{
            InputStream csvInputStream = new FileInputStream(new File(path));
            List<String[]> csvData = CsvHelper.readCsv(csvInputStream);

            for(String[] songData : csvData){
                String songName = songData[0].trim();

                insertNewSong(songName);
            }

            mCursorAdapter.changeCursor(queryDb(""));

        }
        catch (FileNotFoundException ex){
            Log.d(TAG, "File not found : " + ex.toString());
        }

    }

    private void insertNewSong(String songName) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.Songs.SONG_NAME, songName);
        values.put(DbConstants.Songs.LIKED, false);
        //insert returning an ID - no use for now
        long id = mSongSmashDb.insert(DbConstants.Songs.TABLE_NAME, null, values);
        if (id == -1)
            Log.d(TAG, "DB insertion error on value [" + songName + "]");
    }
}
