package com.amitay.arye.songsmash;

import android.provider.BaseColumns;

/**
 * Created by JERLocal on 1/23/2017.
 *
 * This class contains the definitions of the tables in the local database
 */

final class DbConstants {
    private DbConstants(){
        throw new AssertionError("Can't create DbConstants class");
    }

    /**
     * Songs table
     * */
    public static abstract class Songs implements BaseColumns {
        public static final String TABLE_NAME = "SongsTable";
        public static final String SONG_NAME = "SongName";
        public static final String LIKED = "Liked";
    }

    /**
     * Enum that represents song status - Unknown used for query all the data
     * */
    public enum SongStatus{
        Unknown,

        NotSet,

        Liked,

        Unlkied
    }
}
