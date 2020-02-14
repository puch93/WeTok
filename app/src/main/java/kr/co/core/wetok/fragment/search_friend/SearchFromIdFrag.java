package kr.co.core.wetok.fragment.search_friend;


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
import kr.co.core.wetok.databinding.FragmentSearchFromIdBinding;
import kr.co.core.wetok.fragment.BaseFrag;

public class SearchFromIdFrag extends BaseFrag implements View.OnClickListener {
    private FragmentSearchFromIdBinding binding;
    private AppCompatActivity act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_from_id, container, false);
        act = (AppCompatActivity) getActivity();

        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
    }
}