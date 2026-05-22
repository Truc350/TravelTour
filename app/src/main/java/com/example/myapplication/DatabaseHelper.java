package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TravelTour.db";
    private static final int DATABASE_VERSION = 2;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONTACT = "contact";
    private static final String COLUMN_PASSWORD = "password";

    // Passengers table
    public static final String TABLE_PASSENGERS = "passengers";
    public static final String COLUMN_P_ID = "id";
    public static final String COLUMN_P_SALUTATION = "salutation";
    public static final String COLUMN_P_FULLNAME = "fullname";
    public static final String COLUMN_P_BIRTHDATE = "birthdate";
    public static final String COLUMN_P_NATIONALITY = "nationality";
    public static final String COLUMN_P_ISSUING_COUNTRY = "issuing_country";
    public static final String COLUMN_P_EXPIRY_DATE = "expiry_date";
    public static final String COLUMN_P_ID_OR_PASSPORT = "id_or_passport";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_CONTACT + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create Passengers Table
        String CREATE_PASSENGERS_TABLE = "CREATE TABLE " + TABLE_PASSENGERS + "("
                + COLUMN_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_P_SALUTATION + " TEXT,"
                + COLUMN_P_FULLNAME + " TEXT,"
                + COLUMN_P_BIRTHDATE + " TEXT,"
                + COLUMN_P_NATIONALITY + " TEXT,"
                + COLUMN_P_ISSUING_COUNTRY + " TEXT,"
                + COLUMN_P_EXPIRY_DATE + " TEXT,"
                + COLUMN_P_ID_OR_PASSPORT + " TEXT" + ")";
        db.execSQL(CREATE_PASSENGERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSENGERS);
        onCreate(db);
    }

    /**
     * Thêm tài khoản mới vào cơ sở dữ liệu
     */
    public boolean addUser(String name, String contact, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CONTACT, contact);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Kiểm tra xem số điện thoại hoặc email đã tồn tại hay chưa
     */
    public boolean checkUserExists(String contact) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_CONTACT + "=?", new String[]{contact},
                null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    /**
     * Xác thực thông tin tài khoản khi đăng nhập
     */
    public boolean checkUserCredentials(String contact, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_CONTACT + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{contact, password},
                null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    // ==========================================
    // PASSENGER CRUD OPERATIONS
    // ==========================================

    /**
     * Thêm hành khách mới
     */
    public boolean addPassenger(String salutation, String fullname, String birthdate, 
                                String nationality, String issuingCountry, String expiryDate, 
                                String idOrPassport) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_P_SALUTATION, salutation);
        values.put(COLUMN_P_FULLNAME, fullname);
        values.put(COLUMN_P_BIRTHDATE, birthdate);
        values.put(COLUMN_P_NATIONALITY, nationality);
        values.put(COLUMN_P_ISSUING_COUNTRY, issuingCountry);
        values.put(COLUMN_P_EXPIRY_DATE, expiryDate);
        values.put(COLUMN_P_ID_OR_PASSPORT, idOrPassport);

        long result = db.insert(TABLE_PASSENGERS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Cập nhật thông tin hành khách
     */
    public boolean updatePassenger(int id, String salutation, String fullname, String birthdate, 
                                   String nationality, String issuingCountry, String expiryDate, 
                                   String idOrPassport) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_P_SALUTATION, salutation);
        values.put(COLUMN_P_FULLNAME, fullname);
        values.put(COLUMN_P_BIRTHDATE, birthdate);
        values.put(COLUMN_P_NATIONALITY, nationality);
        values.put(COLUMN_P_ISSUING_COUNTRY, issuingCountry);
        values.put(COLUMN_P_EXPIRY_DATE, expiryDate);
        values.put(COLUMN_P_ID_OR_PASSPORT, idOrPassport);

        int result = db.update(TABLE_PASSENGERS, values, COLUMN_P_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    /**
     * Xóa hành khách
     */
    public boolean deletePassenger(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PASSENGERS, COLUMN_P_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    /**
     * Lấy thông tin hành khách theo ID
     */
    public Map<String, String> getPassenger(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PASSENGERS, null, COLUMN_P_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        
        Map<String, String> passenger = null;
        if (cursor != null && cursor.moveToFirst()) {
            passenger = new HashMap<>();
            passenger.put("id", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_P_ID))));
            passenger.put("salutation", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_SALUTATION)));
            passenger.put("fullname", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_FULLNAME)));
            passenger.put("birthdate", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_BIRTHDATE)));
            passenger.put("nationality", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_NATIONALITY)));
            passenger.put("issuing_country", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_ISSUING_COUNTRY)));
            passenger.put("expiry_date", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_EXPIRY_DATE)));
            passenger.put("id_or_passport", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_ID_OR_PASSPORT)));
        }
        if (cursor != null) cursor.close();
        db.close();
        return passenger;
    }

    /**
     * Lấy tất cả danh sách hành khách
     */
    public List<Map<String, String>> getAllPassengers() {
        List<Map<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PASSENGERS, null, null, null, null, null, COLUMN_P_ID + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Map<String, String> passenger = new HashMap<>();
                passenger.put("id", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_P_ID))));
                passenger.put("salutation", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_SALUTATION)));
                passenger.put("fullname", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_FULLNAME)));
                passenger.put("birthdate", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_BIRTHDATE)));
                passenger.put("nationality", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_NATIONALITY)));
                passenger.put("issuing_country", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_ISSUING_COUNTRY)));
                passenger.put("expiry_date", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_EXPIRY_DATE)));
                passenger.put("id_or_passport", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_P_ID_OR_PASSPORT)));
                list.add(passenger);
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
        db.close();
        return list;
    }
}

