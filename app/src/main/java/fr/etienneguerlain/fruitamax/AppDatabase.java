package fr.etienneguerlain.fruitamax;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;


// Class that represent the application database

@Database(entities = {Sale.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SaleDao saleDao();
}
