package kr.co.core.wetok.data;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class NoticeChildData implements Parcelable {
    private String contents;

    public NoticeChildData(String contents) {
        this.contents = contents;
    }

    protected NoticeChildData(Parcel in) {
    }

    public static final Creator<NoticeChildData> CREATOR = new Creator<NoticeChildData>() {
        @Override
        public NoticeChildData createFromParcel(Parcel in) {
            return new NoticeChildData(in);
        }

        @Override
        public NoticeChildData[] newArray(int size) {
            return new NoticeChildData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
