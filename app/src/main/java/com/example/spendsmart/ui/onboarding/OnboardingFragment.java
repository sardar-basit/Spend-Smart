package com.example.spendsmart.ui.onboarding;

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
import com.example.spendsmart.databinding.FragmentOnboardingBinding;
import com.example.spendsmart.util.InsetsHelper;

public class OnboardingFragment extends Fragment {
    private FragmentOnboardingBinding binding;
    private int page;

    private static final int[] TITLES = {
            R.string.onboarding_title_1, R.string.onboarding_title_2, R.string.onboarding_title_3
    };
    private static final int[] BODIES = {
            R.string.onboarding_body_1, R.string.onboarding_body_2, R.string.onboarding_body_3
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InsetsHelper.applyStatusBarPadding(binding.getRoot());
        render();
        binding.btnSkip.setOnClickListener(v -> finishOnboarding());
        binding.btnNext.setOnClickListener(v -> {
            if (page >= TITLES.length - 1) {
                finishOnboarding();
            } else {
                page++;
                render();
            }
        });
    }

    private void render() {
        binding.textTitle.setText(TITLES[page]);
        binding.textBody.setText(BODIES[page]);
        binding.btnNext.setText(page >= TITLES.length - 1 ? R.string.get_started : R.string.next);
    }

    private void finishOnboarding() {
        MainActivity activity = (MainActivity) requireActivity();
        activity.getAppContainer().preferencesManager.setOnboardingDone(true);
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.onboardingFragment, true)
                .build();
        Navigation.findNavController(requireView()).navigate(R.id.loginFragment, null, options);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
