package ru.bmstu.evernote.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.database.ContentProviderHelperService;
import ru.bmstu.evernote.provider.database.EvernoteContract;
import ru.bmstu.evernote.provider.database.IClientAPI;

/**
 * Created by Admin on 22.01.2015.
 */
public class CreateNoteActivity extends Activity implements View.OnClickListener {
    private IClientAPI mService = null;
    private long notebooksId;
    private EditText titleNote = null;
    private EditText contentNote = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ContentProviderHelperService.ContentProviderHelperBinder binder = (ContentProviderHelperService.ContentProviderHelperBinder)iBinder;
            mService = binder.getClientApiService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_note_dialog);

        Button yes = (Button) findViewById(R.id.btnYes);
        Button no = (Button) findViewById(R.id.btnNo);
        TextView simpleNotebook = (TextView) findViewById(R.id.simpleNote);
        titleNote = (EditText)this.findViewById(R.id.titleNote);
        contentNote = (EditText)this.findViewById(R.id.contentNote);

        Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        Typeface regular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface slabRegular = Typeface.createFromAsset(getAssets(), "fonts/RobotoSlab-Regular.ttf");
        yes.setTypeface(bold);
        no.setTypeface(regular);
        titleNote.setTypeface(regular);
        contentNote.setTypeface(regular);
        simpleNotebook.setTypeface(slabRegular);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
//        notesId = getArguments().getLong(ItemDetailFragment.ARG_ITEM_ID);


        Cursor cursor = this.getContentResolver().query(
                EvernoteContract.Notebooks.CONTENT_URI,
                EvernoteContract.Notebooks.ALL_COLUMNS_PROJECTION,
                EvernoteContract.Notebooks.NOT_DELETED_SELECTION,
                null, null);
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, new String[]{EvernoteContract.Notebooks.NAME}, new int[]{android.R.id.text1}, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c = (Cursor)adapterView.getItemAtPosition(i);
                notebooksId = c.getLong(c.getColumnIndex(EvernoteContract.Notebooks._ID));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Cursor c = (Cursor)adapterView.getItemAtPosition(0);
                notebooksId = c.getLong(c.getColumnIndex(EvernoteContract.Notebooks._ID));
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ContentProviderHelperService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mService != null) {
            this.unbindService(serviceConnection);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnYes:
                if (titleNote.getText().toString().trim().length() > 0){
                    mService.insertNote(titleNote.getText().toString(), contentNote.getText().toString(), notebooksId);
                    finish();
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Sorry, Mario, our princess is in another castle! =(";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                break;
            case R.id.btnNo:
                finish();
                break;
            default:
                break;
        }
    }
}
