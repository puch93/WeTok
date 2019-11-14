package kr.co.core.wetok.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.lang.reflect.Field;

import kr.co.core.wetok.R;
import kr.co.core.wetok.adapter.WriteSpinnerAdapter;
import kr.co.core.wetok.databinding.ActivityBirthBinding;
import kr.co.core.wetok.databinding.ActivityNameBinding;
import kr.co.core.wetok.util.CustomSpinner;

public class BirthAct extends AppCompatActivity {
    ActivityBirthBinding binding;
    Activity act;

    ActionBar actionBar;

    private String[] birth_codes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_birth, null);
        act = this;

        birth_codes = getResources().getStringArray(R.array.birth_code);

        setActionBar();
        setSpinner();
    }

    private void setSpinner() {
        // set spinner
        WriteSpinnerAdapter adapter_area = new WriteSpinnerAdapter(act, android.R.layout.simple_spinner_item, birth_codes);
        adapter_area.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_height_set(binding.spinner, 1000);
        binding.spinner.setAdapter(adapter_area);

        // set spinner open/close listener
        binding.spinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(Spinner spinner) {
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
