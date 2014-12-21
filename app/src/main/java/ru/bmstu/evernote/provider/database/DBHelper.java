package ru.bmstu.evernote.provider.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.bmstu.evernote.provider.database.tables.NotebooksTable;
import ru.bmstu.evernote.provider.database.tables.NotesTable;
import ru.bmstu.evernote.provider.database.tables.ResourcesTable;
import ru.bmstu.evernote.provider.database.tables.TransactionsTable;

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
        sqLiteDatabase.execSQL(NotebooksTable.SQL_CREATE);
        sqLiteDatabase.execSQL(NotesTable.SQL_CREATE);
        sqLiteDatabase.execSQL(ResourcesTable.SQL_CREATE);
        sqLiteDatabase.execSQL(TransactionsTable.SQL_CREATE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(ResourcesTable.SQL_DROP);
        sqLiteDatabase.execSQL(NotesTable.SQL_DROP);
        sqLiteDatabase.execSQL(NotebooksTable.SQL_DROP);
        sqLiteDatabase.execSQL(TransactionsTable.SQL_DROP);
        onCreate(sqLiteDatabase);
    }
}