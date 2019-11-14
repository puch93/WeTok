package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;

import java.lang.reflect.Field;

import kr.co.core.wetok.R;
import kr.co.core.wetok.adapter.WriteSpinnerAdapter;
import kr.co.core.wetok.databinding.ActivityAddFriendBinding;
import kr.co.core.wetok.util.CustomSpinner;

public class AddFriendAct extends AppCompatActivity implements View.OnClickListener {
    ActivityAddFriendBinding binding;
    Activity act;

    ActionBar actionBar;

    private String[] country_codes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_friend, null);
        act = this;

        country_codes = getResources().getStringArray(R.array.country_code);

        setActionBar();
        setClickListener();

        binding.llFromNumber.performClick();

        setSpinner();
    }

    private void setClickListener() {
        binding.llFromId.setOnClickListener(this);
        binding.llFromNumber.setOnClickListener(this);
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

    private void setSpinner() {
        // set spinner
        WriteSpinnerAdapter adapter_area = new WriteSpinnerAdapter(act, android.R.layout.simple_spinner_item, country_codes);
        adapter_area.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_height_set(binding.spinner, 1000);
        binding.spinner.setAdapter(adapter_area);

        // set spinner open/close listener
        binding.spinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(Spinner spinner) {

                // 현재 focus 되어있는 view 가 있으면 키보드를 내리고, focus 제거
                View view = act.getCurrentFocus();
                if(view != null) {
                    InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    view.clearFocus();
                } else {
                    view = binding.etName;
                    InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                binding.ivArrow.setSelected(true);
            }

            @Override
            public void onSpinnerClosed(Spinner spinner) {
                binding.ivArrow.setSelected(false);
            }
        });
    }

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_from_id:
                binding.llFromId.setSelected(true);
                binding.llFromNumber.setSelected(false);

                binding.ivFromNumber.setVisibility(View.INVISIBLE);
                binding.ivFromId.setVisibility(View.VISIBLE);

                binding.llFromNumberArea.setVisibility(View.GONE);
                binding.llFromIdArea.setVisibility(View.VISIBLE);
                break;

            case R.id.ll_from_number:
                binding.llFromId.setSelected(false);
                binding.llFromNumber.setSelected(true);

                binding.ivFromNumber.setVisibility(View.VISIBLE);
                binding.ivFromId.setVisibility(View.INVISIBLE);

                binding.llFromNumberArea.setVisibility(View.VISIBLE);
                binding.llFromIdArea.setVisibility(View.GONE);
                break;
        }
    }
}