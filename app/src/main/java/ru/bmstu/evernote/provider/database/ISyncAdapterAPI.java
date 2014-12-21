package ru.bmstu.evernote.provider.database;

/**
 * Created by Ivan on 20.12.2014.
 */
public interface ISyncAdapterAPI {
    boolean insertNotebook(String name, String guid, long usn, long created, long updated);
    boolean insertNote(String title, String guid, long usn, long created, long updated, long notebooksId);
    boolean insertResource(String guid, String resource, String mimeType, long notesId);
    boolean updateNotebook(String name, long usn, long updated, long notebooksId);
    boolean updateNote(String title, long usn, long updated, long notesId);
    boolean deleteNotebookFromDatabase(long notebooksId);
    boolean deleteNoteFromDatabase(long notesId);
    boolean deleteResourceFromDatabase(long resourcesId);
}