package ru.bmstu.evernote.provider.database.tables;

import android.provider.BaseColumns;

/**
 * Created by Ivan on 18.12.2014.
 */
public interface Resources extends BaseColumns {
    String TABLE_NAME = "resources";

    String MIME_TYPE = "mime_type";
    String PATH_TO_RESOURCE = "path_to_resource";
    String GUID = "guid";
    String NOTES_ID = "notes_id";

    String IS_LOCALLY_DELETED = "is_locally_deleted";

    String[] ALL_COLUMNS = new String[]{_ID, GUID, MIME_TYPE, PATH_TO_RESOURCE, NOTES_ID};
    String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
            PATH_TO_RESOURCE + " TEXT NOT NULL" + "," +
            GUID + " TEXT UNIQUE" + "," +
            MIME_TYPE + " TEXT NOT NULL" + "," +
            IS_LOCALLY_DELETED + " INTEGER" + "," +
            NOTES_ID + " INTEGER NOT NULL" + " REFERENCES " + Notes.TABLE_NAME + " (" + Notes._ID + ")" + " ON DELETE CASCADE" +
            ")";

    String[] DEFAULT_PROJECTION = new String[]{_ID, MIME_TYPE, PATH_TO_RESOURCE};
    String[] TEST_PROJECTION = new String[]{_ID, GUID, MIME_TYPE, PATH_TO_RESOURCE, NOTES_ID};

    String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    String NOT_DELETED_CONDITION = IS_LOCALLY_DELETED + "=0";
    String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.resources";
    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.resources";
}
