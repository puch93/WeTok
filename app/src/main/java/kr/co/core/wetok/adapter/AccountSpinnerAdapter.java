package kr.co.core.wetok.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kr.co.core.wetok.R;

public class AccountSpinnerAdapter extends ArrayAdapter<String> {
    String[] spinnerNames;
    int[] spinnerImages;
    Context mContext;
    int resource;


    public AccountSpinnerAdapter(@NonNull Context context, String[] names, int[] images, int resource) {
        super(context, resource);

        this.spinnerNames = names;
        this.spinnerImages = images;
        this.mContext = context;
        this.resource = resource;
    }

    public void setRefresh(String[] names, int[] images, int resource) {
        this.spinnerNames = names;
        this.spinnerImages = images;
        this.resource = resource;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return spinnerNames.length;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (null != spinnerImages) {
            ImageViewHolder mViewHolder = new ImageViewHolder();

            if (convertView == null) {

                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(resource, parent, false);

                mViewHolder.mImage = (ImageView) convertView.findViewById(R.id.iv_country);
                mViewHolder.mName = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(mViewHolder);

            } else {
                mViewHolder = (ImageViewHolder) convertView.getTag();
            }

            mViewHolder.mImage.setImageResource(spinnerImages[position]);
            mViewHolder.mImage.setPadding(20, 0, 0, 0);
            mViewHolder.mName.setText(spinnerNames[position]);

            if(spinnerNames[position].equalsIgnoreCase("선택")) {
                mViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.color_803c4449));
            } else {
                mViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.color_3c4449));
            }
        } else {
            ViewHolder mViewHolder = new ViewHolder();

            if (convertView == null) {

                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(resource, parent, false);

                mViewHolder.mName = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(mViewHolder);

            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            mViewHolder.mName.setText(spinnerNames[position]);
            mViewHolder.mName.setPadding(20, 0, 0, 0);

            if(spinnerNames[position].equalsIgnoreCase("선택")) {
                mViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.color_803c4449));
            } else {
                mViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.color_3c4449));
            }
        }

        return convertView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (null != spinnerImages) {
            ImageViewHolder mViewHolder = new ImageViewHolder();

            if (convertView == null) {

                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(resource, parent, false);

                mViewHolder.mImage = (ImageView) convertView.findViewById(R.id.iv_country);
                mViewHolder.mName = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(mViewHolder);

            } else {
                mViewHolder = (ImageViewHolder) convertView.getTag();
            }

            mViewHolder.mImage.setImageResource(spinnerImages[position]);
            mViewHolder.mName.setText(spinnerNames[position]);

            if(spinnerNames[position].equalsIgnoreCase("선택")) {
                mViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.color_803c4449));
            } else {
                mViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.color_3c4449));
            }
        } else {
            ViewHolder mViewHolder = new ViewHolder();

            if (convertView == null) {

                LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(resource, parent, false);

                mViewHolder.mName = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(mViewHolder);

            } else {
                mViewHolder = (ViewHolder) convertView.getTag();
            }

            mViewHolder.mName.setText(spinnerNames[position]);

            if(spinnerNames[position].equalsIgnoreCase("선택")) {
                mViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.color_803c4449));
            } else {
                mViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.color_3c4449));
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView mName;
    }

    private static class ImageViewHolder extends ViewHolder {
        ImageView mImage;
    }
}
