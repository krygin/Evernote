package ru.bmstu.evernote.provider.database.tables;

import android.provider.BaseColumns;

/**
 * Created by Ivan on 18.12.2014.
 */
public interface ResourcesTable extends BaseColumns {
    String TABLE_NAME = "resources";

    String MIME_TYPE = "mime_type";
    String FILENAME = "filename";
    String GUID = "guid";
    String NOTES_ID = "notes_id";

    String IS_LOCALLY_DELETED = "is_locally_deleted";

    String[] ALL_COLUMNS = new String[]{_ID, GUID, MIME_TYPE, FILENAME, NOTES_ID};
    String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
            GUID + " TEXT UNIQUE" + "," +
            MIME_TYPE + " TEXT NOT NULL" + "," +
            FILENAME + " TEXT" + "," +
            IS_LOCALLY_DELETED + " INTEGER" + "," +
            NOTES_ID + " INTEGER NOT NULL" + " REFERENCES " + NotesTable.TABLE_NAME + " (" + NotesTable._ID + ")" + " ON DELETE CASCADE" +
            ")";


    String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    String NOT_DELETED_CONDITION = IS_LOCALLY_DELETED + "=0";
    String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.resources";
    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.resources";
    String TABLE_NAME_TRANSACT = TABLE_NAME + "/transact";
}