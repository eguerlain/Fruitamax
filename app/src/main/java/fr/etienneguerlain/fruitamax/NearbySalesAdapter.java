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

    This adapter takes a list of NearbySales (grabbed from the server by the getNearbySalesService)
    and creates a View for each element in it

 */

public class NearbySalesAdapter extends ArrayAdapter<NearbySale> {

    public NearbySalesAdapter(Context context, ArrayList<NearbySale> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        NearbySale sale = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.nearby_sale, parent, false);
        }

        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.saleTitle);
        TextView quantity = (TextView) convertView.findViewById(R.id.saleQuantity);
        TextView unit = (TextView) convertView.findViewById(R.id.saleUnit);
        TextView price = (TextView) convertView.findViewById(R.id.salePrice);
        TextView unitForPrice = (TextView) convertView.findViewById(R.id.unitForPrice);
        TextView distance = (TextView) convertView.findViewById(R.id.saleDistance);

        // Populate the data into the template view using the data object
        title.setText(sale.getTitle());
        quantity.setText("" + Math.round(sale.getQuantity()));
        unit.setText(sale.getUnit());
        price.setText("" + sale.getPrice());
        unitForPrice.setText(sale.getUnit());
        distance.setText("" + Math.round(sale.getDistance()));

        // Return the completed view to render on screen
        return convertView;
    }
}