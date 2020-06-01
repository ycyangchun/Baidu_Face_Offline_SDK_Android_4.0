package com.baidu.idl.face.main.patrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.FaceRGBCloseDebugSearchActivity;
import com.baidu.idl.face.main.patrol.scanner.CaptureActivity;
import com.baidu.idl.facesdkdemo.R;
import com.flyco.roundview.RoundTextView;

import java.util.ArrayList;
import java.util.List;

public class PatrolMainActivity extends BaseActivity {

    RecyclerView recyclerView;
    PatrolMainActivity mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patrollist);
        mContext = this;
        recyclerView = this.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");
        list.add("1");

        PatrolAdapter adapter = new PatrolAdapter(list);
        recyclerView.setAdapter(adapter);

        RoundTextView btnSelect = findViewById(R.id.scan);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CaptureActivity.class);
                startActivityForResult(intent, 111);
            }
        });
    }

}
