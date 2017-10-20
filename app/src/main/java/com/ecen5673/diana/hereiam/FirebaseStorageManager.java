package com.ecen5673.diana.hereiam;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by diana on 11/15/16.
 *
 * Contains methods for uploading/downloading images to Firebase Storage
 */
public class FirebaseStorageManager {

    private static final String TAG = "FirebaseStorageManager";

    // Code to request image capture from camera
    public static final int REQUEST_CODE_CAMERA_CAPTURE = 56;
    public static final int REQUEST_CODE_PICK_IMAGE = 57;
    public static final int PERMISSION_READ_WRITE_EXTERNAL_STORAGE = 58;

    public static void storeImageToFirebase(StorageReference storageRef, Uri fileUri) {
        UploadTask uploadTask = storageRef.putFile(fileUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "Failure to upload new image.");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Log.d(TAG, "Success uploading new image!");
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG, "Download URL: " + downloadUrl);
            }
        });
    }

    public static void storeImageToFirebase(StorageReference storageRef, Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "Failed to upload Bitmap");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Log.d(TAG, "Successfuly uploaded Bitmap");
            }
        });
    }

    public static void setupDefaultProfileImage(final StorageReference storageRef){
        // Get default profile image
        StorageReference profileRef = storageRef.child(Constants.PROFILE_STRING);

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri newUri) {
                Log.d(TAG, "Found URI: " + newUri);
                storeImageToFirebase(storageRef.child(MainActivity.user.getUserID()).child(Constants.PROFILE_STRING), newUri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d(TAG, "Default Profile image was not found.");
            }
        });
    }

    public static void pickImage(Activity activity, boolean fromCamera) {
        // Check that user is able to read from storage
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_READ_WRITE_EXTERNAL_STORAGE);
        else {
            if (fromCamera){
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(activity)));
                cameraIntent.putExtra("return-data", true);
                activity.startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA_CAPTURE);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }
        }
    }

    public static File getTempFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), "profile");
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }

    //  Method to load a bitmap of arbitrarily large size into an ImageView that displays a profile image sized pixel thumbnail
    public static Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {

        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

}
