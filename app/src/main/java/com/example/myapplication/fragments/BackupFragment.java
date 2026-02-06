package com.example.myapplication.fragments;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.example.myapplication.R;
import com.example.myapplication.BackupManager;
import com.example.myapplication.GoogleDriveBackupManager;
import java.io.File;

public class BackupFragment extends Fragment {

    private MaterialButton btnExport, btnImport, btnCloudBackup, btnCloudRestore;
    private BackupManager backupManager;
    private GoogleDriveBackupManager driveBackupManager;
    private ActivityResultLauncher<Intent> importLauncher;
    private ActivityResultLauncher<Intent> signInLauncher;
    private ProgressDialog progressDialog;
    private Handler mainHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler(Looper.getMainLooper());

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == -1 && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            importBackup(uri);
                        }
                    }
                }
        );

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        driveBackupManager.handleSignInResult(result.getData(),
                                new GoogleDriveBackupManager.SignInCallback() {
                                    @Override
                                    public void onSuccess() {
                                        mainHandler.post(() ->
                                                Toast.makeText(requireContext(),
                                                        "Signed in to Google Drive",
                                                        Toast.LENGTH_SHORT).show()
                                        );
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        mainHandler.post(() ->
                                                Toast.makeText(requireContext(),
                                                        "Sign in failed: " + error,
                                                        Toast.LENGTH_SHORT).show()
                                        );
                                    }
                                });
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_backup, container, false);

        initViews(view);
        backupManager = new BackupManager(requireContext());
        driveBackupManager = new GoogleDriveBackupManager(requireContext());
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        btnExport = view.findViewById(R.id.btn_export);
        btnImport = view.findViewById(R.id.btn_import);
        btnCloudBackup = view.findViewById(R.id.btn_cloud_backup);
        btnCloudRestore = view.findViewById(R.id.btn_cloud_restore);
    }

    private void setupClickListeners() {
        btnExport.setOnClickListener(v -> exportData());
        btnImport.setOnClickListener(v -> selectFile());
        btnCloudBackup.setOnClickListener(v -> cloudBackup());
        btnCloudRestore.setOnClickListener(v -> cloudRestore());
    }

    private void exportData() {
        try {
            File backupFile = backupManager.saveBackupToFile();
            Toast.makeText(requireContext(),
                    "Backup saved to: " + backupFile.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "Export failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        importLauncher.launch(intent);
    }

    private void importBackup(Uri uri) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Import")
                .setMessage("This will replace all current data. Continue?")
                .setPositiveButton("Import", (dialog, which) -> {
                    try {
                        backupManager.loadBackupFromFile(uri);
                        Toast.makeText(requireContext(),
                                "Data imported successfully!",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(),
                                "Import failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void cloudBackup() {
        if (!driveBackupManager.isSignedIn()) {
            signInLauncher.launch(driveBackupManager.getSignInIntent());
            return;
        }

        showProgress("Uploading to Google Drive...");

        try {
            String jsonData = backupManager.exportToJson();

            driveBackupManager.uploadBackup(jsonData,
                    new GoogleDriveBackupManager.UploadCallback() {
                        @Override
                        public void onSuccess(String message) {
                            mainHandler.post(() -> {
                                hideProgress();
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            mainHandler.post(() -> {
                                hideProgress();
                                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
        } catch (Exception e) {
            hideProgress();
            Toast.makeText(requireContext(),
                    "Backup failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void cloudRestore() {
        if (!driveBackupManager.isSignedIn()) {
            signInLauncher.launch(driveBackupManager.getSignInIntent());
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Restore")
                .setMessage("This will replace all current data with backup from Google Drive. Continue?")
                .setPositiveButton("Restore", (dialog, which) -> {
                    showProgress("Downloading from Google Drive...");

                    driveBackupManager.downloadBackup(
                            new GoogleDriveBackupManager.DownloadCallback() {
                                @Override
                                public void onSuccess(String jsonData) {
                                    mainHandler.post(() -> {
                                        try {
                                            backupManager.importFromJson(jsonData);
                                            hideProgress();
                                            Toast.makeText(requireContext(),
                                                    "Data restored from Google Drive!",
                                                    Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            hideProgress();
                                            Toast.makeText(requireContext(),
                                                    "Restore failed: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(String error) {
                                    mainHandler.post(() -> {
                                        hideProgress();
                                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                                    });
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showProgress(String message) {
        mainHandler.post(() -> {
            progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();
        });
    }

    private void hideProgress() {
        mainHandler.post(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }
}

