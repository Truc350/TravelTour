package com.example.myapplication;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment hiển thị nội dung tạm thời cho các tab chưa phát triển đầy đủ.
 */
public class PlaceholderFragment extends Fragment {

    public static PlaceholderFragment newInstance(String title) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String title = "";
        if (getArguments() != null) {
            title = getArguments().getString("title", "");
        }

        // Tạo TextView hiển thị tên tab
        TextView textView = new TextView(getContext());
        textView.setText(title);
        textView.setTextSize(20f);
        textView.setTextColor(0xFF333333);
        textView.setGravity(Gravity.CENTER);

        // Tạo container bọc ngoài
        FrameLayout layout = new FrameLayout(getContext());
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        layout.addView(textView);

        return layout;
    }
}
