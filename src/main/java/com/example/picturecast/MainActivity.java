package com.example.picturecast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Uri photoURI;
    FirebaseVisionImage image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showSettingsAlert();


    }

   @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Button scanAgain = findViewById(R.id.button);
        scanAgain.setOnClickListener(this);
        if (requestCode == 1) {
            try {
                image = FirebaseVisionImage.fromFilePath(getApplicationContext(), photoURI);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();

            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            // Task completed successfully
                            TextView textView = findViewById(R.id.textView);
                            StringBuffer scanned = new StringBuffer();
                            float temp = 0;
                            Map<Float, FirebaseVisionImageLabel> something= new HashMap<>();

                            for (FirebaseVisionImageLabel label: labels) {
                                String text = label.getText();
                                String entityId = label.getEntityId();
                                something.put(label.getConfidence(), label);
                                temp = Math.max(temp, label.getConfidence());
                            }

                            if(something.get(temp).getText().toString().equals("Television")){
                                startActivity(new Intent("android.settings.CAST_SETTINGS"));

                            }
                            textView.setText(something.get(temp).getText());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
        }//if
    }//onActivityResult method
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.picturecast",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);


            }//if
        }//if
    }//dispatchTakePictureIntent method
    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dispatchTakePictureIntent();
                    }
                });

        alertDialog.show();
    }
    private File createImageFile() {
        // Create an image file name
        String imageFileName = "PNG" + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".png",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.button){
            dispatchTakePictureIntent();
        }

    }//onClick method

    @Override public boolean onCreateOptionsMenu(Menu menu){ getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_fav)
        {
             return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
