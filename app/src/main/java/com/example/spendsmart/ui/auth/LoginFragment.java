package com.example.spendsmart.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.databinding.FragmentLoginBinding;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(activity, new ViewModelFactory(activity.getAppContainer()))
                .get(AuthViewModel.class);

        InsetsHelper.applyStatusBarPadding(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> viewModel.login(
                text(binding.inputEmail),
                text(binding.inputPassword)
        ));
        binding.linkSignup.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_signup));

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean busy = Boolean.TRUE.equals(loading);
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!busy);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) {
                binding.textError.setVisibility(View.GONE);
            } else {
                binding.textError.setVisibility(View.VISIBLE);
                binding.textError.setText(error);
            }
        });
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
