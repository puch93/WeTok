package kr.co.core.wetok.fragment.pay.account;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.lang.reflect.Field;

import kr.co.core.wetok.R;
import kr.co.core.wetok.adapter.AccountSpinnerAdapter;
import kr.co.core.wetok.databinding.FragmentNormalAccountBinding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.util.FragmentResultListener;

public class NormalAccountFrag extends BaseFrag implements View.OnClickListener {
    private FragmentNormalAccountBinding binding;
    private AppCompatActivity act;
    private String[] default_banks = {"선택"};
    private String[] korea_banks = {"선택", "부산은행", "신한은행", "국민은행"};
    private String[] country_codes = {"선택", "한국", "중국"};
    private String[] china_banks = {"선택", "족팡매야", "니취팔러마"};
    private int[] country_flags = {0, R.drawable.korea_flag, R.drawable.china_flag};

    FragmentResultListener resultListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_normal_account, container, false);
        act = (AppCompatActivity) getActivity();

        setSpinner();

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof FragmentResultListener) {
            resultListener = (FragmentResultListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTimePickerSetListener");
        }
    }

    private void setSpinner() {
        // set bank spinner
        AccountSpinnerAdapter adapter_bank = new AccountSpinnerAdapter(act, korea_banks, null, R.layout.item_bank);
        spinner_height_set(binding.spinnerBank, 1000);
        adapter_bank.setDropDownViewResource(R.layout.item_bank);
        binding.spinnerBank.setAdapter(adapter_bank);


        // set country spinner
        AccountSpinnerAdapter adapter_country = new AccountSpinnerAdapter(act, country_codes, country_flags, R.layout.item_country);
        spinner_height_set(binding.spinnerCountry, 1000);
        adapter_country.setDropDownViewResource(R.layout.item_country);
        binding.spinnerCountry.setAdapter(adapter_country);

        binding.spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (country_codes[position]) {
                    case "선택":
                        adapter_bank.setRefresh(default_banks, null, R.layout.item_bank);
                        break;

                    case "한국":
                        adapter_bank.setRefresh(korea_banks, null, R.layout.item_bank);
                        break;

                    case "중국":
                        adapter_bank.setRefresh(china_banks, null, R.layout.item_bank);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
    }
}