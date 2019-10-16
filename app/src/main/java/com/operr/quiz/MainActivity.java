package com.operr.quiz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;
import com.operr.quiz.databinding.ActivityMainBinding;
import com.operr.quiz.service.BreakIntimationService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.variant.setText(BuildConfig.BUILD_TYPE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBreakService();
    }

    /**
     * Start foreground service
     */
    private void startBreakService() {

        String message;

        if (!BreakIntimationService.SERVICE_ENABLED) {
            /*
             *Create intent for BreakIntimationService
             */
            Intent breakIntent = new Intent(this, BreakIntimationService.class);

            startService(breakIntent); // start foreground service

            message = getString(R.string.break_service_started);
        } else
            message = getString(R.string.break_service_already_started);

        /*
         *Show alert status of Break Intimation Service
         */
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLite));
        snackbar.show();
    }
}
