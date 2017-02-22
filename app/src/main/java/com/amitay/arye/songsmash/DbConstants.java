package com.amitay.arye.songsmash;

import android.provider.BaseColumns;

/**
 * Created by JERLocal on 1/23/2017.
 */

final class DbConstants {
    private DbConstants(){
        throw new AssertionError("Can't create DbConstants class");
    }


    public static abstract class Songs implements BaseColumns {
        public static final String TABLE_NAME = "SongsTable";
        public static final String SONG_NAME = "SongName";
        public static final String LIKED = "Liked";
    }

    public static abstract class SyncedUsers implements BaseColumns {
        public static final String TABLE_NAME = "SyncedUsersTable";
        public static final String USER_NAME = "UserName";
    }

    public static abstract class SyncedUsersLikedSongs implements BaseColumns {
        public static final String TABLE_NAME = "SyncedUsersLikedSongsTable";
        public static final String USER_NAME = "UserName";
    }

    public enum SongStatus{
        Unknown,

        Liked,

        Unlkied
    }
}
