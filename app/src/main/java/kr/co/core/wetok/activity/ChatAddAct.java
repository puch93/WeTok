package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.core.wetok.R;
import kr.co.core.wetok.adapter.ChattingAddAdapter;
import kr.co.core.wetok.data.CheckUserData;
import kr.co.core.wetok.databinding.ActivityChatAddBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class ChatAddAct extends AppCompatActivity {
    ActivityChatAddBinding binding;
    Activity act;
    ActionBar actionBar;

    private ArrayList<CheckUserData> list = new ArrayList<>();
    private LinearLayoutManager manager;
    private ChattingAddAdapter adapter;
    private boolean isScroll = true;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_add, null);
        act = this;

        setActionBar();

        setRecyclerView();

        getFriendList(page);

        binding.tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<CheckUserData> inviteList = adapter.getAllData();

                ArrayList<String> selectedList = new ArrayList<>();
                for (int i = 0; i < inviteList.size(); i++) {
                    CheckUserData data = inviteList.get(i);
                    if (data.isChecked()) {
                        selectedList.add(data.getIdx());
                    }
                }

                if (selectedList.size() < 1) {
                    Common.showToast(act, "초대인원을 1명이상 선택해주세요.");
                } else {
                    String idxs = "";
                    for (int i = 0; i < selectedList.size(); i++) {
                        idxs += selectedList.get(i);
                        if (i != selectedList.size() - 1) {
                            idxs += ",";
                        }
                    }

                    Intent intent = new Intent();
                    intent.putExtra("idxs", idxs);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
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
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setRecyclerView() {
        manager = new LinearLayoutManager(act);
        binding.rcvInvite.setLayoutManager(manager);
        binding.rcvInvite.setHasFixedSize(true);
        binding.rcvInvite.setItemViewCacheSize(20);
        adapter = new ChattingAddAdapter(act, list);
        binding.rcvInvite.setAdapter(adapter);

        binding.rcvInvite.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void getFriendList(int page) {
        isScroll = true;

        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            if (page == 1) {
                                list.clear();
                            }

                            JSONArray ja = jo.getJSONArray("value");

                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject job = ja.getJSONObject(i);
                                Log.e(StringUtil.TAG, "job(" + i + "): " + job);

                                CheckUserData data = new CheckUserData();
                                data.setIdx(job.getString("m_idx"));
                                data.setId(job.getString("m_id"));
                                data.setPw(job.getString("m_pass"));
                                data.setHp(job.getString("m_hp"));
                                data.setIntro(job.getString("m_intro"));
                                data.setName(job.getString("m_nickname"));

                                data.setProfile_img(job.getString("m_profile"));
                                data.setBackground_img(job.getString("m_background"));

                                data.setChecked(false);

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
                            if (page == 1) {
                                list.clear();
                                adapter.setList(list);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } catch (JSONException e) {
                        isScroll = false;
                        if (page == 1) {
                            list.clear();
                            adapter.setList(list);
                            adapter.notifyDataSetChanged();
                        }
                        e.printStackTrace();
                        Common.showToastNetwork(act);
                    }
                } else {
                    isScroll = false;
                    if (page == 1) {
                        list.clear();
                        adapter.setList(list);
                        adapter.notifyDataSetChanged();
                    }

                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("Friend List (Chat Add)");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "getFriendList");
        server.addParams("pagenum", String.valueOf(page));
        server.execute(true, false);
    }
}
