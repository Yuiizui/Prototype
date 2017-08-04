package com.example.nthucs.prototype.SportList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.nthucs.prototype.Utility.MyDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/7/27.
 */
public class SportDAO {

    public static final String TABLE_NAME = "sport";

    public static final String KEY_ID = "_id";
    public static final String USER_ID = "userID";
    public static final String TITLE_COLUMN = "title";
    public static final String CONTENT_COLUMN = "content";
    public static final String CALORIE_COLUMN = "calorie";
    public static final String DATETIME_COLUMN = "datetime";
    public static final String TOTALTIME_COLUMN = "totalTime";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER NOT NULL, " +
                    USER_ID + " INTEGER NOT NULL, " +
                    TITLE_COLUMN + " TEXT NOT NULL, " +
                    CONTENT_COLUMN + " TEXT NOT NULL, "+
                    CALORIE_COLUMN + " TEXT NOT NULL, " +
                    DATETIME_COLUMN + " INTEGER NOT NULL, " +
                    TOTALTIME_COLUMN + " INTEGER NOT NULL)";

    private SQLiteDatabase db;

    public SportDAO(Context context) {db = MyDBHelper.getDatabase(context);}

    public void close() { db.close();}

    public Sport insert(Sport sport) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ID,sport.getId());
        cv.put(USER_ID,sport.getUserID());
        cv.put(TITLE_COLUMN, sport.getTitle());
        cv.put(CONTENT_COLUMN, sport.getContent());
        cv.put(CALORIE_COLUMN, sport.getCalorie());
        cv.put(DATETIME_COLUMN, sport.getDatetime());
        cv.put(TOTALTIME_COLUMN, sport.getTotalTime());


        this.db.insert(TABLE_NAME, null, cv);
        /*long id = this.db.insert(TABLE_NAME, null, cv);

        sport.setId(id);*/

        return sport;
    }

    public boolean update(Sport sport) {
        ContentValues cv = new ContentValues();

        cv.put(TITLE_COLUMN, sport.getTitle());
        cv.put(CONTENT_COLUMN, sport.getContent());
        cv.put(CALORIE_COLUMN, sport.getCalorie());
        cv.put(DATETIME_COLUMN, sport.getDatetime());
        cv.put(TOTALTIME_COLUMN, sport.getTotalTime());

        String where = KEY_ID + "=" + sport.getId();

        return this.db.update(TABLE_NAME, cv, where, null) > 0;
    }

    public boolean delete(long id){

        String where = KEY_ID + "=" + id;

        return db.delete(TABLE_NAME, where, null) > 0;
    }

    public List<Sport> getAll() {
        List<Sport> result = new ArrayList<>();
        Cursor cursor  = db.query(TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) result.add(getRecord(cursor));

        cursor.close();
        return result;
    }

    public List<Sport> getSelectedDate(int year, int month, int day){
        List<Sport> result = getAll();
        List<Sport> finalResult = new ArrayList<>();
        String date = year + "/" + month + "/" + day;

        for (Sport s : result){
            if (s.getYYYYMD().equals(date))
                finalResult.add(s);
        }

        return finalResult;
    }

    public Sport get(long id) {
        Sport sport = null;

        String where = KEY_ID + "=" + id;

        Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);

        if (result.moveToFirst()) sport = getRecord(result);

        result.close();
        return sport;
    }

    public Sport getRecord(Cursor cursor) {
        Sport result = new Sport();

        result.setId(cursor.getLong(0));
        result.setUserID(cursor.getLong(1));
        result.setTitle(cursor.getString(2));
        result.setContent(cursor.getString(3));
        result.setCalorie(cursor.getFloat(4));
        result.setDatetime(cursor.getLong(5));
        result.setTotalTime(cursor.getLong(6));

        return result;
    }
}
