package com.example.gpslocationrecorder;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ParkingDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_detail);

        // 1. Intent로 전달받은 데이터 꺼내기
        latitude = getIntent().getDoubleExtra("lat", 0);
        longitude = getIntent().getDoubleExtra("lng", 0);
        String photoPath = getIntent().getStringExtra("path");
        String floor = getIntent().getStringExtra("floor");
        String memo = getIntent().getStringExtra("memo");
        long createdAt = getIntent().getLongExtra("time", 0);

        // 2. UI 연결
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView ivPhoto = findViewById(R.id.iv_detail_photo);
        TextView tvDate = findViewById(R.id.tv_detail_date);
        TextView tvFloor = findViewById(R.id.tv_detail_floor);
        TextView tvMemo = findViewById(R.id.tv_detail_memo);
        mapView = findViewById(R.id.map_view_detail);

        // 3. 데이터 표시
        tvFloor.setText(floor == null || floor.isEmpty() ? "정보 없음" : floor);
        tvMemo.setText(memo == null || memo.isEmpty() ? "내용 없음" : memo);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 a hh:mm", Locale.KOREA);
        tvDate.setText(sdf.format(new Date(createdAt)));

        if (photoPath != null) {
            Glide.with(this)
                    .load(photoPath)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .into(ivPhoto);
        }

        // 4. 지도 초기화
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        // 5. 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        // 전달받은 위치에 마커 찍기
        LatLng location = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(location).title("주차 위치"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f));
        googleMap.getUiSettings().setScrollGesturesEnabled(false); // 지도는 보기만 가능하게 스크롤 막기 (선택사항)
    }

    // MapView 생명주기 관리 (필수)
    @Override
    protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override
    protected void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
}