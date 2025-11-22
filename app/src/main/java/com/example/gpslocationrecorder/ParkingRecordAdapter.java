package com.example.gpslocationrecorder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA);

    public ParkingRecordAdapter(Context context) {
        this.context = context;
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

        // ★★★ [핵심] 클릭 이벤트 리스너 ★★★
        // 사용자가 리스트의 아이템(itemView)을 클릭하면 실행됩니다.
        holder.itemView.setOnClickListener(v -> {
            // 1. 상세 화면(ParkingDetailActivity)으로 갈 준비
            Intent intent = new Intent(context, ParkingDetailActivity.class);

            // 2. 상세 화면에 보여줄 데이터들을 담기 (Intent에 putExtra로 넣음)
            intent.putExtra("lat", item.latitude);
            intent.putExtra("lng", item.longitude);
            intent.putExtra("path", item.photoPath);
            intent.putExtra("floor", item.floor);
            intent.putExtra("memo", item.memo);
            intent.putExtra("time", item.createdAt);

            // 3. 화면 이동 시작!
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTime, tvFloor, tvMemo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // item_parking_record.xml의 ID들과 연결
            ivImage = itemView.findViewById(R.id.iv_record_image);
            tvTime = itemView.findViewById(R.id.tv_record_time);
            tvFloor = itemView.findViewById(R.id.tv_record_floor);
            tvMemo = itemView.findViewById(R.id.tv_record_memo);
        }
    }
}