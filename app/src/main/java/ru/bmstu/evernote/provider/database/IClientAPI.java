package ru.bmstu.evernote.provider.database;


/**
 * Created by Ivan on 20.12.2014.
 */
public interface IClientAPI {
    boolean insertNotebook(String name);
    boolean insertNote(String title, String content, long notebooksId);
    boolean insertResource(long notesId, String resource);
    boolean updateNotebook(long notebooksId, String name);
    boolean updateNote(String title, String content, long notesId);
    boolean deleteNote(long notesId);
    boolean deleteNotebook(long notebooksId);
    boolean deleteResource(long resourceId);
}