package kr.co.core.wetok.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.fragment.pay.account.NormalAccountFrag;
import kr.co.core.wetok.fragment.pay.account.WeChatAccountFrag;

public class AccountSettingPagerAdapter extends FragmentStatePagerAdapter {

    public AccountSettingPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment currentFragment = null;
        switch (i) {
            case 0:
                currentFragment = new NormalAccountFrag();
                break;
            case 1:
                currentFragment = new WeChatAccountFrag();
                break;
        }
        return currentFragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return (BaseFrag) super.instantiateItem(container, position);
    }
}
