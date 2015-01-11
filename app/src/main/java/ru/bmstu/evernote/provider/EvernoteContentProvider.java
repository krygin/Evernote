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
import ru.bmstu.evernote.provider.database.tables.NotebooksTable;
import ru.bmstu.evernote.provider.database.tables.NotesTable;
import ru.bmstu.evernote.provider.database.tables.ResourcesTable;
import ru.bmstu.evernote.provider.database.tables.TransactionsTable;

import static ru.bmstu.evernote.provider.database.tables.TransactionsTable.Method;
import static ru.bmstu.evernote.provider.database.tables.TransactionsTable.Type;

public class EvernoteContentProvider extends ContentProvider {
    private static final String LOGTAG = EvernoteContentProvider.class.getSimpleName();

    public static final String AUTHORITY = "ru.bmstu.evernote.provider";

    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final Uri TRANSACTION_URI = Uri.withAppendedPath(AUTHORITY_URI, TransactionsTable.TABLE_NAME);

    public static final Uri NOTEBOOKS_URI = Uri.withAppendedPath(AUTHORITY_URI, NotebooksTable.TABLE_NAME);
    public static final Uri NOTES_URI = Uri.withAppendedPath(AUTHORITY_URI, NotesTable.TABLE_NAME);
    public static final Uri RESOURCES_URI = Uri.withAppendedPath(AUTHORITY_URI, ResourcesTable.TABLE_NAME);


    public static final Uri NOTEBOOKS_URI_TRANSACT = Uri.withAppendedPath(NOTEBOOKS_URI, "transact");
    public static final Uri NOTES_URI_TRANSACT = Uri.withAppendedPath(NOTES_URI, "transact");
    public static final Uri RESOURCES_URI_TRANSACT = Uri.withAppendedPath(RESOURCES_URI, "transact");

    private static final UriMatcher URI_MATCHER;

    private DBHelper helper;

    private static final int TRANSACTIONS = 0;
    private static final int TRANSACTIONS_ID = 1;

    private static final int NOTEBOOKS_TRANSACT = 2;
    private static final int NOTEBOOKS_ID_TRANSACT = 3;
    private static final int NOTES_TRANSACT = 4;
    private static final int NOTES_ID_TRANSACT = 5;
    private static final int RESOURCES_TRANSACT = 6;
    private static final int RESOURCES_ID_TRANSACT = 7;

    private static final int NOTEBOOKS = 8;
    private static final int NOTEBOOKS_ID = 9;
    private static final int NOTES = 10;
    private static final int NOTES_ID = 11;
    private static final int RESOURCES = 12;
    private static final int RESOURCES_ID = 13;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, TransactionsTable.TABLE_NAME, TRANSACTIONS);
        URI_MATCHER.addURI(AUTHORITY, TransactionsTable.TABLE_NAME + "/#", TRANSACTIONS_ID);
        URI_MATCHER.addURI(AUTHORITY, NotebooksTable.TABLE_NAME_TRANSACT, NOTEBOOKS_TRANSACT);
        URI_MATCHER.addURI(AUTHORITY, NotebooksTable.TABLE_NAME_TRANSACT + "/#", NOTEBOOKS_ID_TRANSACT);
        URI_MATCHER.addURI(AUTHORITY, NotesTable.TABLE_NAME_TRANSACT, NOTES_TRANSACT);
        URI_MATCHER.addURI(AUTHORITY, NotesTable.TABLE_NAME_TRANSACT + "/#", NOTES_ID_TRANSACT);
        URI_MATCHER.addURI(AUTHORITY, ResourcesTable.TABLE_NAME_TRANSACT, RESOURCES_TRANSACT);
        URI_MATCHER.addURI(AUTHORITY, ResourcesTable.TABLE_NAME_TRANSACT + "/#", RESOURCES_ID_TRANSACT);

        URI_MATCHER.addURI(AUTHORITY, NotebooksTable.TABLE_NAME, NOTEBOOKS);
        URI_MATCHER.addURI(AUTHORITY, NotebooksTable.TABLE_NAME + "/#", NOTEBOOKS_ID);
        URI_MATCHER.addURI(AUTHORITY, NotesTable.TABLE_NAME, NOTES);
        URI_MATCHER.addURI(AUTHORITY, NotesTable.TABLE_NAME + "/#", NOTES_ID);
        URI_MATCHER.addURI(AUTHORITY, ResourcesTable.TABLE_NAME, RESOURCES);
        URI_MATCHER.addURI(AUTHORITY, ResourcesTable.TABLE_NAME + "/#", RESOURCES_ID);
    }

    @Override
    public boolean onCreate() {
        helper = new DBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case NOTEBOOKS_TRANSACT:
                return NotebooksTable.CONTENT_TYPE;
            case NOTEBOOKS_ID_TRANSACT:
                return NotebooksTable.CONTENT_ITEM_TYPE;
            case NOTES_TRANSACT:
                return NotesTable.CONTENT_TYPE;
            case NOTES_ID_TRANSACT:
                return NotesTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        long id = 0;
        final SQLiteDatabase dbConnection = helper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case TRANSACTIONS:
            case TRANSACTIONS_ID:
                long transactionId = dbConnection.insertOrThrow(TransactionsTable.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(uri, transactionId);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case NOTEBOOKS_TRANSACT:
            case NOTEBOOKS_ID_TRANSACT:
                dbConnection.beginTransaction();
                id = dbConnection.insertOrThrow(NotebooksTable.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(NOTEBOOKS_URI, id);
                updateTransactionsTable(Method.CREATE_OR_UPDATE, Type.NOTEBOOK, id);
                dbConnection.setTransactionSuccessful();
                dbConnection.endTransaction();
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case NOTEBOOKS:
            case NOTEBOOKS_ID:
                id = dbConnection.insertOrThrow(NotebooksTable.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(NOTEBOOKS_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case NOTES_TRANSACT:
            case NOTES_ID_TRANSACT:
                dbConnection.beginTransaction();
                try {
                    id = dbConnection.insertOrThrow(NotesTable.TABLE_NAME, null, values);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                result = ContentUris.withAppendedId(NOTES_URI, id);
                updateTransactionsTable(Method.CREATE_OR_UPDATE, Type.NOTE, id);
                dbConnection.setTransactionSuccessful();
                dbConnection.endTransaction();
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case NOTES:
            case NOTES_ID:
                id = dbConnection.insertOrThrow(NotesTable.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(NOTES_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case RESOURCES:
            case RESOURCES_ID:
                id = dbConnection.insertOrThrow(ResourcesTable.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(RESOURCES_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case RESOURCES_TRANSACT:
            case RESOURCES_ID_TRANSACT:
                dbConnection.beginTransaction();
                id = dbConnection.insertOrThrow(ResourcesTable.TABLE_NAME, null, values);
                result = ContentUris.withAppendedId(RESOURCES_URI, id);
                updateTransactionsTable(Method.CREATE_OR_UPDATE, Type.RESOURCE, id);
                dbConnection.setTransactionSuccessful();
                dbConnection.endTransaction();
                getContext().getContentResolver().notifyChange(result, null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return result;
    }

    private void updateTransactionsTable(Method method, Type type, long id) {
        ContentValues transaction = new ContentValues();
        transaction.put(TransactionsTable.METHOD, method.ordinal());
        transaction.put(TransactionsTable.TYPE, type.ordinal());
        transaction.put(TransactionsTable.ID, id);
        insert(TRANSACTION_URI, transaction);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String groupBy = null;
        String having = null;
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case TRANSACTIONS:
                builder.setTables(TransactionsTable.TABLE_NAME);
                break;
            case TRANSACTIONS_ID:
                builder.setTables(TransactionsTable.TABLE_NAME);
                builder.appendWhere(TransactionsTable._ID + "=" + uri.getLastPathSegment());
                break;
            case NOTEBOOKS:
                builder.setTables(NotebooksTable.TABLE_NAME);
                break;
            case NOTEBOOKS_ID:
                builder.setTables(NotebooksTable.TABLE_NAME);
                builder.appendWhere(NotebooksTable._ID + "=" + uri.getLastPathSegment());
                break;
            case NOTES:
                builder.setTables(NotesTable.TABLE_NAME);
                break;
            case NOTES_ID:
                builder.setTables(NotesTable.TABLE_NAME + " LEFT JOIN " + ResourcesTable.TABLE_NAME + " ON " + ResourcesTable.TABLE_NAME + "." + ResourcesTable.NOTES_ID + "=" + NotesTable.TABLE_NAME + "." + NotesTable._ID);
                builder.appendWhere(NotesTable.TABLE_NAME + "." + NotesTable._ID + "=" + uri.getLastPathSegment());
                break;
            case RESOURCES:
                builder.setTables(ResourcesTable.TABLE_NAME);
                break;
            case RESOURCES_ID:
                builder.setTables(ResourcesTable.TABLE_NAME);
                builder.appendWhere(ResourcesTable.TABLE_NAME + "." + ResourcesTable._ID + "=" + uri.getLastPathSegment());
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
        Uri result = null;
        long id;
        final SQLiteDatabase dbConnection = helper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case NOTEBOOKS_ID_TRANSACT:
                id = Long.parseLong(uri.getLastPathSegment());
                selection = NotebooksTable._ID + "=" + id;
                dbConnection.beginTransaction();
                updated = dbConnection.update(NotebooksTable.TABLE_NAME, values, selection, null);
                updateTransactionsTable(Method.CREATE_OR_UPDATE, Type.NOTEBOOK, id);
                dbConnection.setTransactionSuccessful();
                dbConnection.endTransaction();
                result = ContentUris.withAppendedId(NOTEBOOKS_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case NOTEBOOKS_ID:
                id = Long.parseLong(uri.getLastPathSegment());
                selection = NotebooksTable._ID + "=" + id;
                updated = dbConnection.update(NotebooksTable.TABLE_NAME, values, selection, null);
                result = ContentUris.withAppendedId(NOTEBOOKS_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case NOTES_ID_TRANSACT:
                id = Long.parseLong(uri.getLastPathSegment());
                selection = NotesTable._ID + "=" + uri.getLastPathSegment();
                dbConnection.beginTransaction();
                updated = dbConnection.update(NotesTable.TABLE_NAME, values, selection, null);
                updateTransactionsTable(Method.CREATE_OR_UPDATE, Type.NOTE, id);
                dbConnection.setTransactionSuccessful();
                dbConnection.endTransaction();
                result = ContentUris.withAppendedId(NOTES_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case NOTES_ID:
                id = Long.parseLong(uri.getLastPathSegment());
                selection = NotesTable._ID + "=" + id;
                updated = dbConnection.update(NotesTable.TABLE_NAME, values, selection, null);
                result = ContentUris.withAppendedId(NOTES_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case RESOURCES_ID_TRANSACT:
                id = Long.parseLong(uri.getLastPathSegment());
                selection = NotesTable._ID + "=" + uri.getLastPathSegment();
                dbConnection.beginTransaction();
                updated = dbConnection.update(ResourcesTable.TABLE_NAME, values, selection, null);
                updateTransactionsTable(Method.CREATE_OR_UPDATE, Type.RESOURCE, id);
                dbConnection.setTransactionSuccessful();
                dbConnection.endTransaction();
                result = ContentUris.withAppendedId(RESOURCES_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            case RESOURCES_ID:
                id = Long.parseLong(uri.getLastPathSegment());
                selection = NotesTable._ID + "=" + id;
                updated = dbConnection.update(ResourcesTable.TABLE_NAME, values, selection, null);
                result = ContentUris.withAppendedId(RESOURCES_URI, id);
                getContext().getContentResolver().notifyChange(result, null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return updated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleted = 0;
        ContentValues contentValues = new ContentValues();
        final SQLiteDatabase dbConnection = helper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case TRANSACTIONS:
                deleted = dbConnection.delete(TransactionsTable.TABLE_NAME, selection, selectionArgs);
                break;
            case TRANSACTIONS_ID:
                selection = TransactionsTable._ID + "=" + uri.getLastPathSegment();
                deleted = dbConnection.delete(TransactionsTable.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTEBOOKS_ID_TRANSACT:
                selection = NotebooksTable._ID + "=" + uri.getLastPathSegment();
                contentValues.put(NotebooksTable.IS_LOCALLY_DELETED, 1);
                deleted = dbConnection.update(NotebooksTable.TABLE_NAME, contentValues, selection, null);
                break;
            case NOTEBOOKS_ID:
                selection = NotebooksTable._ID + "=" + uri.getLastPathSegment();
                deleted = dbConnection.delete(NotebooksTable.TABLE_NAME, selection, null);
                break;
            case NOTES_ID_TRANSACT:
                selection = NotesTable._ID + "=" + uri.getLastPathSegment();
                contentValues.put(NotesTable.IS_LOCALLY_DELETED, 1);
                deleted = dbConnection.update(NotesTable.TABLE_NAME, contentValues, selection, null);
                break;
            case NOTES_ID:
                selection = NotesTable._ID + "=" + uri.getLastPathSegment();
                deleted = dbConnection.delete(NotesTable.TABLE_NAME, selection, null);
                break;
            case RESOURCES_ID_TRANSACT:
                selection = ResourcesTable._ID + "=" + uri.getLastPathSegment();
                contentValues.put(ResourcesTable.IS_LOCALLY_DELETED, 1);
                deleted = dbConnection.update(ResourcesTable.TABLE_NAME, contentValues, selection, null);
                break;
            case RESOURCES_ID:
                selection = ResourcesTable._ID + "=" + uri.getLastPathSegment();
                deleted = dbConnection.delete(ResourcesTable.TABLE_NAME, selection, null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return deleted;
    }
}