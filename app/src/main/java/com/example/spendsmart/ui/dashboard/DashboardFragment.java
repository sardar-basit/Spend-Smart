package com.example.spendsmart.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.databinding.FragmentDashboardBinding;
import com.example.spendsmart.domain.model.AuthState;
import com.example.spendsmart.ui.adapter.TransactionAdapter;
import com.example.spendsmart.ui.auth.AuthViewModel;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private AuthViewModel authViewModel;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        ViewModelFactory factory = new ViewModelFactory(activity.getAppContainer());
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);
        authViewModel = new ViewModelProvider(activity, factory).get(AuthViewModel.class);

        adapter = new TransactionAdapter(tx -> {
            Bundle args = new Bundle();
            args.putInt("transactionId", tx.id);
            Navigation.findNavController(view).navigate(R.id.addTransactionFragment, args);
        });
        binding.recyclerRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRecent.setAdapter(adapter);

        InsetsHelper.applyStatusBarPadding(binding.appBar);

        binding.fabAdd.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_add));
        binding.btnOpenSavings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_savings));
        binding.btnOpenSplit.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_split));

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_settings);
                return true;
            }
            if (id == R.id.action_logout) {
                authViewModel.logout();
                return true;
            }
            return false;
        });

        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state != null && state.status == AuthState.Status.LOGGED_IN && state.user != null) {
                binding.textGreeting.setText("Hi, " + state.user.name);
            }
        });

        viewModel.getUiState().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(DashboardUiState state) {
        if (state == null) {
            return;
        }
        binding.textBalance.setText(CurrencyFormatter.format(state.balance));
        binding.textIncome.setText(CurrencyFormatter.format(state.totalIncome));
        binding.textExpense.setText(CurrencyFormatter.format(state.totalExpense));
        binding.textDailyLimit.setText(state.dailyLimitText);

        if (state.monthlyLimit > 0) {
            int pct = (int) Math.min(100, (state.totalExpense / state.monthlyLimit) * 100);
            binding.progressBudget.setProgress(pct);
            binding.textBudget.setText(CurrencyFormatter.format(state.totalExpense)
                    + " / " + CurrencyFormatter.format(state.monthlyLimit));
        } else {
            binding.progressBudget.setProgress(0);
            binding.textBudget.setText("Set a monthly budget in Settings");
        }

        if (state.savingsGoal > 0) {
            double saved = Math.max(0, state.totalIncome - state.totalExpense);
            int pct = (int) Math.min(100, (saved / state.savingsGoal) * 100);
            binding.progressSavings.setProgress(pct);
            binding.textSavings.setText(CurrencyFormatter.format(saved)
                    + " / " + CurrencyFormatter.format(state.savingsGoal));
        } else {
            binding.progressSavings.setProgress(0);
            binding.textSavings.setText("Set a savings goal in Settings");
        }

        adapter.submit(state.recentTransactions);
        binding.textEmpty.setVisibility(
                state.recentTransactions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
