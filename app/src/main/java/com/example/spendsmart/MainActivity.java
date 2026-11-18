package com.example.spendsmart;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.spendsmart.data.preferences.ThemeMode;
import com.example.spendsmart.databinding.ActivityMainBinding;
import com.example.spendsmart.di.AppContainer;
import com.example.spendsmart.domain.model.AuthState;
import com.example.spendsmart.ui.auth.AuthViewModel;
import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.viewmodel.ViewModelFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;
    private boolean unlockedThisSession;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppContainer container = ((SpendSmartApp) getApplication()).getContainer();
        applyTheme(container.preferencesManager.getThemeMode());
        CurrencyFormatter.setActiveCurrency(container.preferencesManager.getCurrencyCode());

        authViewModel = new ViewModelProvider(this, new ViewModelFactory(container))
                .get(AuthViewModel.class);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHost == null) {
            throw new IllegalStateException("NavHostFragment missing");
        }
        navController = navHost.getNavController();

        BottomNavigationView bottomNav = binding.bottomNav;
        NavigationUI.setupWithNavController(bottomNav, navController);
        InsetsHelper.applyNavigationBarPadding(bottomNav);

        Set<Integer> topLevel = new HashSet<>();
        topLevel.add(R.id.dashboardFragment);
        topLevel.add(R.id.historyFragment);
        topLevel.add(R.id.budgetFragment);
        topLevel.add(R.id.statsFragment);

        Set<Integer> lightStatusScreens = new HashSet<>();
        lightStatusScreens.add(R.id.loginFragment);
        lightStatusScreens.add(R.id.signupFragment);
        lightStatusScreens.add(R.id.onboardingFragment);
        lightStatusScreens.add(R.id.lockFragment);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean show = topLevel.contains(destination.getId());
            bottomNav.setVisibility(show ? View.VISIBLE : View.GONE);
            boolean authScreen = lightStatusScreens.contains(destination.getId());
            boolean nightMode = (getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            WindowInsetsControllerCompat insetsController =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            insetsController.setAppearanceLightStatusBars(authScreen && !nightMode);
            insetsController.setAppearanceLightNavigationBars(!nightMode);
        });

        authViewModel.getAuthState().observe(this, this::onAuthState);
        container.preferencesManager.getThemeModeLive().observe(this, this::applyTheme);
        container.preferencesManager.getCurrencyCodeLive().observe(this, CurrencyFormatter::setActiveCurrency);

        requestNotificationPermission();
        handleQuickAddIntent(getIntent());
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleQuickAddIntent(intent);
    }

    private void handleQuickAddIntent(android.content.Intent intent) {
        if (intent == null || !intent.getBooleanExtra(
                com.example.spendsmart.widget.QuickAddWidgetProvider.EXTRA_OPEN_ADD, false)) {
            return;
        }
        intent.removeExtra(com.example.spendsmart.widget.QuickAddWidgetProvider.EXTRA_OPEN_ADD);
        binding.getRoot().post(() -> {
            if (navController == null) {
                return;
            }
            if (getAppContainer().sessionManager.getUserId() != null) {
                navController.navigate(R.id.addTransactionFragment);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (authViewModel != null && authViewModel.getAuthState().getValue() != null
                && authViewModel.getAuthState().getValue().isLoggedIn()) {
            getAppContainer().recurringTransactionRepository.processDueForCurrentUser(null);
        }
    }

    public void setUnlockedThisSession(boolean unlocked) {
        unlockedThisSession = unlocked;
    }

    public boolean isUnlockedThisSession() {
        return unlockedThisSession;
    }

    private void onAuthState(AuthState state) {
        if (state == null || state.status == AuthState.Status.LOADING) {
            return;
        }
        int current = navController.getCurrentDestination() != null
                ? navController.getCurrentDestination().getId() : 0;
        if (current == R.id.splashFragment
                || current == R.id.onboardingFragment
                || current == R.id.lockFragment) {
            return;
        }
        NavOptions clearStack = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build();
        if (state.isLoggedIn()) {
            if (current == R.id.loginFragment || current == R.id.signupFragment || current == 0) {
                navController.navigate(R.id.dashboardFragment, null, clearStack);
            }
        } else {
            if (current != R.id.loginFragment && current != R.id.signupFragment) {
                navController.navigate(R.id.loginFragment, null, clearStack);
            }
        }
    }

    private void applyTheme(ThemeMode mode) {
        if (mode == null) {
            mode = ThemeMode.SYSTEM;
        }
        switch (mode) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    public AppContainer getAppContainer() {
        return ((SpendSmartApp) getApplication()).getContainer();
    }
}
