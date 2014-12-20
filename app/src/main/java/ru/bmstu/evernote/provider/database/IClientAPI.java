package ru.bmstu.evernote.provider.database;


import java.util.List;

/**
 * Created by Ivan on 20.12.2014.
 */
public interface IClientAPI {
    boolean insertNotebook(String name);
    boolean insertNote(String title, long notebooksId, List<String> resources);
    boolean updateNotebook(long notebooksId, String name);
    boolean updateNote(String name, long notebooksId);
    boolean deleteNote(long notesId);
    boolean deleteNotebook(long notebooksId);
    boolean insertResource(long notesId, String resource);
    boolean deleteResource(long resourceId);
}