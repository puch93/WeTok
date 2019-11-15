package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kr.co.core.wetok.BuildConfig;
import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityProfileBinding;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class ProfileAct extends AppCompatActivity implements View.OnClickListener {
    ActivityProfileBinding binding;
    Activity act;

    ActionBar actionBar;

    private static final int PICK_FROM_ALBUM = 1001;
    private static final int PICK_FROM_CAMERA = 1002;
    private static final int CROP_IMAGE = 1003;

    private static final String TYPE_PROFILE = "profile";
    private static final String TYPE_BACKGROUND = "background";
    private String img_type;

    private Uri photoUri;
    private String mPathProfile;
    private String mPathBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile, null);
        act = this;

        setActionBar();
        setClickListener();
    }

    private void setImage() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {

                        } else {

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Common.showToastNetwork(act);
                    }
                } else {
                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("Image " + img_type);
        server.addParams("dbControl", NetUrls.SET_PROFILE_IMAGE);
        server.addParams("m_idx", "setUserImage");

        if(img_type.equalsIgnoreCase(TYPE_PROFILE)) {
            File file = new File(mPathProfile);
            server.addFileParams(TYPE_PROFILE, file);
        } else {
            File file = new File(mPathBackground);
            server.addFileParams(TYPE_PROFILE, file);
        }
        server.execute(true, false);
    }

    private void setImageDelete() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {

                        } else {

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Common.showToastNetwork(act);
                    }
                } else {
                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("Image Delete " + img_type);
        server.addParams("dbControl", NetUrls.SET_PROFILE_IMAGE_DEL);
        server.addParams("m_idx", "setUserImage");
        server.addParams("type", img_type);
        server.execute(true, false);
    }

    private void setClickListener() {
        binding.flProfileImg.setOnClickListener(this);
        binding.flBackgroundImg.setOnClickListener(this);

        binding.llName.setOnClickListener(this);
        binding.llIntroduce.setOnClickListener(this);
        binding.llBirth.setOnClickListener(this);
        binding.llNumber.setOnClickListener(this);
        binding.llId.setOnClickListener(this);
        binding.llPw.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Common.showToast(act, "이미지 처리 오류! 다시 시도해주세요.");
            e.printStackTrace();
        }

        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
            } else {
                photoUri = Uri.fromFile(photoFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    private void getAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "wetok" + timeStamp;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/WeTOK/");
//        File storageDir = new File(getExternalFilesDir(null) + "/WeTOK/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    private void cropImage() {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoUri, "image/*");

        // 파일 생성
        try {
            File albumFile = createImageFile();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile(act, "kr.co.core.wetok.provider", albumFile);
            } else {
                photoUri = Uri.fromFile(albumFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", photoUri);

        // 여러 카메라어플중 기본앱 세팅
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(cropIntent, 0);

        Intent i = new Intent(cropIntent);
        ResolveInfo res = list.get(0);

        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        grantUriPermission(res.activityInfo.packageName, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
        startActivityForResult(i, CROP_IMAGE);
    }

    private void showDialog() {
        LayoutInflater dialog = LayoutInflater.from(this);
        View dlgLayout = dialog.inflate(R.layout.dialog_imgload, null);
        final Dialog dlgImgload = new Dialog(this);
        dlgImgload.setContentView(dlgLayout);

        dlgImgload.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dlgImgload.show();

        TextView tv_album = (TextView) dlgLayout.findViewById(R.id.tv_album);
        TextView tv_camera = (TextView) dlgLayout.findViewById(R.id.tv_camera);
        TextView tv_delete = (TextView) dlgLayout.findViewById(R.id.tv_delete);

        // 앨범에서 추가
        tv_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAlbum();
                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });

        // 촬영해서 추가
        tv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });

        //사진삭제
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (img_type.equalsIgnoreCase(TYPE_PROFILE)) {
                    if (StringUtil.isNull(mPathProfile)) {

                    } else {
                        binding.ivProfile.setImageBitmap(null);
                        mPathProfile = null;
                        //TODO 프로필 사진 삭제 서버전송
                    }
                } else {
                    if (StringUtil.isNull(mPathBackground)) {

                    } else {
                        binding.ivBackground.setImageBitmap(null);
                        mPathBackground = null;
                        //TODO 배경 사진 삭제 서버전송
                    }
                }

                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_FROM_ALBUM:
                    if (data == null) {
                        Common.showToast(act, "사진불러오기 실패");
                        return;
                    }

                    photoUri = data.getData();
                    cropImage();
                    break;

                case PICK_FROM_CAMERA:
                    cropImage();
                    break;

                case CROP_IMAGE:
                    if (img_type.equalsIgnoreCase(TYPE_PROFILE)) {
                        mPathProfile = photoUri.getPath();

                        if (!StringUtil.isNull(mPathProfile)) {
                            binding.ivProfileNone.setVisibility(View.VISIBLE);
                            Glide.with(act)
                                    .load(mPathProfile)
                                    .transform(new CircleCrop())
                                    .into(binding.ivProfile);

                            binding.ivProfileNone.setVisibility(View.GONE);

                            //TODO 프로필 사진 서버전송 (mPathProfile)
                        } else {
                            Common.showToast(act, "사진생성 실패");
                        }
                    } else {
                        mPathBackground = photoUri.getPath();

                        if (!StringUtil.isNull(mPathBackground)) {
                            Glide.with(act)
                                    .load(mPathBackground)
                                    .into(binding.ivBackground);

                            //TODO 배경 사진 서버전송 (mPathBackground)
                        } else {
                            Common.showToast(act, "사진생성 실패");
                        }
                    }

                    MediaScannerConnection.scanFile(act, new String[]{photoUri.getPath()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {

                                }
                            });
                    photoUri = null;
                    break;
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_profile_img:
                img_type = TYPE_PROFILE;
                showDialog();
                break;

            case R.id.fl_background_img:
                img_type = TYPE_BACKGROUND;
                showDialog();
                break;


            case R.id.ll_name:
                startActivity(new Intent(act, NameAct.class));
                break;

            case R.id.ll_birth:
                startActivity(new Intent(act, BirthAct.class));
                break;

            case R.id.ll_introduce:
                startActivity(new Intent(act, IntroduceAct.class));
                break;

            case R.id.ll_pw:
                startActivity(new Intent(act, ModifyPwAct.class));
                break;

            case R.id.ll_number:
            case R.id.ll_id:
                Intent intent = new Intent(act, InfoAct.class);
                if (v.getId() == R.id.ll_id) {
                    intent.putExtra("type", StringUtil.TYPE_INFO_ID);
                } else {
                    intent.putExtra("type", StringUtil.TYPE_INFO_NUMBER);
                }
                startActivity(intent);
                break;
        }
    }
}
