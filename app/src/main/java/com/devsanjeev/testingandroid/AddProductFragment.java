package com.devsanjeev.testingandroid;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class AddProductFragment extends Fragment implements
        AdapterView.OnItemSelectedListener {
    private Spinner Spinner;
    private FirebaseAuth mAuth;
    private String category;
    private static final String TAG = "";
    private static final int REQCODE = 100;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Button btnChoose, btnUploadImage, btnUploadData;
    private ImageView imageView;
    private FirebaseFirestore db;
    private Uri filePath;
    private ArrayList<String> imageUrls = new ArrayList<>();
    private ArrayList<File> imageFiles = new ArrayList<>();
    private final int PICK_IMAGE_REQUEST = 71;
    private ProgressDialog progressDialog;
    private EditText productName, productDescription;

    public AddProductFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);
        productName = view.findViewById(R.id.product_name);
        mAuth = FirebaseAuth.getInstance();
        productDescription = view.findViewById(R.id.product_description);
        Spinner = view.findViewById(R.id.category);
        Spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Automobile");
        categories.add("Business Services");
        categories.add("Computers");
        categories.add("Education");
        categories.add("Personal");
        categories.add("Travel");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        Spinner.setAdapter(dataAdapter);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        btnChoose = view.findViewById(R.id.btnChoose);
        btnUploadImage = view.findViewById(R.id.btnUploadImage);
        imageView = view.findViewById(R.id.imgView);
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageFiles.clear();
                imageUrls.clear();
                chooseImage();
            }
        });
        btnUploadData = view.findViewById(R.id.btnUpload);
//        btnUploadData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                upload();
//            }
//        });
        btnUploadData.setVisibility(View.GONE);

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(getActivity(),
                        "ProgressDialog",
                        "Wait for few seconds");
                upload2();
            }
        });
        db = FirebaseFirestore.getInstance();
        return view;
    }


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose application"), REQCODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            // When an Image is picked
            if (requestCode == REQCODE && resultCode == RESULT_OK
                    && null != data) {
                if (data.getData() != null) {
                    Uri mImageUri = data.getData();
                    File file = new File(getRealPathFromURI_API19(getActivity(), mImageUri));
                    imageFiles.add(file);

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        ArrayList<File> mArrayCamp = new ArrayList<File>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();

                            mArrayUri.add(uri);

                            File file = new File(getRealPathFromURI_API19(getActivity(), uri));
                            mArrayCamp.add(file);
                        }
                        if (!mArrayCamp.isEmpty()) {
                            imageFiles = mArrayCamp;
                        }
                        //noImage.setText("Selected Images: " + mArrayUri.size());

                    }
                }

            } else {
                Toast.makeText(getActivity(), "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong: " + e, Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    private void upload2() {
        progressDialog.setCancelable(false);
        for (File file : imageFiles) {
            final StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            UploadTask uploadTask = ref.putFile(Uri.fromFile(file));

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();

                    }
                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        imageUrls.add(downloadUri.toString());
                        upload(imageUrls.size());
                    } else {
                        // Handle failures
                        // ...
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Error Occurred In Image Uploading", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }


    }

    private void upload(int size) {
        if (size == imageFiles.size()) {
            ArrayList<Comments> comments = new ArrayList<>();
            comments.add(new Comments("comm", "comm"));
            String userId = "";
            if (mAuth.getCurrentUser() != null) {
                userId = mAuth.getCurrentUser().getUid();
            }
            NewProduct newProduct = new NewProduct(productName.getText().toString(), category, userId, productDescription.getText().toString(), comments, imageUrls);

            db.collection("products")
                    .document()
                    .set(newProduct)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {
                                Toast.makeText(getActivity(), "Failed :" + task.getException(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Failed" + e.toString(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        category = adapterView.getItemAtPosition(i).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

    /*
private class AsyncTaskRunner extends AsyncTask<String, String, String> {

    ProgressDialog progressDialog;

    @Override
    protected String doInBackground(String... params) {

        upload2();
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation

       // finalResult.setText(result);
       // upload(imageUrls.size());
    }


    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(getActivity(),
                "ProgressDialog",
                "Wait for few seconds");
    }

}
}
*/

