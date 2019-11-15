package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityJoinBinding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.fragment.join.Join01Frag;
import kr.co.core.wetok.fragment.join.Join02Frag;
import kr.co.core.wetok.fragment.join.Join03Frag;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class JoinAct extends BaseAct {
    ActivityJoinBinding binding;
    public Activity act;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join, null);
        act = this;

        setActionBar();

        replaceFragment(new Join01Frag());
    }


    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }

    public void replaceFragment(BaseFrag frag) {
        /* replace layout */
        if(frag instanceof Join01Frag) {
            binding.tvTitle.setText(getString(R.string.act_join_process01));
            replaceLayout(R.id.ll_process01);
        } else if(frag instanceof Join02Frag) {
            binding.tvTitle.setText(getString(R.string.act_join_process02));
            replaceLayout(R.id.ll_process02);
        } else if(frag instanceof Join03Frag) {
            binding.tvTitle.setText(getString(R.string.act_join_process03));
            replaceLayout(R.id.ll_process03);
        } else {
            binding.tvTitle.setText(getString(R.string.act_join_process04));
            replaceLayout(R.id.ll_process04);
        }

        /* replace fragment */
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(!(frag instanceof Join01Frag)) {
            transaction.setCustomAnimations(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left);
        }
        transaction.replace(R.id.ll_replacements, frag);
        transaction.commit();
    }

    private void replaceLayout(int id) {
        binding.llProcess01.setSelected(false);
        binding.llProcess02.setSelected(false);
        binding.llProcess03.setSelected(false);
        binding.llProcess04.setSelected(false);

        (findViewById(id)).setSelected(true);
    }
}
