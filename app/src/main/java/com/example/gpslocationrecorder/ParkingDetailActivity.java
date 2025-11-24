package com.example.gpslocationrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
    private String newPhotoPath = null;

    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    // ★ [추가] 텍스트 자동 클리어를 위한 플래그
    private boolean isFloorCleared = false;
    private boolean isMemoCleared = false;

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
        cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) openCamera();
            else Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleCameraResult);
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

        btnBack.setOnClickListener(v -> handleBackButton());
        btnEdit.setOnClickListener(v -> toggleEditMode(true));
        btnSave.setOnClickListener(v -> saveRecord());
        btnChangePhoto.setOnClickListener(v -> {
            if (hasCameraPermission()) openCamera();
            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        });
    }

    private void displayData() {
        etFloor.setText(currentRecord.floor);
        etMemo.setText(currentRecord.memo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 a hh:mm", Locale.KOREA);
        tvDate.setText(sdf.format(new Date(currentRecord.createdAt)));

        if (currentRecord.photoPath != null) {
            Glide.with(this).load(currentRecord.photoPath).placeholder(android.R.drawable.ic_menu_camera).into(ivPhoto);
        } else {
            ivPhoto.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    private void toggleEditMode(boolean isEditing) {
        etFloor.setEnabled(isEditing);
        etMemo.setEnabled(isEditing);

        // ★ [수정] 배경 변경 및 텍스트 클리어 로직 추가
        if (isEditing) {
            Drawable background = ContextCompat.getDrawable(this, R.drawable.edit_text_background);
            etFloor.setBackground(background);
            etMemo.setBackground(background.getConstantState().newDrawable()); // 배경 복사해서 사용

            // 수정 모드 진입 시, 클리어 플래그 초기화
            isFloorCleared = false;
            isMemoCleared = false;

            // 포커스 리스너 설정 (포커스 될 때 딱 한번 텍스트 클리어)
            etFloor.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && !isFloorCleared) {
                    etFloor.setText("");
                    isFloorCleared = true;
                }
            });
            etMemo.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && !isMemoCleared) {
                    etMemo.setText("");
                    isMemoCleared = true;
                }
            });

            etFloor.requestFocus();
        } else {
            // 조회 모드로 돌아갈 때 배경 제거 및 리스너 null 처리
            etFloor.setBackground(null);
            etMemo.setBackground(null);
            etFloor.setOnFocusChangeListener(null);
            etMemo.setOnFocusChangeListener(null);
        }

        btnChangePhoto.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(isEditing ? View.VISIBLE : View.GONE);
    }

    private void handleBackButton() {
        if (btnSave.getVisibility() == View.VISIBLE) {
            toggleEditMode(false);
            displayData();
            newPhotoPath = null;
            Toast.makeText(this, "수정이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleBackButton();
    }

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
                    newPhotoPath = saveBitmapToFile(capturedImageBitmap);
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
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveRecord() {
        currentRecord.floor = etFloor.getText().toString();
        currentRecord.memo = etMemo.getText().toString();
        if (newPhotoPath != null) {
            currentRecord.photoPath = newPhotoPath;
        }
        new Thread(() -> {
            db.parkingRecordDao().update(currentRecord);
            runOnUiThread(() -> {
                Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                toggleEditMode(false);
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