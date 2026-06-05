package com.example.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.data.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    long insertUser(User user);

    @Update
    int updateUser(User user);

    @Query("SELECT * FROM users WHERE contact = :contact LIMIT 1")
    User getUserByContact(String contact);

    @Query("SELECT * FROM users WHERE contact = :contact AND password = :password LIMIT 1")
    User checkLogin(String contact, String password);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);
}
