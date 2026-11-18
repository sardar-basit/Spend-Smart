package com.example.spendsmart.ui.stats;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.databinding.FragmentStatsBinding;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.DateUtils;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;

public class StatsFragment extends Fragment {
    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(this, new ViewModelFactory(activity.getAppContainer()))
                .get(StatsViewModel.class);

        InsetsHelper.applyStatusBarPadding(binding.appBar);

        binding.btnPrevMonth.setOnClickListener(v -> viewModel.previousMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        viewModel.getUiState().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(StatsUiState state) {
        if (state == null) {
            return;
        }
        binding.textMonth.setText(DateUtils.formatMonthYear(state.selectedMonth));
        binding.textIncomeStats.setText(CurrencyFormatter.format(state.totalIncome));
        binding.textExpenseStats.setText(CurrencyFormatter.format(state.totalExpense));

        double max = Math.max(state.totalIncome, state.totalExpense);
        if (max <= 0) {
            binding.barIncome.setProgress(0);
            binding.barExpense.setProgress(0);
        } else {
            binding.barIncome.setProgress((int) ((state.totalIncome / max) * 100));
            binding.barExpense.setProgress((int) ((state.totalExpense / max) * 100));
        }

        double expenseDelta = state.totalExpense - state.previousExpense;
        double incomeDelta = state.totalIncome - state.previousIncome;
        String comparison = "vs last month: expenses "
                + (expenseDelta >= 0 ? "+" : "")
                + CurrencyFormatter.format(expenseDelta)
                + ", income "
                + (incomeDelta >= 0 ? "+" : "")
                + CurrencyFormatter.format(incomeDelta);
        binding.textComparison.setText(comparison);

        binding.pieChart.setSlices(state.slices);
        binding.weeklyBarChart.setBars(state.weeklyBars);

        binding.insightList.removeAllViews();
        for (String insight : state.insights) {
            TextView row = new TextView(requireContext());
            row.setText("• " + insight);
            row.setTextColor(requireContext().getColor(R.color.on_surface));
            row.setPadding(0, 6, 0, 6);
            binding.insightList.addView(row);
        }

        binding.categoryList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (CategorySlice slice : state.slices) {
            View row = inflater.inflate(R.layout.item_category_slice, binding.categoryList, false);
            View dot = row.findViewById(R.id.colorDot);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(slice.color);
            dot.setBackground(bg);
            ((TextView) row.findViewById(R.id.textCategory)).setText(slice.category);
            ((TextView) row.findViewById(R.id.textAmount))
                    .setText(CurrencyFormatter.format(slice.amount));
            ((TextView) row.findViewById(R.id.textPercent))
                    .setText(((int) (slice.percentage * 100)) + "%");
            binding.categoryList.addView(row);
        }
        if (state.slices.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No expenses this month");
            empty.setPadding(0, 16, 0, 0);
            binding.categoryList.addView(empty);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
