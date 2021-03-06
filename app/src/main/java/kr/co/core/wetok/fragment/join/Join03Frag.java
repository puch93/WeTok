package kr.co.core.wetok.fragment.join;


import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kr.co.core.wetok.BuildConfig;
import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.JoinAct;
import kr.co.core.wetok.databinding.FragmentJoin03Binding;
import kr.co.core.wetok.dialog.DatePickerDialog;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

import static android.app.Activity.RESULT_OK;

public class Join03Frag extends BaseFrag implements View.OnClickListener {
    private FragmentJoin03Binding binding;
    private AppCompatActivity act;

    private String[] birth_codes;
    private String id, pw, fcm_token;

    private static final int PICK_FROM_ALBUM = 1001;
    private static final int PICK_FROM_CAMERA = 1002;
    private static final int CROP_IMAGE = 1003;
    private static final int DATE_PICKER = 1004;

    private Uri photoUri;
    private String mPathProfile;

    private String phoneNum;

    int year = 1970;
    int month = 1;
    int day = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_03, container, false);
        act = (AppCompatActivity) getActivity();

        birth_codes = getResources().getStringArray(R.array.birth_code);

        id = getArguments().getString("id");
        pw = getArguments().getString("pw");

        phoneNum = Common.getPhoneNumber(act);

        if (StringUtil.isNull(UserPref.getFcmToken(act))) {
            getFcmToken();
        } else {
            fcm_token = UserPref.getFcmToken(act);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.ivProfile.setClipToOutline(true);
        }
        binding.tvConfirm.setOnClickListener(this);
        binding.flProfileImg.setOnClickListener(this);
        binding.tvBirth.setOnClickListener(this);

        binding.etName.addTextChangedListener(new TextWatcher() {
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

//        setSpinner();

        return binding.getRoot();
    }

    private void nextProcess() {
        BaseFrag fragment = new Join04Frag();

        Bundle bundle = new Bundle(1);
        bundle.putString("name", binding.etName.getText().toString());
        bundle.putString("image", mPathProfile);
        bundle.putString("id", id);
        bundle.putString("pw", pw);
        fragment.setArguments(bundle);

        ((JoinAct) act).replaceFragment(fragment);
    }


    private void setJoin() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        final String result = jo.getString("result");
                        final String message = jo.getString("message");

                        if (result.equalsIgnoreCase("Y")) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nextProcess();
                                }
                            });
                        } else {
                            Common.showToast(act, message);
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

        server.setTag("Join");
        server.addParams("dbControl", NetUrls.USER_REGI);
        server.addParams("m_regi", fcm_token);
        server.addParams("m_uniq", UserPref.getDeviceId(act));
        server.addParams("m_nickname", binding.etName.getText().toString());
        server.addParams("m_id", id);
        server.addParams("m_pass", pw);
        server.addParams("m_pass_confirm", pw);
        server.addParams("m_birthday", binding.tvBirth.getText().toString());
//        server.addParams("m_hp", phoneNum);
        server.addParams("m_hp", Common.getRandomPhoneNumber());

        File file = new File(mPathProfile);
        server.addFileParams("m_profile", file);
        Common.showToast(act, server.toString());
//        server.execute(true, true);
    }

    private void getFcmToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.i(StringUtil.TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        UserPref.setFcmToken(act, token);
                        fcm_token = token.replace("%3", ":");

                        Log.e(StringUtil.TAG, "myFcmToken: " + token);
                    }
                });
    }

//    private void setSpinner() {
//        // set spinner
//        WriteSpinnerAdapter adapter_area = new WriteSpinnerAdapter(act, android.R.layout.simple_spinner_item, birth_codes);
//        adapter_area.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner_height_set(binding.spinner, 1000);
//        binding.spinner.setAdapter(adapter_area);
//
//        // set spinner open/close listener
//        binding.spinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
//            @Override
//            public void onSpinnerOpened(Spinner spinner) {
//
//                // 현재 focus 되어있는 view 가 있으면 키보드를 내리고, focus 제거
//                View view = act.getCurrentFocus();
//                if (view != null) {
//                    InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                    view.clearFocus();
//                }
//
//                binding.ivArrow.setSelected(true);
//            }
//
//            @Override
//            public void onSpinnerClosed(Spinner spinner) {
//                binding.ivArrow.setSelected(false);
//            }
//        });
//    }

    //Spinner 길이설정
    private void spinner_height_set(Spinner spinner, int height) {
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinner);

            // Set popupWindow height to 500px
            popupWindow.setHeight(height);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
    }

    private void checkButtonActivation() {
        if (
                !StringUtil.isNull(binding.etName.getText().toString()) &&
                        !StringUtil.isNull(phoneNum) &&
                        !StringUtil.isNull(mPathProfile)

        ) {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_enable_191022);
        } else {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_disable_191022);
        }
    }

    private void getAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Common.showToast(act, getString(R.string.join_picture_process_warning));
            e.printStackTrace();
        }

        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile(act,
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
            } else {
                photoUri = Uri.fromFile(photoFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "wetok" + timeStamp;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/WeTOK/");
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
        List<ResolveInfo> list = act.getPackageManager().queryIntentActivities(cropIntent, 0);

        Intent i = new Intent(cropIntent);
        ResolveInfo res = list.get(0);

        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        act.grantUriPermission(res.activityInfo.packageName, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
        startActivityForResult(i, CROP_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_FROM_ALBUM:
                    if (data == null) {
                        Common.showToast(act, getString(R.string.join_picture_load_warning));
                        return;
                    }

                    photoUri = data.getData();
                    cropImage();
                    break;

                case PICK_FROM_CAMERA:
                    cropImage();
                    break;

                case CROP_IMAGE:
                    mPathProfile = photoUri.getPath();

                    if (!StringUtil.isNull(mPathProfile)) {
                        Glide.with(act)
                                .load(mPathProfile)
                                .into(binding.ivProfile);
                    } else {
                        Common.showToast(act, getString(R.string.join_picture_warning));
                    }

                    break;

                case DATE_PICKER:
                    year = data.getIntExtra("year", 0);
                    month = data.getIntExtra("month", 0);
                    day = data.getIntExtra("day", 0);

                    String year_s = String.valueOf(year);
                    String month_s = String.valueOf(month);
                    String day_s = String.valueOf(day);

                    if (month_s.length() == 1)
                        month_s = "0" + month_s;

                    if (day_s.length() == 1)
                        day_s = "0" + day_s;


                    binding.tvBirth.setText(year_s + month_s + day_s);
                    break;
            }
        }
    }

    private void showDialog() {
        LayoutInflater dialog = LayoutInflater.from(act);
        View dlgLayout = dialog.inflate(R.layout.dialog_imgload, null);
        final Dialog dlgImgload = new Dialog(act);
        dlgImgload.setContentView(dlgLayout);

        dlgImgload.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dlgImgload.show();

        TextView tv_album = (TextView) dlgLayout.findViewById(R.id.tv_album);
        TextView tv_camera = (TextView) dlgLayout.findViewById(R.id.tv_camera);
        TextView tv_delete = (TextView) dlgLayout.findViewById(R.id.tv_delete);
        tv_delete.setVisibility(View.GONE);

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
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_confirm) {
            // 닉네임 길이 0 검사
            if (StringUtil.isNull(binding.etName.getText().toString())) {
                Common.showToast(act, getString(R.string.join_name_warning));
                return;
            }

            // 프로필 사진 검사
            if (StringUtil.isNull(mPathProfile)) {
                Common.showToast(act, getString(R.string.join_profile_warning));
                return;
            }

            // 휴대폰번호 검사
            if (StringUtil.isNull(phoneNum)) {
                Common.showToast(act, getString(R.string.join_number_warning));
                return;
            }

            // 생년월일 검사
            if (StringUtil.isNull(binding.tvBirth.getText().toString())) {
                Common.showToast(act, getString(R.string.join_birth_warning));
                return;
            }

            setJoin();
        } else if (v.getId() == R.id.fl_profile_img) {
            showDialog();
        } else if (v.getId() == R.id.tv_birth) {
            Intent intent = new Intent(act, DatePickerDialog.class);
            intent.putExtra("year", year);
            intent.putExtra("month", month);
            intent.putExtra("day", day);
            startActivityForResult(intent, DATE_PICKER);
        }
    }
}