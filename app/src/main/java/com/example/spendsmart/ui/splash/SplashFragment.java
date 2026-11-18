package com.example.spendsmart.ui.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.spendsmart.databinding.FragmentSplashBinding;
import com.example.spendsmart.util.InsetsHelper;

public class SplashFragment extends Fragment {
    private FragmentSplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InsetsHelper.applyStatusBarPadding(binding.getRoot());
        handler.postDelayed(this::routeNext, 1200);
    }

    private void routeNext() {
        if (!isAdded()) {
            return;
        }
        MainActivity activity = (MainActivity) requireActivity();
        PreferencesManager prefs = activity.getAppContainer().preferencesManager;
        SessionManager session = activity.getAppContainer().sessionManager;
        int dest;
        if (!prefs.isOnboardingDone()) {
            dest = R.id.onboardingFragment;
        } else if (prefs.isAppLockEnabled() && !activity.isUnlockedThisSession()) {
            dest = R.id.lockFragment;
        } else if (session.getUserId() != null) {
            dest = R.id.dashboardFragment;
        } else {
            dest = R.id.loginFragment;
        }
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.splashFragment, true)
                .build();
        Navigation.findNavController(requireView()).navigate(dest, null, options);
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        binding = null;
    }
}
