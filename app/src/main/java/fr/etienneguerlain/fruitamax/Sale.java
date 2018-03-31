package fr.etienneguerlain.fruitamax;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


/*

    This class represent the sale object, stored in the local SQLite database

 */

@Entity
public class Sale {

    @PrimaryKey(autoGenerate = true)
    public Integer id;

    public String title;
    public double price;
    public String unit;
    public double quantity;


    // To identify the "owner" of the sale, we use the authentication token of the user
    public String user_token;
}