package com.amitay.arye.songsmash;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by A&A on 2/21/2017.
 */

public class SongsCursorAdapter extends CursorAdapter {

    private Context mContext;
    private LayoutInflater inflater;

    public SongsCursorAdapter(Context context, Cursor c) {
        super(context, c, false);
        mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.song_compact_layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView txtSongName = (TextView) view.findViewById(R.id.txtSongName);
        //TextView txtPhone = (TextView) view.findViewById(R.id.txtListContactPhone);
        //ImageView img = (ImageView) view.findViewById(R.id.imgPhone);

        txtSongName.setText(cursor.getString(cursor.getColumnIndex(DbConstants.Songs.SONG_NAME)));
        /*
        String phone = cursor.getString(cursor.getColumnIndex(DbConstants.Contacts.PHONE));

        if(phone.length() > 0){
            txtPhone.setText(phone);
            img.setColorFilter(Color.GREEN);
        }
        else{
            txtPhone.setText(R.string.NoPhone);
            img.setColorFilter(Color.GRAY);
        }
         */
    }
}
