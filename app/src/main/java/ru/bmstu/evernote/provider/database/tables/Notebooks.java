package ru.bmstu.evernote.provider.database.tables;

import android.provider.BaseColumns;

/**
 * Created by Ivan on 11.12.2014.
 */
public interface Notebooks extends BaseColumns {
    String TABLE_NAME = "notebooks";


    String NAME = "name";
    String GUID = "guid";
    String UPDATED = "updated";
    String CREATED = "created";
    String USN = "usn";


    String TABLE_PATH = "notebooks";

    String[] ALL_COLUMNS = new String[]{_ID, NAME, GUID, UPDATED, CREATED, USN};
    String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + _ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + "," + NAME + " TEXT UNIQUE" + ","
			+ GUID + " TEXT UNIQUE" + "," + UPDATED + " INTEGER" + "," + CREATED
			+ " INTEGER" + "," + USN + " INTEGER" + " )";

    String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" + NAME + "," + GUID
            + "," + UPDATED + "," + CREATED + "," + USN
            + ") VALUES ( ?, ?, ?, ?, ? )";

    String[] DEFAULT_PROJECTION = new String[]{_ID, NAME, GUID, UPDATED, CREATED, USN};


    String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.notebooks";
    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.notebooks";
}