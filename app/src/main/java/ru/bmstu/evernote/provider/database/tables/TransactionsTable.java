package ru.bmstu.evernote.provider.database.tables;

import android.provider.BaseColumns;

/**
 * Created by Ivan on 16.12.2014.
 */
public interface TransactionsTable extends BaseColumns {
    String TABLE_NAME = "transactions";

    String METHOD = "method";
    String ID = "id";
    String TYPE = "type";

    
    enum Method {
        CREATE_OR_UPDATE,
        DELETE
    }
    enum Type {
        NOTEBOOK,
        NOTE,
        RESOURCE
    }

    String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
            METHOD + " INTEGER NOT NULL" + "," +
            TYPE + " INTEGER NOT NULL" + "," +
            ID + " INTEGER NOT NULL" + "," +
            "UNIQUE (" + TYPE + "," + ID + ")" + "ON CONFLICT REPLACE" +
            ")";
    String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
    String[] ALL_COLUMNS = new String[]{_ID, METHOD, TYPE, ID};


    String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.evernote.transactions";
    String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.evernote.transactions";
}