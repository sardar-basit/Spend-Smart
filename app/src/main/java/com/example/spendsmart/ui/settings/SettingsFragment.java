package com.example.spendsmart.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.data.local.entity.CategoryBudget;
import com.example.spendsmart.data.local.entity.CustomCategory;
import com.example.spendsmart.data.local.entity.RecurringTransaction;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.data.preferences.ThemeMode;
import com.example.spendsmart.databinding.FragmentSettingsBinding;
import com.example.spendsmart.domain.model.AuthState;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.DateUtils;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private boolean budgetSeeded;
    private ArrayAdapter<String> categoryBudgetAdapter;
    private ArrayAdapter<String> recurringCategoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        InsetsHelper.applyStatusBarPadding(binding.appBar);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(activity.getAppContainer()))
                .get(SettingsViewModel.class);

        categoryBudgetAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        binding.spinnerCategoryBudget.setAdapter(categoryBudgetAdapter);
        recurringCategoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        binding.spinnerRecurringCategory.setAdapter(recurringCategoryAdapter);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        binding.btnOpenBudget.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_settings_to_budget));
        binding.btnOpenSavings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_settings_to_savings));
        binding.btnOpenSplit.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_settings_to_split));
        binding.btnSetPin.setOnClickListener(v -> showSetPinDialog());
        binding.switchAppLock.setChecked(viewModel.isAppLockEnabled());
        binding.switchAppLock.setOnCheckedChangeListener((button, checked) -> {
            if (checked && !viewModel.isAppLockEnabled()) {
                showSetPinDialog();
                binding.switchAppLock.setChecked(viewModel.isAppLockEnabled());
            } else if (!checked) {
                viewModel.setAppLockEnabled(false);
            }
        });

        binding.btnSaveProfile.setOnClickListener(v ->
                viewModel.saveProfile(text(binding.inputName)));
        binding.btnChangePassword.setOnClickListener(v -> {
            viewModel.changePassword(
                    text(binding.inputCurrentPassword),
                    text(binding.inputNewPassword));
            binding.inputCurrentPassword.setText("");
            binding.inputNewPassword.setText("");
        });
        binding.btnSaveBudget.setOnClickListener(v -> viewModel.saveBudget(
                text(binding.inputMonthlyBudget),
                text(binding.inputSavingsGoal)));
        binding.btnSaveCategoryBudget.setOnClickListener(v -> {
            Object selected = binding.spinnerCategoryBudget.getSelectedItem();
            viewModel.saveCategoryBudget(
                    selected == null ? null : selected.toString(),
                    text(binding.inputCategoryBudgetLimit));
            binding.inputCategoryBudgetLimit.setText("");
        });
        binding.btnAddRecurring.setOnClickListener(v -> {
            Object selected = binding.spinnerRecurringCategory.getSelectedItem();
            viewModel.addRecurring(
                    text(binding.inputRecurringAmount),
                    selected == null ? null : selected.toString(),
                    binding.radioRecurringIncome.isChecked(),
                    binding.radioFreqWeekly.isChecked(),
                    text(binding.inputRecurringNote));
            binding.inputRecurringAmount.setText("");
            binding.inputRecurringNote.setText("");
        });
        binding.btnAddCategory.setOnClickListener(v -> {
            TransactionType type = binding.radioCatIncome.isChecked()
                    ? TransactionType.INCOME : TransactionType.EXPENSE;
            viewModel.addCategory(text(binding.inputCategoryName), type);
            binding.inputCategoryName.setText("");
        });
        binding.btnExportCsv.setOnClickListener(v -> viewModel.exportCsv());
        binding.btnExportPdf.setOnClickListener(v -> {
            AuthState state = viewModel.getAuthState().getValue();
            String name = state != null && state.user != null ? state.user.name : "Student";
            viewModel.exportPdf(requireContext().getApplicationContext(), name);
        });
        binding.btnLogout.setOnClickListener(v -> viewModel.logout());

        binding.radioCurrency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioUsd) {
                viewModel.setCurrency("USD");
            } else if (checkedId == R.id.radioPkr) {
                viewModel.setCurrency("PKR");
            }
        });
        binding.radioTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioThemeLight) {
                viewModel.setThemeMode(ThemeMode.LIGHT);
            } else if (checkedId == R.id.radioThemeDark) {
                viewModel.setThemeMode(ThemeMode.DARK);
            } else if (checkedId == R.id.radioThemeSystem) {
                viewModel.setThemeMode(ThemeMode.SYSTEM);
            }
        });
        binding.switchBudgetAlerts.setOnCheckedChangeListener((button, isChecked) ->
                viewModel.setBudgetAlerts(isChecked));
        binding.radioRecurringType.setOnCheckedChangeListener((group, checkedId) ->
                refreshRecurringCategories());

        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state.status == AuthState.Status.LOGGED_IN && state.user != null) {
                binding.textEmail.setText(state.user.email);
                if (text(binding.inputName).isEmpty()) {
                    binding.inputName.setText(state.user.name);
                }
            }
        });
        viewModel.getBudget().observe(getViewLifecycleOwner(), budget -> {
            if (budget != null && !budgetSeeded) {
                budgetSeeded = true;
                if (budget.monthlyLimit > 0) {
                    binding.inputMonthlyBudget.setText(
                            SettingsViewModel.formatNumber(budget.monthlyLimit));
                }
                if (budget.savingsGoal > 0) {
                    binding.inputSavingsGoal.setText(
                            SettingsViewModel.formatNumber(budget.savingsGoal));
                }
            }
        });
        viewModel.getExpenseCategories().observe(getViewLifecycleOwner(), cats -> {
            categoryBudgetAdapter.clear();
            if (cats != null) {
                categoryBudgetAdapter.addAll(cats);
            } else {
                categoryBudgetAdapter.addAll(viewModel.defaultExpenseCategories());
            }
            categoryBudgetAdapter.notifyDataSetChanged();
            refreshRecurringCategories();
        });
        viewModel.getCategoryBudgets().observe(getViewLifecycleOwner(), this::renderCategoryBudgets);
        viewModel.getRecurring().observe(getViewLifecycleOwner(), this::renderRecurring);
        viewModel.getCurrency().observe(getViewLifecycleOwner(), code -> {
            if ("USD".equalsIgnoreCase(code)) {
                binding.radioUsd.setChecked(true);
            } else {
                binding.radioPkr.setChecked(true);
            }
        });
        viewModel.getThemeMode().observe(getViewLifecycleOwner(), mode -> {
            if (mode == ThemeMode.LIGHT) {
                binding.radioThemeLight.setChecked(true);
            } else if (mode == ThemeMode.DARK) {
                binding.radioThemeDark.setChecked(true);
            } else {
                binding.radioThemeSystem.setChecked(true);
            }
        });
        viewModel.getBudgetAlerts().observe(getViewLifecycleOwner(), enabled ->
                binding.switchBudgetAlerts.setChecked(Boolean.TRUE.equals(enabled)));
        viewModel.getCustomCategories().observe(getViewLifecycleOwner(), this::renderCategories);
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                binding.textFeedback.setText(msg);
                binding.textFeedback.setTextColor(
                        requireContext().getColor(R.color.teal_primary));
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                binding.textFeedback.setText(err);
                binding.textFeedback.setTextColor(
                        requireContext().getColor(R.color.expense_red));
            }
        });
        viewModel.getExportCsv().observe(getViewLifecycleOwner(), csv -> {
            if (csv != null) {
                shareCsv(csv);
                viewModel.consumeExport();
            }
        });
        viewModel.getExportPdf().observe(getViewLifecycleOwner(), file -> {
            if (file != null) {
                sharePdf(file);
                viewModel.consumePdfExport();
            }
        });
    }

    private void refreshRecurringCategories() {
        boolean income = binding.radioRecurringIncome.isChecked();
        List<String> cats = income
                ? com.example.spendsmart.domain.model.Categories.INCOME
                : (categoryBudgetAdapter.getCount() > 0
                ? adapterItems(categoryBudgetAdapter)
                : viewModel.defaultExpenseCategories());
        recurringCategoryAdapter.clear();
        recurringCategoryAdapter.addAll(cats);
        recurringCategoryAdapter.notifyDataSetChanged();
    }

    private static List<String> adapterItems(ArrayAdapter<String> adapter) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
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
            ImageButton delete = row.findViewById(R.id.btnDelete);
            delete.setOnClickListener(v -> viewModel.deleteCategoryBudget(budget));
            binding.categoryBudgetList.addView(row);
        }
    }

    private void renderRecurring(List<RecurringTransaction> items) {
        binding.recurringList.removeAllViews();
        if (items == null) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (RecurringTransaction item : items) {
            View row = inflater.inflate(R.layout.item_recurring, binding.recurringList, false);
            String title = item.category + " · "
                    + (item.type == TransactionType.INCOME ? "+" : "-")
                    + CurrencyFormatter.format(item.amount);
            ((TextView) row.findViewById(R.id.textTitle)).setText(title);
            String meta = item.frequency.name()
                    + " · "
                    + getString(R.string.next_run, DateUtils.formatDisplayDate(item.nextRunAt));
            ((TextView) row.findViewById(R.id.textMeta)).setText(meta);
            ImageButton delete = row.findViewById(R.id.btnDelete);
            delete.setOnClickListener(v -> viewModel.deleteRecurring(item));
            binding.recurringList.addView(row);
        }
    }

    private void renderCategories(List<CustomCategory> categories) {
        binding.customCategoryList.removeAllViews();
        if (categories == null) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (CustomCategory category : categories) {
            View row = inflater.inflate(R.layout.item_custom_category,
                    binding.customCategoryList, false);
            ((TextView) row.findViewById(R.id.textName)).setText(category.name);
            ((TextView) row.findViewById(R.id.textType)).setText(category.type.name());
            ImageButton delete = row.findViewById(R.id.btnDelete);
            delete.setOnClickListener(v -> viewModel.deleteCategory(category));
            binding.customCategoryList.addView(row);
        }
    }

    private void showSetPinDialog() {
        LinearLayout box = new LinearLayout(requireContext());
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, 0);
        EditText pin = new EditText(requireContext());
        pin.setHint(R.string.enter_pin);
        pin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        EditText confirm = new EditText(requireContext());
        confirm.setHint(R.string.confirm_pin);
        confirm.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        box.addView(pin);
        box.addView(confirm);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.set_pin)
                .setView(box)
                .setPositiveButton("Save", (d, w) -> {
                    viewModel.setPin(pin.getText().toString().trim(),
                            confirm.getText().toString().trim());
                    binding.switchAppLock.setChecked(viewModel.isAppLockEnabled());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareCsv(String csv) {
        try {
            File file = new File(requireContext().getCacheDir(), "spendsmart_export.csv");
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(csv.getBytes(StandardCharsets.UTF_8));
            }
            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file
            );
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Share CSV"));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePdf(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    file
            );
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Share PDF report"));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "PDF share failed", Toast.LENGTH_SHORT).show();
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
