package ru.bmstu.evernote.provider.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.bmstu.evernote.provider.database.tables.Notebooks;
import ru.bmstu.evernote.provider.database.tables.Notes;
import ru.bmstu.evernote.provider.database.tables.Transactions;

/**
 * Created by Ivan on 11.12.2014.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "evernote.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Notebooks.SQL_CREATE);
        sqLiteDatabase.execSQL(Notes.SQL_CREATE);
        sqLiteDatabase.execSQL(Transactions.SQL_CREATE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(Notebooks.SQL_DROP);
        sqLiteDatabase.execSQL(Notes.SQL_DROP);
        sqLiteDatabase.execSQL(Transactions.SQL_DROP);
        onCreate(sqLiteDatabase);
    }
}