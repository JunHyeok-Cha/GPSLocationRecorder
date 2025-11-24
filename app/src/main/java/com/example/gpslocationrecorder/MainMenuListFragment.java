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
    private ImageButton btnDeleteAll;

    private ParkingRecordAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_list);
        tvNoRecords = view.findViewById(R.id.tv_no_records);
        btnDeleteAll = view.findViewById(R.id.btn_delete_all);

        adapter = new ParkingRecordAdapter(requireContext(), this::showDeleteDialog);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = AppDatabase.getInstance(requireContext());

        btnDeleteAll.setOnClickListener(v -> showDeleteAllDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeParkingRecords();
    }

    private void observeParkingRecords() {
        db.parkingRecordDao().getAll().observe(getViewLifecycleOwner(), records -> {
            if (records == null || records.isEmpty()) {
                tvNoRecords.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                btnDeleteAll.setVisibility(View.GONE);
            } else {
                tvNoRecords.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                btnDeleteAll.setVisibility(View.VISIBLE);
                adapter.setItems(records);
            }
        });
    }

    private void showDeleteDialog(ParkingRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("기록 삭제")
                .setMessage("이 주차 기록을 정말 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteRecord(record))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteRecord(ParkingRecord record) {
        new Thread(() -> {
            db.parkingRecordDao().delete(record);
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void showDeleteAllDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("전체 기록 삭제")
                .setMessage("정말로 모든 주차 기록을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
                .setPositiveButton("전체 삭제", (dialog, which) -> deleteAllRecords())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteAllRecords() {
        new Thread(() -> {
            db.parkingRecordDao().deleteAll();
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "모든 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}