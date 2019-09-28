package com.example.wanted.wanted;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
public class Home extends AppCompatActivity {

    private static final int CHOOSING_IMAGE_REQUEST = 1234;
    private Uri fileUri;
    private DatabaseReference mDataReference;
    private StorageReference imageReference;
    private StorageReference fileRef;
    private RecyclerView rcvListImg;
    ProgressDialog progressDialog;
    private static final int PICK_IMAGE_REQUEST = 234;
    private Button buttonUpload;
    private ImageView imageView;
    private ImageView buttonChoose;
    private Uri filePath;
    private Toolbar homeToolbar;
    private Toolbar fileToolbar;
    private String email;
    private String num;
    private String fileName;
    static GridView gridView;
    private Hosted_EventAdaptor hosted_eventAdaptor;
    private FragmentActivity fragmentActivity;
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> urlList = new ArrayList<>();
    private EditText SearchText;
    private SearchView searchView;
    private FirebaseAuth mAuth;
    Toolbar toolbar, searchtollbar;
    Menu search_menu;
    MenuItem item_search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fileToolbar = (Toolbar) findViewById(R.id.files);
        setSupportActionBar(fileToolbar);
        getSupportActionBar().setTitle("Files");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSearchtollbar();


        //    homeToolbar = (Toolbar)findViewById(R.id.home_toolbar);
        // setSupportActionBar(homeToolbar);
        //getSupportActionBar().setTitle("Device To Drive");


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        num = user.getPhoneNumber();
        email = user.getPhoneNumber();
        num = user.getPhoneNumber();
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(Home.this);

        //  SearchText = (EditText)findViewById(R.id.searchText);
        gridView = (GridView) findViewById(R.id.homeGrid);

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showFileChooser();
            }
        });
     /*   SearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(s != null)
                    hosted_eventAdaptor.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
*/


        Query imagesQuery = FirebaseDatabase.getInstance().getReference().child(num).child("uploaded images").orderByKey();//.limitToLast(20);

        imagesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                urlList.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    {
                        HashMap<String, String> map = (HashMap<String, String>) dataSnapshot1.getValue();
                        String name = map.get("name");
                        String url = map.get("url");
                        nameList.add(name);
                        urlList.add(url);
                    }
                }
                hosted_eventAdaptor = new Hosted_EventAdaptor(Home.this, nameList, urlList);
                gridView.setAdapter(hosted_eventAdaptor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void showFileChooser() {
        //Intent intent = new Intent();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        //startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            fileUri = filePath;

            File file = new File(fileUri.getPath());
            fileName = file.getName();
            Log.d("7777777777777777777777", data.getDataString());
            uploadFile();
        }
    }


    //// Get File Extension
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    //// Upload File tO Drive
    private void uploadFile() {
        if (fileUri != null) {
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            mDataReference = FirebaseDatabase.getInstance().getReference(email).child("uploaded images");
            imageReference = FirebaseStorage.getInstance().getReference().child(num).child("uploaded");
            fileRef = imageReference.child(fileName);// + "." + getFileExtension(fileUri));
            fileRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            String name = taskSnapshot.getMetadata().getName();
                            String url = taskSnapshot.getDownloadUrl().toString();
                            writeNewImageInfoToDB(name, url);

                            AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                            builder.setMessage("Image Uploaded")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do things
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();

                            Toast.makeText(Home.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            // percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    })
                    .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("Upload is paused!");
                        }
                    });
        } else {
            Toast.makeText(Home.this, "No File!", Toast.LENGTH_LONG).show();
        }

    }

    ////Write to Firebase Database Function
    private void writeNewImageInfoToDB(String name, String url) {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("url", url);
        String key = mDataReference.push().getKey();

        mDataReference.child(fileName).setValue(map);
    }

    ///// Signout Function.....
    public void signOut() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do You Really Want To Sign Out")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        Toast.makeText(Home.this, "Signed Out", Toast.LENGTH_SHORT).show();


                        mAuth.signOut();
                        PhoneAuth a = new PhoneAuth();
                        // a.setTrue();
                        a.savePreferences(false, Home.this);
                        sendV s = new sendV();
                        Intent intent = new Intent(Home.this, PhoneAuth.class);
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }


    ///// Menu Item Creation
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }


    /////Mennu item Selection..
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_search:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    circleReveal(R.id.searchtoolbar, 1, true, true);

                } else {
                    searchtollbar.setVisibility(View.VISIBLE);

                }

                item_search.expandActionView();
                return true;

            case R.id.signout:
                signOut();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /// Setting Search Toolbar
    public void setSearchtollbar() {
        searchtollbar = (Toolbar) findViewById(R.id.searchtoolbar);
        if (searchtollbar != null) {
            searchtollbar.inflateMenu(R.menu.menu_search);
            search_menu = searchtollbar.getMenu();

            searchtollbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        circleReveal(R.id.searchtoolbar, 1, true, false);
                    } else
                        searchtollbar.setVisibility(View.GONE);
                }
            });

            item_search = search_menu.findItem(R.id.action_filter_search);

            MenuItemCompat.setOnActionExpandListener(item_search, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // Do something when collapsed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        circleReveal(R.id.searchtoolbar, 1, true, false);
                    } else
                        searchtollbar.setVisibility(View.GONE);
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    // Do something when expanded
                    return true;
                }
            });

            initSearchView();


        } else
            Log.d("toolbar", "setSearchtollbar: NULL");
    }

    /////initialing Search.....
    public void initSearchView() {
        final android.support.v7.widget.SearchView searchView =
                (android.support.v7.widget.SearchView) search_menu.findItem(R.id.action_filter_search).getActionView();

        // Enable/Disable Submit button in the keyboard

        searchView.setSubmitButtonEnabled(false);

        // Change search close button image

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setImageResource(R.drawable.ic_close);


        // set hint and the text colors

        EditText txtSearch = ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setHint("Search..");
        txtSearch.setHintTextColor(getResources().getColor(R.color.WANTED));
        txtSearch.setTextColor(getResources().getColor(R.color.white));


        // set the cursor

        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.search_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callSearch(newText);
                return true;
            }

            public void callSearch(String query) {
                //Do searching
                Log.i("query", "" + query);
                hosted_eventAdaptor.filter(query);

            }

        });

    }

    ////Animation of Search Toolbar....
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void circleReveal(int viewID, int posFromRight, boolean containsOverflow, final boolean isShow) {
        final View myView = findViewById(viewID);
        final View view = findViewById(R.id.toolbar);
        int width = myView.getWidth();

        if (posFromRight > 0)
            width -= (posFromRight * getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)) - (getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) / 2);
        if (containsOverflow)
            width -= getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material);

        int cx = width;
        int cy = myView.getHeight() / 2;

        Animator anim;
        if (isShow)
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, (float) width);
        else
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, (float) width, 0);

        anim.setDuration((long) 220);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // make the view visible and start the animation
        if (isShow) {
            myView.setVisibility(View.VISIBLE);
        }
        // start the animation
        anim.start();


    }

}
