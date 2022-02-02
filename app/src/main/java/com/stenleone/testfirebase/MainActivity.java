package com.stenleone.testfirebase;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String NUMBER_ID = "number";

    String verification;
    PhoneAuthProvider.ForceResendingToken refreshToken;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            if (e instanceof FirebaseAuthInvalidCredentialsException) {

            } else if (e instanceof FirebaseTooManyRequestsException) {

            }

        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            verification = verificationId;
            refreshToken = token;
            findViewById(R.id.codeText).setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
//        sendCode();
        setupEditText();
        setupNumberListener();
        setupSendNumberButton();
    }

    private void setupNumberListener() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(NUMBER_ID).document(NUMBER_ID).addSnapshotListener((value, error) -> {

            if (value != null) {
                try {
                    ArrayList<Long> numberData = (ArrayList<Long>) value.get("array");

                    LinearLayout container = findViewById(R.id.dataContainer);

                    container.removeAllViews();

                    for (int i = 0; i < numberData.size(); i++) {
                        TextView textNumberView = new TextView(this);
                        textNumberView.setText(String.valueOf(numberData.get(Integer.valueOf(i))));
                        container.addView(textNumberView);
                    }


                } catch (Exception e) {
                    Log.v("112233", "");
                }
            }

        });
    }

    private void setupSendNumberButton() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Button buttonAddNumber = findViewById(R.id.buttonAddNumber);

        buttonAddNumber.setOnClickListener(v -> {
            EditText numberInput = findViewById(R.id.numberInput);

            firestore.collection(NUMBER_ID).document(NUMBER_ID).get().addOnSuccessListener(value -> {

                if (value != null) {
                    try {
                        ArrayList<Long> numberData = (ArrayList<Long>) value.get("array");

                        try {
                            Map<String, Object> hashData = new HashMap<>();
                            numberData.add(Long.valueOf(numberInput.getText().toString()));
                            hashData.put("array", numberData);
                            firestore.collection(NUMBER_ID).document(NUMBER_ID).set(hashData);
                        } catch (Exception e) {
                            Toast.makeText(this, "We have error :)", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.v("112233", "");
                    }
                }

            });
        });
    }

    private void sendCode() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber("+380675899323")
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void setupEditText() {
        EditText codeInput = findViewById(R.id.codeText);
        Button buttonVerify = findViewById(R.id.buttonVerify);

        Thread thread = new Thread(() -> runOnUiThread(() -> {
            ObjectAnimator animator = ObjectAnimator.ofFloat(buttonVerify, "rotation", 0, 360);
            animator.setDuration(3000);
            animator.setStartDelay(2000);
            animator.setRepeatMode(ObjectAnimator.REVERSE);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.start();
        }));
        thread.start();

        buttonVerify.setOnClickListener(view -> {
            String code = codeInput.getText().toString();

            if (code != null && code != "") {
                FirebaseAuth.getInstance().signInWithCredential(PhoneAuthProvider.getCredential(verification, code)).addOnSuccessListener((OnSuccessListener) o -> {

                });
            }
        });
    }

}
