package com.example.gpslocationrecorder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gpslocationrecorder.data.db.AppDatabase;
import com.example.gpslocationrecorder.data.entity.ParkingRecord;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainMenuHomeFragment extends Fragment implements OnMapReadyCallback {

    private LinearLayout layoutNoRecord, layoutHasRecord;
    private Button btnCreateFirstRecord, btnNewRecord, btnViewAllRecords;
    private TextView textRecentAddress;
    private MapView mapView;
    private GoogleMap googleMap;

    private AppDatabase db;
    private ParkingRecord currentRecord;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu_home, container, false);

        db = AppDatabase.getInstance(requireContext());

        layoutNoRecord = view.findViewById(R.id.layout_no_record);
        layoutHasRecord = view.findViewById(R.id.layout_has_record);
        btnCreateFirstRecord = view.findViewById(R.id.btn_create_first_record);
        btnNewRecord = view.findViewById(R.id.btn_new_record);
        btnViewAllRecords = view.findViewById(R.id.btn_view_all_records);
        textRecentAddress = view.findViewById(R.id.text_recent_address);
        mapView = view.findViewById(R.id.map_view_home);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        setupClickListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeLatestRecord();
    }

    private void observeLatestRecord() {
        db.parkingRecordDao().getLatestRecord().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(ParkingRecord latestRecord) {
        currentRecord = latestRecord;
        if (latestRecord != null) {
            layoutNoRecord.setVisibility(View.GONE);
            layoutHasRecord.setVisibility(View.VISIBLE);

            if (latestRecord.memo != null && !latestRecord.memo.isEmpty()) {
                textRecentAddress.setText(latestRecord.memo);
            } else if (latestRecord.floor != null && !latestRecord.floor.isEmpty()) {
                textRecentAddress.setText("층수: " + latestRecord.floor);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm 기록", Locale.getDefault());
                textRecentAddress.setText(sdf.format(new Date(latestRecord.createdAt)));
            }
            updateMapLocation();
        } else {
            layoutNoRecord.setVisibility(View.VISIBLE);
            layoutHasRecord.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        updateMapLocation();
    }

    private void updateMapLocation() {
        if (googleMap == null || currentRecord == null) return;

        LatLng location = new LatLng(currentRecord.latitude, currentRecord.longitude);
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(location).title("최근 주차 위치"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
    }

    private void setupClickListeners() {
        View.OnClickListener listener = v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectMenuItem(R.id.menu_record);
            }
        };
        btnCreateFirstRecord.setOnClickListener(listener);
        btnNewRecord.setOnClickListener(listener);

        btnViewAllRecords.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectMenuItem(R.id.menu_list);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
