package kr.co.core.wetok.data;

import lombok.Data;

@Data
public class StoryData {
    private String idx;
    private String text;
    private String image;
    private String regDate;

    public StoryData(String idx, String text, String image, String regDate) {
        this.idx = idx;
        this.text = text;
        this.image = image;
        this.regDate = regDate;
    }
}
