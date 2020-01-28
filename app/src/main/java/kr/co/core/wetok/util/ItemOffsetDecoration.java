package kr.co.core.wetok.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import kr.co.core.wetok.R;

public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {
    Activity act;

    public ItemOffsetDecoration(Activity act) {
        this.act = act;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view);

        outRect.top = act.getResources().getDimensionPixelSize(R.dimen.dimen_10);
    }
}