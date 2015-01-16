package ru.bmstu.evernote.provider.database.tables;

import android.provider.BaseColumns;

/**
 * Created by Ivan on 14.12.2014.
 */
public interface NotesTable extends BaseColumns {
    String TABLE_NAME = "notes";

    String TITLE = "title";
    String GUID = "guid";
    String CONTENT = "content";
    String UPDATED = "updated";
    String CREATED = "created";
    String USN = "usn";
    String NOTEBOOKS_ID = "notebooks_id";
    String IS_LOCALLY_DELETED = "is_locally_deleted";

    String[] ALL_COLUMNS = new String[]{_ID, TITLE, CONTENT, GUID, CREATED, UPDATED, USN, NOTEBOOKS_ID};
    String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
            TITLE + " TEXT" + "," +
            GUID + " TEXT" + "," +
            CONTENT + " TEXT" + "," +
            CREATED + " NUMERIC NOT NULL" + "," +
            UPDATED + " NUMERIC NOT NULL" + "," +
            USN + " INTEGER" + "," +
            IS_LOCALLY_DELETED + " INTEGER" + "," +
            NOTEBOOKS_ID + " INTEGER NOT NULL" + " REFERENCES " + NotebooksTable.TABLE_NAME + " (" + NotebooksTable._ID + ")" + " ON DELETE CASCADE" +
            ")";

    String[] DEFAULT_PROJECTION = new String[]{_ID, TITLE, GUID, CREATED, UPDATED, USN};

    String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.notes";
    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.notes";

    String NOT_DELETED_CONDITION = IS_LOCALLY_DELETED + "=0";

    String[] BASE_PROJECTION = new String[]{_ID, TITLE, CREATED, UPDATED};
    String[] BASE_SELECTION_CLAUSE = new String[] { "WHERE " + NOTEBOOKS_ID + "= ?" };

    String TABLE_NAME_TRANSACT = TABLE_NAME + "/transact";
}