package ru.bmstu.evernote.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import ru.bmstu.evernote.provider.database.Database;
import ru.bmstu.evernote.provider.database.tables.Notebooks;

public class EvernoteContentProvider extends ContentProvider {
    private static final String LOGTAG = EvernoteContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "ru.bmstu.evernote.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri NOTEBOOKS_URI = Uri.withAppendedPath(AUTHORITY_URI, Notebooks.TABLE_PATH);

    private static final UriMatcher URI_MATCHER;

    private Database database;

    private static final int NOTEBOOKS = 0;
    private static final int NOTEBOOKS_ID = 1;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, Notebooks.TABLE_PATH, NOTEBOOKS);
        URI_MATCHER.addURI(AUTHORITY, Notebooks.TABLE_PATH + "/#", NOTEBOOKS_ID);
    }

    @Override
    public boolean onCreate() {
        database = new Database(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)){
            case NOTEBOOKS:
                return Notebooks.CONTENT_ITEM_TYPE;
            case NOTEBOOKS_ID:
                return Notebooks.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        final SQLiteDatabase dbConnection = database.getWritableDatabase();
        try {
            switch (URI_MATCHER.match(uri)) {
                case NOTEBOOKS:
                case NOTEBOOKS_ID:
                    final long notebookId = dbConnection.insertOrThrow(Notebooks.TABLE_NAME, null, values);
                    result = ContentUris.withAppendedId(NOTEBOOKS_URI, notebookId);
                    getContext().getContentResolver().notifyChange(result, null);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Insert exception: " + e.toString());
        }
        return result;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase dbConnection = database.getReadableDatabase();
        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        try {
            switch (URI_MATCHER.match(uri)) {
                case NOTEBOOKS:
                    builder.setTables(Notebooks.TABLE_NAME);
                    break;
                case NOTEBOOKS_ID:
                    builder.appendWhere(Notebooks._ID + "=" + uri.getLastPathSegment());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Query exception: " + e.toString());
        }

        Cursor cursor = builder.query(dbConnection, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
