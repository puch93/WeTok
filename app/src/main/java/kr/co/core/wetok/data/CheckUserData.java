package kr.co.core.wetok.data;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class CheckUserData implements Serializable{
    private String idx;
    private String id;
    private String pw;
    private String hp;
    private String intro;
    private String name;
    private String birth;

    private String profile_img;
    private String background_img;

    boolean isChecked = false;

    public void setData(String idx, String id, String pw, String hp, String intro, String name, String birth,
                        String profile_img, String background_img, boolean isChecked) {
        this.idx = idx;
        this.id = id;
        this.pw = pw;
        this.hp = hp;
        this.intro = intro;
        this.name = name;
        this.birth = birth;

        this.profile_img = profile_img;
        this.background_img = background_img;

        this.isChecked = isChecked;
    }
}
