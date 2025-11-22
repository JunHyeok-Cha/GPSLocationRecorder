package com.example.gpslocationrecorder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    // 데이터를 리스트 모양으로 만들어주는 어댑터
    private ParkingRecordAdapter adapter;
    // 데이터베이스 인스턴스
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. 레이아웃(XML) 연결
        View view = inflater.inflate(R.layout.fragment_main_menu_list, container, false);

        // 2. 화면 요소(View) 찾아오기
        recyclerView = view.findViewById(R.id.recycler_view_list);
        tvNoRecords = view.findViewById(R.id.tv_no_records);

        // 3. 어댑터 설정 (리스트뷰 초기화)
        adapter = new ParkingRecordAdapter(requireContext());
        recyclerView.setAdapter(adapter);
        // 리스트를 세로 방향으로 나열하겠다고 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 4. 데이터베이스 준비
        db = AppDatabase.getInstance(requireContext());

        return view;
    }

    // 프래그먼트가 화면에 보일 때마다 호출됩니다.
    // (기록 탭에서 저장하고 목록 탭으로 넘어왔을 때 데이터를 새로고침하기 위함)
    @Override
    public void onResume() {
        super.onResume();
        loadRecordsFromDb();
    }

    // DB에서 데이터를 불러오는 함수
    private void loadRecordsFromDb() {
        // DB 작업은 메인 스레드(UI 멈춤 방지)가 아닌 별도 스레드에서 실행해야 합니다.
        new Thread(() -> {
            // 1. DB에게 "저장된 모든 기록 내놔!" 하고 요청 (최신순 정렬은 DAO 쿼리에 있음)
            List<ParkingRecord> records = db.parkingRecordDao().getAll();

            // 2. 가져온 데이터를 화면에 그리는 건 다시 메인 스레드에서 해야 합니다.
            if (getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (records.isEmpty()) {
                        // 기록이 하나도 없으면: "기록 없음" 글자 보여주고 리스트 숨기기
                        tvNoRecords.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        // 기록이 있으면: "기록 없음" 글자 숨기고 리스트 보여주기
                        tvNoRecords.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        // 어댑터에게 데이터 전달 (화면 갱신)
                        adapter.setItems(records);
                    }
                });
            }
        }).start();
    }
}