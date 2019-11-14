package kr.co.core.wetok.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityFindPwBinding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.fragment.find.Find01Frag;

public class FindPwAct extends AppCompatActivity {
    ActivityFindPwBinding binding;
    public Activity act;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_pw, null);
        act = this;

        setActionBar();

        replaceFragment(new Find01Frag());
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

    public void replaceFragment(BaseFrag frag) {
        /* replace fragment */
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!(frag instanceof Find01Frag)) {
            transaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
        }
        transaction.replace(R.id.ll_replacements, frag);
        transaction.commit();
    }
}
