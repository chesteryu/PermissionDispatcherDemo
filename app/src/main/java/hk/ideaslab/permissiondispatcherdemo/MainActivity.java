package hk.ideaslab.permissiondispatcherdemo;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 42;
//    private File currentFile;

    private ImageView resultImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultImage = (ImageView) findViewById(R.id.result_image);
    }

    public void onCaptureClicked(View v) {
        // call generated method
        MainActivityPermissionsDispatcher.showCameraWithCheck(this);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void showCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = null;
        try {
            // This file path needs to be saved
            // For convenience, fixed path is used here
            photoFile = getImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "hk.ideaslab.permissiondispatcherdemo.fileprovider",
                    photoFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }

    @OnShowRationale({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("App want to use camera")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                request.proceed();
                            }
                        }
                )
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void showDeniedForCamera() {
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    void showNeverAskForCamera() {
        Toast.makeText(this, "Never ask again", Toast.LENGTH_SHORT).show();
    }


    public File getImageFile() throws IOException {
        // Create an image file name
//        String timeStamp = new SimpleDateFormat();
        String imageFileName = "camera_capture";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir + "/" + String.format("%s%s", imageFileName, ".png"));
        if (!image.exists()) {
            image.createNewFile();
        }
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                try {
                    // load file from the previous specified path
                    File photoFile = getImageFile();
                    Uri uri = Uri.fromFile(photoFile);
                    Crop.of(uri, uri).asSquare().start(this, Crop.REQUEST_CROP);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else if (requestCode == Crop.REQUEST_CROP) {
                // handle cropped image
                Uri uri = Crop.getOutput(data);
                resultImage.setImageURI(uri);
            }
        }
    }

}
