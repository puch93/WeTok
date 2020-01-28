package kr.co.core.wetok.data;

import java.util.ArrayList;

import lombok.Data;

@Data
public class ChattingListData {
    private String idx;
    private String roomIdx;
    private String sendTime;
    private String readCount;
    private String text;

    private ArrayList<UserData> userArray;
    private String userNames;

    public ChattingListData (String idx, String roomIdx, String sendTime, String readCount, String text, ArrayList<UserData> userArray, String userNames) {
        this.idx = idx;
        this.roomIdx = roomIdx;
        this.sendTime = sendTime;
        this.readCount = readCount;
        this.text = text;
        this.userArray = userArray;
        this.userNames = userNames;
    }
}
