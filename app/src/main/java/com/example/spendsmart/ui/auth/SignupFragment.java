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
import com.example.spendsmart.databinding.FragmentSignupBinding;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;

public class SignupFragment extends Fragment {
    private FragmentSignupBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(activity, new ViewModelFactory(activity.getAppContainer()))
                .get(AuthViewModel.class);

        InsetsHelper.applyStatusBarPadding(binding.getRoot());

        binding.btnSignup.setOnClickListener(v -> viewModel.signUp(
                text(binding.inputName),
                text(binding.inputEmail),
                text(binding.inputPassword),
                text(binding.inputConfirmPassword)
        ));
        binding.linkLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean busy = Boolean.TRUE.equals(loading);
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            binding.btnSignup.setEnabled(!busy);
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
