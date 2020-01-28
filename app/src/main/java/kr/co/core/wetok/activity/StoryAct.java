package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import kr.co.core.wetok.R;
import kr.co.core.wetok.adapter.FriendListAdapter;
import kr.co.core.wetok.adapter.StoryAdapter;
import kr.co.core.wetok.data.StoryData;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.ActivityStoryBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomApplication;
import kr.co.core.wetok.util.ItemOffsetDecoration;
import kr.co.core.wetok.util.StringUtil;

public class StoryAct extends AppCompatActivity {
    ActivityStoryBinding binding;
    Activity act;
    ActionBar actionBar;

    /* recycler view */
    StoryAdapter adapter;
    ArrayList<StoryData> list = new ArrayList<>();
    private LinearLayoutManager manager;
    private boolean isScroll = true;
    private int page = 1;

    UserData user = new UserData();

    private static final int TYPE_REGISTER = 1001;

    boolean isMe = false;

    UserData myInfoFromDB;
    Realm realm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(StringUtil.TAG, "onCreate in");

        binding = DataBindingUtil.setContentView(this, R.layout.activity_story, null);
        act = this;

        user = (UserData) getIntent().getSerializableExtra("user");

        // 내스토리일 경우에만 글쓰기 버튼 활성화
        if (null == user) {
            isMe = true;

            /* set realm and get my info */
            CustomApplication application = (CustomApplication) act.getApplication();
            realm = application.getRealmObject();

            user = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
        }

        // set data
        binding.tvTitleName.setText(user.getName());

        setActionBar();

        setRecyclerView();

        getStoryList(page);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(StringUtil.TAG, "onResume in");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(StringUtil.TAG, "onStart in");
    }

    private void getStoryList(int page) {
        isScroll = true;

        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        final String result = jo.getString("result");
                        final String message = jo.getString("message");

                        if (result.equalsIgnoreCase("Y")) {
                            if(page == 1) {
                                list.clear();
                            }

                            JSONArray ja = jo.getJSONArray("value");
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject job = ja.getJSONObject(i);

                                String idx = job.getString("s_idx");
                                String text = job.getString("s_contents");
                                String image = job.getString("s_img");
                                String regDate = job.getString("s_regdate");

                                SimpleDateFormat orgin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.act_story_time));
                                try {
                                    Date old = orgin.parse(regDate);
                                    regDate = sdf.format(old);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                list.add(new StoryData(idx, text, image, regDate));
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isScroll = false;
                                    adapter.setList(list);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            isScroll = false;

                            if(page == 1) {
                                list = new ArrayList<>();
                                adapter.setList(list);
                                adapter.notifyDataSetChanged();
                                Common.showToast(act, message);
                            }
                        }

                    } catch (JSONException e) {
                        isScroll = false;
                        e.printStackTrace();
                        Common.showToastNetwork(act);
                    }
                } else {
                    isScroll = false;
                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("Story List");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.GET_STORY_LIST);
        server.addParams("idx", user.getIdx());
        server.addParams("pagenum", String.valueOf(page));
        server.execute(true, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TYPE_REGISTER:
                    page = 1;
                    getStoryList(page);
                    break;
            }
        }
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(StringUtil.TAG, "onCreateOptionsMenu in");

        if (isMe) {
            getMenuInflater().inflate(R.menu.appbar_actionbar_story, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_register:
                startActivityForResult(new Intent(act, StoryRegisterAct.class), TYPE_REGISTER);
                return true;

            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setRecyclerView() {
        manager = new LinearLayoutManager(act);
        binding.rcvStory.setLayoutManager(manager);
        binding.rcvStory.setHasFixedSize(true);
        binding.rcvStory.setItemViewCacheSize(20);
        adapter = new StoryAdapter(act, user, isMe, list, new StoryAdapter.AfterDelete() {
            @Override
            public void afterDelete() {
                Log.e(StringUtil.TAG, "afterDelete in");
                page = 1;
                getStoryList(page);
            }
        });
        binding.rcvStory.setAdapter(adapter);
        binding.rcvStory.addItemDecoration(new ItemOffsetDecoration(act));

        binding.rcvStory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalCount = manager.getItemCount();
                int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
                if (!isScroll) {
                    if (totalCount - 1 == lastItemPosition) {
                        isScroll = true;
                        ++page;
                        getStoryList(page);
                    }
                }
            }
        });
    }
}
