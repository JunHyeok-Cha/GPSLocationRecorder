package com.example.gpslocationrecorder;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenuHomeFragment extends Fragment {

    // UI 요소 선언
    private LinearLayout layoutNoRecord;
    private LinearLayout layoutHasRecord;
    private Button btnCreateFirstRecord;
    private Button btnNewRecord;
    private Button btnViewAllRecords;
    private TextView textRecentAddress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // fragment_main_menu_home.xml 레이아웃을 인플레이트합니다.
        View view = inflater.inflate(R.layout.fragment_main_menu_home, container, false);

        // 뷰 초기화 (findViewById)
        layoutNoRecord = view.findViewById(R.id.layout_no_record);
        layoutHasRecord = view.findViewById(R.id.layout_has_record);
        btnCreateFirstRecord = view.findViewById(R.id.btn_create_first_record);
        btnNewRecord = view.findViewById(R.id.btn_new_record);
        btnViewAllRecords = view.findViewById(R.id.btn_view_all_records);
        textRecentAddress = view.findViewById(R.id.text_recent_address);

        // 초기 데이터 로딩 및 UI 설정
        updateUIBasedOnRecordStatus();

        // 버튼 클릭 리스너 설정
        setupClickListeners();

        return view;
    }

    /**
     * 기록 유무에 따라 UI를 업데이트하는 함수
     */
    private void updateUIBasedOnRecordStatus() {
        boolean hasRecentRecord = getRecentRecordStatus();

        if (hasRecentRecord) {
            layoutNoRecord.setVisibility(View.GONE);
            layoutHasRecord.setVisibility(View.VISIBLE);

            // TODO: 실제 기록 데이터를 가져와 뷰에 표시
        } else {
            layoutNoRecord.setVisibility(View.VISIBLE);
            layoutHasRecord.setVisibility(View.GONE);
        }
    }

    /**
     * 임시 기록 상태 확인 함수 (현재는 기록 없음(false)으로 가정)
     */
    private boolean getRecentRecordStatus() {
        return false;
    }

    /**
     * 버튼 클릭 이벤트 처리
     */
    private void setupClickListeners() {

        // '첫 번째 기록 만들기' 버튼 클릭 이벤트
        btnCreateFirstRecord.setOnClickListener(v -> startNewRecordProcess());

        // '새 기록' 버튼 클릭 이벤트
        btnNewRecord.setOnClickListener(v -> startNewRecordProcess());

        // '전체 목록' 버튼 클릭 이벤트
        btnViewAllRecords.setOnClickListener(v -> {
            // MainActivity의 selectMenuItem 함수를 호출하여 목록 탭(R.id.menu_list)으로 전환
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectMenuItem(R.id.menu_list);
            }
        });
    }

    /**
     * 새 기록 생성 로직 (Record Fragment로 이동)
     */
    private void startNewRecordProcess() {
        Toast.makeText(getContext(), "새 기록 기능을 시작합니다. Record 탭으로 이동.", Toast.LENGTH_SHORT).show();

        if (getActivity() instanceof MainActivity) {
            // R.id.menu_record 탭으로 전환
            ((MainActivity) getActivity()).selectMenuItem(R.id.menu_record);
        }
    }
}