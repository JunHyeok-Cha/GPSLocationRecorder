package com.example.gpslocationrecorder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "ParkingRecorderChannel";
    private static final String CHANNEL_NAME = "주차 기록 알림";
    private static final String CHANNEL_DESC = "주차 기록 완료, 시간 초과 등 핵심 정보 알림";
    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        // 인스턴스가 생성될 때 알림 채널을 등록합니다.
        createNotificationChannel();
    }

    /**
     * Android O (8.0) 이상을 위해 알림 채널을 생성합니다.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // 중요도 높음: 팝업 알림 (Heads-up)으로 표시
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 실제로 알림을 생성하고 시스템에 표시하는 함수입니다.
     * 이 함수를 주차 기록 완료 시점(다른 팀 담당)에 호출합니다.
     * @param title 알림 제목 (예: "주차 완료!")
     * @param body 알림 내용 (예: "주차 위치가 자동으로 기록되었습니다.")
     * @param notificationId 각 알림을 구별하는 고유 ID
     */
    public void sendNotification(String title, String body, int notificationId) {

        // NotificationCompat.Builder를 사용하여 알림 디자인 및 내용 설정
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // 상태 바에 표시되는 작은 아이콘
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // 탭하면 알림이 사라지게 설정

        // NotificationManager를 통해 알림 띄우기
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
}