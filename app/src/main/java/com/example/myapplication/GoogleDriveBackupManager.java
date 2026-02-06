package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GoogleDriveBackupManager {

    private static final String BACKUP_FOLDER_NAME = "InventoryProBackup";
    private static final String BACKUP_FILE_NAME = "inventory_backup.json";
    private static final Executor executor = Executors.newSingleThreadExecutor();

    private Context context;
    private Drive driveService;
    private GoogleSignInClient googleSignInClient;

    public GoogleDriveBackupManager(Context context) {
        this.context = context;
        initializeGoogleSignIn();
    }

    private void initializeGoogleSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, signInOptions);
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void handleSignInResult(Intent data, SignInCallback callback) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(account -> {
                    initializeDriveService(account);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void initializeDriveService(GoogleSignInAccount account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        driveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("Inventory Pro")
                .build();
    }

    public boolean isSignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            initializeDriveService(account);
            return true;
        }
        return false;
    }

    public void signOut(SignOutCallback callback) {
        googleSignInClient.signOut()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Upload backup to Google Drive
    public void uploadBackup(String jsonData, UploadCallback callback) {
        if (driveService == null) {
            callback.onFailure("Not signed in to Google Drive");
            return;
        }

        executor.execute(() -> {
            try {
                // Find or create backup folder
                String folderId = findOrCreateFolder(BACKUP_FOLDER_NAME);

                // Check if backup file exists
                String existingFileId = findFileInFolder(BACKUP_FILE_NAME, folderId);

                if (existingFileId != null) {
                    // Update existing file
                    updateFile(existingFileId, jsonData);
                } else {
                    // Create new file
                    createFile(BACKUP_FILE_NAME, jsonData, folderId);
                }

                callback.onSuccess("Backup uploaded to Google Drive successfully!");
            } catch (Exception e) {
                callback.onFailure("Upload failed: " + e.getMessage());
            }
        });
    }

    // Download backup from Google Drive
    public void downloadBackup(DownloadCallback callback) {
        if (driveService == null) {
            callback.onFailure("Not signed in to Google Drive");
            return;
        }

        executor.execute(() -> {
            try {
                // Find backup folder
                String folderId = findFolder(BACKUP_FOLDER_NAME);
                if (folderId == null) {
                    callback.onFailure("No backup found on Google Drive");
                    return;
                }

                // Find backup file
                String fileId = findFileInFolder(BACKUP_FILE_NAME, folderId);
                if (fileId == null) {
                    callback.onFailure("No backup file found");
                    return;
                }

                // Download file content
                String jsonData = downloadFileContent(fileId);
                callback.onSuccess(jsonData);
            } catch (Exception e) {
                callback.onFailure("Download failed: " + e.getMessage());
            }
        });
    }

    private String findOrCreateFolder(String folderName) throws IOException {
        String folderId = findFolder(folderName);
        if (folderId != null) {
            return folderId;
        }
        return createFolder(folderName);
    }

    private String findFolder(String folderName) throws IOException {
        String pageToken = null;
        do {
            FileList result = driveService.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and name='" + folderName + "' and trashed=false")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            if (result.getFiles() != null && !result.getFiles().isEmpty()) {
                return result.getFiles().get(0).getId();
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return null;
    }

    private String createFolder(String folderName) throws IOException {
        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");

        File folder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute();

        return folder.getId();
    }

    private String findFileInFolder(String fileName, String folderId) throws IOException {
        String pageToken = null;
        do {
            FileList result = driveService.files().list()
                    .setQ("name='" + fileName + "' and '" + folderId + "' in parents and trashed=false")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            if (result.getFiles() != null && !result.getFiles().isEmpty()) {
                return result.getFiles().get(0).getId();
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return null;
    }

    private void createFile(String fileName, String content, String folderId) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        java.io.File tempFile = java.io.File.createTempFile("backup", ".json", context.getCacheDir());
        java.io.FileWriter writer = new java.io.FileWriter(tempFile);
        writer.write(content);
        writer.close();

        com.google.api.client.http.FileContent mediaContent =
                new com.google.api.client.http.FileContent("application/json", tempFile);

        driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        tempFile.delete();
    }

    private void updateFile(String fileId, String content) throws IOException {
        java.io.File tempFile = java.io.File.createTempFile("backup", ".json", context.getCacheDir());
        java.io.FileWriter writer = new java.io.FileWriter(tempFile);
        writer.write(content);
        writer.close();

        com.google.api.client.http.FileContent mediaContent =
                new com.google.api.client.http.FileContent("application/json", tempFile);

        driveService.files().update(fileId, null, mediaContent).execute();

        tempFile.delete();
    }

    private String downloadFileContent(String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        driveService.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream);

        return outputStream.toString();
    }

    // Callbacks
    public interface SignInCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface SignOutCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface UploadCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface DownloadCallback {
        void onSuccess(String jsonData);
        void onFailure(String error);
    }
}

