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
 * Created by user on 2016-01-13.
 */
public class InAppListAdapter  extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    private ArrayList<InAppList> inAppList;
    int layout;

    public InAppListAdapter(Context context, int iLayout, ArrayList<InAppList> newList) {
        mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = iLayout;
        inAppList = newList;
    }

    public void setInAppList(ArrayList<InAppList> newList){
        inAppList = newList;
    }

    public ArrayList<InAppList> getInAppList(){
        return inAppList;
    }

    public int getCount(){
        return inAppList.size();
    }

    public Object getItem(int position){
        return inAppList.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(layout, parent, false);
        }

        TextView ssid = (TextView)convertView.findViewById(R.id.tvDescription);
        ssid.setText(inAppList.get(position).getProductId());

        TextView capa = (TextView)convertView.findViewById(R.id.tvProductId);
        capa.setText(inAppList.get(position).getDescription());

        return convertView;
    }

    public void add(InAppList pt){
        inAppList.add(pt);
    }
}
