package com.example.assignment3;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText tag;
    EditText size;
    Button load;
    ImageView imageView1;

    SQLiteDatabase db;

    String taggy = "";
    int sizey;
    boolean tagEntered;
    boolean sizeEntered;

    int idValue = 0;

    String[] tags;
    String[] tagsLoad;

    ArrayList<Bitmap> matches = new ArrayList<>(); //holds all the matches
    int matchID = 0;

    Bitmap tempBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tag = (EditText) findViewById(R.id.tag);
        size = (EditText) findViewById(R.id.size);
        load = (Button) findViewById(R.id.load);
        imageView1 = (ImageView) findViewById(R.id.imageView1);

        db = this.openOrCreateDatabase
                ("SomeDB", Context.MODE_PRIVATE, null);

        db.execSQL("drop table if exists Photos;");
        db.execSQL("drop table if exists Tags;");

        db.execSQL("create table Photos (ID int, Photo blob, Size int);");
        db.execSQL("create table Tags (ID int, Tag text);");
    }

    public void saveSomething(View v) {
        tagEntered = true;
        sizeEntered = true;

        //use a try catch, try the stuff if its not null, then catch if it is null
        try {
            tags = tag.getText().toString().split(";"); //creates a temp array to hold all the tags inputted
        } catch (Exception e){
            Log.d("tag", "tag is null");
            tagEntered = false;
        }

        try {
            sizey = Integer.parseInt(size.getText().toString());
        } catch (Exception e) {
            Log.d("size", "size is null");
            sizeEntered = false;
        }

        try {
            ContentValues values = new ContentValues();

            //if no photo is in the imageView, then this will throw an exception
            tempBitmap = ((BitmapDrawable)imageView1.getDrawable()).getBitmap();

            if (tagEntered && sizeEntered) { //check if both tag and size are entered

                values.clear();

                values.put("ID", idValue);
                values.put("Photo", bitmapToByte(tempBitmap)); //might not be saving properly
                values.put("Size", sizey);
                db.insert("Photos", null, values);

                Log.v("Photos Table", "ID: " + idValue + " Photo: " + bitmapToByte(tempBitmap) + " Size: " + sizey);

                for (int i = 0; i < tags.length; i++) {

                    values.clear();

                    values.put("ID", idValue);
                    values.put("Tag", tags[i]);
                    db.insert("Tags", null, values);

                    Log.v("DOES TAG FUCKING WORK?", "" + tags[i]);
                    Log.v("Tags Table", "ID: " + idValue + " Tag: " + tags[i]);
                }
            } else if (tagEntered) { //works
                for (int i = 0; i < tags.length; i++) {

                    values.clear();

                    values.put("ID", idValue);
                    values.put("Tag", tags[i]);
                    db.insert("Tags", null, values);

                    Log.v("Tags Table", "ID: " + idValue + " Tag: " + tags[i]);

                }
            } else { //works

                values.clear();

                values.put("ID", idValue);
                values.put("Photo", bitmapToByte(tempBitmap));
                values.put("Size", sizey);
                db.insert("Photos", null, values);

                Log.v("Photos Table", "ID: " + idValue + " Photo: " + bitmapToByte(tempBitmap) + " Size: " + sizey);
            }
            printTable();
            idValue++;
            tag.setText(""); //might not be clearing well and causing the LoadSomething method to pick up empty strings
            size.getText().clear();
            imageView1.setImageResource(android.R.color.transparent);

            //make toast to help user know it saved
            Toast toast=Toast.makeText(getApplicationContext(),"Saved!",Toast.LENGTH_SHORT);
            toast.show();

        } catch (Exception e) {
            e.printStackTrace();
            Log.v("error msg", "no image taken so cannot save");
            Toast toast=Toast.makeText(getApplicationContext(),"Unable to save, no photos taken", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public void loadSomething(View v) {

        matches.clear();

        tagEntered = true;
        sizeEntered = true;

        double min = 0;
        double max = 0;

        //use a try catch, try the stuff if its not null, then catch if it is null
        try {
            if (!tag.getText().toString().equals("")) { //maybe it shouldn't be the tags variable
                tagsLoad = tag.getText().toString().split(";"); //always thinks that the tag has something in it, never sees it as empty
            } else {
                throw new Exception("empty string WORKS!");
            }
        } catch (Exception e){
            Log.d("tag", "tag is null");
            tagEntered = false;
        }

        try {
            sizey = Integer.parseInt(size.getText().toString());
            min = (double) sizey * .75;
            max = (double) sizey * 1.25;
        } catch (Exception e) {
            Log.d("size", "size is null");
            sizeEntered = false;
        }


        try {
            Cursor c = db.rawQuery("SELECT Tags.ID,Photo,Size,Tag from Photos,Tags WHERE Photos.ID = Tags.ID;", null);
            c.moveToFirst();

            do {

                int id = c.getInt(0);
                byte[] photo = c.getBlob(1);
                int size1 = c.getInt(2);
                String tag1 = c.getString(3);

                Log.v("debugggggggggggggg", "ID: " + id + " Photo: " + photo + " Size: " + size1 + " Tag: " + tag1);

                if (tagEntered && sizeEntered) { //check if both tag and size are entered

                    for (int j = 0; j<tagsLoad.length; j++) {
                        if (tagsLoad[j].equals(tag1) && min <= size1 && size1 <= max) {
                            matches.add(byteToBitmap(photo));
                        }
                    }
                } else if (tagEntered) { //works

                    for (int j = 0; j < tagsLoad.length; j++) {

                        //Log.v("tagsLoad", tagsLoad[j]);
                        //Log.v("tag1", tag1);

                        if (tagsLoad[j].equals(tag1)) { //this condition causes an error or something
                            matches.add(byteToBitmap(photo));
                        }
                    }
                } else { //works
                    if (min <= size1 && size1 <= max) {
                        matches.add(byteToBitmap(photo));
                    }
                }
            } while (c.moveToNext());

            tag.setText(""); //might not be clearing well and causing the LoadSomething method to pick up empty strings
            size.getText().clear();
            imageView1.setImageBitmap(matches.get(0));

        } catch (Exception e) { //when the idValue is 0
            e.printStackTrace();

            Log.v("error caught", "no matches");
            Toast toast=Toast.makeText(getApplicationContext(),"No matches", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void captureSomething(View v) {
        Intent w = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(w, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent x) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = x.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            imageView1.setImageBitmap(photo);
            tempBitmap = photo;
            size.setText(Integer.toString(photo.getByteCount())); //sets size of image in the size location
        }
    }

    public void previous(View v) { //fix the matches.size() since i hard coded the size of the arraylist
        try {
            matchID--;
            imageView1.setImageBitmap(matches.get(matchID));
        } catch (Exception e) {
            matchID++;
            Log.v("no previous", "no matches before this");
            Toast.makeText(this, "No matches before this", Toast.LENGTH_SHORT).show();
        }


    }

    public void next(View v) {
        try {
            matchID++;
            imageView1.setImageBitmap(matches.get(matchID));
        } catch (Exception e) {
            matchID--;
            Log.v("no next", "no matches after this");
            Toast.makeText(this, "No matches after this", Toast.LENGTH_SHORT).show();
        }

    }

    private byte[] bitmapToByte(Bitmap b) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] ba = stream.toByteArray();
        return ba;
    }

    private Bitmap byteToBitmap(byte[] ba) {
        Bitmap b = BitmapFactory.decodeByteArray(ba, 0, ba.length);
        return b;
    }

    private void printTable() {
        Cursor cc = db.rawQuery("SELECT Tags.ID,Photo,Size,Tag from Photos,Tags WHERE Photos.ID = Tags.ID;", null);
        cc.moveToFirst();

        do {

            int id = cc.getInt(0);
            byte[] photo = cc.getBlob(1);
            int size1 = cc.getInt(2);
            String tag1 = cc.getString(3);

            Log.v("TABLE", "ID: " + id + " Photo: " + photo + " Size: " + size1 + " Tag: " + tag1);
        } while (cc.moveToNext());
    }
}
