package kr.co.core.wetok.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WriteSpinnerAdapter extends ArrayAdapter<String> {


    public WriteSpinnerAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        //change the size to which ever you want
        ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        ((TextView) view).setPadding(40,30,0,30);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        //change the size to which ever you want
        ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        ((TextView) view).setPadding(40,40,0,40);
        return view;
    }
}
