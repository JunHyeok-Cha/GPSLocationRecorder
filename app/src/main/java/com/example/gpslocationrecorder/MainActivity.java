package com.example.gpslocationrecorder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationView;

    private MainMenuHomeFragment fragmentHome;
    private MainMenuRecordFragment fragmentRecord;
    private MainMenuListFragment fragmentList;
    private MainMenuSettingFragment fragmentSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 13 이상에서 알림 권한 요청 (앱 시작 시 한 번만)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        fragmentManager = getSupportFragmentManager();
        bottomNavigationView = findViewById(R.id.menu_bottom_navigation);

        fragmentHome = new MainMenuHomeFragment();
        fragmentRecord = new MainMenuRecordFragment();
        fragmentList = new MainMenuListFragment();
        fragmentSetting = new MainMenuSettingFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.menu_frame_layout, fragmentHome)
                .commitAllowingStateLoss();

        bottomNavigationView.setOnItemSelectedListener(new ItemSelectedListener());
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
    }

    public void selectMenuItem(int itemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(itemId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "알림 권한이 거부되었습니다. 앱 설정에서 권한을 허용할 수 있습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    class ItemSelectedListener implements BottomNavigationView.OnItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            int id = item.getItemId();
            Fragment targetFragment = null;

            if (id == R.id.menu_home) {
                targetFragment = fragmentHome;
            } else if (id == R.id.menu_record) {
                targetFragment = fragmentRecord;
            } else if (id == R.id.menu_list) {
                targetFragment = fragmentList;
            } else if (id == R.id.menu_setting) {
                targetFragment = fragmentSetting;
            }

            if (targetFragment != null) {
                transaction.replace(R.id.menu_frame_layout, targetFragment);
                transaction.commitAllowingStateLoss();
            }

            return true;
        }
    }
}
