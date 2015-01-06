package ru.bmstu.evernote;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ru.bmstu.evernote.data.FileData;

/**
 * Created by Ivan on 27.12.2014.
 */
public class FileProcessor {
    private final Context mContext;

    public FileProcessor(Context context) {
        mContext = context;
    }

    public void writeFile(String filename, byte[] bytes) {
        FileOutputStream outputStream;
        try {
            outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(bytes);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileData readFile(String filename) {
        FileData fileData = null;
        try {
            FileInputStream fileInputStream = mContext.openFileInput(filename);
            InputStream inputStream = new BufferedInputStream(fileInputStream);
            fileData = new FileData(EvernoteUtil.hash(inputStream), new File(mContext.getFilesDir(), filename));
            inputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData;
    }
}