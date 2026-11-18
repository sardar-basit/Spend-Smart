package com.example.spendsmart.ui.savings;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.data.local.entity.SavingsGoal;
import com.example.spendsmart.databinding.FragmentSavingsBinding;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class SavingsFragment extends Fragment {
    private FragmentSavingsBinding binding;
    private SavingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSavingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(this, new ViewModelFactory(activity.getAppContainer()))
                .get(SavingsViewModel.class);
        InsetsHelper.applyStatusBarPadding(binding.appBar);
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        binding.btnAddGoal.setOnClickListener(v -> {
            viewModel.addGoal(
                    text(binding.inputGoalTitle),
                    text(binding.inputGoalTarget),
                    text(binding.inputGoalSaved));
            binding.inputGoalTitle.setText("");
            binding.inputGoalTarget.setText("");
            binding.inputGoalSaved.setText("");
        });

        viewModel.getGoals().observe(getViewLifecycleOwner(), this::renderGoals);
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderGoals(List<SavingsGoal> goals) {
        binding.goalsList.removeAllViews();
        if (goals == null || goals.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No savings goals yet. Add one like “Save for a laptop”.");
            empty.setPadding(0, 16, 0, 0);
            binding.goalsList.addView(empty);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SavingsGoal goal : goals) {
            View row = inflater.inflate(R.layout.item_savings_goal, binding.goalsList, false);
            ((TextView) row.findViewById(R.id.textTitle)).setText(goal.title);
            ((TextView) row.findViewById(R.id.textProgress)).setText(
                    CurrencyFormatter.format(goal.savedAmount)
                            + " / " + CurrencyFormatter.format(goal.targetAmount));
            LinearProgressIndicator progress = row.findViewById(R.id.progress);
            int pct = goal.targetAmount <= 0 ? 0
                    : (int) Math.min(100, (goal.savedAmount / goal.targetAmount) * 100);
            progress.setProgress(pct);
            ((MaterialButton) row.findViewById(R.id.btnAddMoney)).setOnClickListener(v ->
                    promptAddMoney(goal));
            ((MaterialButton) row.findViewById(R.id.btnDelete)).setOnClickListener(v ->
                    viewModel.delete(goal));
            binding.goalsList.addView(row);
        }
    }

    private void promptAddMoney(SavingsGoal goal) {
        EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Amount");
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_to_savings)
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    try {
                        double amount = Double.parseDouble(input.getText().toString().trim());
                        viewModel.addMoney(goal, amount);
                    } catch (Exception ignored) {
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
