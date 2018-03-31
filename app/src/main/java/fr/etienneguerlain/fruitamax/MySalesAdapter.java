package fr.etienneguerlain.fruitamax;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import fr.etienneguerlain.fruitamax.NearbySale;

/*

    This adapter takes a list of Sales (posted by the user) and creates a View for each element in it

 */

public class MySalesAdapter extends ArrayAdapter<Sale> {

    public MySalesAdapter(Context context, ArrayList<Sale> sales) {
        super(context, 0, sales);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Sale sale = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_sale, parent, false);
        }

        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.mySaleTitle);
        TextView quantity = (TextView) convertView.findViewById(R.id.mySaleQuantity);
        TextView unit = (TextView) convertView.findViewById(R.id.mySaleUnit);
        TextView price = (TextView) convertView.findViewById(R.id.mySalePrice);
        TextView unitForPrice = (TextView) convertView.findViewById(R.id.mySaleUnitForPrice);

        // Populate the data into the template view using the data object
        title.setText(sale.title);
        quantity.setText("" + Math.round(sale.quantity));
        unit.setText(sale.unit);
        price.setText("" + sale.price);
        unitForPrice.setText(sale.unit);

        // Return the completed view to render on screen
        return convertView;
    }
}