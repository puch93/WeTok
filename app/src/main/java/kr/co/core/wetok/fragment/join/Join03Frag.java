package kr.co.core.wetok.fragment.join;



import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.lang.reflect.Field;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.JoinAct;
import kr.co.core.wetok.adapter.WriteSpinnerAdapter;
import kr.co.core.wetok.databinding.FragmentJoin02Binding;
import kr.co.core.wetok.databinding.FragmentJoin03Binding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomSpinner;
import kr.co.core.wetok.util.StringUtil;

public class Join03Frag extends BaseFrag implements View.OnClickListener {
    private FragmentJoin03Binding binding;
    private AppCompatActivity act;

    private String[] birth_codes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_03, container, false);
        act = (AppCompatActivity) getActivity();

        birth_codes = getResources().getStringArray(R.array.birth_code);

        binding.tvConfirm.setOnClickListener(this);
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

        setSpinner();

        return binding.getRoot();
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

                // 현재 focus 되어있는 view 가 있으면 키보드를 내리고, focus 제거
                View view = act.getCurrentFocus();
                if(view != null) {
                    InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    view.clearFocus();
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

    private void checkButtonActivation() {
        if(!StringUtil.isNull(binding.etName.getText().toString())) {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_enable_191022);
        } else {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_disable_191022);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tv_confirm) {
            if(StringUtil.isNull(binding.etName.getText().toString())) {
                Common.showToast(act, "이름을 입력해주세요");
                return;
            }

            ((JoinAct) act).replaceFragment(new Join04Frag());
        }
    }
}