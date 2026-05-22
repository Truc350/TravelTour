package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Bottom Sheet hiển thị Lịch khởi hành dưới dạng GridView.
 * Hỗ trợ chuyển đổi tháng linh hoạt, bôi màu hôm nay, đánh dấu ngày chọn và khóa các ngày quá khứ.
 */
public class CalendarBottomSheet extends BottomSheetDialogFragment {

    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;

    private int activeMonth;
    private int activeYear;

    private TextView tvMonthYear;
    private GridView gvCalendar;
    private List<Integer> daysList = new ArrayList<>();
    private CalendarAdapter calendarAdapter;

    public static CalendarBottomSheet newInstance(int day, int month, int year) {
        CalendarBottomSheet fragment = new CalendarBottomSheet();
        Bundle args = new Bundle();
        args.putInt("selected_day", day);
        args.putInt("selected_month", month);
        args.putInt("selected_year", year);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDay = getArguments().getInt("selected_day", 23);
            selectedMonth = getArguments().getInt("selected_month", Calendar.MAY);
            selectedYear = getArguments().getInt("selected_year", 2026);
        } else {
            selectedDay = 23;
            selectedMonth = Calendar.MAY;
            selectedYear = 2026;
        }
        activeMonth = selectedMonth;
        activeYear = selectedYear;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_calendar, container, false);

        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        gvCalendar = view.findViewById(R.id.gvCalendar);
        ImageView btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        ImageView btnNextMonth = view.findViewById(R.id.btnNextMonth);

        calendarAdapter = new CalendarAdapter();
        if (gvCalendar != null) {
            gvCalendar.setAdapter(calendarAdapter);
        }

        // Chuyển sang tháng trước
        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                activeMonth--;
                if (activeMonth < 0) {
                    activeMonth = 11;
                    activeYear--;
                }
                updateCalendarGrid();
            });
        }

        // Chuyển sang tháng sau
        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                activeMonth++;
                if (activeMonth > 11) {
                    activeMonth = 0;
                    activeYear++;
                }
                updateCalendarGrid();
            });
        }

        // Cập nhật giao diện lịch lần đầu
        updateCalendarGrid();

        return view;
    }

    private void updateCalendarGrid() {
        daysList.clear();

        if (tvMonthYear != null) {
            tvMonthYear.setText("Tháng " + (activeMonth + 1) + " " + activeYear);
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, activeYear);
        cal.set(Calendar.MONTH, activeMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        // Lấy ngày trong tuần của ngày đầu tiên trong tháng
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Chủ nhật = 1, Thứ hai = 2...

        // Chuyển đổi sang chỉ mục bắt đầu từ Thứ Hai: Thứ Hai = 0, Thứ Ba = 1, ..., Chủ Nhật = 6
        int leadingBlanks = (firstDayOfWeek + 5) % 7;

        // Số ngày tối đa của tháng hiện tại
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Thêm các ô trống ở đầu tuần
        for (int i = 0; i < leadingBlanks; i++) {
            daysList.add(0);
        }

        // Thêm các ngày thực tế của tháng vào danh sách
        for (int i = 1; i <= maxDays; i++) {
            daysList.add(i);
        }

        if (calendarAdapter != null) {
            calendarAdapter.notifyDataSetChanged();
        }
    }

    private class CalendarAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return daysList.size();
        }

        @Override
        public Object getItem(int position) {
            return daysList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_calendar_day, parent, false);
            }

            TextView tvDay = itemView.findViewById(R.id.tvDay);
            Integer day = daysList.get(position);

            if (tvDay != null) {
                if (day == 0) {
                    tvDay.setText("");
                    tvDay.setBackground(null);
                    itemView.setClickable(false);
                    itemView.setEnabled(false);
                } else {
                    tvDay.setText(String.valueOf(day));
                    tvDay.setBackground(null); // Reset background

                    // Tạo lịch so sánh ngày
                    Calendar cellCal = Calendar.getInstance();
                    cellCal.set(Calendar.YEAR, activeYear);
                    cellCal.set(Calendar.MONTH, activeMonth);
                    cellCal.set(Calendar.DAY_OF_MONTH, day);
                    zeroTime(cellCal);

                    // Lấy ngày hôm nay
                    Calendar todayCal = Calendar.getInstance();
                    zeroTime(todayCal);

                    if (cellCal.before(todayCal)) {
                        // Ngày quá khứ -> mờ và tắt click
                        tvDay.setTextColor(Color.parseColor("#CBD5E1"));
                        itemView.setClickable(false);
                        itemView.setEnabled(false);
                    } else if (activeYear == selectedYear && activeMonth == selectedMonth && day == selectedDay) {
                        // Ngày đang được chọn -> chữ trắng, nền tròn cyan
                        tvDay.setTextColor(Color.WHITE);
                        tvDay.setBackgroundResource(R.drawable.bg_circle_cyan);
                        itemView.setClickable(true);
                        itemView.setEnabled(true);
                    } else if (cellCal.equals(todayCal)) {
                        // Ngày hôm nay -> chữ cyan đậm
                        tvDay.setTextColor(Color.parseColor("#00B4D8"));
                        itemView.setClickable(true);
                        itemView.setEnabled(true);
                    } else {
                        // Ngày tương lai thông thường -> chữ đen
                        tvDay.setTextColor(Color.parseColor("#1A202C"));
                        itemView.setClickable(true);
                        itemView.setEnabled(true);
                    }

                    // Sự kiện khi chọn một ngày hợp lệ
                    itemView.setOnClickListener(v -> {
                        Bundle result = new Bundle();
                        result.putInt("day", day);
                        result.putInt("month", activeMonth);
                        result.putInt("year", activeYear);
                        getParentFragmentManager().setFragmentResult("date_request", result);
                        dismiss();
                    });
                }
            }

            return itemView;
        }

        private void zeroTime(Calendar c) {
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
        }
    }
}
