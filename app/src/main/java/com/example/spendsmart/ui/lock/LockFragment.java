package com.example.spendsmart.ui.lock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.data.preferences.PreferencesManager;
import com.example.spendsmart.data.session.SessionManager;
import com.example.spendsmart.databinding.FragmentLockBinding;
import com.example.spendsmart.util.InsetsHelper;

public class LockFragment extends Fragment {
    private FragmentLockBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLockBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InsetsHelper.applyStatusBarPadding(binding.getRoot());
        MainActivity activity = (MainActivity) requireActivity();
        PreferencesManager prefs = activity.getAppContainer().preferencesManager;
        SessionManager session = activity.getAppContainer().sessionManager;

        binding.btnUnlock.setOnClickListener(v -> {
            String pin = binding.inputPin.getText() == null
                    ? "" : binding.inputPin.getText().toString().trim();
            if (prefs.verifyPin(pin)) {
                activity.setUnlockedThisSession(true);
                int dest = session.getUserId() != null ? R.id.dashboardFragment : R.id.loginFragment;
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.lockFragment, true)
                        .build();
                Navigation.findNavController(view).navigate(dest, null, options);
            } else {
                binding.textError.setVisibility(View.VISIBLE);
                binding.textError.setText(R.string.wrong_pin);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
