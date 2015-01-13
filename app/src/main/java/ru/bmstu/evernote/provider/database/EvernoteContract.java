package ru.bmstu.evernote.provider.database;

/**
 * Created by Ivan on 14.01.2015.
 */
public final class EvernoteContract {
    public static final class Notebooks {
        public String TABLE_NAME = "notebooks";
        public String GUID = "guid";
        public String CREATED = "created";
        public String UPDATED = "updated";
        public String STATE_DELETED = "state_deleted";
        public String STATE_SYNC_REQUIRED = "state_sync_required";

        public String [] ALL_COLUMNS_PROJECTION = {TABLE_NAME, GUID, CREATED, UPDATED, STATE_DELETED, STATE_SYNC_REQUIRED};
    }

    public static final class Notes {

    }

    public static final class Resources {

    }
}
