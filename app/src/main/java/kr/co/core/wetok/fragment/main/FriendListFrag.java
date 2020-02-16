package kr.co.core.wetok.fragment.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.AddFriendAct;
import kr.co.core.wetok.activity.MainAct;
import kr.co.core.wetok.adapter.FriendListAdapter;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.FragmentFriendListBinding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomApplication;
import kr.co.core.wetok.util.StringUtil;

public class FriendListFrag extends BaseFrag implements View.OnClickListener, MainAct.onKeyBackPressedListener {
    FragmentFriendListBinding binding;
    private AppCompatActivity act;
    private ActionBar actionBar;

    /* recycler view */
    private FriendListAdapter adapter;
    private ArrayList<UserData> list = new ArrayList<>();
    private ArrayList<UserData> list_search = new ArrayList<>();
    private LinearLayoutManager manager;
    private boolean isScroll = true;
    private int page = 1;

    private static final int ADD_FRIEND = 1001;
    private static final int FROM_PROFILE = 1002;

    UserData myInfoFromDB;
    Realm realm = null;

    private String id;
    private String pw;
    private String hp;
    private String intro;
    private String name;
    private String birth;

    private String profile_img;
    private String background_img;

    private boolean isSearch = false;
    private CustomApplication application;
    private InputMethodManager imm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(StringUtil.TAG, "onCreateView: FriendListFrag");
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_friend_list, container, false);
        act = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

        setActionBar(false);

        setRecyclerView();

        // set back pressed listener
        ((MainAct) act).setOnKeyBackPressedListener(this);

        /* set search edit text */
        imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        binding.etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
                    String cmp = binding.etSearch.getText().toString();

                    if(!StringUtil.isNull(cmp)) {
                        list_search.clear();
                        for (int i = 0; i < list.size(); i++) {
                            if ((!StringUtil.isNull(list.get(i).getName()) && list.get(i).getName().contains(cmp)) ||
                                    (!StringUtil.isNull(list.get(i).getIntro()) && list.get(i).getIntro().contains(cmp))) {
                                list_search.add(list.get(i));
                            }
                        }

                        adapter.setList(list_search);
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter.setList(list);
                        adapter.notifyDataSetChanged();
                    }

                    return true;
                }
                return false;
            }
        });

        /* set realm and get my info */
        application = (CustomApplication) act.getApplication();
        realm = application.getRealmObject();
        checkMyInfo(false);

        /* set friend count */
        if(!StringUtil.isNull(application.getFriend_count())) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.tvFriendCount.setText(application.getFriend_count());
                }
            });
        }

        return binding.getRoot();
    }

    private void checkMyInfo(boolean fromProfile) {
        myInfoFromDB = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
        if (null != myInfoFromDB) {
            setMyInfo(fromProfile);
        } else {
            getMyInfo(fromProfile);
        }
    }

    private void setMyInfo(boolean fromProfile) {
        if (!fromProfile) {
            page = 1;
            getFriendList(page);
        } else {
            list.set(0, myInfoFromDB);

            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.setList(list);
                    adapter.notifyItemChanged(0);
                }
            });
        }
    }

    private void getMyInfo(boolean fromProfile) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (resultData.getResult() != null) {
                            id = jo.getString("m_id");
                            pw = jo.getString("m_pass");
                            hp = jo.getString("m_hp");
                            intro = jo.getString("m_intro");
                            name = jo.getString("m_nickname");
                            birth = jo.getString("m_birthday");
                            profile_img = jo.getString("m_profile");
                            background_img = jo.getString("m_background");

                            writeDB(fromProfile);

                        } else {
                            Common.showToastNetwork(act);
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

        server.setTag("My Info");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.GET_MY_INFO);
        server.execute(true, false);
    }

    private void getFriendList(int page) {
        isScroll = true;

        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            if(page == 1) {
                                // set all count
                                String total = jo.getString("total");
                                if(StringUtil.isNull(application.getFriend_count())) {
                                    application.setFriend_count(total);
                                    act.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.tvFriendCount.setText(total);
                                        }
                                    });
                                }

                                list.clear();
                                list.add(myInfoFromDB);
                            }

                            JSONArray ja = jo.getJSONArray("value");

                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject job = ja.getJSONObject(i);
                                Log.e(StringUtil.TAG, "job(" + i + "): " + job);

                                UserData data = new UserData();
                                data.setIdx(job.getString("m_idx"));
                                data.setId(job.getString("m_id"));
                                data.setPw(job.getString("m_pass"));
                                data.setHp(job.getString("m_hp"));
                                data.setIntro(job.getString("m_intro"));
                                data.setName(job.getString("m_nickname"));

                                data.setProfile_img(job.getString("m_profile"));
                                data.setBackground_img(job.getString("m_background"));

                                list.add(data);
                            }

                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isScroll = false;

                                    adapter.setList(list);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            if(page == 1) {
                                binding.tvFriendCount.setText("0");

                                list.clear();
                                list.add(myInfoFromDB);

                                adapter.setList(list);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        isScroll = false;
                        if(page == 1) {
                            binding.tvFriendCount.setText("0");

                            list.clear();
                            list.add(myInfoFromDB);

                            adapter.setList(list);
                            adapter.notifyDataSetChanged();
                        }

                        e.printStackTrace();
                        Common.showToastNetwork(act);
                    }
                } else {
                    isScroll = false;
                    if(page == 1) {
                        binding.tvFriendCount.setText("0");

                        list.clear();
                        list.add(myInfoFromDB);

                        adapter.setList(list);
                        adapter.notifyDataSetChanged();
                    }

                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("Friend List");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "getFriendList");
        server.addParams("pagenum", String.valueOf(page));
        server.execute(true, false);
    }

    private void writeDB(boolean fromProfile) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                UserData data = realm.createObject(UserData.class, "0");
                data.setData(UserPref.getMidx(act), id, pw, hp, intro, name, birth, profile_img, background_img);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myInfoFromDB = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
                        setMyInfo(fromProfile);
                    }
                });
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e(StringUtil.TAG, "onError: " + error.getMessage());
                updateDB(fromProfile);
            }
        });
    }

    private void updateDB(boolean fromProfile) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                UserData data = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
                if (data != null) {
                    data.setData(UserPref.getMidx(act), id, pw, hp, intro, name, birth, profile_img, background_img);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myInfoFromDB = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
                        setMyInfo(fromProfile);
                    }
                });
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e(StringUtil.TAG, "updateDB onError: " + error.getMessage());
            }
        });
    }

    private void setRecyclerView() {
        manager = new LinearLayoutManager(act);
        binding.rcvFriend.setLayoutManager(manager);
        binding.rcvFriend.setHasFixedSize(true);
        binding.rcvFriend.setItemViewCacheSize(20);
        adapter = new FriendListAdapter(act, list, this);
        binding.rcvFriend.setAdapter(adapter);

        binding.rcvFriend.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalCount = manager.getItemCount();
                int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
                if (!isScroll) {
                    if (totalCount - 1 == lastItemPosition) {
                        isScroll = true;
                        ++page;
                        getFriendList(page);
                    }
                }
            }
        });
    }

    private void setActionBar(boolean search) {
        if(!search) {
            setActionBarDefault();
        } else {
            setActionBarSearch();
        }
    }

    private void setActionBarDefault() {
        binding.toolbarDefault.setVisibility(View.VISIBLE);
        binding.toolbarSearch.setVisibility(View.GONE);

        act.setSupportActionBar(binding.toolbarDefault);
        actionBar = act.getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    private void setActionBarSearch() {
        binding.toolbarDefault.setVisibility(View.GONE);
        binding.toolbarSearch.setVisibility(View.VISIBLE);

        binding.etSearch.setFocusableInTouchMode(true);
        binding.etSearch.requestFocus();
        imm.showSoftInput(binding.etSearch, 0);

        act.setSupportActionBar(binding.toolbarSearch);
        actionBar = act.getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if(!isSearch) {
            inflater.inflate(R.menu.appbar_actionbar, menu);
        } else {
            inflater.inflate(R.menu.appbar_actionbar_search, menu);
        }
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ADD_FRIEND:
                    application.setFriend_count(null);
                    checkMyInfo(false);
                    break;

                case FROM_PROFILE:
                    Log.e(StringUtil.TAG, "from profile in");
                    checkMyInfo(true);
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                if(!isSearch) {
                    isSearch = true;
                    isScroll = true;
                    setActionBar(true);
                } else {
                    imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
                    String cmp = binding.etSearch.getText().toString();

                    if(!StringUtil.isNull(cmp)) {
                        list_search.clear();
                        for (int i = 0; i < list.size(); i++) {
                            if ((!StringUtil.isNull(list.get(i).getName()) && list.get(i).getName().contains(cmp)) ||
                                    (!StringUtil.isNull(list.get(i).getIntro()) && list.get(i).getIntro().contains(cmp))) {
                                list_search.add(list.get(i));
                            }
                        }

                        adapter.setList(list_search);
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter.setList(list);
                        adapter.notifyDataSetChanged();
                    }
                }
                return true;

            case R.id.action_add:
                startActivityForResult(new Intent(act, AddFriendAct.class), ADD_FRIEND);
                return true;

            case android.R.id.home:
                binding.etSearch.setText(null);
                imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);

                isSearch = false;
                isScroll = false;
                setActionBar(false);

                adapter.setList(list);
                adapter.notifyDataSetChanged();
                break;

            case R.id.action_init:
                binding.etSearch.setText(null);
                binding.etSearch.setFocusableInTouchMode(true);
                binding.etSearch.requestFocus();
                imm.showSoftInput(binding.etSearch, 0);

                adapter.setList(list);
                adapter.notifyDataSetChanged();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBack() {
        if(isSearch) {
            binding.etSearch.setText(null);
            imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);

            isSearch = false;
            setActionBar(false);

            adapter.setList(list);
            adapter.notifyDataSetChanged();
        } else {
            ((MainAct) act).setOnKeyBackPressedListener(null);
            act.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isSearch) {
            binding.etSearch.setText(null);
            imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);

            isSearch = false;
            setActionBar(false);

            adapter.setList(list);
            adapter.notifyDataSetChanged();
        }
    }
}