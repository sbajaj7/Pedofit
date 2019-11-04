package com.example.pedofit;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.pedofit.Adapter.StepAdapter;
import com.example.pedofit.Model.Steps;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Pedofit";
    private static final String PACKAGE_NAME = "com.google.android.apps.fitness";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private static List<Steps> biWeeklyStepsList = new ArrayList<>();

    private FitnessOptions fitnessOptions;
    private RecyclerView recyclerView;
    private AnimationDrawable anim;

    private ToggleButton toggleOrderButton;
    private TextView title_text_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIElements();
        initializeRecordingClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (anim != null && !anim.isRunning())
            anim.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (anim != null && anim.isRunning())
            anim.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
            }
        }
    }

    @CheckResult
    public boolean isFitInstalled() {
        try {
            getPackageManager().getPackageInfo(PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void initializeUIElements() {
        title_text_view = findViewById(R.id.title_text_view);
        toggleOrderButton = findViewById(R.id.toggleOrderButton);

        RelativeLayout container = findViewById(R.id.main_activity_view);
        anim = (AnimationDrawable) container.getBackground();
        anim.setEnterFadeDuration(6000);
        anim.setExitFadeDuration(2000);
        recyclerView = findViewById(R.id.rvWeeklySteps);
    }

    private void initializeRecordingClient() {
        fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            askPermissions();
        } else {
            subscribe();
        }
    }

    private void askPermissions() {
        GoogleSignIn.requestPermissions(
                this,
                REQUEST_OAUTH_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions);
    }

    /**
     * Records step data by requesting a subscription to background step data.
     */
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");

                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private void readData() {
        if (GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            if (isFitInstalled()) {
                Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                        .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                        .addOnSuccessListener(
                                new OnSuccessListener<DataSet>() {
                                    @Override
                                    public void onSuccess(DataSet dataSet) {
                                        long total = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                        Log.i(TAG, "Total steps: " + total);
                                        if (!dataSet.isEmpty())
                                            initializeBiWeeklyData();
                                        else
                                            Toast.makeText(getApplicationContext(), R.string.no_data_available, Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "There was a problem getting the step count.", e);
                                    }
                                });
            } else
                Toast.makeText(this, R.string.install_google_fit, Toast.LENGTH_SHORT).show();
        } else {
            askPermissions();
            Toast.makeText(this, R.string.select_google_account, Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeBiWeeklyData() {

        DataReadRequest readRequest = prepareBiWeeklyReadRequest();

        Task<DataReadResponse> response = Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).readData(readRequest);

        response.addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
            @Override
            public void onSuccess(DataReadResponse dataReadResponse) {
                //Used for aggregated data
                List<Bucket> buckets = dataReadResponse.getBuckets();
                if (buckets.size() > 0) {
                    Log.e("History", "Number of buckets: " + buckets.size());
                    for (Bucket bucket : buckets) {
                        List<DataSet> dataSets = bucket.getDataSets();
                        for (DataSet dataSet : dataSets) {
                            prepareDataSet(dataSet);
                        }
                    }
                    displayBiWeeklyStepData();
                }
                //Used for non-aggregated data
                else if (dataReadResponse.getDataSets().size() > 0) {
                    Log.e("History", "Number of returned DataSets: " + dataReadResponse.getDataSets().size());
                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                        prepareDataSet(dataSet);
                    }
                }
            }
        });
    }

    private DataReadRequest prepareBiWeeklyReadRequest() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime));
        Log.e("History", "Range End: " + dateFormat.format(endTime));

        //Check how many steps were walked and recorded in the last 7 days
        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    private void prepareDataSet(DataSet dataSet) {
        //Log.e("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                Log.e("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));

                Steps dailySteps = new Steps();
                Log.i(TAG,dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));

                long timeInMillis = getStartDateTimestamp(dp.getStartTime(TimeUnit.MILLISECONDS));

                dailySteps.setTimeStamp(dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                dailySteps.setSteps(dp.getValue(field).asInt());
                dailySteps.setTimestampMilliseconds(timeInMillis);
                if (!biWeeklyStepsList.contains(dailySteps))
                    biWeeklyStepsList.add(dailySteps);
            }
        }
    }

    private void displayBiWeeklyStepData() {
        StepAdapter stepAdapter = new StepAdapter(biWeeklyStepsList);
        stepAdapter.setHasStableIds(true);

        title_text_view.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(stepAdapter);
        stepAdapter.notifyDataSetChanged();
        toggleOrderButton.setChecked(false);
        toggleOrderButton.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_read_data) {
            readData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onToggleClicked(View view) {
        boolean IsChronological = ((ToggleButton) view).isChecked();

        if (!IsChronological) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(false);
            linearLayoutManager.setStackFromEnd(false);
            recyclerView.setLayoutManager(linearLayoutManager);
        } else {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);
        }
    }

    private long getStartDateTimestamp(long timeStamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);

        // Set time fields to zero
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
