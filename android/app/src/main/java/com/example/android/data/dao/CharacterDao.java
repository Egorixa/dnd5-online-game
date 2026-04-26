package com.example.android.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.android.data.model.Character;
import java.util.List;

@Dao
public interface CharacterDao {

    @Insert
    long insert(Character character);

    @Update
    void update(Character character);

    @Delete
    void delete(Character character);

    @Query("SELECT * FROM characters WHERE userId = :userId ORDER BY updatedAt DESC")
    List<Character> getByUser(int userId);

    @Query("SELECT * FROM characters WHERE id = :id LIMIT 1")
    Character findById(int id);
}
