package kr.co.core.wetok.adapter.notice;

import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;


import kr.co.core.wetok.R;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class NoticeParentViewHolder extends GroupViewHolder {

    private TextView title, date;
    private ImageView arrow;
    private LinearLayout item_layout;
    private boolean isExpaneded;



    public NoticeParentViewHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.tv_title);
        date = (TextView) itemView.findViewById(R.id.tv_date);

        arrow = (ImageView) itemView.findViewById(R.id.iv_item_arrow);
        item_layout = (LinearLayout) itemView.findViewById(R.id.ll_item_layout);
    }



    public void setData(String titleText, String dateText, boolean isNew, boolean isVisible) {
        if (isVisible) {
            item_layout.setVisibility(View.VISIBLE);
            title.setText(titleText);
            date.setText(dateText);

            if (isExpaneded)
                imageExpand();
            else
                imageCollapse();


        } else {
            item_layout.setVisibility(View.GONE);
        }
    }


    @Override
    public void expand() {
        isExpaneded = true;
        imageExpand();
    }

    @Override
    public void collapse() {
        isExpaneded = false;
        imageCollapse();
    }


    /* toggle button set animation */
    private void animateExpand() {
        Log.e("TEST_HOME", "expand");
        RotateAnimation rotate =
                new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.startAnimation(rotate);
    }

    private void animateCollapse() {
        Log.e("TEST_HOME", "collapse");
        RotateAnimation rotate =
                new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.startAnimation(rotate);
    }


    /* toggle button set image */
    private void imageExpand() {
        arrow.setImageResource(R.drawable.wt_icon_close_191022);
    }

    private void imageCollapse() {
        arrow.setImageResource(R.drawable.wt_icon_open_191022);
    }
}