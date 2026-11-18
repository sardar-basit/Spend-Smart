package com.example.spendsmart.ui.split;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.data.local.entity.SplitBill;
import com.example.spendsmart.databinding.FragmentSplitBillsBinding;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SplitBillsFragment extends Fragment {
    private FragmentSplitBillsBinding binding;
    private SplitBillsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSplitBillsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(this, new ViewModelFactory(activity.getAppContainer()))
                .get(SplitBillsViewModel.class);
        InsetsHelper.applyStatusBarPadding(binding.appBar);
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        binding.btnAddSplit.setOnClickListener(v -> {
            viewModel.add(
                    text(binding.inputTitle),
                    text(binding.inputFriend),
                    text(binding.inputTotal),
                    text(binding.inputShare),
                    binding.radioFriendOwes.isChecked());
            binding.inputTitle.setText("");
            binding.inputFriend.setText("");
            binding.inputTotal.setText("");
            binding.inputShare.setText("");
        });

        viewModel.getBills().observe(getViewLifecycleOwner(), this::render);
        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render(List<SplitBill> bills) {
        binding.splitList.removeAllViews();
        if (bills == null || bills.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("No split bills yet. Track money friends owe you.");
            empty.setPadding(0, 16, 0, 0);
            binding.splitList.addView(empty);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (SplitBill bill : bills) {
            View row = inflater.inflate(R.layout.item_split_bill, binding.splitList, false);
            ((TextView) row.findViewById(R.id.textTitle)).setText(bill.title
                    + (bill.settled ? " ✓" : ""));
            double owed = Math.max(0, bill.totalAmount - bill.yourShare);
            String meta = bill.friendName + " · total "
                    + CurrencyFormatter.format(bill.totalAmount)
                    + " · "
                    + (bill.friendOwesYou
                    ? "owes you " + CurrencyFormatter.format(owed)
                    : "you owe " + CurrencyFormatter.format(owed));
            ((TextView) row.findViewById(R.id.textMeta)).setText(meta);
            MaterialButton settle = row.findViewById(R.id.btnSettle);
            settle.setText(bill.settled ? R.string.mark_unsettled : R.string.mark_settled);
            settle.setOnClickListener(v -> viewModel.toggleSettled(bill));
            ((MaterialButton) row.findViewById(R.id.btnDelete))
                    .setOnClickListener(v -> viewModel.delete(bill));
            binding.splitList.addView(row);
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
