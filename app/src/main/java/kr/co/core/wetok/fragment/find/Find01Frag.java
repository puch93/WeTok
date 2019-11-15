package kr.co.core.wetok.fragment.find;


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

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.FindPwAct;
import kr.co.core.wetok.adapter.WriteSpinnerAdapter;
import kr.co.core.wetok.databinding.FragmentFind01Binding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomSpinner;

public class Find01Frag extends BaseFrag implements View.OnClickListener {
    private FragmentFind01Binding binding;
    private AppCompatActivity act;

    private String[] country_codes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_01, container, false);
        act = (AppCompatActivity) getActivity();

        country_codes = getResources().getStringArray(R.array.country_code);

        binding.tvNext.setOnClickListener(this);

        binding.etId.addTextChangedListener(textWatcher);
        binding.etNumber.addTextChangedListener(textWatcher);

        setSpinner();

        return binding.getRoot();
    }

    private void setCheckPw() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {
                            ((FindPwAct) act).replaceFragment(new Find02Frag());
                        } else {
                            Common.showToast(act, getString(R.string.find_check_pw_warning));
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

        String hp = binding.spinner.getSelectedItem().toString() + binding.etNumber;

        server.setTag("Check Password");
        server.addParams("dbControl", NetUrls.CHECK_FIND_PW);
        server.addParams("m_id", binding.etId.getText().toString());
        server.addParams("m_hp", hp);
        server.execute(true, false);
    }


    private TextWatcher textWatcher = new TextWatcher() {
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
    };

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
        if (binding.etId.length() != 0 &&
                        binding.etNumber.length() != 0
        ) {
            binding.tvNext.setBackgroundResource(R.drawable.wt_btn360_enable_191022);
        } else {
            binding.tvNext.setBackgroundResource(R.drawable.wt_btn360_disable_191022);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_next) {
            if (binding.etId.length() == 0) {
                Common.showToast(act, getString(R.string.find_name_warning));
                return;
            }

            if (binding.etNumber.length() == 0) {
                Common.showToast(act, getString(R.string.find_number_warning));
                return;
            }

//            setCheckPw();
            ((FindPwAct) act).replaceFragment(new Find02Frag());
        }
    }
}