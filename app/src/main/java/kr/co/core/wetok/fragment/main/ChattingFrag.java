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
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.AddFriendAct;
import kr.co.core.wetok.activity.ChatAct;
import kr.co.core.wetok.activity.ChatAddAct;
import kr.co.core.wetok.activity.MainAct;
import kr.co.core.wetok.adapter.ChattingListAdapter;
import kr.co.core.wetok.data.ChattingListData;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.FragmentChattingBinding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomApplication;
import kr.co.core.wetok.util.StringUtil;

public class ChattingFrag extends BaseFrag implements MainAct.onKeyBackPressedListener {
    FragmentChattingBinding binding;
    private AppCompatActivity act;

    private ActionBar actionBar;

    private ArrayList<ChattingListData> list = new ArrayList<>();
    private ArrayList<ChattingListData> list_search = new ArrayList<>();
    private ChattingListAdapter adapter;
    private LinearLayoutManager manager;

    private boolean isSearch = false;
    private CustomApplication application;
    private InputMethodManager imm;

    private static final int TYPE_CHAT_ADD = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(StringUtil.TAG, "onCreateView: ChattingFrag");
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chatting, container, false);
        act = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

        setActionBar(false);

        setRecyclerView();

        getChatList();

        // set back pressed listener
        ((MainAct) act).setOnKeyBackPressedListener(this);

        /* set search edit text */
        imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        binding.etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
                    String cmp = binding.etSearch.getText().toString();

                    if (!StringUtil.isNull(cmp)) {
                        list_search.clear();
                        for (int i = 0; i < list.size(); i++) {
                            String names = list.get(i).getUserNames();

                            if ((!StringUtil.isNull(names) && names.contains(cmp)) ||
                                    (!StringUtil.isNull(list.get(i).getText()) && list.get(i).getText().contains(cmp))) {
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

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            getChatList();

            ((MainAct) MainAct.act).getReadCount();
        }
    }

    private void setRecyclerView() {
        manager = new LinearLayoutManager(act);
        binding.rcvChatting.setLayoutManager(manager);
        binding.rcvChatting.setHasFixedSize(true);
        binding.rcvChatting.setItemViewCacheSize(20);
        adapter = new ChattingListAdapter(act, list);
        binding.rcvChatting.setAdapter(adapter);
    }

    public void getChatList() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        final String result = jo.getString("result");
                        final String message = jo.getString("message");

                        if (result.equalsIgnoreCase("Y")) {
                            list.clear();

                            JSONArray ja = jo.getJSONArray("data");
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject job = ja.getJSONObject(i);
                                Log.e(StringUtil.TAG, "chat list data(" + i + "): " + job);

                                // idx
                                String idx = job.getString("c_idx");

                                // room idx
                                String roomIdx = job.getString("c_room_idx");

                                // read count
                                String readCount = job.getString("sum");

                                // contents
                                String contents;
                                String type = job.getString("c_msg_type");
                                if (type.equalsIgnoreCase("photo")) {
                                    contents = "사진";
                                } else if (type.equalsIgnoreCase("movie")) {
                                    contents = "영상";
                                } else if (type.equalsIgnoreCase("file")) {
                                    contents = "파일";
                                } else if (type.equalsIgnoreCase("mic")) {
                                    contents = "음성";
                                } else {
                                    contents = job.getString("c_msg");
                                }

                                // reg date
                                String regDate = job.getString("c_regdate");
                                try {
                                    SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
                                    Date date1 = dateFormat1.parse(regDate);

                                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("a hh:mm", java.util.Locale.getDefault());
                                    regDate = dateFormat2.format(date1);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                                // set other profile data
                                JSONArray userRow = job.getJSONArray("userRow");
                                ArrayList<UserData> userArray = new ArrayList<>();
                                String names = "";

                                for (int j = 0; j < userRow.length(); j++) {
                                    JSONObject userObject = userRow.getJSONObject(j);

                                    UserData otherData = new UserData();
                                    otherData.setIdx(userObject.getString("m_idx"));
                                    otherData.setName(userObject.getString("m_nickname"));
//                                    otherData.setHp(userObject.getString("m_hp"));
//                                    otherData.setId(userObject.getString("m_id"));
//                                    otherData.setBirth(userObject.getString("m_birthday"));
//                                    otherData.setIntro(userObject.getString("m_intro"));
                                    otherData.setProfile_img(userObject.getString("m_profile"));
//                                    otherData.setBackground_img(userObject.getString("m_background"));
                                    userArray.add(otherData);

                                    if (j != userRow.length() - 1) {
                                        names += userObject.getString("m_nickname") + ",";
                                    } else {
                                        names += userObject.getString("m_nickname");
                                    }
                                }


                                // all data set
                                ChattingListData data = new ChattingListData(idx, roomIdx, regDate, readCount, contents, userArray, names);

                                list.add(data);

                                act.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.setList(list);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } else {
                            Common.showToast(act, message);
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

        server.setTag("Chat List");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.GET_CHAT_LIST);
        server.execute(true, false);
    }

    private void setActionBar(boolean search) {
        if (!search) {
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
        if (!isSearch) {
            inflater.inflate(R.menu.appbar_actionbar_chat, menu);
        } else {
            inflater.inflate(R.menu.appbar_actionbar_search, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void createRoomMulti(final String idxs) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            String room_idx = jo.getString("room_idx");
                            if (!room_idx.contains("R")) {
                                room_idx = "R" + room_idx;
                            }

                            Intent intent = new Intent(act, ChatAct.class);
                            intent.putExtra("roomIdx", room_idx);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            String message = jo.getString("message");
                            Common.showToast(act, message);
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

        server.setTag("Create Room Multi");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "setNewRoomCreation");
        server.addParams("user_idx", UserPref.getMidx(act));
        server.addParams("guest_idx", idxs);
        server.addParams("m_os", "android");
        server.execute(true, false);
    }

    private void createRoomSingle(final String idx) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        final String result = jo.getString("result");

                        if (result.equalsIgnoreCase("Y")) {
                            String room_idx = jo.getString("room_idx");
                            if (!room_idx.contains("R")) {
                                room_idx = "R" + room_idx;
                            }

                            Intent intent = new Intent(act, ChatAct.class);
                            intent.putExtra("roomIdx", room_idx);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            String message = jo.getString("message");
                            Common.showToast(act, message);
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

        server.setTag("Create Room Single");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "getChatRoomNumber");
        server.addParams("user_idx", UserPref.getMidx(act));
        server.addParams("guest_idx_ar", idx);
        server.addParams("multi_is", "N");
        server.addParams("m_os", "android");
        server.execute(true, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TYPE_CHAT_ADD:
                    String idx = data.getStringExtra("idxs");
                    if (idx.contains(",")) {
                        createRoomMulti(idx);
                    } else {
                        createRoomSingle(idx);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                if (!isSearch) {
                    isSearch = true;
                    setActionBar(true);
                } else {
                    imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
                    String cmp = binding.etSearch.getText().toString();

                    if (!StringUtil.isNull(cmp)) {
                        list_search.clear();
                        for (int i = 0; i < list.size(); i++) {
                            String names = list.get(i).getUserNames();

                            if ((!StringUtil.isNull(names) && names.contains(cmp)) ||
                                    (!StringUtil.isNull(list.get(i).getText()) && list.get(i).getText().contains(cmp))) {
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

            case R.id.action_add_friend:
                startActivity(new Intent(act, AddFriendAct.class));
                return true;

            case R.id.action_add_chat:
                startActivityForResult(new Intent(act, ChatAddAct.class), TYPE_CHAT_ADD);
                return true;

            case android.R.id.home:
                binding.etSearch.setText(null);
                imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);

                isSearch = false;
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
    public void onBack() {
        if (isSearch) {
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
        if (isSearch) {
            binding.etSearch.setText(null);
            imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);

            isSearch = false;
            setActionBar(false);

            adapter.setList(list);
            adapter.notifyDataSetChanged();
        }
    }
}