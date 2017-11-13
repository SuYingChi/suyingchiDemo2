package com.ihs.inputmethod.uimodules.softgame;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSJsonUtil;
import com.ihs.inputmethod.uimodules.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoftGameItemFragment extends Fragment {

    public static final int SOFT_GAME_LOAD_COUNT = 50;
    public static final String JSON_GAMES = "games";

    private ArrayList<SoftGameItemBean> softGameItemArrayList = new ArrayList<>();

    private RecyclerView recyclerView;
    private SoftGameItemAdapter softGameItemAdapter;
    private ProgressBar progressBar;

    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Object url = getArguments().get("url");
        String placementName = getArguments().getString("placementName");
        if (url == null) {
            return null;
        }

        View v = inflater.inflate(R.layout.frag_game_hot, container, false);

        progressBar = (ProgressBar) v.findViewById(R.id.soft_game_progress_bar);
        recyclerView = (RecyclerView) v.findViewById(R.id.soft_game_main_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(HSApplication.getContext(), LinearLayoutManager.VERTICAL, false));
        softGameItemAdapter = new SoftGameItemAdapter(placementName);
        recyclerView.setAdapter(softGameItemAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        HSHttpConnection hsHttpConnection = new HSHttpConnection(url.toString());
        hsHttpConnection.startAsync();
        hsHttpConnection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                progressBar.setVisibility(View.GONE);
                JSONObject bodyJSON = hsHttpConnection.getBodyJSON();
                try {
                    List<Object> jsonMap = HSJsonUtil.toList(bodyJSON.getJSONArray(JSON_GAMES));
                    for (Object stringObjectMap : jsonMap) {
                        Map<String, String> object = (Map<String, String>) stringObjectMap;
                        String name = object.get("name");
                        String description = object.get("description");
                        String thumb = object.get("thumb");
                        String link = object.get("link");
                        SoftGameItemBean bean = new SoftGameItemBean(name, description, thumb, link);
                        softGameItemArrayList.add(bean);
                    }
                    softGameItemAdapter.refreshDataList(softGameItemArrayList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                hsError.getMessage();
            }
        });

        return v;
    }

    public static SoftGameItemFragment newInstance(String text, String placementName) {

        SoftGameItemFragment f = new SoftGameItemFragment();
        Bundle b = new Bundle();
        b.putString("url", text);
        b.putString("placementName", placementName);
        f.setArguments(b);

        return f;
    }
}