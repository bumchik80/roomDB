package ru.bumchik.roomdb;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;


public class RoomDBApp extends Application {
    private static final String DATABASE_NAME = "DATABASE_USER";
    private static volatile AppDatabase database;
    public static RoomDBApp INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, DATABASE_NAME).build();
        INSTANCE = this;
    }

    public RoomDatabase getDB() {
        return database;
    }

    public static RoomDBApp get() {
        return INSTANCE;
    }
}
