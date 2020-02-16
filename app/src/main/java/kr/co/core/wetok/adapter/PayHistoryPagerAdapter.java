package kr.co.core.wetok.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.fragment.pay.PayHistoryFrag;
import kr.co.core.wetok.fragment.pay.account.NormalAccountFrag;
import kr.co.core.wetok.fragment.pay.account.WeChatAccountFrag;

public class PayHistoryPagerAdapter extends FragmentStatePagerAdapter {

    public PayHistoryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment currentFragment = new PayHistoryFrag();
        return currentFragment;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return (BaseFrag) super.instantiateItem(container, position);
    }
}
