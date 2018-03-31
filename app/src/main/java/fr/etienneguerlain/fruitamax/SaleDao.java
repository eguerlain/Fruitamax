package fr.etienneguerlain.fruitamax;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


/*

    This class handles all the operations on the local SQLite database concerning the sales

 */

@Dao
public interface SaleDao {


    // As of now, it is possible to get all the sales stored in the database, but without differenciating
    // them by their user_token. This means that the mySalesActivity will display the entire content
    // of the DB, and not only those owned by the current logged user.

    // If a user A posts a sale, then logs out, and then a user B logs in (in the same app, on the same phone)
    // user B will see the previously sale posted by user A

    @Query("SELECT * FROM sale")
    List<Sale> getAll();


    // The following method allows to insert (a) new sale(s) in the local SQLite database

    @Insert
    void insertAll(Sale... sales);

}