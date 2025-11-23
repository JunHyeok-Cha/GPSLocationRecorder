package com.example.gpslocationrecorder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpslocationrecorder.data.db.AppDatabase;
import com.example.gpslocationrecorder.data.entity.ParkingRecord;

import java.util.List;

public class MainMenuListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoRecords;
    private ImageButton btnDeleteAll; // 전체 삭제 버튼

    private ParkingRecordAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_list);
        tvNoRecords = view.findViewById(R.id.tv_no_records);
        btnDeleteAll = view.findViewById(R.id.btn_delete_all); // 버튼 찾아오기

        // 개별 삭제 리스너를 포함한 어댑터 설정
        adapter = new ParkingRecordAdapter(requireContext(), this::showDeleteDialog);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 데이터베이스 준비
        db = AppDatabase.getInstance(requireContext());

        // ★ [추가] 전체 삭제 버튼 클릭 리스너 설정
        btnDeleteAll.setOnClickListener(v -> showDeleteAllDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecordsFromDb();
    }

    private void loadRecordsFromDb() {
        new Thread(() -> {
            List<ParkingRecord> records = db.parkingRecordDao().getAll();
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (records.isEmpty()) {
                        tvNoRecords.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        btnDeleteAll.setVisibility(View.GONE); // 기록 없으면 버튼도 숨김
                    } else {
                        tvNoRecords.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        btnDeleteAll.setVisibility(View.VISIBLE); // 기록 있으면 버튼 보임
                        adapter.setItems(records);
                    }
                });
            }
        }).start();
    }

    // 개별 기록 삭제 확인 팝업
    private void showDeleteDialog(ParkingRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("기록 삭제")
                .setMessage("이 주차 기록을 정말 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteRecord(record))
                .setNegativeButton("취소", null)
                .show();
    }

    // 개별 기록 DB 삭제 로직
    private void deleteRecord(ParkingRecord record) {
        new Thread(() -> {
            db.parkingRecordDao().delete(record);
            loadRecordsFromDb(); // 목록 새로고침
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // ★ [추가] 전체 삭제 확인 팝업
    private void showDeleteAllDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("전체 기록 삭제")
                .setMessage("정말로 모든 주차 기록을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
                .setPositiveButton("전체 삭제", (dialog, which) -> deleteAllRecords())
                .setNegativeButton("취소", null)
                .show();
    }

    // ★ [추가] 전체 기록 DB 삭제 로직
    private void deleteAllRecords() {
        new Thread(() -> {
            db.parkingRecordDao().deleteAll();
            loadRecordsFromDb(); // 목록 새로고침
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "모든 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}