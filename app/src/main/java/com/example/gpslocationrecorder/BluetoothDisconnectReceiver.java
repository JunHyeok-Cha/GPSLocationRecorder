package com.example.gpslocationrecorder;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.example.gpslocationrecorder.data.db.AppDatabase;
import com.example.gpslocationrecorder.data.dao.ParkingRecordDao;
import com.example.gpslocationrecorder.data.entity.ParkingRecord;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * 차량 블루투스가 해제될 때 현재 위치를 자동으로 DB에 저장하고
 * 알림을 띄우는 리시버.
 */
public class BluetoothDisconnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
            return;
        }

        // 필요하면 여기서 특정 차량 이름/주소로 필터링 가능
        // BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        final PendingResult pendingResult = goAsync();
        Context appContext = context.getApplicationContext();

        // 위치 권한 체크 (실제 권한 요청은 Activity 쪽에서 해둔 상태라고 가정)
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            pendingResult.finish();
            return;
        }

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(appContext);

        // 간단하게 lastLocation 사용 (이미 다른 곳에서 위치를 쓰고 있다면 동일한 방식으로 맞춰줘도 됨)
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        pendingResult.finish();
                        return;
                    }

                    saveRecordAndNotify(appContext, location, pendingResult);
                })
                .addOnFailureListener(e -> pendingResult.finish());
    }

    private void saveRecordAndNotify(Context context, Location location, PendingResult pendingResult) {
        new Thread(() -> {
            try {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                long now = System.currentTimeMillis();

                // 새 ParkingRecord 생성 (사진/층/메모는 비워둠)
                ParkingRecord record = new ParkingRecord(
                        lat,
                        lng,
                        null,   // photoPath
                        "",     // floor
                        "",     // memo
                        now
                ); // :contentReference[oaicite:1]{index=1}

                // Room DB에 저장
                AppDatabase db = AppDatabase.getInstance(context);
                ParkingRecordDao dao = db.parkingRecordDao();
                long newId = dao.insert(record); // :contentReference[oaicite:2]{index=2}

                // 알림 클릭 시 곧바로 상세/수정 화면으로 이동
                Intent detailIntent = new Intent(context, ParkingDetailActivity.class);
                detailIntent.putExtra("lat", record.latitude);
                detailIntent.putExtra("lng", record.longitude);
                detailIntent.putExtra("path", record.photoPath);
                detailIntent.putExtra("floor", record.floor);
                detailIntent.putExtra("memo", record.memo);
                detailIntent.putExtra("time", record.createdAt); // :contentReference[oaicite:3]{index=3}
                detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                PendingIntent contentIntent = PendingIntent.getActivity(
                        context,
                        (int) newId, // 각 기록마다 다른 requestCode
                        detailIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                NotificationHelper helper = new NotificationHelper(context);
                helper.sendNotification(
                        "주차 위치 자동 저장",
                        "차량 블루투스가 해제되어 현재 위치를 기록했어요.",
                        (int) newId,
                        contentIntent
                );

            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}
