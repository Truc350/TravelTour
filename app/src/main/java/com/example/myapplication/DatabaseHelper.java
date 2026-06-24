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
    private static final int DATABASE_VERSION = 6;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONTACT = "contact";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_GENDER = "gender";
    private static final String COLUMN_DOB = "dob";
    private static final String COLUMN_AVATAR = "avatar";
    private static final String COLUMN_INVOICE_COMPANY = "invoice_company";
    private static final String COLUMN_INVOICE_TAX_CODE = "invoice_tax_code";
    private static final String COLUMN_INVOICE_ADDRESS = "invoice_address";
    private static final String COLUMN_INVOICE_EMAIL = "invoice_email";

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
    public static final String COLUMN_P_STATUS = "status";

    // Favorites table
    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_F_ID = "id";
    private static final String COLUMN_F_CONTACT = "user_contact";
    private static final String COLUMN_F_TOUR_TYPE = "tour_type";

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
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_GENDER + " TEXT,"
                + COLUMN_DOB + " TEXT,"
                + COLUMN_AVATAR + " TEXT,"
                + COLUMN_INVOICE_COMPANY + " TEXT,"
                + COLUMN_INVOICE_TAX_CODE + " TEXT,"
                + COLUMN_INVOICE_ADDRESS + " TEXT,"
                + COLUMN_INVOICE_EMAIL + " TEXT" + ")";
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
                + COLUMN_P_ID_OR_PASSPORT + " TEXT,"
                + COLUMN_P_STATUS + " TEXT DEFAULT 'PENDING'" + ")";
        db.execSQL(CREATE_PASSENGERS_TABLE);

        // Create Favorites Table
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_F_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_F_CONTACT + " TEXT,"
                + COLUMN_F_TOUR_TYPE + " TEXT,"
                + "UNIQUE(" + COLUMN_F_CONTACT + ", " + COLUMN_F_TOUR_TYPE + ")" + ")";
        db.execSQL(CREATE_FAVORITES_TABLE);

        // Pre-insert default test credentials (admin@gmail.com / 123456)
        ContentValues defaultUser = new ContentValues();
        defaultUser.put(COLUMN_NAME, "Ngọc Quyên");
        defaultUser.put(COLUMN_CONTACT, "admin@gmail.com");
        defaultUser.put(COLUMN_PASSWORD, "123456");
        defaultUser.put(COLUMN_GENDER, "Nữ");
        defaultUser.put(COLUMN_DOB, "18/09/2004");
        db.insert(TABLE_USERS, null, defaultUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSENGERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
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
        values.put(COLUMN_GENDER, "");
        values.put(COLUMN_DOB, "");

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

    public int getUserIdByContact(String contact) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_CONTACT + "=?", new String[]{contact},
                null, null, null);
        int userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }
        if (cursor != null) cursor.close();
        db.close();
        return userId;
    }

    /**
     * Lấy thông tin chi tiết của người dùng theo contact (email hoặc sđt)
     */
    public Map<String, String> getUserDetails(String contact) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COLUMN_CONTACT + "=?", new String[]{contact},
                null, null, null);
        
        Map<String, String> user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new HashMap<>();
            user.put("name", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            user.put("contact", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT)));
            user.put("gender", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)));
            user.put("dob", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB)));
            user.put("avatar", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR)));
            user.put("invoice_company", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INVOICE_COMPANY)));
            user.put("invoice_tax_code", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INVOICE_TAX_CODE)));
            user.put("invoice_address", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INVOICE_ADDRESS)));
            user.put("invoice_email", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INVOICE_EMAIL)));
        }
        if (cursor != null) cursor.close();
        db.close();
        return user;
    }

    /**
     * Cập nhật hồ sơ thông tin cá nhân của người dùng
     */
    public boolean updateUserProfile(String oldContact, String name, String newContact, String dob, String gender) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CONTACT, newContact);
        values.put(COLUMN_DOB, dob);
        values.put(COLUMN_GENDER, gender);
        
        int result = db.update(TABLE_USERS, values, COLUMN_CONTACT + "=?", new String[]{oldContact});
        db.close();
        return result > 0;
    }

    /**
     * Cập nhật ảnh đại diện của người dùng
     */
    public boolean updateUserAvatar(String contact, String avatarPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AVATAR, avatarPath);
        int result = db.update(TABLE_USERS, values, COLUMN_CONTACT + "=?", new String[]{contact});
        db.close();
        return result > 0;
    }

    /**
     * Cập nhật thông tin hóa đơn điện tử của người dùng
     */
    public boolean updateUserInvoiceInfo(String contact, String company, String taxCode, String address, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INVOICE_COMPANY, company);
        values.put(COLUMN_INVOICE_TAX_CODE, taxCode);
        values.put(COLUMN_INVOICE_ADDRESS, address);
        values.put(COLUMN_INVOICE_EMAIL, email);
        int result = db.update(TABLE_USERS, values, COLUMN_CONTACT + "=?", new String[]{contact});
        db.close();
        return result > 0;
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

    /**
     * Cập nhật mật khẩu cho người dùng
     */
    public boolean updatePassword(String contact, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);
        int result = db.update(TABLE_USERS, values, COLUMN_CONTACT + "=?", new String[]{contact});
        db.close();
        return result > 0;
    }

    /**
     * Lấy mật khẩu hiện tại của người dùng
     */
    public String getPassword(String contact) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_PASSWORD},
                COLUMN_CONTACT + "=?", new String[]{contact},
                null, null, null);
        String password = "";
        if (cursor != null && cursor.moveToFirst()) {
            password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
        }
        if (cursor != null) cursor.close();
        db.close();
        return password;
    }

    /**
     * Lấy contact của người dùng cuối cùng đăng ký (dùng làm fallback khi chưa đăng nhập)
     */
    public String getLastUserContact() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_CONTACT},
                null, null, null, null, COLUMN_ID + " DESC", "1");
        String contact = "";
        if (cursor != null && cursor.moveToFirst()) {
            contact = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT));
        }
        if (cursor != null) cursor.close();
        db.close();
        return contact;
    }

    // ==========================================
    // WISHLIST (FAVORITES) CRUD OPERATIONS
    // ==========================================

    /**
     * Thêm tour vào danh sách yêu thích
     */
    public boolean addFavorite(String contact, String tourType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_F_CONTACT, contact);
        values.put(COLUMN_F_TOUR_TYPE, tourType);

        long result = db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result != -1;
    }

    /**
     * Xóa tour khỏi danh sách yêu thích
     */
    public boolean removeFavorite(String contact, String tourType) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_FAVORITES, COLUMN_F_CONTACT + "=? AND " + COLUMN_F_TOUR_TYPE + "=?",
                new String[]{contact, tourType});
        db.close();
        return result > 0;
    }

    /**
     * Kiểm tra xem tour có được yêu thích bởi người dùng không
     */
    public boolean isTourFavorited(String contact, String tourType) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_F_ID},
                COLUMN_F_CONTACT + "=? AND " + COLUMN_F_TOUR_TYPE + "=?",
                new String[]{contact, tourType},
                null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    /**
     * Lấy danh sách tất cả các loại tour yêu thích của người dùng
     */
    public List<String> getFavoriteTours(String contact) {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_F_TOUR_TYPE},
                COLUMN_F_CONTACT + "=?", new String[]{contact},
                null, null, COLUMN_F_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_F_TOUR_TYPE)));
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
        db.close();
        return list;
    }
}

