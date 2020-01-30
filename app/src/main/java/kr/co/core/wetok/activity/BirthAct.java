package kr.co.core.wetok.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import kr.co.core.wetok.R;
import kr.co.core.wetok.adapter.WriteSpinnerAdapter;
import kr.co.core.wetok.databinding.ActivityBirthBinding;
import kr.co.core.wetok.databinding.ActivityNameBinding;
import kr.co.core.wetok.dialog.DatePickerDialog;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomSpinner;

public class BirthAct extends AppCompatActivity {
    ActivityBirthBinding binding;
    Activity act;

    ActionBar actionBar;

    private String[] birth_codes;

    int year = 1970;
    int month = 1;
    int day = 1;

    private static final int DATE_PICKER = 1004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_birth, null);
        act = this;
        setActionBar();

//        birth_codes = getResources().getStringArray(R.array.birth_code);
//        setSpinner();
//        for (int i = 0; i < birth_codes.length; i++) {
//            if(birth_codes[i].equalsIgnoreCase(getIntent().getStringExtra("birth"))) {
//                binding.spinner.setSelection(i, true);
//                break;
//            }
//        }

        String birth = getIntent().getStringExtra("birth");

        if(birth.length() > 7) {
            String year_s = birth.substring(0, 4);
            String month_s = birth.substring(4, 6);
            String day_s = birth.substring(6);

            binding.tvBirth.setText(year_s + month_s + day_s);

            year = Integer.parseInt(year_s);
            month = Integer.parseInt(month_s);
            day = Integer.parseInt(day_s);
        }

        binding.tvBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(act, DatePickerDialog.class);
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("day", day);
                startActivityForResult(intent, DATE_PICKER);
            }
        });


        binding.tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBirth();
            }
        });
    }

    private void setBirth() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
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

        server.setTag("Set Birth");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.SET_PROFILE_BIRTH);
        server.addParams("m_name", binding.tvBirth.getText().toString());
        server.execute(true, false);
    }


//    private void setSpinner() {
////        // set spinner
////        WriteSpinnerAdapter adapter_area = new WriteSpinnerAdapter(act, android.R.layout.simple_spinner_item, birth_codes);
////        adapter_area.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
////        spinner_height_set(binding.spinner, 1000);
////        binding.spinner.setAdapter(adapter_area);
////
////        // set spinner open/close listener
////        binding.spinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
////            @Override
////            public void onSpinnerOpened(Spinner spinner) {
////                binding.ivArrow.setSelected(true);
////            }
////
////            @Override
////            public void onSpinnerClosed(Spinner spinner) {
////                binding.ivArrow.setSelected(false);
////            }
////        });
////    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == DATE_PICKER) {
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
            }
        }
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
}
