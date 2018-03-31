package fr.etienneguerlain.fruitamax;

import java.util.Comparator;


/*

    This class holds useful information about a nearby sale (a sale that is on the server, and grabbed
    by the "Get Nearby Sales" Service if it is in the range of the user location

 */

public class NearbySale {

    private int _id;
    private double _distance;   // Distance in km between the user and where the sale has been set
    private String _title;
    private double _quantity;
    private String _unit;
    private double _price;

    public NearbySale(int id, double distance, String title, double quantity, String unit, double price){
        _id = id;
        _distance = distance;
        _title = title;
        _quantity = quantity;
        _unit = unit;
        _price = price;
    }

    public int getId(){
        return _id;
    }

    public void setId(int id){
        _id = id;
    }

    public double getDistance(){
        return _distance;
    }

    public void setDistance(double distance){
        _distance = distance;
    }

    public String getTitle(){
        return _title;
    }

    public void setTitle(String title){
        _title = title;
    }

    public double getQuantity(){
        return _quantity;
    }

    public void setQuantity(double quantity){
        _quantity = quantity;
    }

    public String getUnit(){
        return _unit;
    }

    public void setUnit(String unit){
        _unit = unit;
    }

    public double getPrice(){
        return _price;
    }

    public void setPrice(double price){
        _price = price;
    }


    // This comparator is used to sort a list of Nearby Sales
    public static Comparator<NearbySale> getCompByDistance()
    {
        Comparator comp = new Comparator<NearbySale>(){
            @Override
            public int compare(NearbySale s1, NearbySale s2)
            {
                if(s1._distance > s2._distance){
                    return 1;
                }else{
                    return -1;
                }
            }
        };
        return comp;
    }

}