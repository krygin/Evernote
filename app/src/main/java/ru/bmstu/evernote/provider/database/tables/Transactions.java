package ru.bmstu.evernote.provider.database.tables;

import android.provider.BaseColumns;

/**
 * Created by Ivan on 16.12.2014.
 */
public interface Transactions extends BaseColumns {
    String TABLE_NAME = "transactions";

    String METHOD = "method";
    String URI = "uri";
    enum Method {
        CREATE,
        UPDATE,
        DELETE
    }

    String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
            METHOD + " TEXT NOT NULL" + "," +
            URI + " TEXT NOT NULL" +
            ")";

    String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    String[] ALL_COLUMNS = new String[]{_ID, METHOD, URI};
}