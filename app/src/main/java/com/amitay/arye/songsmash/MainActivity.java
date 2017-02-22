package com.amitay.arye.songsmash;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    //Class members
    private SQLiteDatabase mSongSmashDb;
    private SongsCursorAdapter mSongsAdapter;
    private SwipeActionAdapter mSwipeActionAdapter;

    //Consts
    private final int FILE_SELECT_CODE = 1;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;

    //The applied filter on the list view
    private DbConstants.SongStatus appliedFilter = DbConstants.SongStatus.Unknown;

    public static final String TAG = "SongSmashTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSongSmashDb = new DBHelper(this).getReadableDatabase();

        initializeUI();

        initializeSwipableListView();

        checkForIncomingIntent();

        Log.d(TAG, "SongSmash created successfully");
    }

    /**
     * Initialize the list view with thw Cursor adapter and the Swipe Action adapter
     * */
    private void initializeSwipableListView() {
        mSongsAdapter = new SongsCursorAdapter(this, queryDb("", appliedFilter));

        mSwipeActionAdapter = new SwipeActionAdapter(mSongsAdapter);

        mSwipeActionAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener() {
            @Override
            public boolean hasActions(int position, SwipeDirection direction) {
                return true;
            }

            @Override
            public boolean shouldDismiss(int position, SwipeDirection direction) {
                return false;
            }

            @Override
            public void onSwipe(int[] position, SwipeDirection[] direction) {

                View view = getViewByPosition(position[0]);
                String songName = ((TextView) view.findViewById(R.id.txtSongName)).getText().toString();

                if(     direction[0] == SwipeDirection.DIRECTION_FAR_LEFT ||
                        direction[0] == SwipeDirection.DIRECTION_NORMAL_LEFT ){
                    updateSongStatus(songName, DbConstants.SongStatus.Unlkied);
                }
                else if(direction[0] == SwipeDirection.DIRECTION_FAR_RIGHT ||
                        direction[0] == SwipeDirection.DIRECTION_NORMAL_RIGHT ){
                    updateSongStatus(songName, DbConstants.SongStatus.Liked);
                }

                mSongsAdapter.changeCursor(queryDb("", appliedFilter));
            }

            private View getViewByPosition(int pos) {
                final int firstListItemPosition = listSongsList.getFirstVisiblePosition();
                final int lastListItemPosition = firstListItemPosition + listSongsList.getChildCount() - 1;

                if (pos < firstListItemPosition || pos > lastListItemPosition ) {
                    return listSongsList.getAdapter().getView(pos, null, listSongsList);
                } else {
                    final int childIndex = pos - firstListItemPosition;
                    return listSongsList.getChildAt(childIndex);
                }
            }
        });

        mSwipeActionAdapter
                .setDimBackgrounds(true)
                .setListView(listSongsList)
                .addBackground(SwipeDirection.DIRECTION_FAR_LEFT, R.layout.row_bg_left)
                .addBackground(SwipeDirection.DIRECTION_NORMAL_LEFT, R.layout.row_bg_left)
                .addBackground(SwipeDirection.DIRECTION_FAR_RIGHT, R.layout.row_bg_right)
                .addBackground(SwipeDirection.DIRECTION_NORMAL_RIGHT,R.layout.row_bg_right);

        listSongsList.setAdapter(mSwipeActionAdapter);

        listSongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = ((TextView) view.findViewById(R.id.txtSongName)).getText().toString();
                searchInYouTube(songName);
            }
        });
    }

    /**
     * Starting YouTube application using Intent with search query for the given song name
     *
     * @param songName the song name to search in YouTube
     * */
    private void searchInYouTube(String songName) {
        Log.d(TAG, "GOTO YouTube with search: [" + songName + "]");

        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra("query", songName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Asks for permissions if not granted
     *
     * @param permission the permission to ask
     * @param requestId The request ID - to be used in permission request result
     * */
    private void askForPermissionsIfNeeded(String permission, int requestId) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Asking for "+permission+" permission");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {

                // Show an explanation to the user asynchronously -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        requestId);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Read permissions granted!!");

                    } else {
                        Log.d(TAG, "Read permissions denied :(");
                    }
                    return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Write permissions granted!!");
                } else {
                    Log.d(TAG, "Write permissions denied :(");
                }
                return;
            }
        }
    }

    /**
     * Checks if the application has been started from other intent.
     * Used to catch Shazam share
     * */
    private void checkForIncomingIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleReceivedText(intent);
            }
        }
    }

    /**
     * Handling received text in intent:
     * If the Intent received from Shazam, the received song will be added to the database
     * */
    private void handleReceivedText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Log.d(TAG, "Received text: " + sharedText);

            Pattern shazamPattern = Pattern.compile("(I used Shazam to discover)(.*)(https://shz.am/.*)");
            Matcher match = shazamPattern.matcher(sharedText);

            if(match.matches()){
                Log.d(TAG, "Song name is: " + match.group(2));
                insertNewSong(match.group(2));

                mSongsAdapter.changeCursor(queryDb("", DbConstants.SongStatus.NotSet));
                //Remove the data to avoid re insertion in next time
                intent.removeExtra(Intent.EXTRA_TEXT);
            }
            else
                Log.d(TAG, "Match not found - Does the intent received from Shazam?");
        }
    }

    /**
     * Initializing the UI members
     * */
    private void initializeUI() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listSongsList = (ListView) findViewById(R.id.listSongsList);
    }

    /**
     * query the DB and get cursor with the query result.
     *
     * @param songName song name to filter from the DB - use empty string to get all
     * @param songStatus song status to filter from the DB - use SongStatus.Unknown to get all
     *
     * @return Cursor with the query result
     *
     * */
    private Cursor queryDb(String songName, DbConstants.SongStatus songStatus) {
        Log.d(TAG, "Query DB with params: song name [" + songName + "], song status [" + String.valueOf(songStatus) + "]");
        String where = null;
        String[] whereValues = null;

        if (!songName.isEmpty()) {
            where = DbConstants.Songs.SONG_NAME + " LIKE ?";
            whereValues = new String[]{"%" + songName + "%"};
        }

        if(songStatus != DbConstants.SongStatus.Unknown){
            where = DbConstants.Songs.LIKED + " LIKE ?";
            whereValues = new String[]{"%" + String.valueOf(songStatus) + "%"};
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_import) {
            askForPermissionsIfNeeded(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            showFileChooser();
        } else if (id == R.id.nav_export) {
            askForPermissionsIfNeeded(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            String exportedFilePath = exportDbToCsv();
            if(exportedFilePath != null){
                sentMailWithAttachment(exportedFilePath);
            }
        } else if (id == R.id.filter_all) {
            appliedFilter = DbConstants.SongStatus.Unknown;
            mSongsAdapter.changeCursor(queryDb("", DbConstants.SongStatus.Unknown));
        } else if (id == R.id.filter_notSet) {
            appliedFilter = DbConstants.SongStatus.NotSet;
            mSongsAdapter.changeCursor(queryDb("", DbConstants.SongStatus.NotSet));
        } else if (id == R.id.filter_liked) {
            appliedFilter = DbConstants.SongStatus.Liked;
            mSongsAdapter.changeCursor(queryDb("", DbConstants.SongStatus.Liked));
        } else if (id == R.id.filter_unliked) {
            appliedFilter = DbConstants.SongStatus.Unlkied;
            mSongsAdapter.changeCursor(queryDb("", DbConstants.SongStatus.Unlkied));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Starting mail application in send page with an attachment using Intent
     *
     * @param attachedFilePath the path to the attached file
     * */
    private void sentMailWithAttachment(String attachedFilePath) {
        Uri path = Uri.fromFile(new File(attachedFilePath));
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = { getString(R.string.DjExampleEmail) };
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mailSubject));
        startActivity(Intent.createChooser(emailIntent , getString(R.string.emailShareTitle)));
    }

    /**
     * Exports current database content to CSV file.
     *
     * @return path to the exported CSV file
     * */
    private String exportDbToCsv() {
        String exportedFilePath = null;
        Cursor cursor = queryDb("", DbConstants.SongStatus.Unknown);
        List<String[]> list = new ArrayList<>();

        cursor.moveToFirst();
        do {
            list.add(new String[] {
                    cursor.getString(cursor.getColumnIndex(DbConstants.Songs.SONG_NAME)),
                    cursor.getString(cursor.getColumnIndex(DbConstants.Songs.LIKED))
            });
        }while (cursor.moveToNext());

        try {
             exportedFilePath = CsvHelper.writeToCsv(list, null);
        } catch (IOException ex) {
            Log.d(TAG, "Failed to export csv : " + ex.toString());
        }
        return exportedFilePath;
    }

    /**
     * Starting file chooser application using Intent
     * */
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.selectFileHeader)),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.NoFileManagerMsg,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    importSongsFromFileChooserIntent(data);
                }
                else{
                    Toast.makeText(this, R.string.noFileSelected,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Imports the songs from the selected file that selected in the file chooser into the database
     * */
    private void importSongsFromFileChooserIntent(Intent data){
        Uri uri = data.getData();
        Log.d(TAG, "File Uri: " + uri.toString());
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
                insertNewSong(songData[0].trim());
            }

            mSongsAdapter.changeCursor(queryDb("", DbConstants.SongStatus.NotSet));
        }
        catch (FileNotFoundException ex){
            Log.d(TAG, "File not found : " + ex.toString());
        }
    }

    /**
     * Inserts new song to the database with status SongStatus.NotSet
     * */
    private void insertNewSong(String songName) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.Songs.SONG_NAME, songName);
        values.put(DbConstants.Songs.LIKED, String.valueOf(DbConstants.SongStatus.NotSet));

        long id = mSongSmashDb.insert(DbConstants.Songs.TABLE_NAME, null, values);
        if (id == -1)
            Log.d(TAG, "DB insertion error on value [" + songName + "]");
    }

    /**
     * Updates song status - overrides the current status
     *
     * @param songName The name of the song to be updated
     * @param newStatus The new status to set to the song
     * */
    private void updateSongStatus(String songName, DbConstants.SongStatus newStatus){
        Log.d(TAG, "Updating song [" + songName + "] status to: [" + String.valueOf(newStatus) + "]");
        ContentValues values = new ContentValues();
        values.put(DbConstants.Songs.LIKED, String.valueOf(newStatus));

        String where = DbConstants.Songs.SONG_NAME + " LIKE ?";
        String[] whereValues = new String[]{"%" + songName + "%"};

        int numOfAffectedRows = mSongSmashDb.update(
                DbConstants.Songs.TABLE_NAME,
                values,
                where,
                whereValues);

        Log.d(TAG, "Number of affected rows: [" + numOfAffectedRows + "]");
    }
}