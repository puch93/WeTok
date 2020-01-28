package kr.co.core.wetok.adapter.notice;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import kr.co.core.wetok.R;
import kr.co.core.wetok.data.NoticeChildData;
import kr.co.core.wetok.data.NoticeParentData;


public class NoticeAdapter extends ExpandableRecyclerViewAdapter<NoticeParentViewHolder, NoticeChildViewHolder> {
    List<? extends ExpandableGroup> groups;

    public NoticeAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
        this.groups = groups;
    }


    @Override
    public NoticeParentViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice_parent, parent, false);
        return new NoticeParentViewHolder(view);
    }


    @Override
    public NoticeChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice_child, parent, false);
        return new NoticeChildViewHolder(view);
    }


    @Override
    public void onBindChildViewHolder(NoticeChildViewHolder holder, int flatPosition,
                                      ExpandableGroup group, int childIndex) {

        final NoticeChildData data = ((NoticeParentData) group).getItems().get(childIndex);
        holder.setContents(data.getContents());
    }


    @Override
    public void onBindGroupViewHolder(NoticeParentViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        Log.e("TEST_HOME", "flatPosition: " + flatPosition);
        final NoticeParentData data = (NoticeParentData) group;
        data.setPos(flatPosition);
        if (flatPosition != groups.size() - 1) {
            holder.setData(data.getTitle(), data.getDate(), false, true);
        } else {
            holder.setData(data.getTitle(), data.getDate(), false, false);
        }
    }
}
