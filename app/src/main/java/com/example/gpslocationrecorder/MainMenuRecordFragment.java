package com.example.gpslocationrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import com.example.gpslocationrecorder.data.db.AppDatabase;
import com.example.gpslocationrecorder.data.entity.ParkingRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainMenuRecordFragment extends Fragment implements OnMapReadyCallback {

    // UI
    private TextView tvLatitude;
    private TextView tvLongitude;
    private TextView tvLocationDescription;
    private Button btnGetLocation;
    private Button btnTakePhoto;
    private Button btnRecord;
    private EditText etFloor;
    private EditText etMemo;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private Double currentLat = null;
    private Double currentLng = null;
    private MapView mapView;
    private GoogleMap googleMap;
    // 상단 필드들 옆에 추가
    private ImageView ivPhotoPreview;
    private View photoPlaceholderLayout;



    // (옵션) 찍은 사진 썸네일 저장용
    private Bitmap capturedImageBitmap = null;
    // ✅ 실제 파일 경로 저장용
    private String capturedPhotoPath = null;

    // Activity Result / Permission 런처
    private ActivityResultLauncher<String> locationPermissionLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 위치 클라이언트 초기화
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        // 위치 권한 런처
        locationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                getCurrentLocation();
                            } else {
                                Toast.makeText(
                                        requireContext(),
                                        "위치 권한이 필요합니다.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

        // 카메라 권한 런처
        cameraPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                openCamera();
                            } else {
                                Toast.makeText(
                                        requireContext(),
                                        "카메라 권한이 필요합니다.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

        // 카메라 실행 런처
        cameraLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        this::handleCameraResult
                );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_main_menu_record, container, false
        );

        mapView = rootView.findViewById(R.id.map_view);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        // UI 연결
        tvLatitude = rootView.findViewById(R.id.tv_latitude);
        tvLongitude = rootView.findViewById(R.id.tv_longitude);
        tvLocationDescription = rootView.findViewById(R.id.tv_location_description);
        btnGetLocation = rootView.findViewById(R.id.btn_get_location);
        btnTakePhoto = rootView.findViewById(R.id.btn_take_photo);
        btnRecord = rootView.findViewById(R.id.btn_record);
        etFloor = rootView.findViewById(R.id.et_floor);
        etMemo = rootView.findViewById(R.id.et_memo);
        ivPhotoPreview = rootView.findViewById(R.id.iv_photo_preview);
        photoPlaceholderLayout = rootView.findViewById(R.id.layout_photo_placeholder);

        if (mapView != null) {
            mapView.setVisibility(View.GONE);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        // 버튼 리스너
        btnGetLocation.setOnClickListener(v -> onClickGetLocation());
        btnTakePhoto.setOnClickListener(v -> onClickTakePhoto());
        btnRecord.setOnClickListener(v -> onClickRecord());

        return rootView;
    }

    /* ===========================
       버튼 클릭 핸들러
       =========================== */

    private void onClickGetLocation() {
        if (hasLocationPermission()) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
            );
        }
    }

    private void onClickTakePhoto() {
        if (hasCameraPermission()) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void onClickRecord() {
        if (currentLat == null || currentLng == null) {
            Toast.makeText(requireContext(),
                    "먼저 현재 위치를 가져와 주세요.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String floor = etFloor.getText().toString().trim();
        String memo = etMemo.getText().toString().trim();

        long now = System.currentTimeMillis();
        String photoPath = capturedPhotoPath;

        // ParkingRecord 엔티티 생성
        ParkingRecord record = new ParkingRecord(
                currentLat,
                currentLng,
                photoPath,
                floor,
                memo,
                now
        );

        // DB 인스턴스
        AppDatabase db = AppDatabase.getInstance(requireContext());

        // Room 은 메인스레드에서 접근하면 예외를 던지므로, 새 쓰레드에서 실행
        new Thread(() -> {
            db.parkingRecordDao().insert(record);
            // 필요하면 여기서 Log 찍거나, 나중에 콜백 처리
        }).start();

        // UI 쪽은 바로 처리해도 OK (DB insert는 백그라운드에서 돌게 됨)
        Toast.makeText(
                requireContext(),
                "주차 기록이 저장되었습니다.",
                Toast.LENGTH_SHORT
        ).show();

        // 주차 기록 완료 알림 보내기
        NotificationHelper notificationHelper = new NotificationHelper(requireContext());
        notificationHelper.sendNotification(
                "주차 위치 기록 완료",
                "새로운 주차 위치가 성공적으로 저장되었습니다.",
                (int) System.currentTimeMillis() // Unique ID for the notification
        );

        resetLocationAndMap();
        resetPhoto();
        resetTextFields();
    }

    /* ===========================
       위치 관련
       =========================== */

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        // 여기서도 한 번 더 권한 체크
        if (!hasLocationPermission()) {
            Toast.makeText(
                    requireContext(),
                    "위치 권한이 필요합니다.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();

        fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        updateLocationUI(location);
                    } else {
                        Toast.makeText(
                                requireContext(),
                                "위치 정보를 가져오지 못했습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(
                        requireContext(),
                        "위치 정보 요청 중 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                ).show());
    }


    private void updateLocationUI(@NonNull Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();

        tvLatitude.setText("위도: " + currentLat);
        tvLongitude.setText("경도: " + currentLng);
        tvLocationDescription.setText("위치 정보를 가져왔습니다.");

        if (mapView != null && mapView.getVisibility() != View.VISIBLE) {
            mapView.setVisibility(View.VISIBLE);
        }
        updateMapLocation();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // 이미 위치를 받아둔 상태라면, 지도에 바로 반영
        updateMapLocation();
    }
    private void updateMapLocation() {
        if (googleMap == null || currentLat == null || currentLng == null) return;

        LatLng here = new LatLng(currentLat, currentLng);

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(here)
                .title("현재 위치"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(here, 16f));
    }
    private void resetLocationAndMap() {
        currentLat = null;
        currentLng = null;

        // 지도에 찍힌 마커 제거
        if (googleMap != null) {
            googleMap.clear();
        }

        // 지도 숨기기
        if (mapView != null) {
            mapView.setVisibility(View.GONE);
        }

        // 텍스트도 초기 상태로 되돌리기
        tvLatitude.setText("위도: -");
        tvLongitude.setText("경도: -");
        tvLocationDescription.setText("위치 정보를 가져와주세요");
    }
    /* ===========================
       카메라 관련
       =========================== */

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(
                requireActivity().getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(
                    requireContext(),
                    "카메라 앱을 찾을 수 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void handleCameraResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getExtras() != null) {
                Object extras = data.getExtras().get("data");
                if (extras instanceof Bitmap) {
                    capturedImageBitmap = (Bitmap) extras;

                    // ✅ Bitmap을 파일로 저장하고 경로를 멤버 변수에 저장
                    capturedPhotoPath = saveBitmapToFile(capturedImageBitmap);

                    // ✅ 썸네일을 ImageView에 표시
                    if (ivPhotoPreview != null) {
                        ivPhotoPreview.setImageBitmap(capturedImageBitmap);
                        ivPhotoPreview.setVisibility(View.VISIBLE);
                    }

                    // ✅ 기본 카메라 아이콘/텍스트는 숨기기
                    if (photoPlaceholderLayout != null) {
                        photoPlaceholderLayout.setVisibility(View.GONE);
                    }
                }
            }
            Toast.makeText(
                    requireContext(),
                    "사진이 촬영되었습니다.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /** Bitmap을 앱 전용 Pictures 폴더에 JPG로 저장하고, 파일 경로를 반환 */
    private String saveBitmapToFile(Bitmap bitmap) {
        if (bitmap == null) return null;

        // 앱 전용 외부 저장소: /storage/emulated/0/Android/data/패키지명/files/Pictures
        File directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory == null) return null;

        // 파일 이름: parking_타임스탬프.jpg
        String fileName = "parking_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // JPEG 형식으로 압축해서 저장 (품질 90)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            return file.getAbsolutePath();  // ✅ 이 경로를 DB에 넣을 것
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void resetPhoto() {
        capturedImageBitmap = null;
        capturedPhotoPath = null;

        if (ivPhotoPreview != null) {
            ivPhotoPreview.setImageDrawable(null);
            ivPhotoPreview.setVisibility(View.GONE);
        }

        if (photoPlaceholderLayout != null) {
            photoPlaceholderLayout.setVisibility(View.VISIBLE);
        }
    }

    private void resetTextFields() {
        if (etFloor != null) {
            etFloor.setText("");
        }
        if (etMemo != null) {
            etMemo.setText("");
        }
    }

    //생명 주기
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }




}
