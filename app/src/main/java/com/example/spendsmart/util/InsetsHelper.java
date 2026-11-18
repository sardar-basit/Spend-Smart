package com.example.spendsmart.util;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class InsetsHelper {
    private InsetsHelper() {
    }

    public static void applyStatusBarPadding(@NonNull View view) {
        final int left = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int right = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(left, top + bars.top, right, bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    public static void applyNavigationBarPadding(@NonNull View view) {
        final int left = view.getPaddingLeft();
        final int top = view.getPaddingTop();
        final int right = view.getPaddingRight();
        final int bottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(left, top, right, bottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    public static void applyStatusBarMargin(@NonNull View view) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        final int originalTop = params.topMargin;
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = originalTop + bars.top;
            v.setLayoutParams(lp);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}
