package com.example.nthucs.prototype.Utility;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.nthucs.prototype.Activity.LoginActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by user on 2016/11/20.
 */

public class DBFunctions {

    MyDBHelper db;

    public DBFunctions(Context context){
        db = new MyDBHelper(context,"food.db",null,MyDBHelper.VERSION);
    }


    /**
     * Get list of Users from SQLite DB as Array List
     * @return
     */
    public ArrayList<HashMap<String, String>> getAllUsers() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM myProfile";
        SQLiteDatabase database = db.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("userId", cursor.getString(0));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return wordList;
    }

    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeUserfromSQLite(){
        ArrayList<HashMap<String, String>> wordList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM myProfile";
        SQLiteDatabase database = db.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put("userId", cursor.getString(1));
                map.put("userName", LoginActivity.facebookName);
                map.put("userHeight", cursor.getString(6));
                map.put("userWeight", cursor.getString(7));
                map.put("userSex", cursor.getString(5));
                map.put("userBorn", getDate(cursor.getLong(4),"yyyy-MM-dd"));
                map.put("userAddedTime",cursor.getString(10));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }
    /**
     * Compose JSON  from FoodDAO out of SQLite records
     * @return
     */
    public String composeFoodfromSQLite(){
        ArrayList<HashMap<String, String>> wordList= new ArrayList<>();
        String selectQuery = "SELECT  * FROM food";
        SQLiteDatabase database = db.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put("foodUserId", cursor.getString(1));
                map.put("foodName", cursor.getString(7));
                map.put("foodCalories", cursor.getString(2));
                map.put("foodTime", getDateTime(cursor.getLong(11)));
                map.put("foodImage",cursor.getString(3));
                map.put("mealTypeIndex",cursor.getString(12));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    public String composeSportfromSQLite(){
        ArrayList<HashMap<String, String>> wordList= new ArrayList<>();
        String selectQuery = "SELECT  * FROM sport";
        SQLiteDatabase database = db.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put("sportId", cursor.getString(0));
                map.put("sportUserId",cursor.getString(1));
                map.put("sportName", cursor.getString(2));
                map.put("sportExpenditure", cursor.getString(4));
                map.put("sportTotalTime", String.valueOf(cursor.getLong(6)/(1000*60)));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    public String composeHealthfromSQLite(){
        ArrayList<HashMap<String, String>> wordList= new ArrayList<>();
        String selectQuery = "SELECT  * FROM health";
        SQLiteDatabase database = db.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put("healthUserId",cursor.getString(1));
                map.put("systolic", cursor.getString(6));
                map.put("diastolic", cursor.getString(7));
                map.put("pulse", cursor.getString(8));
                map.put("drunkwater", cursor.getString(5));
                map.put("tempurature", cursor.getString(4));
                map.put("healthRecordedDate", cursor.getString(2));
                wordList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }

    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM myProfile ";
        SQLiteDatabase database = db.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public String getDateTime(long unixTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //轉換字串
        String time=sdf.format(unixTime);
        return time;
    }

}
