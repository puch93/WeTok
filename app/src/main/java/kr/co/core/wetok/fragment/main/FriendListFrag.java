package kr.co.core.wetok.fragment.main;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.AddFriendAct;
import kr.co.core.wetok.databinding.FragmentFriendListBinding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;

public class FriendListFrag extends BaseFrag implements View.OnClickListener {
    FragmentFriendListBinding binding;
    private AppCompatActivity act;

    private ActionBar actionBar;

    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_list, container, false);
        act = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

        setActionBar();
        getFriendList();
        return binding.getRoot();
    }

    private void setActionBar() {
        act.setSupportActionBar(binding.toolbar);
        actionBar = act.getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    private void getFriendList() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {

                        } else {

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

        server.setTag("Friend List");
        server.addParams("dbControl", "getFriendList");
        server.addParams("m_idx", UserPref.getMidx(act));
        server.execute(true, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.appbar_actionbar, menu);

        /* 0 */
//        searchView = (SearchView) menu.findItem(R.id.action_search)
//                .getActionView();
//
//        searchView.setOnQueryTextListener(onQueryTextListener);
//        searchView.setMaxWidth(Integer.MAX_VALUE);

        /* 1 */
//        MenuItem mSearch = menu.findItem(R.id.action_search);
//        SearchView mSearchView = (SearchView) mSearch.getActionView();
//        mSearchView.setQueryHint("Search");
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                Common.showToast(act, "onQueryTextChange");
//                return true;
//            }
//        });

        /* 2 */
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        searchView.setMaxWidth(Integer.MAX_VALUE);
//        searchView.setQueryHint("검색어를 입력해주세요");
//
//
//        searchView.setOnQueryTextListener(queryTextListener);
//
//        SearchManager searchManager = (SearchManager) act.getSystemService(Context.SEARCH_SERVICE);
//        if(null != searchManager) {
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(act.getComponentName()));
//        }
//        searchView.setIconifiedByDefault(true);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            Common.showToast(act, "onQueryTextSubmit");
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Common.showToast(act, "onQueryTextChange");
            return false;
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                Common.showToast(act, "action_search menu01");
                return true;

            case R.id.action_add:
                startActivity(new Intent(act, AddFriendAct.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}