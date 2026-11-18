package com.example.spendsmart.ui.addtransaction;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.spendsmart.MainActivity;
import com.example.spendsmart.R;
import com.example.spendsmart.data.local.entity.PaymentMethod;
import com.example.spendsmart.data.local.entity.TransactionType;
import com.example.spendsmart.databinding.FragmentAddTransactionBinding;
import com.example.spendsmart.util.DateUtils;
import com.example.spendsmart.util.InsetsHelper;
import com.example.spendsmart.util.ReceiptStore;
import com.example.spendsmart.viewmodel.ViewModelFactory;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTransactionFragment extends Fragment {
    private FragmentAddTransactionBinding binding;
    private AddTransactionViewModel viewModel;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> paymentAdapter;
    private boolean suppressCategoryCallback;
    private boolean suppressPaymentCallback;
    private Uri cameraUri;
    private File cameraFile;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (Boolean.TRUE.equals(granted)) {
                    launchCamera();
                } else {
                    Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT)
                            .show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success) && cameraFile != null && cameraFile.exists()) {
                    viewModel.setReceiptPath(cameraFile.getAbsolutePath());
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                String path = ReceiptStore.copyToReceipts(requireContext(), uri);
                if (path != null) {
                    viewModel.setReceiptPath(path);
                } else {
                    Toast.makeText(requireContext(), "Could not attach receipt", Toast.LENGTH_SHORT)
                            .show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int transactionId = getArguments() != null
                ? getArguments().getInt("transactionId", AddTransactionViewModel.NO_ID)
                : AddTransactionViewModel.NO_ID;

        MainActivity activity = (MainActivity) requireActivity();
        viewModel = new ViewModelProvider(this,
                new ViewModelFactory(activity.getAppContainer(), transactionId))
                .get(AddTransactionViewModel.class);

        InsetsHelper.applyStatusBarPadding(binding.appBar);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        binding.spinnerCategory.setAdapter(categoryAdapter);
        paymentAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, paymentLabels());
        binding.spinnerPayment.setAdapter(paymentAdapter);

        binding.toggleType.check(R.id.btnExpense);
        binding.toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.btnIncome) {
                viewModel.onTypeChange(TransactionType.INCOME);
            } else {
                viewModel.onTypeChange(TransactionType.EXPENSE);
            }
        });

        binding.inputAmount.addTextChangedListener(simpleWatcher(s ->
                viewModel.onAmountChange(s == null ? "" : s.toString())));
        binding.inputNote.addTextChangedListener(simpleWatcher(s ->
                viewModel.onNoteChange(s == null ? "" : s.toString())));

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (suppressCategoryCallback) {
                    return;
                }
                String value = categoryAdapter.getItem(position);
                if (value != null) {
                    viewModel.onCategoryChange(value);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.spinnerPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (suppressPaymentCallback) {
                    return;
                }
                viewModel.onPaymentMethodChange(PaymentMethod.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.btnDate.setOnClickListener(v -> showDatePicker());
        binding.btnSave.setOnClickListener(v -> viewModel.save());
        binding.btnDelete.setOnClickListener(v -> viewModel.delete());
        binding.btnTakePhoto.setOnClickListener(v -> ensureCameraAndLaunch());
        binding.btnPickGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.btnRemoveReceipt.setOnClickListener(v -> viewModel.clearReceipt());

        viewModel.getCategories().observe(getViewLifecycleOwner(), this::updateCategories);
        viewModel.getCategory().observe(getViewLifecycleOwner(), this::selectCategory);
        viewModel.getAmountText().observe(getViewLifecycleOwner(), amount -> {
            if (amount != null && !amount.equals(text(binding.inputAmount))) {
                binding.inputAmount.setText(amount);
            }
        });
        viewModel.getNote().observe(getViewLifecycleOwner(), note -> {
            if (note != null && !note.equals(text(binding.inputNote))) {
                binding.inputNote.setText(note);
            }
        });
        viewModel.getDateMillis().observe(getViewLifecycleOwner(), millis -> {
            if (millis != null) {
                binding.btnDate.setText(DateUtils.formatDisplayDate(millis));
            }
        });
        viewModel.getAmountError().observe(getViewLifecycleOwner(), error -> {
            if (error == null) {
                binding.textAmountError.setVisibility(View.GONE);
            } else {
                binding.textAmountError.setVisibility(View.VISIBLE);
                binding.textAmountError.setText(error);
            }
        });
        viewModel.getIsEditMode().observe(getViewLifecycleOwner(), edit -> {
            boolean isEdit = Boolean.TRUE.equals(edit);
            binding.toolbar.setTitle(isEdit ? R.string.edit_transaction : R.string.add_transaction);
            binding.btnDelete.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        });
        viewModel.getType().observe(getViewLifecycleOwner(), type -> {
            if (type == TransactionType.INCOME) {
                binding.toggleType.check(R.id.btnIncome);
            } else {
                binding.toggleType.check(R.id.btnExpense);
            }
        });
        viewModel.getReceiptPath().observe(getViewLifecycleOwner(), this::renderReceipt);
        viewModel.getPaymentMethod().observe(getViewLifecycleOwner(), method -> {
            if (method == null) {
                return;
            }
            int index = method.ordinal();
            if (index >= 0 && index < paymentAdapter.getCount()) {
                suppressPaymentCallback = true;
                binding.spinnerPayment.setSelection(index);
                suppressPaymentCallback = false;
            }
        });
        viewModel.getSaveCompleted().observe(getViewLifecycleOwner(), done -> {
            if (Boolean.TRUE.equals(done)) {
                viewModel.consumeSaveCompleted();
                Navigation.findNavController(view).navigateUp();
            }
        });
        viewModel.getDeleteCompleted().observe(getViewLifecycleOwner(), done -> {
            if (Boolean.TRUE.equals(done)) {
                viewModel.consumeDeleteCompleted();
                Navigation.findNavController(view).navigateUp();
            }
        });
    }

    private void ensureCameraAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        cameraFile = ReceiptStore.createCameraTarget(requireContext());
        cameraUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                cameraFile
        );
        takePictureLauncher.launch(cameraUri);
    }

    private void renderReceipt(String path) {
        if (path == null || path.isEmpty()) {
            binding.imageReceipt.setVisibility(View.GONE);
            binding.textReceiptStatus.setVisibility(View.GONE);
            binding.btnRemoveReceipt.setVisibility(View.GONE);
            binding.imageReceipt.setImageDrawable(null);
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            binding.imageReceipt.setVisibility(View.GONE);
            binding.textReceiptStatus.setVisibility(View.VISIBLE);
            binding.textReceiptStatus.setText(R.string.receipt_attached);
            binding.btnRemoveReceipt.setVisibility(View.VISIBLE);
            return;
        }
        binding.imageReceipt.setVisibility(View.VISIBLE);
        binding.imageReceipt.setImageURI(Uri.fromFile(file));
        binding.textReceiptStatus.setVisibility(View.VISIBLE);
        binding.textReceiptStatus.setText(R.string.receipt_attached);
        binding.btnRemoveReceipt.setVisibility(View.VISIBLE);
    }

    private void updateCategories(List<String> cats) {
        categoryAdapter.clear();
        if (cats != null) {
            categoryAdapter.addAll(cats);
        }
        categoryAdapter.notifyDataSetChanged();
        selectCategory(viewModel.getCategory().getValue());
    }

    private void selectCategory(String category) {
        if (category == null) {
            return;
        }
        int index = categoryAdapter.getPosition(category);
        if (index >= 0) {
            suppressCategoryCallback = true;
            binding.spinnerCategory.setSelection(index);
            suppressCategoryCallback = false;
        }
    }

    private void showDatePicker() {
        Long current = viewModel.getDateMillis().getValue();
        LocalDate date = current == null
                ? LocalDate.now()
                : DateUtils.millisToLocalDate(current);
        Calendar cal = Calendar.getInstance();
        cal.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            LocalDate selected = LocalDate.of(year, month + 1, day);
            viewModel.onDateChange(DateUtils.localDateToMillis(selected));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private static TextWatcher simpleWatcher(java.util.function.Consumer<CharSequence> onChange) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onChange.accept(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private List<String> paymentLabels() {
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.cash));
        labels.add(getString(R.string.card));
        labels.add(getString(R.string.jazzcash));
        labels.add(getString(R.string.easypaisa));
        labels.add(getString(R.string.all));
        // Replace last "All" with Other
        labels.set(labels.size() - 1, "Other");
        return labels;
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
