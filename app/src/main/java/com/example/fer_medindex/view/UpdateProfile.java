package com.example.fer_medindex.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fer_medindex.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfile extends AppCompatActivity {

    private EditText editTextUpdateName , editTextUpdateDoB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName , textDoB , textGender , textMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Upload Profile Details");
        progressBar = findViewById(R.id.progressBar);
        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_gender);
        //x??c th???c firebase
        authProfile = FirebaseAuth.getInstance();
        // l???y ng?????i d??ng hi???n ??ang ????ng nh???p b???ng s??? d???ng h??? s?? auth
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        //Show profile data
      showProfile(firebaseUser);

      //Upload Profile Pic
        Button buttonUploadProfilePic = findViewById(R.id.button_upload_profile_pic);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfile.this,UploadProfile.class);
                startActivity(intent);
                finish();
            }
        });
        //Update Email
        Button buttonUploadEmail = findViewById(R.id.button_profile_update_email);
        buttonUploadEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfile.this,UpdateEmail.class);
                startActivity(intent);
                finish();
            }
        });

        //setting up DatePicker on EditText
        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Hi???n th??? ng??y ???? ch???n tr?????c hi???n th??? tr??n data picker dialog
                //V??n b???n s??? ???????c t??ch ra theo d???u g???ch ch??o sau ???? ch??ng ta ch??? c?? th??? l???y nh???ng con s??? ???? v?? l??u ch??ng v??o c??c bi???n kh??c nhau
                String textSADoB[] = textDoB.split("/");
                //Integer.parseInt chuy???n String th??nh Integer
                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1])-1; // th??ng trong m???ng index b???t ?????u t??? 0
                int year = Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;

                //Date Picker Dialog
                picker = new DatePickerDialog(UpdateProfile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDoB.setText(dayOfMonth+ "/"+(month+1)+"/"+year);
                    }
                },year,month,day); // 3 tham s??? x??c ?????nh
                picker.show();
            }
        });
        // Update Profile
        Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  chuy???n ng?????i d??ng firebase
                updateProfile(firebaseUser);
            }
        });
    }
    // Update Profile
    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        // l??u n?? d?????i d???ng c???p nh???t gi???i t??nh n??t radio
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        //X??c th???c ??i???n tho???i di ?????ng s??? d???ng Matcher v?? Pattern
        String mobileRegex ="[0][0-9]{9}";
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex); // x??c ?????nh m???u di ?????ng
        mobileMatcher = mobilePattern.matcher(textMobile);

        if(TextUtils.isEmpty(textFullName)){
            Toast.makeText(UpdateProfile.this,"Please enter your full name",Toast.LENGTH_LONG).show();
            editTextUpdateName.setError("Full Name is required");
            editTextUpdateName.requestFocus();// yeu cau nhap lai
        }   else if (TextUtils.isEmpty(textDoB)) {
            Toast.makeText(UpdateProfile.this,"Please enter date of birth",Toast.LENGTH_LONG).show();
            editTextUpdateDoB.setError("Date of birth is required");
            editTextUpdateDoB.requestFocus();
        }else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())){
            Toast.makeText(UpdateProfile.this,"Please select your gender",Toast.LENGTH_LONG).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        }else if(TextUtils.isEmpty(textMobile)) {
            Toast.makeText(UpdateProfile.this,"Please enter your mobile ",Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile is required");
            editTextUpdateMobile.requestFocus();
        }else if(textMobile.length() !=10){
            Toast.makeText(UpdateProfile.this,"Please re-enter your mobile ",Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile shoule be 10 digits");
            editTextUpdateMobile.requestFocus();
        }else if(!mobileMatcher.find()){
            Toast.makeText(UpdateProfile.this,"Please re-enter your mobile ",Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile is not valid");
            editTextUpdateMobile.requestFocus();
        } else {
            // Obtain the data entered by user
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            //Enter User Data into the Firebase Realtime Database .Set up dependencies
            // Ghi nh???ng th??ng tin ng?????i d??ng nh???p v??o c?? s??? d??? li???u
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textFullName,textDoB,textGender,textMobile);
            // Extract User reference from Database for " Registered Users"
            // Tr??ch xu???t m???t tham chi???u ng?????i d??ng t??? c?? s??? d??? li???u cho ng?????i d??ng ???? ????ng k??
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
            // L???y id c???a ng?????i d??ng // firebase cha
            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);
            // Tham chi???u v??o firebase con chuy???n id ng?????i d??ng b???ng ph????ng th???c set value
            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){ //ng??y sinh , gi???i t??nh, mobile ,gender ???? ???????c ghi th??nh c??ng v??o c?? s??? d??? li???u
                        //Setting new display name C???p nh???t t??n hi???n th??? v??o ?????i t?????ng ng?????i d??ng firebase
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                                setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfile.this, "Update Successful!", Toast.LENGTH_SHORT).show();
                        //Quay l???i h??? s?? c???p nh???t sau khi c???p nh???t th??nh c??ng
                        // Stop user from returning to UpdateProfileActivity on pressing back button and close Activity
                        Intent intent = new Intent(UpdateProfile.this,UserProfile.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else {
                        try{
                            throw task.getException();
                        }catch (Exception e){
                            Toast.makeText(UpdateProfile.this, e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

    }

    // n???p d??? li???u t??? c?? s??? d??? li???u
    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered = firebaseUser.getUid();
        // N??t tham chi???u t??? c?? s??? d??? li???u cho ng?????i d??ng ???? ????ng k??
        //Extracting User Reference from database for :" Registered Users"
        // H??? s?? tham chi???u ?????n csdl l???y d??? li???u firebase
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        // l???p cha uid v?? l???p con id
        progressBar.setVisibility(View.VISIBLE);
      referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              // ?????c v?? ghi chi ti???t ng?????i d??ng b???ng snapshot
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
              if( readUserDetails != null) {
                  textFullName = firebaseUser.getDisplayName();
                  textDoB = readUserDetails.getDoB();
                  textGender = readUserDetails.getGender();
                  textMobile = readUserDetails.getMobile();

                  editTextUpdateName.setText(textFullName);
                  editTextUpdateDoB.setText(textDoB);
                  editTextUpdateMobile.setText(textMobile);

                  //Show Gender through Radio Button
                  if(textGender.equals("Male")){ // Kiem tra neu gender = nam
                      radioButtonUpdateGenderSelected = findViewById(R.id.radio_update_male);
                  } else {
                      radioButtonUpdateGenderSelected = findViewById(R.id.radio_update_female);
                  }
                  radioButtonUpdateGenderSelected.setChecked(true);
              }else{
                  Toast.makeText(UpdateProfile.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
              }
              progressBar.setVisibility(View.GONE);


          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
              Toast.makeText(UpdateProfile.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
              progressBar.setVisibility(View.GONE);
          }
      });
    }
}