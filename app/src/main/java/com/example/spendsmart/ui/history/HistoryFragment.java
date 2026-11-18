package com.example.spendsmart.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.data.local.entity.Transaction;
import com.example.spendsmart.databinding.FragmentHistoryBinding;
import com.example.spendsmart.ui.adapter.TransactionAdapter;
import com.example.spendsmart.util.DateUtils;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private TransactionAdapter adapter;
    private List<String> categories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(this, new ViewModelFactory(activity.getAppContainer()))
                .get(HistoryViewModel.class);

        InsetsHelper.applyStatusBarPadding(binding.appBar);

        adapter = new TransactionAdapter(tx -> {
            Bundle args = new Bundle();
            args.putInt("transactionId", tx.id);
            Navigation.findNavController(view).navigate(R.id.action_history_to_add, args);
        });
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHistory.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Transaction tx = adapter.getItem(viewHolder.getBindingAdapterPosition());
                viewModel.deleteTransaction(tx);
                Snackbar.make(binding.getRoot(), "Deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> viewModel.undoDelete())
                        .show();
            }
        }).attachToRecyclerView(binding.recyclerHistory);

        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.chipAll.setOnClickListener(v -> viewModel.setTypeFilter(TypeFilter.ALL));
        binding.chipIncome.setOnClickListener(v -> viewModel.setTypeFilter(TypeFilter.INCOME));
        binding.chipExpense.setOnClickListener(v -> viewModel.setTypeFilter(TypeFilter.EXPENSE));

        binding.btnMonth.setOnClickListener(v -> showMonthPicker());
        binding.btnCategory.setOnClickListener(v -> showCategoryPicker());
        binding.btnClearFilters.setOnClickListener(v -> {
            viewModel.setMonthFilter(null);
            viewModel.setCategoryFilter(null);
            viewModel.setTypeFilter(TypeFilter.ALL);
            viewModel.setSearchQuery("");
            viewModel.clearAmountFilters();
            binding.inputSearch.setText("");
            binding.inputMinAmount.setText("");
            binding.inputMaxAmount.setText("");
            binding.chipAll.setChecked(true);
            binding.btnMonth.setText(R.string.filter_month);
            binding.btnCategory.setText(R.string.category);
        });

        binding.inputMinAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setMinAmount(parseAmount(s));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.inputMaxAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setMaxAmount(parseAmount(s));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        viewModel.getTransactions().observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            binding.textEmpty.setVisibility(
                    list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getCategories().observe(getViewLifecycleOwner(), cats ->
                categories = cats != null ? cats : new ArrayList<>());
        viewModel.getMonthFilter().observe(getViewLifecycleOwner(), ym -> {
            if (ym == null) {
                binding.btnMonth.setText(R.string.filter_month);
            } else {
                binding.btnMonth.setText(DateUtils.formatMonthYear(ym));
            }
        });
        viewModel.getCategoryFilter().observe(getViewLifecycleOwner(), cat -> {
            if (cat == null) {
                binding.btnCategory.setText(R.string.category);
            } else {
                binding.btnCategory.setText(cat);
            }
        });
    }

    private void showMonthPicker() {
        YearMonth now = YearMonth.now();
        String[] options = new String[7];
        YearMonth[] values = new YearMonth[7];
        options[0] = "All months";
        values[0] = null;
        for (int i = 0; i < 6; i++) {
            YearMonth ym = now.minusMonths(i);
            options[i + 1] = DateUtils.formatMonthYear(ym);
            values[i + 1] = ym;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by month")
                .setItems(options, (d, which) -> viewModel.setMonthFilter(values[which]))
                .show();
    }

    private static Double parseAmount(CharSequence s) {
        if (s == null || s.toString().trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(s.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void showCategoryPicker() {
        List<String> options = new ArrayList<>();
        options.add("All categories");
        options.addAll(categories);
        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by category")
                .setAdapter(new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_list_item_1, options),
                        (d, which) -> {
                            if (which == 0) {
                                viewModel.setCategoryFilter(null);
                            } else {
                                viewModel.setCategoryFilter(options.get(which));
                            }
                        })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
