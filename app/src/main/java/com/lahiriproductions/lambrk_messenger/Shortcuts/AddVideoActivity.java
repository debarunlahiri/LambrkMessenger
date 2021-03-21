package com.lahiriproductions.lambrk_messenger.Shortcuts;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.lahiriproductions.lambrk_messenger.AddStoryActivity;
import com.lahiriproductions.lambrk_messenger.R;
import com.lahiriproductions.lambrk_messenger.Utils.FileUtil;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddVideoActivity extends AppCompatActivity {

    private static final String TAG = AddVideoActivity.class.getSimpleName();
    private CameraKitView cameraKitView;
    private Button bDummyRecordVideo;
    private Object cameraKitVideo;
    private boolean isVideoStartedRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        cameraKitView = findViewById(R.id.camera);
        bDummyRecordVideo = findViewById(R.id.bDummyRecordVideo);

        bDummyRecordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //                                        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Lambrk Messenger");
//                                        boolean success = true;
//                                        if (!folder.exists()) {
//                                            success = folder.getParentFile().mkdirs();
//                                        }
//                                        if (success) {
//                                            try {
//                                                File savedPhoto = new File(folder, System.currentTimeMillis() + ".jpg");
//                                                FileOutputStream fos = new FileOutputStream(savedPhoto.getPath());
//                                                fos.write(o.get);
//                                                fos.close();
//                                                Log.d(TAG, folder.getAbsolutePath());
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                                Toast.makeText(AddVideoActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                                            }
//                                        } else {
//                                            // Do something else on failure
//                                            Toast.makeText(getApplicationContext(), "Cannot create directory", Toast.LENGTH_LONG).show();
//                                        }
//            }
//            }

                Dexter.withActivity(AddVideoActivity.this)
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                cameraKitView.startVideo();
                                bDummyRecordVideo.setText("Recording");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        cameraKitView.stopVideo();
                                        bDummyRecordVideo.setText("Start Recording");
                                        Toast.makeText(AddVideoActivity.this, "Recording stopped", Toast.LENGTH_SHORT).show();
                                    }
                                }, 2500);
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                MultiplePermissionsListener dialogMultiplePermissionsListener =
                                        DialogOnAnyDeniedMultiplePermissionsListener.Builder
                                                .withContext(AddVideoActivity.this)
                                                .withTitle("Camera & File permission")
                                                .withMessage("Both camera and File permission are needed to take pictures of your cat and store in directory.")
                                                .withButtonText(android.R.string.ok)
                                                .withIcon(R.mipmap.ic_launcher)
                                                .build();
                                dialogMultiplePermissionsListener.onPermissionRationaleShouldBeShown(list, permissionToken);
//                                permissionToken.continuePermissionRequest();
                            }
                        })
                        .check();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}