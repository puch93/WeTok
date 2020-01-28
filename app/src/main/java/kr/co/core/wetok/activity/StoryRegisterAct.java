package kr.co.core.wetok.activity;

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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kr.co.core.wetok.BuildConfig;
import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityStoryRegisterBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class StoryRegisterAct extends BaseAct implements View.OnClickListener {
    ActivityStoryRegisterBinding binding;
    Activity act;
    ActionBar actionBar;

    private static final int PICK_FROM_ALBUM = 1001;
    private static final int PICK_FROM_CAMERA = 1002;
    private static final int CROP_IMAGE = 1003;

    private Uri photoUri;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_story_register, null);
        act = this;

        setActionBar();

        binding.flImage.setOnClickListener(this);
        binding.tvRegister.setOnClickListener(this);

        binding.etContents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkButtonActivation();
            }
        });
    }

    /*&&
                        !StringUtil.isNull(mImagePath)*/
    private void checkButtonActivation() {
        if (
                !StringUtil.isNull(binding.etContents.getText().toString())
        ) {
            binding.tvRegister.setBackgroundResource(R.drawable.wt_btn360_enable_191022);
        } else {
            binding.tvRegister.setBackgroundResource(R.drawable.wt_btn360_disable_191022);
        }
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
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
                setImageDelete();

                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });
    }

    private void setImageDelete() {
        photoUri = null;
        mImagePath = null;
        binding.ivImage.setImageBitmap(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_FROM_ALBUM:
                    if (data == null) {
                        Common.showToast(act, getString(R.string.profile_image_load_fail));
                        return;
                    }

                    photoUri = data.getData();
                    cropImage();
                    break;
                case PICK_FROM_CAMERA:
                    cropImage();
                    break;

                case CROP_IMAGE:
                    mImagePath = photoUri.getPath();

                    if (!StringUtil.isNull(mImagePath)) {
                        Glide.with(act)
                                .load(mImagePath)
                                .into(binding.ivImage);

                    } else {
                        Common.showToast(act, getString(R.string.profile_image_fail));
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

    private void setRegisterStory() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {
                            Common.showToast(act, "등록이 완료되었습니다");
                            setResult(RESULT_OK);
                            finish();
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

        server.setTag("Story Register");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.SET_STORY);
        server.addParams("contents", binding.etContents.getText().toString());

        if(mImagePath != null) {
            File file = new File(mImagePath);
            server.addFileParams("s_img", file);
        }

        server.execute(true, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_image:
                showDialog();
                break;

            case R.id.tv_register:
                if(StringUtil.isNull(binding.etContents.getText().toString())) {
                    Common.showToast(act, getString(R.string.act_register_warning01));
                    return;
                }

//                if(StringUtil.isNull(mImagePath)) {
//                    Common.showToast(act, getString(R.string.act_register_warning02));
//                    return;
//                }

                setRegisterStory();
                break;
        }
    }
}
