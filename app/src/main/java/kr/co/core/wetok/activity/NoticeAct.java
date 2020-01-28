package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.core.wetok.R;
import kr.co.core.wetok.adapter.notice.NoticeAdapter;
import kr.co.core.wetok.data.NoticeChildData;
import kr.co.core.wetok.data.NoticeParentData;
import kr.co.core.wetok.databinding.ActivityNoticeBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;

public class NoticeAct extends AppCompatActivity {
    ActivityNoticeBinding binding;
    Activity act;

    ActionBar actionBar;

    NoticeAdapter adapter;
    List<NoticeParentData> list = new ArrayList<>();
    private LinearLayoutManager manager;
    private boolean isScroll = true;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notice, null);
        act = this;

        setActionBar();

        setRecyclerView();

        getNoticeList(page);
    }

    private void setRecyclerView() {
        manager = new LinearLayoutManager(act);

        RecyclerView.ItemAnimator animator = binding.rcvNotice.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        binding.rcvNotice.setLayoutManager(manager);
        binding.rcvNotice.setItemViewCacheSize(20);
        binding.rcvNotice.setHasFixedSize(true);

        binding.rcvNotice.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalCount = manager.getItemCount();
                int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
                if (!isScroll) {
                    if (totalCount - 1 == lastItemPosition) {
                        isScroll = true;
                        ++page;
                        getNoticeList(page);
                    }
                }
            }
        });
    }

    private void getNoticeList(int page) {
        isScroll = true;

        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        final String result = jo.getString("result");
                        final String message = jo.getString("message");

                        if(result.equalsIgnoreCase("Y")) {
                            if(page == 1) {
                                list.clear();
                            }

                            JSONArray ja = jo.getJSONArray("data");
                            for (int i = 0; i < ja.length(); i++) {
                                JSONObject job = ja.getJSONObject(i);

                                // get data
                                String title = job.getString("b_title");
                                String regDate = job.getString("b_regdate");

                                SimpleDateFormat orgin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    Date old = orgin.parse(regDate);
                                    regDate = sdf.format(old);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                String idx = job.getString("b_idx");
                                String contents = job.getString("b_contents");


                                // set child data
                                List<NoticeChildData> childDataList = new ArrayList<>();
                                NoticeChildData sub = new NoticeChildData(contents);
                                childDataList.add(sub);

                                // set parent data
                                NoticeParentData data = new NoticeParentData(title, regDate, false, childDataList);
                                data.setIdx(idx);

                                list.add(data);
                            }

                            /* last item => 넣어줘야 마지막 애니메이션 적용됨 */
                            List<NoticeChildData> lastChildList = new ArrayList<>();
                            NoticeChildData lastItem_sub = new NoticeChildData("");
                            lastChildList.add(lastItem_sub);

                            NoticeParentData lastItem = new NoticeParentData("", "", false, lastChildList);
                            list.add(lastItem);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    isScroll = false;

                                    adapter = new NoticeAdapter(list);
                                    binding.rcvNotice.setAdapter(adapter);
                                }
                            });
                        } else {
                            isScroll = false;
                            if(page == 1)
                                Common.showToast(act, message);
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

        server.setTag("Notice List");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.GET_NOTICE);
        server.addParams("pagenum", String.valueOf(page));
        server.execute(true, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }
}
