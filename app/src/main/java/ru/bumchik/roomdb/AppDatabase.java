package ru.bumchik.roomdb;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.RoomDatabase;
import android.support.annotation.NonNull;

@Database(entities = {RoomModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RoomModelDao productDao();

}
