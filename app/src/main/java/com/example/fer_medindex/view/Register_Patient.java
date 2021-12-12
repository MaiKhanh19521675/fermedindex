package com.example.fer_medindex.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fer_medindex.R;
import com.example.fer_medindex.ml.Model;
import com.example.fer_medindex.utils.Extensions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class Register_Patient extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private TextView lable_out;
    private  ImageView img;
    private EditText editTextRegisterFullName, editTextRegisterEmail,
            editTextRegisterDoB, editTextRegisterMobile, editTextRegiterCMND, editRegisterAddress;
    private ProgressBar progressBar;
    private RadioButton radioButtonRegisterGenderSelected;
    private RadioGroup radioGroupRegisterGender;
    private CheckBox checkBoxHo, checkBoxSot, checkBoxKhotho, checkboxdaunguoi, checkboxsuckhoetot, checkboxcamket;
    private DatePickerDialog picker;
    private FirebaseDatabase firebaseDatabase;
    private ImageView button_camera;
    private KProgressHUD hud;
    private ImageView imageProfile;

    private StorageReference reference = FirebaseStorage.getInstance().getReference();
    private Uri uriImage;
    public static final int REQUEST_CODE_CAMERA = 456;
    public static final int SELECT_PICTURE = 123;

    private static final int REQUEST_PHOTO_GALLERY = 100;
    private static final int REQUEST_CAPTURE_IMAGE = 110;
    private static final String TAG = "RegisterActivity";
    private String textFullName;
    private String textEmail;
    private String textDoB;
    private String textMobile;
    private String textCMND;
    private String textAddress;
    private String textHo;
    private String textSot;
    private String textKhotho;
    private String textDaunguoi;
    private String textSuckhoetot;
    private String textcamket;
    private String mobileRegex;
    private Matcher mobileMatcher;
    private Pattern mobilePattern;
    private String textGender;
    private Map<String,String> emotions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_patient);

//        Button camera_bt = findViewById(R.id.button_camera);
//        lable_out = findViewById(R.id.lable_out);
//        img = findViewById(R.id.image_camera);
//
//        camera_bt.setOnClickListener(v -> {
//            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
//            {
//                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
//            }
//            else
//            {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
//            }
//        });
        init();

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogChooseImage();
            }
        });

        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //Date Picker Dialog
                picker = new DatePickerDialog(Register_Patient.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day); // 3 tham số xác định
                picker.show();

            }
        });

        Button buttonRegisterPatient = findViewById(R.id.button_register_patient);
        buttonRegisterPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);
                textFullName = editTextRegisterFullName.getText().toString();
                textEmail = editTextRegisterEmail.getText().toString();
                textDoB = editTextRegisterDoB.getText().toString();
                textMobile = editTextRegisterMobile.getText().toString();
                textCMND = editTextRegiterCMND.getText().toString();
                textAddress = editRegisterAddress.getText().toString();
                textHo = checkBoxHo.getText().toString();
                textSot = checkBoxSot.getText().toString();
                textKhotho = checkBoxKhotho.getText().toString();
                textDaunguoi = checkboxdaunguoi.getText().toString();
                textSuckhoetot = checkboxsuckhoetot.getText().toString();
                textcamket = checkboxcamket.getText().toString();
                textGender = radioButtonRegisterGenderSelected.getText().toString();

                mobileRegex = "[0][0-9]{9}";
                mobilePattern = Pattern.compile(mobileRegex); // xác định mẫu di động
                mobileMatcher = mobilePattern.matcher(textMobile);


                if (TextUtils.isEmpty(textFullName)) {
                    Toast.makeText(Register_Patient.this, "Vui lòng nhập đầy đủ họ và tên của bạn", Toast.LENGTH_LONG).show();
                    editTextRegisterFullName.setError("Bắt buộc nhập họ và tên");
                    editTextRegisterFullName.requestFocus();// yeu cau nhap lai
                } else if (TextUtils.isEmpty((textEmail))) {
                    Toast.makeText(Register_Patient.this, "Vui lòng nhập email của bạn", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Bắt buộc nhập email");
                    editTextRegisterEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) { // khác true
                    Toast.makeText(Register_Patient.this, "Vui lòng nhập lại email của bạn", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError(" email không hợp lệ");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textDoB)) {
                    Toast.makeText(Register_Patient.this, "Vui lòng nhập ngày sinh của bạn", Toast.LENGTH_LONG).show();
                    editTextRegisterDoB.setError("Bắt buộc nhập ngày sinh");
                    editTextRegisterDoB.requestFocus();
                } else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(Register_Patient.this, "Vui lòng chọn giới tính của bạn", Toast.LENGTH_LONG).show();
                    radioButtonRegisterGenderSelected.setError("Bắt buộc phải chọn giới tính");
                    radioButtonRegisterGenderSelected.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(Register_Patient.this, "Vui lòng nhập số điện thoại của bạn ", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Bắt buộc nhập số điện thoại");
                    editTextRegisterMobile.requestFocus();
                } else if (textMobile.length() != 10) {
                    Toast.makeText(Register_Patient.this, "Vui lòng nhập số điện thoại của bạn ", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Điện thoại di động phải có 10 chữ số");
                    editTextRegisterMobile.requestFocus();
                } else if (!mobileMatcher.find()) {
                    Toast.makeText(Register_Patient.this, "Vui lòng nhập số điện thoại của bạn ", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Điện thoại di động không hợp lệ");
                    editTextRegisterMobile.requestFocus();
                } else if (TextUtils.isEmpty((textCMND))) {
                    Toast.makeText(Register_Patient.this, "Vui lòng nhập CMND của bạn", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Bắt buộc nhập CMND");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty((textAddress))) {
                    Toast.makeText(Register_Patient.this, "Vui lòng nhập địa chỉ của bạn", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Bắt buộc nhập địa chỉ");
                    editTextRegisterEmail.requestFocus();
                } else if (!checkBoxHo.isChecked() && !checkBoxSot.isChecked() && !checkboxdaunguoi.isChecked() && !checkBoxKhotho.isChecked() && !checkboxdaunguoi.isChecked() && !checkboxsuckhoetot.isChecked()) {
                    Toast.makeText(Register_Patient.this, "Vui lòng chọn tình trạng hiện tại của bạn", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Bắt buộc chọn tình trạng hiện tại");
                    editTextRegisterEmail.requestFocus();
                } else if (!checkboxcamket.isChecked()) {
                    Toast.makeText(Register_Patient.this, "Vui lòng chọn cam kết những thông tin khai là đúng sự thật", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Bắt buộc chọn cam kết");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textSot) && TextUtils.isEmpty(textHo) && TextUtils.isEmpty(textKhotho) && TextUtils.isEmpty(textDaunguoi) && TextUtils.isEmpty(textSuckhoetot)) {
                    Toast.makeText(Register_Patient.this, "Vui lòng chọn tình trạng hiện tại của bạn", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Bắt buộc chọn tình trạng hiện tại");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textcamket)) {
                    Toast.makeText(Register_Patient.this, "Vui lòng chọn cam kết những thông tin khai là đúng sự thật", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Bắt buộc chọn cam kết");
                    editTextRegisterEmail.requestFocus();
                } else {
                    uploadToFirebase(uriImage);
                }
            }
        });
    }
    public void init() {

        imageProfile = findViewById(R.id.image_camera);
        editTextRegisterFullName = findViewById(R.id.editText_register_patient_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_patient_email);
        editTextRegisterDoB = findViewById(R.id.editText_register_patient_dob);
        editTextRegisterMobile = findViewById(R.id.editText_register_patient_mobile);
        editTextRegiterCMND = findViewById(R.id.editText_register_patient_passport);
        editRegisterAddress = findViewById(R.id.editText_register_patient_address);

        checkBoxHo = findViewById(R.id.checkbox_ho);
        checkBoxSot = findViewById(R.id.checkbox_sot);
        checkBoxKhotho = findViewById(R.id.checkbox_kho_tho);
        checkboxdaunguoi = findViewById(R.id.checkbox_dau_nguoi);
        checkboxsuckhoetot = findViewById(R.id.checkbox_suc_khoe_tot);
        progressBar = findViewById(R.id.progressBar);
        lable_out = findViewById(R.id.lable_out);
        checkboxcamket = findViewById(R.id.checkbox_register_patient_CheckAgainst);

        radioGroupRegisterGender = findViewById(R.id.radio_group_register_patient_gender);
        radioGroupRegisterGender.clearCheck();

    }
    private String Status() {
        String status = "";
        if (checkBoxHo.isChecked()) {
            status += "Ho";
        }
        if (checkBoxSot.isChecked()) {
            status += " Sốt";
        }
        if (checkboxdaunguoi.isChecked()) {
            status += " Khó thở";
        }
        if (checkBoxKhotho.isChecked()) {
            status += " Đau người , mệt mỏi";
        }
        if (checkboxsuckhoetot.isChecked()) {
            status += " Sức khoẻ tốt";
        }
        return status;
    }
    private void uploadToFirebase(Uri uri) {

        final StorageReference fileRef = reference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.VISIBLE);
                                ReadWritePatientDetails writerPatientDetails = new ReadWritePatientDetails(textFullName, textDoB, textGender, textMobile, textCMND, textEmail, textAddress, Status(),uri.toString());
                                writerPatientDetails.setEmotions(emotions);
                                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Patients");

                                referenceProfile.child(writerPatientDetails.patientId).setValue(writerPatientDetails).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        PatientFormInput.createFrom(writerPatientDetails);
                                        Toast.makeText(Register_Patient.this, "User registered successfully, Please vertify your email", Toast.LENGTH_LONG).show();
                                        // truyền dữ liệu qua lại giữa các activity
                                        Intent intent = new Intent(Register_Patient.this, ProfilePatient.class);
                                        intent.putExtra(ProfilePatient.PATIENT_ID, writerPatientDetails.patientId);
                                        intent.putExtra(ProfilePatient.FULLNAME,writerPatientDetails.fullname);
                                        intent.putExtra(ProfilePatient.EMAIL,writerPatientDetails.email);
                                        intent.putExtra(ProfilePatient.GIOITINH,writerPatientDetails.gioitinh);
                                        intent.putExtra(ProfilePatient.SODIENTHOAI,writerPatientDetails.sodienthoai);
                                        intent.putExtra(ProfilePatient.CMND,writerPatientDetails.cmnd);
                                        intent.putExtra(ProfilePatient.DIACHI,writerPatientDetails.diachi);
                                        intent.putExtra(ProfilePatient.TRANGTHAI,writerPatientDetails.trangthai);
                                        intent.putExtra(ProfilePatient.HINHANH,writerPatientDetails.imgHinh);
                                        intent.putExtra(ProfilePatient.NGAYSINH,writerPatientDetails.ngaysinh);
                                        intent.putExtra(ProfilePatient.CREATE_TIME,writerPatientDetails.getCreateTimeString());
                                        intent.putExtra(ProfilePatient.EMOTION,(Serializable)writerPatientDetails.getEmotions());
                                        //intent.putExtra(ProfilePatient.)
                                        //put profile patient to intent
                                        // Ngan nguoi dung dang ki thanh cong khong quay lai dang ki lai lan nua , nguoi dung dang ki thanh cong se chuyen den trang ho so
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                        startActivity(intent);
                                        finish(); // dong hoat dong Register
                                    } else {
                                        Toast.makeText(Register_Patient.this, "User registered failed, Please try again", Toast.LENGTH_LONG).show();
                                    }
                                    // ẩn progressBar khi người dùng đăng kí thành công hoặc thất bại
                                    progressBar.setVisibility(View.GONE);

                                });

                            }
                        }, 1000);

                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }


    protected Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth,
                                                int reqHeight, ContentResolver contentResolver) throws IOException {
        InputStream in = contentResolver.openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);
        in.close();
        int width = options.outWidth / reqWidth + 1;
        int height = options.outHeight / reqHeight + 1;
        options.inSampleSize = Math.max(width, height);
        options.inJustDecodeBounds = false;
        in = contentResolver.openInputStream(uri);
        Bitmap tmpImg = BitmapFactory.decodeStream(in, null, options);
        in.close();
        return tmpImg;
    }

    public void dialogChooseImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        ArrayList<String> list = new ArrayList<String>();

        list.add("Máy ảnh");
        list.add("Album");

        CharSequence[] items = list.toArray(new CharSequence[list.size()]);
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(Register_Patient.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_CAMERA);
                        break;
                    case 1:
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(Register_Patient.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SELECT_PICTURE);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void callGallery() {
        // gọi album ảnh
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_PHOTO_GALLERY);
    }

    protected String getPath(ContentResolver contentResolver, Uri uri) {
        String[] columns = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(uri, columns, null, null, null);
        cursor.moveToFirst();
        String path = cursor.getString(0);
        cursor.close();
        return path;
    }

    public void callCamera(){
        String filename = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
        Bundle bundleOptions = new Bundle();
        // trả về ảnh gọi  startActivityForResult hàm để trở về truyền Permision
        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE, bundleOptions);
    }
    // chọn camera hay chọn ảnh từ thư viện
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                callCamera();
            } else if (requestCode == SELECT_PICTURE) {
                callGallery();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
        private void xuLyAnh(Bitmap bitmap) {

            try {
                Model model = Model.newInstance(Register_Patient.this);

                // Creates inputs for reference.
                TensorImage image = TensorImage.fromBitmap(bitmap);

                // Runs model inference and gets result.
                Model.Outputs outputs = model.process(image);
                List<Category> probability = outputs.getProbabilityAsCategoryList();

                // Releases model resources if no longer used.
                model.close();

                StringBuilder outstr = new StringBuilder();

                for (Category item : probability) {
                    outstr.append(item.getLabel()).append(": ").append(String.valueOf(item.getScore()*100)).append("%\n");
                }
                lable_out.setText(outstr.toString());
            } catch (IOException e) {
                // TODO Handle the exception
            }
        }
    private Uri mPhotoUri;
    // dựa hàm onActivityResult để trả về cho mình
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String path = null;
        Bitmap tmpImg = null;
        File cache = null;
        if (requestCode == REQUEST_PHOTO_GALLERY && resultCode == RESULT_OK) {
            try {// đọc được đường dẫn của ảnh đấy
                InputStream in = getContentResolver().openInputStream(data.getData());
                cache = Extensions.cache(this, in);
                path = cache.getAbsolutePath();

            } catch (FileNotFoundException e) {
                throw new AssertionError();
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                xuLyAnh(bitmap);
                tmpImg = this.decodeSampledBitmapFromUri(data.getData(), bitmap.getWidth(), bitmap.getHeight(), getContentResolver());
                uriImage = data.getData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            final boolean existsData = data != null && data.getData() != null;
            Uri uri = existsData ? data.getData() : mPhotoUri;
            // truyền vào hàm đẩy lên ảnh
            uriImage = uri;
            try {// covert uri sang bitmap sau đó đổ bitmap lên ảnh
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                xuLyAnh(bitmap);
                //decode dung lượng ảnh hoặc kích thước ảnh bé theo kích thước của mình
                tmpImg = this.decodeSampledBitmapFromUri(uri, bitmap.getWidth(), bitmap.getHeight(), getContentResolver());
            } catch (IOException e) {
                e.printStackTrace();
            }
            path = this.getPath(getContentResolver(), uri);
        }
        if (path == null) {
            return;
        }

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exif == null && tmpImg == null) {
            return;
        }

        Matrix mat = new Matrix();
        String exifDate = null;
        if (exif != null) {
            exifDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
            mat = Extensions.asMatrixByOrientation(exif);
        }

        Bitmap img = null;
        try {
            // làm rõ ảnh cho ảnh hiện lên ko bị mờ
            img = Bitmap.createBitmap(tmpImg, 0, 0, tmpImg.getWidth(), tmpImg.getHeight(), mat, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (img.getConfig() != null) {
            switch (img.getConfig()) {
                case RGBA_F16: // dạng chuẩn bitmap là ARGB 8888
                    img = img.copy(Bitmap.Config.ARGB_8888, true);
                    break;
            }
        }
        try {
            imageProfile.setImageBitmap(img);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cache != null) ((File) cache).delete();
    }




}