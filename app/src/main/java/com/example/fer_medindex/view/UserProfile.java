package com.example.fer_medindex.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fer_medindex.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfile extends AppCompatActivity {
    private TextView textViewWelcome , textViewFullName, textViewEmail,textViewDoB, textViewGender , textViewMobile;
    private ProgressBar progressBar;
    private String fullName, email ,doB, gender, mobile;
    private ImageView imageView;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setTitle("Home");

        textViewWelcome = findViewById(R.id.textview_show_welcome);
        textViewFullName = findViewById(R.id.textview_show_full_name);
        textViewEmail = findViewById(R.id.textview_show_email);
        textViewDoB = findViewById(R.id.textview_show_dob);
        textViewGender = findViewById(R.id.textview_show_gender);
        textViewMobile = findViewById(R.id.textview_show_mobile);
        progressBar = findViewById(R.id.progressBar);

        //Set onClickListener on ImageView to Open UploadProfileActivity
        imageView = findViewById(R.id.imageView_user_profile_dp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfile.this,UploadProfile.class);
                startActivity(intent);
            }
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser == null) {
            Toast.makeText(UserProfile.this, "Something went wrong! User's detail are not available at the moment", Toast.LENGTH_SHORT).show();
        } else {
            checkEmailVerified(firebaseUser);
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
            PatientFormInput.clearForm();
        }
    }

    private void checkEmailVerified(FirebaseUser firebaseUser) {
        //N???u ng?????i d??ng ch??a x??c nh???n email g???i h???p tho???i c???nh b??o
        if(!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        //thi???t l???p tr??nh t???o c???nh b??o
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);
        builder.setTitle("Email not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification. ");

        //M??? ???ng d???ng email n???u ng?????i d??ng nh???p / nh???n v??o n??t ti???p t???c
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // xu???t hi???n trong tr??nh kh???i ch???y m??n h??nh ch??nh
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                //G???i ???ng d???ng email trong c???a s??? m???i v?? kh??ng ph???i trong ???ng d???ng c???a ch??ng t??i
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        // T???o AlertDialog
        AlertDialog alertDialog = builder.create();
        // Hi???n th??? AlertDialog
        alertDialog.show();

    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userID = firebaseUser.getUid();
        //Extracting user reference form database for " Register Users" Tr??ch xu???t c?? s??? d??? li???u bi???u m???u tham chi???u ng?????i d??ng cho "????ng k?? ng?????i d??ng"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        //th??m Tr??nh nghe cho S??? ki???n Gi?? tr??? ????n
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readWriteUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readWriteUserDetails != null){
                    fullName = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    doB  = readWriteUserDetails.getDoB();
                    gender = readWriteUserDetails.getGender();
                    mobile = readWriteUserDetails.getMobile();

                    textViewWelcome.setText("Welcome " + fullName);
                    textViewFullName.setText(fullName);
                    textViewEmail.setText(email);
                    textViewDoB.setText(doB);
                    textViewGender.setText(gender);
                    textViewMobile.setText(mobile);
                    //Set image after upload successful in firebase
                    //???nh tr??n firebase l??u v??o uri
                    Uri uri = firebaseUser.getPhotoUrl();
                    // Do ???nh thu???c ngu???n b??n ngo??i(v?? d??? firebase, l???y ???nh tr??n internet kh??ng ph???i trong ??i???n tho???i n??n d??ng Picasso)
                    Picasso.with(UserProfile.this).load(uri).into(imageView);
                } else {
                    Toast.makeText(UserProfile.this, "Something went wrong! User's detail are not available at the moment", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfile.this, "Something went wrong! User's detail are not available at the moment", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    //T???o ActionBar Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    //Menu Item ???????c ch???n
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // l???y id c???a m???c menu ???????c l??u tr??? v??o int id
        int id = item.getItemId();
        if(id == R.id.menu_refresh){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if(id == R.id.menu_update_profile) {
            Intent intent = new Intent(UserProfile.this,UpdateProfile.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email){
            Intent intent = new Intent(UserProfile.this,UpdateEmail.class);
            startActivity(intent);
            finish();
        }/*else if (id == R.id.menu_settings) {
            Toast.makeText(UserProfile.this,"menu_setting",Toast.LENGTH_SHORT).show();
        }*/else if(id == R.id.menu_change_password){
            Intent intent = new Intent(UserProfile.this,ChangePassword.class);
            startActivity(intent);
            finish();
        }/*else if(id==R.id.menu_delete_profile){
            Intent intent = new Intent(UserProfile.this,DeleteProfile.class);
            startActivity(intent);
        }*/ else if(id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UserProfile.this,"Logged Out",Toast.LENGTH_SHORT).show();
            //quay l???i ho???t ?????ng ch??nh c???a Activity
            Intent intent = new Intent(UserProfile.this,LoginActivity.class);

            //Xo?? ng??n s???p ????? ng??n ng?????i d??ng quay l???i ho???t ?????ng h??? s?? ng?????i d??ng ???? ????ng xu???t
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();// ????ng UserProfile
        }else{ // N???u ko ch???n item n??o
            Toast.makeText(UserProfile.this,"Something went wrong!",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}