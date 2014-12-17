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
import ru.bmstu.evernote.provider.database.tables.Notebooks;
import ru.bmstu.evernote.provider.database.tables.Notes;
import ru.bmstu.evernote.provider.database.tables.Transactions;

import static ru.bmstu.evernote.provider.database.tables.Transactions.Method;

public class EvernoteContentProvider extends ContentProvider {
    private static final String LOGTAG = EvernoteContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "ru.bmstu.evernote.provider";

    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri TRANSACTION_URI = Uri.withAppendedPath(AUTHORITY_URI, Transactions.TABLE_NAME);

    public static final Uri NOTEBOOKS_URI = Uri.withAppendedPath(AUTHORITY_URI, Notebooks.TABLE_NAME);
    public static final Uri NOTES_URI = Uri.withAppendedPath(AUTHORITY_URI, Notes.TABLE_NAME);

    private static final UriMatcher URI_MATCHER;

    private DBHelper helper;

    private static final int TRANSACTIONS = 0;
    private static final int TRANSACTIONS_ID = 1;

    private static final int NOTEBOOKS = 2;
    private static final int NOTEBOOKS_ID = 3;
    private static final int NOTES = 4;
    private static final int NOTES_ID = 5;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, Transactions.TABLE_NAME, TRANSACTIONS);
        URI_MATCHER.addURI(AUTHORITY, Transactions.TABLE_NAME + "/#", TRANSACTIONS_ID);
        URI_MATCHER.addURI(AUTHORITY, Notebooks.TABLE_PATH, NOTEBOOKS);
        URI_MATCHER.addURI(AUTHORITY, Notebooks.TABLE_PATH + "/#", NOTEBOOKS_ID);
        URI_MATCHER.addURI(AUTHORITY, Notes.TABLE_PATH, NOTES);
        URI_MATCHER.addURI(AUTHORITY, Notes.TABLE_PATH + "/#", NOTES_ID);
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
        Uri result = null;
        final SQLiteDatabase dbConnection = helper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case TRANSACTIONS:
            case TRANSACTIONS_ID:
                long transactionId = dbConnection.insertOrThrow(Transactions.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(uri, transactionId);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case NOTEBOOKS:
            case NOTEBOOKS_ID:
                dbConnection.beginTransaction();
                long notebookId = dbConnection.insertOrThrow(Notebooks.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(NOTEBOOKS_URI, notebookId);
                try {
                    updateTransactionsTable(Method.CREATE, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dbConnection.setTransactionSuccessful();
                dbConnection.endTransaction();
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case NOTES:
            case NOTES_ID:
                final long noteId = dbConnection.insertOrThrow(Notes.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(NOTES_URI, noteId);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return result;
    }

    private void updateTransactionsTable(Method method, Uri result) {
        ContentValues transaction = new ContentValues();
        transaction.put(Transactions.METHOD, method.toString());
        transaction.put(Transactions.URI, result.toString());
        getContext().getContentResolver().insert(EvernoteContentProvider.TRANSACTION_URI, transaction);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String groupBy = null;
        String having = null;
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case TRANSACTIONS:
                builder.setTables(Transactions.TABLE_NAME);
                break;
            case TRANSACTIONS_ID:
                builder.setTables(Transactions.TABLE_NAME);
                builder.appendWhere(Transactions._ID + "=" + uri.getLastPathSegment());
                break;
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
                builder.appendWhere(Notes._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = builder.query(database, projection, selection, selectionArgs, groupBy, having, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int updated = 0;
        final SQLiteDatabase dbConnection = helper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case NOTEBOOKS:
                updated = dbConnection.update(Notebooks.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTEBOOKS_ID:
                selection = Notebooks._ID + "=" + uri.getLastPathSegment();
                updated = dbConnection.update(Notebooks.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES:
                updated = dbConnection.update(Notes.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES_ID:
                selection = Notes._ID + "=" + uri.getLastPathSegment();
                updated = dbConnection.update(Notes.TABLE_NAME, values, selection, selectionArgs);
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
            case TRANSACTIONS:
                deleted = dbConnection.delete(Transactions.TABLE_NAME, selection, selectionArgs);
                break;
            case TRANSACTIONS_ID:
                selection = Transactions._ID + "=" + uri.getLastPathSegment();
                deleted = dbConnection.delete(Transactions.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTEBOOKS:
                deleted = dbConnection.delete(Notebooks.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTEBOOKS_ID:
                selection = Notebooks._ID + "=" + uri.getLastPathSegment();
                deleted = dbConnection.delete(Notebooks.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES:
                deleted = dbConnection.delete(Notes.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES_ID:
                selection = Notes._ID + "=" + uri.getLastPathSegment();
                deleted = dbConnection.delete(Notes.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return deleted;
    }
}