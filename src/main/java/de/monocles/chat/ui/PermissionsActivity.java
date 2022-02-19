package de.monocles.chat.ui;

import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import eu.siacs.conversations.R;
import eu.siacs.conversations.utils.Compatibility;
import me.drakeet.support.toast.ToastCompat;

public class PermissionsActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_LENGTH = 1;
    public static final int STORAGE_PERMISSION = 0;

    private final OnPermissionGranted[] permissionCallbacks = new OnPermissionGranted[PERMISSION_LENGTH];

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            if (Compatibility.hasStoragePermission(PermissionsActivity.this)) {
                permissionCallbacks[STORAGE_PERMISSION].onPermissionGranted();
                permissionCallbacks[STORAGE_PERMISSION] = null;
            } else {
                ToastCompat.makeText(this, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
                requestStoragePermission(permissionCallbacks[STORAGE_PERMISSION]);
            }
        }
    }

    public boolean checkStoragePermission() {
        return Compatibility.hasStoragePermission(PermissionsActivity.this);
    }

    public void requestStoragePermission(@NonNull final PermissionsActivity.OnPermissionGranted onPermissionGranted) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.intro_required_permissions);
        builder.setMessage(getString(R.string.no_storage_permission));
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> finish());
        builder.setPositiveButton(R.string.grant, (dialog, which) -> {
            permissionCallbacks[STORAGE_PERMISSION] = onPermissionGranted;
            ActivityCompat.requestPermissions(PermissionsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        });
        builder.setOnCancelListener(dialog -> finish());
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    public interface OnPermissionGranted {
        void onPermissionGranted();
    }
}
