package ru.bmstu.evernote.provider.database.tables;

import android.provider.BaseColumns;

/**
 * Created by Ivan on 11.12.2014.
 */
public interface NotebooksTable extends BaseColumns {
    String TABLE_NAME = "notebooks";


    String NAME = "name";
    String GUID = "guid";
    String CREATED = "created";
    String UPDATED = "updated";
    String USN = "usn";
    String IS_LOCALLY_DELETED = "is_locally_deleted";


    String[] ALL_COLUMNS = new String[]{_ID, NAME, GUID, CREATED, UPDATED, USN};
    String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
            NAME + " TEXT UNIQUE NOT NULL" + "," +
            GUID + " TEXT UNIQUE" + "," +
            UPDATED + " INTEGER NOT NULL" + "," +
            CREATED + " INTEGER NOT NULL" + "," +
            USN + " INTEGER" + "," +
            IS_LOCALLY_DELETED + " INTEGER" + " )";

    String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" +
            NAME + "," +
            GUID + "," +
            CREATED + "," +
            UPDATED + "," +
            USN +
            ") VALUES ( ?, ?, ?, ?, ? )";

    String[] DEFAULT_PROJECTION = new String[]{_ID, NAME, GUID, UPDATED, CREATED, USN};
    String[] TEST_PROJECTION = new String[]{_ID, NAME, GUID, CREATED};


    String NOTEBOOKS_WITH_NOTES_TABLE = NotebooksTable.TABLE_NAME + " LEFT JOIN " + NotesTable.TABLE_NAME + " ON " + NotesTable.TABLE_NAME + "." + NotesTable.NOTEBOOKS_ID + "=" + NotebooksTable.TABLE_NAME + "." + NotebooksTable._ID;
    String NOTEBOOKS_WITH_NOTES_PROJECTION = "";
    String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.notebooks";
    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.notebooks";

    String NOT_DELETED_CONDITION = IS_LOCALLY_DELETED + "=0";
    String TABLE_NAME_TRANSACT = TABLE_NAME + "/transact";
}