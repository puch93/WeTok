package kr.co.core.wetok.data;

import lombok.Data;

@Data
public class ChattingData {
    private String idx;
    private String user_idx;

    private String sendTime;
    private String dateLine;

    private String isRead;

    private String u_name;
    private String u_image;
    private String system_check;
    private String flag_delete;
    private String limit_time;
    private String file_size;

    private String msg;

    private String type;

    private boolean selected = false;

    public ChattingData(String idx, String msg, String user_idx, String sendTime, String dateLine, String isRead, String type, String flag_delete, String limit_time, String u_name, String u_image, String system_check, String file_size) {
        this.idx = idx;
        this.msg = msg;
        this.user_idx = user_idx;
        this.sendTime = sendTime;
        this.dateLine = dateLine;
        this.isRead = isRead;
        this.type = type;
        this.flag_delete = flag_delete;
        this.limit_time = limit_time;
        this.u_name = u_name;
        this.u_image = u_image;
        this.system_check = system_check;
        this.file_size = file_size;
    }

    public ChattingData(String dateLine, String type) {
        this.dateLine = dateLine;
        this.type = type;
    }
}
