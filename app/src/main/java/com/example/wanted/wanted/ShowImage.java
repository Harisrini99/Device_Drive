package com.example.wanted.wanted;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ShowImage extends AppCompatActivity implements View.OnClickListener
{

    private Context context;
    private ImageView imageView;
    ProgressDialog progressDialog;
    Fragment fragment;
    private String phoneNumber;
    private Toolbar toolbar;
    private boolean run;
    static final Integer WRITE_EXST = 0x3;
    private ImageView Download;
    private String string_url;
    private Uri imageUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle("Download Image");
        imageView = (ImageView)findViewById(R.id.showImage);
        progressDialog = new ProgressDialog(this);
        Download = (ImageView)findViewById(R.id.download_profilepic);
        Download.setOnClickListener(this);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        phoneNumber = firebaseAuth.getCurrentUser().getPhoneNumber().toString();
        Bundle extras = getIntent().getExtras();
        string_url = extras.getString("url");
        imageUrl = Uri.parse(string_url);
        Glide.with(this)
                .load(imageUrl)
                .into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_profilepic: {
                if (checkWriteExternalPermission()) {
                    progressDialog.setTitle("Downloading");
                    progressDialog.setMessage("please wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setIndeterminate(false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new downloadImage().execute();
                } else {
                    askForPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXST);
                }
            }
            break;
        }
    }


    private boolean checkWriteExternalPermission()
    {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
            {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
            else
            {

                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);

            }
        }
    }
    @Override
    public
    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WRITE_EXST) {
            if (resultCode == Activity.RESULT_OK)
            {
                Toast.makeText(context,"yesss",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void galleryAddPic(File path)
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //File f = new File(path);
        Uri contentUri = Uri.fromFile(path);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    public class downloadImage extends AsyncTask<Void, Void, Integer>
    {
        private String display = "";
        private  File path;
        @Override
        protected Integer doInBackground(Void... params)
        {
            URL url = null;
            try {
                url = new URL(string_url);

                long startTime = System.currentTimeMillis();
                URLConnection ucon = url.openConnection();
                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }
                File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Device 2 Drive");
                directory.mkdirs();
                // String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
                //String s[] = contentType.split("/");
                String ext = "jpg";
                File l[] = directory.listFiles();
                int len;
                String filename;
                if (l != null) {
                    len = l.length;
                    if (len != 0)
                        filename = "image(" + Integer.toString(len) + ")." + ext;
                    else
                        filename = "image." + ext;
                    path = new File(Environment.getExternalStorageDirectory() + File.separator + "Device 2 Drive" + File.separator + filename);
                    ;
                } else {
                    filename = "image." + ext;
                    path = new File(Environment.getExternalStorageDirectory() + File.separator + "Device 2 Drive" + File.separator + filename);
                    ;

                }
                File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Device 2 Drive");
                FileOutputStream fos = new FileOutputStream(path);
                fos.write(baf.toByteArray());
                fos.close();
                galleryAddPic(path);
                display="Image Downloaded at Location\n"+path.toString();


            } catch (MalformedURLException e)
            {
                display = e.toString();
                e.printStackTrace();
            } catch (FileNotFoundException e)
            {
                display = e.toString();
                e.printStackTrace();
            } catch (IOException e)
            {
                display = e.toString();
                e.printStackTrace();
            }
            return 0;
        }
        @Override
        protected void onPostExecute(Integer result)
        {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(ShowImage.this);
            builder.setMessage(display)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();


        }
    }

}
