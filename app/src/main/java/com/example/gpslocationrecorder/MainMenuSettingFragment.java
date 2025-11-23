package com.example.gpslocationrecorder;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;

public class MainMenuSettingFragment extends Fragment {

    private SwitchCompat switchRecordNotification;
    private SwitchCompat switchAutoRecord;
    // private Button btnDeleteAllData; // 삭제됨
    // private AppDatabase db; // 삭제됨

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu_setting, container, false);

        switchRecordNotification = view.findViewById(R.id.switch_record_notification);
        switchAutoRecord = view.findViewById(R.id.switch_auto_record);
        // btnDeleteAllData = view.findViewById(R.id.btn_delete_all_data); // 삭제됨

        // db = AppDatabase.getInstance(requireContext()); // 삭제됨

        loadSettings();
        setupListeners();

        return view;
    }

    private void loadSettings() {
        // TODO: SharedPreferences에서 저장된 설정 값을 불러와 UI에 적용합니다.
        switchRecordNotification.setChecked(true);
        switchAutoRecord.setChecked(true);
    }

    private void setupListeners() {
        switchRecordNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: 푸시 알림 상태를 SharedPreferences에 저장합니다.
            String status = isChecked ? "주차 기록 완료 시 알림 ON" : "주차 기록 완료 시 알림 OFF";
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
        });

        switchAutoRecord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // TODO: 자동 위치 감지 상태를 저장하고, 앱의 백그라운드 위치 서비스에 영향을 줍니다.
            String status = isChecked ? "자동 주차 위치 기록 ON" : "자동 주차 위치 기록 OFF";
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
        });

        // btnDeleteAllData.setOnClickListener 리스너 전체 삭제됨
    }

    // showDeleteAllConfirmationDialog() 함수 전체 삭제됨
    // deleteAllRecords() 함수 전체 삭제됨
}