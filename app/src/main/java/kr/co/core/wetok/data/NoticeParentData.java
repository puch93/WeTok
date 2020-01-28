package kr.co.core.wetok.data;


import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import lombok.Data;

@Data
public class NoticeParentData extends ExpandableGroup<NoticeChildData> {
    String idx, title, date;
    int pos;
    boolean isNew;

    public NoticeParentData(String title, String date, boolean isNew, List<NoticeChildData> items) {
        super(title, items);
        this.title = title;
        this.date = date;
        this.isNew = isNew;
    }
}
