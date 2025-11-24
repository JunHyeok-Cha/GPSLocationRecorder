package com.example.gpslocationrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.gpslocationrecorder.data.db.AppDatabase;
import com.example.gpslocationrecorder.data.entity.ParkingRecord;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ParkingDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private AppDatabase db;

    private ImageButton btnEdit, btnSave;
    private Button btnChangePhoto;
    private EditText etFloor, etMemo;
    private TextView tvDate;
    private ImageView ivPhoto;

    private ParkingRecord currentRecord;
    // ★ [수정] 새로 촬영된 사진의 파일 경로를 저장할 변수
    private String newPhotoPath = null;

    // ★ [수정] MainMenuRecordFragment와 동일한 방식의 런처들
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_detail);

        initializeLaunchers();
        db = AppDatabase.getInstance(this);
        loadIntentData();
        setupUI();
        displayData();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void initializeLaunchers() {
        // 카메라 권한 요청 런처
        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openCamera();
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 카메라 실행 및 결과 처리 런처
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleCameraResult
        );
    }

    private void loadIntentData() {
        currentRecord = new ParkingRecord();
        currentRecord.id = getIntent().getLongExtra("id", -1);
        currentRecord.latitude = getIntent().getDoubleExtra("lat", 0);
        currentRecord.longitude = getIntent().getDoubleExtra("lng", 0);
        currentRecord.photoPath = getIntent().getStringExtra("path");
        currentRecord.floor = getIntent().getStringExtra("floor");
        currentRecord.memo = getIntent().getStringExtra("memo");
        currentRecord.createdAt = getIntent().getLongExtra("time", 0);

        if (currentRecord.id == -1) {
            Toast.makeText(this, "오류: 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupUI() {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit);
        btnSave = findViewById(R.id.btn_save);
        ivPhoto = findViewById(R.id.iv_detail_photo);
        tvDate = findViewById(R.id.tv_detail_date);
        etFloor = findViewById(R.id.et_detail_floor);
        etMemo = findViewById(R.id.et_detail_memo);
        mapView = findViewById(R.id.map_view_detail);
        btnChangePhoto = findViewById(R.id.btn_change_photo);

        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> toggleEditMode(true));
        btnSave.setOnClickListener(v -> saveRecord());
        // '사진 변경' 버튼 클릭 시 카메라 권한 확인
        btnChangePhoto.setOnClickListener(v -> {
            if (hasCameraPermission()) {
                openCamera();
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    private void displayData() {
        etFloor.setText(currentRecord.floor);
        etMemo.setText(currentRecord.memo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 a hh:mm", Locale.KOREA);
        tvDate.setText(sdf.format(new Date(currentRecord.createdAt)));

        // ★ [수정] 파일 경로 또는 Uri 문자열을 Glide로 로드
        if (currentRecord.photoPath != null) {
            Glide.with(this)
                    .load(currentRecord.photoPath) // Glide는 경로, Uri 모두 잘 처리함
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .into(ivPhoto);
        }
    }

    private void toggleEditMode(boolean isEditing) {
        etFloor.setEnabled(isEditing);
        etMemo.setEnabled(isEditing);
        btnChangePhoto.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        if (isEditing) etFloor.requestFocus();
    }

    // ★ [추가] MainMenuRecordFragment의 카메라 관련 로직들
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCameraResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getExtras() != null) {
                Object extras = data.getExtras().get("data");
                if (extras instanceof Bitmap) {
                    Bitmap capturedImageBitmap = (Bitmap) extras;
                    // 비트맵을 파일로 저장하고 그 경로를 newPhotoPath에 저장
                    newPhotoPath = saveBitmapToFile(capturedImageBitmap);
                    // ImageView에 촬영된 사진(비트맵)을 표시
                    ivPhoto.setImageBitmap(capturedImageBitmap);
                    Toast.makeText(this, "사진이 촬영되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        if (bitmap == null) return null;
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory == null) return null;

        String fileName = "parking_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            return file.getAbsolutePath(); // 저장된 파일의 절대 경로 반환
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveRecord() {
        currentRecord.floor = etFloor.getText().toString();
        currentRecord.memo = etMemo.getText().toString();
        // ★ [수정] 새로 촬영된 사진이 있으면, 경로를 업데이트
        if (newPhotoPath != null) {
            currentRecord.photoPath = newPhotoPath;
        }

        new Thread(() -> {
            db.parkingRecordDao().update(currentRecord);
            runOnUiThread(() -> {
                Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                toggleEditMode(false);
                // 저장 후 newPhotoPath 초기화
                newPhotoPath = null;
            });
        }).start();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng location = new LatLng(currentRecord.latitude, currentRecord.longitude);
        googleMap.addMarker(new MarkerOptions().position(location).title("주차 위치"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f));
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}