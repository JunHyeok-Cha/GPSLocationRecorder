package com.example.gpslocationrecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.gpslocationrecorder.data.db.AppDatabase;
import com.example.gpslocationrecorder.data.entity.ParkingRecord;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class MainMenuSettingFragment extends Fragment {

    private SwitchCompat switchRecordNotification;
    private SwitchCompat switchAutoRecord;
    private Button btnSimulateBluetooth; // 시뮬레이션 버튼

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 위치 권한 요청 런처 초기화
        locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // 권한이 승인되면 시뮬레이션 로직 다시 실행
                simulateBluetoothDisconnect();
            } else {
                Toast.makeText(requireContext(), "자동 기록을 위해 위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu_setting, container, false);

        switchRecordNotification = view.findViewById(R.id.switch_record_notification);
        switchAutoRecord = view.findViewById(R.id.switch_auto_record);
        btnSimulateBluetooth = view.findViewById(R.id.btn_simulate_bluetooth_disconnect);

        loadSettings();
        setupListeners();

        return view;
    }

    private void loadSettings() {
        // TODO: SharedPreferences에서 설정 값 불러오기
        switchRecordNotification.setChecked(true);
        switchAutoRecord.setChecked(true);
    }

    private void setupListeners() {
        switchRecordNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "주차 기록 완료 시 알림 ON" : "주차 기록 완료 시 알림 OFF";
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
        });

        switchAutoRecord.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "자동 주차 위치 기록 ON" : "자동 주차 위치 기록 OFF";
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
        });

        // ★ [추가] 시뮬레이션 버튼 클릭 리스너
        btnSimulateBluetooth.setOnClickListener(v -> {
            Toast.makeText(getContext(), "블루투스 끊김 시뮬레이션 시작", Toast.LENGTH_SHORT).show();
            simulateBluetoothDisconnect();
        });
    }

    // ★ [추가] 시뮬레이션 로직
    private void simulateBluetoothDisconnect() {
        // 1. 위치 권한 확인
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 요청
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        // 2. 현재 위치 가져오기
        getCurrentLocationAndSave();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocationAndSave() {
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        // 3. 위치를 가져왔으면 DB에 저장
                        saveRecord(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(getContext(), "위치 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "위치 요청 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveRecord(double lat, double lng) {
        ParkingRecord record = new ParkingRecord(
                lat,
                lng,
                null, // 자동 기록 시 사진은 없음
                "자동 기록", // 층 정보 대신 "자동 기록"으로 표시
                "블루투스 연결 해제로 자동 기록됨", // 메모
                System.currentTimeMillis()
        );

        // 새 스레드에서 DB에 저장
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            db.parkingRecordDao().insert(record);

            // 4. 저장이 완료되면 메인 스레드에서 알림 발생
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "자동 기록이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    sendNotification();
                });
            }
        }).start();
    }

    private void sendNotification() {
        NotificationHelper notificationHelper = new NotificationHelper(requireContext());
        notificationHelper.sendNotification(
                "주차 위치 자동 기록 완료",
                "블루투스 연결이 해제되어 현재 위치가 자동으로 저장되었습니다.",
                (int) System.currentTimeMillis() // Unique ID
        );
    }
}