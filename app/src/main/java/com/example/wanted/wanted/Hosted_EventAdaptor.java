package com.example.wanted.wanted;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Hosted_EventAdaptor extends BaseAdapter
{

    Context context;
    String string_url;
    static final Integer WRITE_EXST = 0x3;
    private final ArrayList<String> name;
    private final ArrayList<String> url;
    private final ArrayList<String> wholeName= new ArrayList<>();
    private final ArrayList<String> wholeUrl = new ArrayList<>();
    private ProgressDialog progressDialog;





    public Hosted_EventAdaptor(Context context, ArrayList<String> name,ArrayList<String> url)
    {
        this.context = context;
       this.name=name;
       this.url = url;
        progressDialog = new ProgressDialog(context);
        for(String s:name)
       {
           wholeName.add(s);
       }
       for(String s:url)
        {
            wholeUrl.add(s);
        }

    }

    @Override
    public int getCount()
    {
        return name.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        ViewHolder viewHolder;

        final View result;


        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_image, parent, false);
            int h = context.getResources().getDisplayMetrics().densityDpi;
            int w= (int) context.getResources().getDisplayMetrics().widthPixels;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels - 90;
            convertView.setLayoutParams(new GridView.LayoutParams(width/2, ViewGroup.LayoutParams.FILL_PARENT));
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.tv_img_name);
            viewHolder.imageView = (ImageView)convertView.findViewById(R.id.img_view);
            viewHolder.timage = (ImageView)convertView.findViewById(R.id.aaa);
            viewHolder.set = (ImageView)convertView.findViewById(R.id.settings);
            viewHolder.set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    string_url = url.get(position);
                    PopupMenu popup = new PopupMenu(context, view);
                    popup.inflate(R.menu.popup);
                    popup.setGravity(Gravity.END);
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.down_pop:
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
                                    return true;
                                case R.id.delete_pop:
                                    progressDialog.setTitle("Deleting");
                                    progressDialog.setMessage("please wait...");
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.setIndeterminate(false);
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();
                                    DeleteFromStorage(name.get(position),url.get(position));
                                    return true;

                                default:
                                    return false;
                            }
                        }
                    });

                }
            });

            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(context, ShowImage.class);
                    intent.putExtra("url", url.get(position));
                    context.startActivity(intent);
                }
            });

            result=convertView;

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }
        if(name.get(position).length() > 10)
        {
            String t = name.get(position).substring(0,8);
            viewHolder.txtName.setText(t+".......");
        }
        else {
            int n = name.get(position).length();
            String t = name.get(position);
            for(int i=n;i<17;i++)
                t = t + " ";
            viewHolder.txtName.setText(t);
        }
        String url_str = url.get(position);
      {

            if (url_str != "" | url_str != null) {
                Glide.with(context)
                        .load(url_str)
                        .into(viewHolder.imageView);

            }
        }
        return convertView;
    }

    public void filter(String charText)
    {
        charText = charText.toLowerCase(Locale.getDefault());
        Log.d("00000000000000000000000",charText);

        int n = wholeName.size();
        name.clear();
        url.clear();

        if (charText.length() == 0)
        {
            name.addAll(wholeName);
            url.addAll(wholeUrl);
        }
        else
        {
            for(int i=0;i<n;i++)
            {
                if(wholeName.get(i).toLowerCase(Locale.getDefault()).contains(charText) )
                {
                   name.add(wholeName.get(i));
                   url.add(wholeUrl.get(i));
                }
            }
            Log.d("000000000000",name.toString());

        }
        notifyDataSetChanged();
    }



    private static class ViewHolder {

        TextView txtName;
        ImageView imageView;
        ImageView timage;
        ImageView set;

    }

    private void DeleteFromStorage(String name,String url)
    {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
                DeleteFromDB(name);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });

    }


    private void DeleteFromDB(String name)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String num = firebaseAuth.getCurrentUser().getPhoneNumber();
        Query applesQuery = ref.child(num).orderByChild("uploaded images");
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    for (DataSnapshot d : appleSnapshot.getChildren()) {
                        if (d.getKey().equals(name)) {
                            d.getRef().removeValue();

                        }
                    }
                }
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    private boolean checkWriteExternalPermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Home)context, permission)) {
                ActivityCompat.requestPermissions((Home)context, new String[]{permission}, requestCode);
            } else {

                ActivityCompat.requestPermissions((Home)context, new String[]{permission}, requestCode);

            }
        }
    }

    //@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WRITE_EXST) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(context, "yesss", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void galleryAddPic(File path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //File f = new File(path);
        Uri contentUri = Uri.fromFile(path);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public class downloadImage extends AsyncTask<Void, Void, Integer> {
        private String display = "";
        private File path;

        @Override
        protected Integer doInBackground(Void... params) {
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
                display = "Image Downloaded at Location\n" + path.toString();


            } catch (MalformedURLException e) {
                display = e.toString();
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                display = e.toString();
                e.printStackTrace();
            } catch (IOException e) {
                display = e.toString();
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
