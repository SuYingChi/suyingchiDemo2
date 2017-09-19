package com.keyboard.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.feature.battery.BatteryActivity;
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.feature.cpucooler.CpuCoolerScanActivity;
import com.ihs.inputmethod.uimodules.R;

public class DebugActivity extends HSAppCompatActivity {

    private Class[] activityClasses = {
            BatteryActivity.class,
            BoostPlusActivity.class,
            CpuCoolerScanActivity.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, activityClasses));
        listView.setOnItemClickListener(onItemClickListener);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(DebugActivity.this, activityClasses[position]);
            startActivity(intent);
        }
    };
}
