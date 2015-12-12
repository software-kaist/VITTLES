package kaist.game.battlecar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import kaist.game.battlecar.R;

/**
 * Created by user on 2015-12-12.
 */
public class CarApListAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    private ArrayList<CarApList> carApList;
    int layout;

    public CarApListAdapter(Context context, int iLayout, ArrayList<CarApList> newList) {
        mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = iLayout;
        carApList = newList;
    }

    public void setCarApList(ArrayList<CarApList> newList){
        carApList = newList;
    }

    public ArrayList<CarApList> getCarApList(){
        return carApList;
    }

    public int getCount(){
        return carApList.size();
    }

    public Object getItem(int position){
        return carApList.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
        }

        TextView ssid = (TextView)convertView.findViewById(R.id.carApName);
        ssid.setText(carApList.get(position).getCarApSSID());

        TextView capa = (TextView)convertView.findViewById(R.id.apCapa);
        capa.setText(carApList.get(position).getCapabilities());

        return convertView;
    }

    public void add(CarApList pt){
        carApList.add(pt);
    }
}

