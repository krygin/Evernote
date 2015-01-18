package ru.bmstu.evernote.provider.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ivan on 14.01.2015.
 */
public final class EvernoteContract {
    public static final String AUTHORITY = "ru.bmstu.evernote.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Notebooks implements BaseColumns {
        public static final String TABLE_NAME = "notebooks";
        public static final String NAME = "name";
        public static final String GUID = "guid";
        public static final String CREATED = "created";
        public static final String UPDATED = "updated";
        public static final String USN = "usn";
        public static final String STATE_DELETED = "state_deleted";
        public static final String STATE_SYNC_REQUIRED = "state_sync_required";
        public static final String[] ALL_COLUMNS_PROJECTION = {_ID, NAME, GUID, CREATED, UPDATED, USN, STATE_DELETED, STATE_SYNC_REQUIRED};
        static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                NAME + " TEXT UNIQUE NOT NULL" + "," +
                GUID + " TEXT UNIQUE" + "," +
                CREATED + " INTEGER NOT NULL" + "," +
                UPDATED + " INTEGER NOT NULL" + "," +
                USN + " TEXT UNIQUE" + "," +
                STATE_DELETED + " INTEGER NOT NULL" + "," +
                STATE_SYNC_REQUIRED + " INTEGER NOT NULL" + ");";
        static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String DELETED_SELECTION = STATE_DELETED + "=" + StateDeleted.TRUE.ordinal();
        public static final String NOT_DELETED_SELECTION = STATE_DELETED + "=" + StateDeleted.FALSE.ordinal();
        public static final String NOT_SYNCED_SELECTION = STATE_SYNC_REQUIRED + "=" + StateSyncRequired.PENDING.ordinal();
        public static final String WITH_SPECIFIED_GUID_SELECTION = GUID + "=?";
        public static final String WITH_SPECIFIED_NAME_SELECTION = NAME + "=?";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.notebooks";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.notebooks";

    }

    public static final class Notes implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String GUID = "guid";
        public static final String CREATED = "created";
        public static final String UPDATED = "updated";
        public static final String USN = "usn";
        public static final String STATE_DELETED = "state_deleted";
        public static final String STATE_SYNC_REQUIRED = "state_sync_required";
        public static final String NOTEBOOKS_ID = "notebooks_id";
        public static final String[] ALL_COLUMNS_PROJECTION = {_ID, TITLE, CONTENT, GUID, CREATED, UPDATED, USN, STATE_DELETED, STATE_SYNC_REQUIRED, NOTEBOOKS_ID};
        static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                TITLE + " TEXT NOT NULL" + "," +
                CONTENT + " TEXT" + "," +
                GUID + " TEXT UNIQUE" + "," +
                CREATED + " INTEGER NOT NULL" + "," +
                UPDATED + " INTEGER NOT NULL" + "," +
                USN + " INTEGER UNIQUE" + "," +
                STATE_DELETED + " INTEGER NOT NULL" + "," +
                STATE_SYNC_REQUIRED + " INTEGER NOT NULL" + "," +
                NOTEBOOKS_ID + " INTEGER NOT NULL" +
                " REFERENCES " + Notebooks.TABLE_NAME + " (" + Notebooks._ID + ")" +
                " ON DELETE CASCADE" + ");";
        static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String DELETED_SELECTION = STATE_DELETED + "=" + StateDeleted.TRUE.ordinal();
        public static final String NOT_DELETED_SELECTION = STATE_DELETED + "=" + StateDeleted.FALSE.ordinal();
        public static final String WITH_SPECIFIED_NOTEBOOKS_ID_SELECTION = NOTEBOOKS_ID + "=?";
        public static final String WITH_SPECIFIED_GUID_SELECTION = GUID + "=?";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.notes";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.notes";
    }

    public static final class Resources implements BaseColumns {
        public static final String TABLE_NAME = "resources";
        public static final String GUID = "guid";
        public static final String FILENAME = "filename";
        public static final String MIME_TYPE = "mime_type";
        public static final String USN = "usn";
        public static final String STATE_DELETED = "state_deleted";
        public static final String STATE_SYNC_REQUIRED = "state_sync_required";
        public static final String NOTES_ID = "notes_id";
        public static final String[] ALL_COLUMNS_PROJECTION = {_ID, GUID, FILENAME, MIME_TYPE, USN, STATE_DELETED, STATE_SYNC_REQUIRED, NOTES_ID};
        static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                GUID + " TEXT UNIQUE" + "," +
                FILENAME + " TEXT" + "," +
                MIME_TYPE + " TEXT" + "," +
                USN + " INTEGER UNIQUE" + "," +
                STATE_DELETED + " INTEGER NOT NULL" + "," +
                STATE_SYNC_REQUIRED + " INTEGER NOT NULL" + "," +
                NOTES_ID + " INTEGER NOT NULL" +
                " REFERENCES " + Notes.TABLE_NAME + " (" + Notes._ID + ")" +
                " ON DELETE CASCADE" + ");";
        static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
        public static final String DELETED_SELECTION = STATE_DELETED + "=" + StateDeleted.TRUE.ordinal();
        public static final String NOT_DELETED_SELECTION = STATE_DELETED + "=" + StateDeleted.FALSE.ordinal();
        public static final String WITH_SPECIFIED_NOTES_ID_SELECTION = NOTES_ID + "=?";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, TABLE_NAME);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.resources";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.resources";
    }

    public static enum StateDeleted {
        FALSE,
        TRUE
    }

    public static enum StateSyncRequired {
        PENDING,
        IN_PROCESS,
        SYNCED
    }
}