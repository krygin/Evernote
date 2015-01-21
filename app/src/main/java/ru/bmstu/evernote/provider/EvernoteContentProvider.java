package ru.bmstu.evernote.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import ru.bmstu.evernote.provider.database.DBHelper;

import static ru.bmstu.evernote.provider.database.EvernoteContract.*;

public class EvernoteContentProvider extends ContentProvider {
    private static final String LOGTAG = EvernoteContentProvider.class.getSimpleName();

    private static final UriMatcher URI_MATCHER;

    private DBHelper helper;

    private static final int NOTEBOOKS = 0;
    private static final int NOTEBOOKS_ID = 1;
    private static final int NOTES = 2;
    private static final int NOTES_ID = 3;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

        URI_MATCHER.addURI(AUTHORITY, Notebooks.TABLE_NAME, NOTEBOOKS);
        URI_MATCHER.addURI(AUTHORITY, Notebooks.TABLE_NAME + "/#", NOTEBOOKS_ID);
        URI_MATCHER.addURI(AUTHORITY, Notes.TABLE_NAME, NOTES);
        URI_MATCHER.addURI(AUTHORITY, Notes.TABLE_NAME + "/#", NOTES_ID);
    }

    @Override
    public boolean onCreate() {
        helper = new DBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case NOTEBOOKS:
                return Notebooks.CONTENT_TYPE;
            case NOTEBOOKS_ID:
                return Notebooks.CONTENT_ITEM_TYPE;
            case NOTES:
                return Notes.CONTENT_TYPE;
            case NOTES_ID:
                return Notes.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result;
        long id;
        Integer notebooks = values.getAsInteger(Notebooks.STATE_SYNC_REQUIRED);
        Integer notes = values.getAsInteger(Notes.STATE_SYNC_REQUIRED);
        boolean syncToNetwork =
                (notebooks != null && StateSyncRequired.values()[notebooks].equals(StateSyncRequired.PENDING)) ||
                        (notes != null && StateSyncRequired.values()[notes].equals(StateSyncRequired.PENDING));
        final SQLiteDatabase dbConnection = helper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case NOTEBOOKS:
            case NOTEBOOKS_ID:
                id = dbConnection.insertOrThrow(Notebooks.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(Notebooks.CONTENT_URI, id);
                getContext().getContentResolver().notifyChange(result, null, syncToNetwork);
                break;
            case NOTES:
            case NOTES_ID:
                id = dbConnection.insertOrThrow(Notes.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(Notes.CONTENT_URI, id);
                getContext().getContentResolver().notifyChange(result, null, syncToNetwork);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return result;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String groupBy = null;
        String having = null;
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case NOTEBOOKS:
                builder.setTables(Notebooks.TABLE_NAME);
                break;
            case NOTEBOOKS_ID:
                builder.setTables(Notebooks.TABLE_NAME);
                builder.appendWhere(Notebooks._ID + "=" + uri.getLastPathSegment());
                break;
            case NOTES:
                builder.setTables(Notes.TABLE_NAME);
                break;
            case NOTES_ID:
                builder.setTables(Notes.TABLE_NAME);
                builder.appendWhere(Notes.TABLE_NAME + "." + Notes._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = builder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Integer notebooks = values.getAsInteger(Notebooks.STATE_SYNC_REQUIRED);
        Integer notes = values.getAsInteger(Notes.STATE_SYNC_REQUIRED);
        boolean syncToNetwork =
                (notebooks != null && StateSyncRequired.values()[notebooks].equals(StateSyncRequired.PENDING)) ||
                        (notes != null && StateSyncRequired.values()[notes].equals(StateSyncRequired.PENDING));
        int updated;
        Uri result;
        long id;
        final SQLiteDatabase dbConnection = helper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case NOTEBOOKS_ID:
                id = Long.parseLong(uri.getLastPathSegment());
                selection = Notebooks._ID + "=" + id;
                updated = dbConnection.update(Notebooks.TABLE_NAME, values, selection, null);
                result = ContentUris.withAppendedId(Notebooks.CONTENT_URI, id);
                getContext().getContentResolver().notifyChange(result, null, syncToNetwork);
                break;

            case NOTES_ID:
                id = Long.parseLong(uri.getLastPathSegment());
                selection = Notes._ID + "=" + id;
                updated = dbConnection.update(Notes.TABLE_NAME, values, selection, null);
                result = ContentUris.withAppendedId(Notes.CONTENT_URI, id);
                getContext().getContentResolver().notifyChange(result, null, syncToNetwork);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return updated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleted = 0;
        final SQLiteDatabase dbConnection = helper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case NOTEBOOKS:
                deleted = dbConnection.delete(Notebooks.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTEBOOKS_ID:
                selection = Notebooks._ID + "=" + uri.getLastPathSegment();
                deleted = dbConnection.delete(Notebooks.TABLE_NAME, selection, null);
                break;
            case NOTES:
                deleted = dbConnection.delete(Notes.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES_ID:
                selection = Notes._ID + "=" + uri.getLastPathSegment();
                deleted = dbConnection.delete(Notes.TABLE_NAME, selection, null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null, false);
        return deleted;
    }
}