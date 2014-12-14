package ru.bmstu.evernote.provider.database.tables;

import android.provider.BaseColumns;

/**
 * Created by Ivan on 14.12.2014.
 */
public interface Notes extends BaseColumns {
    String TABLE_NAME = "notes";

    String TITLE = "title";
    String GUID = "guid";
    String UPDATED = "updated";
    String CREATED = "created";
    String USN = "usn";
    String NOTEBOOKS_ID = "notebooks_id";
    String TABLE_PATH = "notes";

    String[] ALL_COLUMNS = new String[]{_ID, TITLE, GUID, CREATED, UPDATED, USN, NOTEBOOKS_ID};
    String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
            TITLE + " TEXT UNIQUE NOT NULL" + "," +
            GUID + " TEXT" + "," +
            CREATED + " NUMERIC NOT NULL" + "," +
            UPDATED + " NUMERIC NOT NULL" + "," +
            USN + " INTEGER" + "," +
            NOTEBOOKS_ID + " INTEGER NOT NULL" + " REFERENCES " + Notebooks.TABLE_NAME + " (" + Notebooks._ID + ")" + " ON DELETE CASCADE" +
            ")";
    String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" +
            TITLE + "," +
            GUID + "," +
            CREATED + "," +
            UPDATED + "," +
            USN + "," +
            NOTEBOOKS_ID +
            ") VALUES ( ?, ?, ?, ?, ?, ? )";

    String[] DEFAULT_PROJECTION = new String[]{_ID, TITLE, GUID, CREATED, UPDATED, USN};

    String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.notes";
    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.notes";

    String[] BASE_PROJECTION = new String[]{_ID, TITLE, CREATED, UPDATED};
    String[] BASE_SELECTION_CLAUSE = new String[] { "WHERE " + NOTEBOOKS_ID + "= ?" };

}