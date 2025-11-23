package com.example.gpslocationrecorder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gpslocationrecorder.data.entity.ParkingRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParkingRecordAdapter extends RecyclerView.Adapter<ParkingRecordAdapter.ViewHolder> {

    private List<ParkingRecord> items = new ArrayList<>();
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);

    // ★ [추가] 삭제 버튼 클릭을 프래그먼트에 알려주기 위한 인터페이스
    public interface OnItemDeleteListener {
        void onDeleteClick(ParkingRecord record);
    }

    private final OnItemDeleteListener deleteListener;

    // ★ [수정] 생성자에서 삭제 리스너를 함께 받도록 변경
    public ParkingRecordAdapter(Context context, OnItemDeleteListener deleteListener) {
        this.context = context;
        this.deleteListener = deleteListener;
    }

    public void setItems(List<ParkingRecord> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parking_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingRecord item = items.get(position);

        // 1. 화면에 데이터 표시 (날짜, 층수, 메모, 사진)
        String dateString = dateFormat.format(new Date(item.createdAt));
        holder.tvTime.setText(dateString);

        if (item.floor == null || item.floor.isEmpty()) {
            holder.tvFloor.setText("위치 정보 없음");
        } else {
            holder.tvFloor.setText(item.floor);
        }

        holder.tvMemo.setText(item.memo);

        if (item.photoPath != null) {
            Glide.with(context)
                    .load(item.photoPath)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_camera);
        }

        // 2. 상세보기 클릭 이벤트
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ParkingDetailActivity.class);
            intent.putExtra("lat", item.latitude);
            intent.putExtra("lng", item.longitude);
            intent.putExtra("path", item.photoPath);
            intent.putExtra("floor", item.floor);
            intent.putExtra("memo", item.memo);
            intent.putExtra("time", item.createdAt);
            context.startActivity(intent);
        });

        // 3. ★ [추가] 삭제 버튼 클릭 이벤트
        holder.btnDelete.setOnClickListener(v -> {
            // 프래그먼트에 "이 아이템 지워줘!" 하고 알림
            deleteListener.onDeleteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ★ [수정] ViewHolder에 삭제 버튼(ImageButton) 추가
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTime, tvFloor, tvMemo;
        ImageButton btnDelete; // 삭제 버튼

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_record_image);
            tvTime = itemView.findViewById(R.id.tv_record_time);
            tvFloor = itemView.findViewById(R.id.tv_record_floor);
            tvMemo = itemView.findViewById(R.id.tv_record_memo);
            btnDelete = itemView.findViewById(R.id.btn_delete_item); // ID로 찾아오기
        }
    }
}