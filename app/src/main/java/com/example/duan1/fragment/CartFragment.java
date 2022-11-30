package com.example.duan1.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duan1.R;
import com.example.duan1.adapter.CartAdapter;
import com.example.duan1.model.Product;
import com.example.duan1.views.CheckOutActivity;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class CartFragment extends Fragment  {
    private Button btnCheckOut;
    public static final String TAG = CartFragment.class.getName();
    private TextView tvCountCart, tvTotalCart;
    private View mView;
    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    int mcount_cart = 0;

    private final Handler mHandler = new Handler(Looper.myLooper());



    public static CartFragment newInstance() {
        Bundle args = new Bundle();
        CartFragment fragment = new CartFragment();
        fragment.setArguments(args);
        return fragment;
    }






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_cart_not_null, container, false);
        //anh xa
        uniUi();
        getRecyclerViewCart();
        setTotalCart();
        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCheckOut();
            }
        });

        return mView;
    }

    private void uniUi(){
        recyclerViewCart = mView.findViewById(R.id.recyclerviewListCart);
         tvCountCart = mView.findViewById(R.id.tvCountCart);
         tvTotalCart = mView.findViewById(R.id.tvTotalCart);
         btnCheckOut = mView.findViewById(R.id.btnCheckOut);
    }


    public void getRecyclerViewCart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail();
        String[] subEmail = userEmail.split("@");
        String pathUserId = "User" + subEmail[0];
        recyclerViewCart = mView.findViewById(R.id.recyclerviewListCart);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false);
        recyclerViewCart.setLayoutManager(linearLayoutManager);
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("duan/User").child(pathUserId);
        FirebaseRecyclerOptions<Product> options =
                new FirebaseRecyclerOptions.Builder<Product>()
                        .setQuery(myRef.child("Cart"), Product.class)
                        .build();
        cartAdapter = new CartAdapter(options, new CartAdapter.IClickCart() {
            @Override
            public void onClickDeleteCart(Product product) {
                AlertDialog.Builder alerBuider = new AlertDialog.Builder(getContext());
                alerBuider.setTitle("Notifications");
                alerBuider.setMessage("Do you want to delete?");
                alerBuider.setCancelable(false);
                alerBuider.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String userEmail = user.getEmail();
                        String[] subEmail = userEmail.split("@");
                        String pathUserId = "User" + subEmail[0];
                        int click =0;
                        FirebaseDatabase.getInstance().getReference("duan/User").child(pathUserId).child("Cart").child(product.getMaSP()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                
                                int check = click+1;
                                Toast.makeText(getContext(), "Remove Success", Toast.LENGTH_SHORT).show();
                                myRef.child("Cart").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int soLuongSanPham = (int) snapshot.getChildrenCount();
                                        tvCountCart.setText((soLuongSanPham)+" items");
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                onClickDelete(product,check);


                            }
                        });
                    }
                }).setNegativeButton("no",null);
                Dialog dialog = alerBuider.create();
                dialog.show();

            }



            @Override
            public void onClickMinus(Product product) {
                Map<String, Object> map = new HashMap<>();
                map.put("soLuong",tru(product));
                int click =0;
                FirebaseDatabase.getInstance().getReference("duan/User").child(pathUserId).child("Cart").child(product.getMaSP()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        int check = click+1;
                        onClickMinus1(product,check);
                    }
                });
            }

            @Override
            public void onClickPlus(Product product) {
                Map<String, Object> map = new HashMap<>();
                map.put("soLuong",cong(product));
//                int click =0;
//                onClickPlus1(product,click+1);
                FirebaseDatabase.getInstance().getReference("duan/User").child(pathUserId).child("Cart").child(product.getMaSP()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        myRef.child("Total").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                double value = snapshot.getValue(Double.class);
                                if (product.getSoLuong() >=1){
                                    double gia =product.getGiaSP();
                                    double tong = value+(gia);
                                    myRef.child("Total").setValue(tong).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            tvTotalCart.setText("Total: $"+tong);
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });





            }
        });
        myRef.child("Cart").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int soLuongSanPham = (int) snapshot.getChildrenCount();
                tvCountCart.setText((soLuongSanPham)+" items");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerViewCart.setAdapter(cartAdapter);
        cartAdapter.notifyDataSetChanged();
//        itemTouchHelper.attachToRecyclerView(recyclerViewCart);

    }



    public int tru(Product product){
        int mTru = product.getSoLuong();
        if(mTru > 1) {
            mTru = product.getSoLuong() - 1;
        }
        return mTru;
    }

    public int cong(Product product){
        int mcong = product.getSoLuong();
        if(mcong >=1){
            mcong = product.getSoLuong()+1;
        }else {
            mcong = 1;
        }
        return mcong;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (cartAdapter != null){
            cartAdapter.startListening();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        tvTotalCart.setText("Total: $"+0);
        tvCountCart.setText(0+" items");
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        cartAdapter.stopListening();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        cartAdapter.stopListening();
//    }

    public void setTotalCart(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail();
        String[] subEmail = userEmail.split("@");
        String pathUserId = "User" + subEmail[0];
        DatabaseReference myRef = database.getReference("duan/User/" + pathUserId);


        myRef.child("Total").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double value = snapshot.getValue(Double.class);
                tvTotalCart.setText("Total: $"+value);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void onClickDelete(Product product, int click){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail();
        String[] subEmail = userEmail.split("@");
        String pathUserId = "User" + subEmail[0];
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("duan/User").child(pathUserId);

        myRef.child("Total").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double value = snapshot.getValue(Double.class);

                if (value>0){

                    double gia =product.getGiaSP()*product.getSoLuong()*click;
                    double tong = value-gia;
                    myRef.child("Total").setValue(tong).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            tvTotalCart.setText("Total: $"+tong);
                        }
                    });

                }else if(value<0){
                    myRef.child("Total").setValue(0);
                    tvTotalCart.setText("Total: $"+0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void onClickMinus1(Product product, int click){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail();
        String[] subEmail = userEmail.split("@");
        String pathUserId = "User" + subEmail[0];
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("duan/User").child(pathUserId);

        myRef.child("Total").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double value = snapshot.getValue(Double.class);
                if (product.getSoLuong() >1){
                    double gia =product.getGiaSP()*click;
                    double tong = value-(gia);
                    myRef.child("Total").setValue(tong).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            tvTotalCart.setText("Total: $"+tong);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    public void onClickPlus1(Product product,int click){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail();
        String[] subEmail = userEmail.split("@");
        String pathUserId = "User" + subEmail[0];
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("duan/User").child(pathUserId);

        myRef.child("Total").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double value = snapshot.getValue(Double.class);

                if (product.getSoLuong() >=1){

                    double gia =product.getGiaSP()*click;
                    double tong = value+(gia);
                    myRef.child("Total").setValue(tong).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            tvTotalCart.setText("Total: $"+tong);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }




    public void onClickCheckOut(){
        Intent intent = new Intent(getActivity(), CheckOutActivity.class);
        startActivity(intent);
    }


}