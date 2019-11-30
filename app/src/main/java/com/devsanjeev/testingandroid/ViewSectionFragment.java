package com.devsanjeev.testingandroid;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ViewSectionFragment extends Fragment {
    private FirebaseFirestore mFirebaseFirestore;
    private ArrayList<NewProduct> products=new ArrayList<>();
    private RecyclerView recyclerView;
    public ViewSectionFragment() {
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
        View view= inflater.inflate(R.layout.fragment_view_section, container, false);
        mFirebaseFirestore=FirebaseFirestore.getInstance();
        recyclerView=view.findViewById(R.id.recycler_product);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        getListItems();
        return view;
    }
    private void getListItems() {
        mFirebaseFirestore.collection("products").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.isEmpty()) {
                            Log.d(TAG, "onSuccess: LIST EMPTY");
                            return;
                        } else {
                            // Convert the whole Query Snapshot to a list
                            // of objects directly! No need to fetch each
                            // document.
                            List<NewProduct> types = documentSnapshots.toObjects(NewProduct.class);
                            products.addAll(types);
                            NewProductAdapter adapter=new NewProductAdapter(products,getActivity());
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }}}
                        ).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Error getting data!!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
}
