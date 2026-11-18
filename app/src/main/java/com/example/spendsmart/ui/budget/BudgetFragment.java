package com.example.spendsmart.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.databinding.FragmentBudgetBinding;
import com.example.spendsmart.ui.settings.SettingsViewModel;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {
    private FragmentBudgetBinding binding;
    private BudgetViewModel viewModel;
    private ArrayAdapter<String> categoryAdapter;
    private boolean budgetSeeded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(this, new ViewModelFactory(activity.getAppContainer()))
                .get(BudgetViewModel.class);
        InsetsHelper.applyStatusBarPadding(binding.appBar);

        categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        binding.spinnerCategoryBudget.setAdapter(categoryAdapter);

        binding.btnSaveBudget.setOnClickListener(v -> viewModel.saveBudget(
                text(binding.inputMonthlyBudget), text(binding.inputLegacySavings)));
        binding.btnSaveCategoryBudget.setOnClickListener(v -> {
            Object selected = binding.spinnerCategoryBudget.getSelectedItem();
            viewModel.saveCategoryBudget(
                    selected == null ? null : selected.toString(),
                    text(binding.inputCategoryBudgetLimit));
            binding.inputCategoryBudgetLimit.setText("");
        });
        binding.switchSemesterMode.setOnCheckedChangeListener((button, checked) -> {
            viewModel.setSemesterMode(checked);
            binding.textSemesterHint.setVisibility(checked ? View.VISIBLE : View.GONE);
            binding.textSemesterHint.setText(checked
                    ? "Semester mode on: treat Fees as semester costs; other categories as living costs."
                    : "");
        });

        viewModel.getDailyLimitText().observe(getViewLifecycleOwner(),
                text -> binding.textDailyLimit.setText(text));
        viewModel.getSemesterMode().observe(getViewLifecycleOwner(), enabled ->
                binding.switchSemesterMode.setChecked(Boolean.TRUE.equals(enabled)));
        viewModel.getBudget().observe(getViewLifecycleOwner(), budget -> {
            if (budget != null && !budgetSeeded) {
                budgetSeeded = true;
                if (budget.monthlyLimit > 0) {
                    binding.inputMonthlyBudget.setText(
                            SettingsViewModel.formatNumber(budget.monthlyLimit));
                }
                if (budget.savingsGoal > 0) {
                    binding.inputLegacySavings.setText(
                            SettingsViewModel.formatNumber(budget.savingsGoal));
                }
            }
        });
        viewModel.getExpenseCategories().observe(getViewLifecycleOwner(), cats -> {
            categoryAdapter.clear();
            if (cats != null) {
                categoryAdapter.addAll(cats);
            }
            categoryAdapter.notifyDataSetChanged();
        });
        viewModel.getCategoryBudgets().observe(getViewLifecycleOwner(), this::renderCategoryBudgets);
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                binding.textFeedback.setText(msg);
            }
        });
    }

    private void renderCategoryBudgets(List<CategoryBudget> budgets) {
        binding.categoryBudgetList.removeAllViews();
        if (budgets == null) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (CategoryBudget budget : budgets) {
            View row = inflater.inflate(R.layout.item_category_budget,
                    binding.categoryBudgetList, false);
            ((TextView) row.findViewById(R.id.textName)).setText(budget.category);
            ((TextView) row.findViewById(R.id.textLimit))
                    .setText("Limit: " + CurrencyFormatter.format(budget.monthlyLimit));
            ((ImageButton) row.findViewById(R.id.btnDelete))
                    .setOnClickListener(v -> viewModel.deleteCategoryBudget(budget));
            binding.categoryBudgetList.addView(row);
        }
    }

    private static String text(com.google.android.material.textfield.TextInputEditText edit) {
        return edit.getText() == null ? "" : edit.getText().toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
