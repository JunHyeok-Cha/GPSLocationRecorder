package com.example.gpslocationrecorder;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    private MainMenuHomeFragment fragmentHome;
    private MainMenuRecordFragment fragmentRecord;
    private MainMenuListFragment fragmentList;
    private MainMenuSettingFragment fragmentSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();

        fragmentHome = new MainMenuHomeFragment();
        fragmentRecord = new MainMenuRecordFragment();
        fragmentList = new MainMenuListFragment();
        fragmentSetting = new MainMenuSettingFragment();

        // 첫 화면: 홈
        fragmentManager.beginTransaction()
                .replace(R.id.menu_frame_layout, fragmentHome)
                .commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.menu_bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new ItemSelectedListener());

        bottomNavigationView.setSelectedItemId(R.id.menu_home);
    }

    class ItemSelectedListener implements BottomNavigationView.OnItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                transaction.replace(R.id.menu_frame_layout, fragmentHome);
            } else if (id == R.id.menu_record) {
                transaction.replace(R.id.menu_frame_layout, fragmentRecord);
            } else if (id == R.id.menu_list) {
                transaction.replace(R.id.menu_frame_layout, fragmentList);
            } else if (id == R.id.menu_setting) {
                transaction.replace(R.id.menu_frame_layout, fragmentSetting);
            } else {
                return false; // 알 수 없는 id
            }

            transaction.commitAllowingStateLoss();
            return true; // 선택된 탭 활성화
        }
    }
}
