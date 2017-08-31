package com.example.nthucs.prototype.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nthucs.prototype.R;
import com.example.nthucs.prototype.SportList.Sport;
import com.example.nthucs.prototype.SportList.SportDAO;
import com.example.nthucs.prototype.TabsBar.ViewPagerAdapter;
import com.example.nthucs.prototype.Utility.FitnessActivity;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;



/**
 * Created by selab on 2016/8/15.
 */
public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    public static final String TAG = "Prototype";
    private TextView exerciseTime,exerciseSteps,exerciseCalories,exerciseDistance;
    public static GoogleApiClient mClient = null;
    private ProgressDialog pd;
    int totalSteps = 0;
    int BACK = 0;
    private Activity activity = HomeActivity.this;
    private int activityIndex = 0;
    private static final int HOME_ACTIVITY = 0;
    private static final int SCAN_FOOD = 2;
    private static final int TAKE_PHOTO = 3;
    private static final String FROM_CAMERA = "scan_food";
    private static final String FROM_GALLERY = "take_photo";
    Float totalCals = (float)0, totalDistance =(float) 0;
    long activityTime=0;

    // element for the bottom of the tab content
    private ViewPager viewPager;
    private TabLayout tabLayout;

    //object to record FitnessActivity
    private FitnessActivity fa;
    SportDAO SD;
    public ArrayList<FitnessActivity> fitnessProperties = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("主頁");
        setContentView(R.layout.activity_home_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        BACK = bundle.getInt("BACK");

        initLayout();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView facebookUsername = (TextView) headerView.findViewById(R.id.Facebook_name);
        facebookUsername.setText("Hello, "+LoginActivity.facebookName);
        ProfilePictureView profilePictureView = (ProfilePictureView) headerView.findViewById(R.id.Facebook_profile_picture);
        profilePictureView.setProfileId(LoginActivity.facebookUserID);


        // initialize tabLayout and viewPager
        //viewPager = (ViewPager)findViewById(R.id.viewPager);
        //tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        //initializeTabLayout();
        // call function to active tabs listener
        //TabsController tabsController = new TabsController(3, HomeActivity.this, tabLayout, viewPager);
        //tabsController.processTabLayout();
        if (BACK == 0) pd = ProgressDialog.show(HomeActivity.this,"計算中","取得資料...",true);
        SD = new SportDAO(getApplicationContext());
        //selectTab(1);
        if (BACK == 0) buildFitnessClient();
    }


    @Override
    public void onResume(){
        super.onResume();
        //init fitness value
        totalSteps = 0;
        totalCals = (float)0;
        totalDistance =(float) 0;
        activityTime=0;
        fitnessProperties.clear();
    }


    private void initLayout(){
        exerciseTime = (TextView)findViewById(R.id.exerciseTime);
        exerciseSteps = (TextView)findViewById(R.id.exerciseSteps);
        exerciseCalories = (TextView)findViewById(R.id.exerciseCalories);
        exerciseDistance = (TextView)findViewById(R.id.exerciseDistance);
    }


    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.  What to do?
                                // Look at some data!!
                                new InsertAndVerifyDataTask().execute();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    pd.dismiss();
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                    pd.dismiss();
                                }
                            }
                        }
                )
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(TAG, "Google Play services connection failed. Cause: " +
                                result.toString());
                        Toast.makeText(getApplicationContext(), "Google Play services connection failed. Cause:"+result.toString(), Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                })
                .build();
    }

    private class InsertAndVerifyDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            // Begin by creating the query.
            DataReadRequest readRequest = queryFitnessData();
            // [START read_dataset]
            // Invoke the History API to fetch the data with the query and await the result of
            // the read request.
            DataReadResult dataReadResult =
                    Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            System.out.println("READRESULT "+dataReadResult);
            // [END read_dataset]

            // For the sake of the sample, we'll print the data so we can see what we just added.
            // In general, logging fitness information should be avoided for privacy reasons.
            printSpeData(dataReadResult);
            //printSpeData(dataReadResult);

            return null;
        }

        @Override
        protected void onPostExecute(Void unused)
        {
            super.onPostExecute(unused);
            exerciseSteps.setText(getString(R.string.homeTextView,totalSteps,""));
            exerciseCalories.setText(getString(R.string.homeTextView,Math.round(totalCals),""));
            exerciseDistance.setText(getString(R.string.homeTextView,Math.round(totalDistance),""));
            exerciseTime.setText(getString(R.string.homeTextView,activityTime/60,""));
            pd.dismiss();

            updateSport();

        }
    }

    public static DataReadRequest queryFitnessData() {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.WEEK_OF_YEAR, -1);


        // today
        Calendar midNight = new GregorianCalendar();
        midNight.set(Calendar.HOUR_OF_DAY, 0);
        midNight.set(Calendar.MINUTE, 0);
        midNight.set(Calendar.SECOND, 0);
        midNight.set(Calendar.MILLISECOND, 0);
        long startTime = midNight.getTimeInMillis();
        // next day
        midNight.add(Calendar.DAY_OF_MONTH, 1);
        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                //.aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                //.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .read(ESTIMATED_STEP_DELTAS)
                .read(DataType.TYPE_ACTIVITY_SEGMENT)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .read(DataType.TYPE_DISTANCE_DELTA)
                //.bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime,System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]
        return readRequest;
    }

    public void printSpeData(DataReadResult dataReadResult){

        if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }

    }


    // [START parse_dataset]
    private void dumpDataSet(DataSet dataSet) {

        System.out.println(dataSet.getDataType().getName());
        System.out.println(dataSet.getDataType().getFields());
        int index = 0;
        for (DataPoint dp : dataSet.getDataPoints()) {
            for(Field field : dp.getDataType().getFields()) {
                if (dataSet.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                    totalSteps += dp.getValue(field).asInt();
                } else if (dataSet.getDataType().equals(DataType.TYPE_CALORIES_EXPENDED)) {
                    //System.out.println("CALORIESTIMESTAMP = "+dp.getTimestamp(TimeUnit.SECONDS));
                    long tmp = dp.getEndTime(TimeUnit.SECONDS) - dp.getStartTime(TimeUnit.SECONDS);
                    totalCals += dp.getValue(field).asFloat();
                    if(index<fitnessProperties.size()) {
                        if (fitnessProperties.get(index).activityTimeStamp == dp.getTimestamp(TimeUnit.MILLISECONDS)) {
                            fitnessProperties.get(index).setActivityExpenditure(dp.getValue(field).asFloat());
                            ++index;
                        }
                    }
                } else if (dataSet.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
                    totalDistance += dp.getValue(field).asFloat();
                } else{
                    String activityName = FitnessActivities.getName(Integer.parseInt(dp.getValue(field).toString()));
                    System.out.println(activityName);
                    if(!(activityName.equals("still")||activityName.equals("in_vehicle"))) {
                        long tmpTime = dp.getEndTime(TimeUnit.SECONDS) - dp.getStartTime(TimeUnit.SECONDS);
                        FitnessActivity tmpFA = new FitnessActivity(activityName,tmpTime,dp.getTimestamp(TimeUnit.MILLISECONDS));
                        fitnessProperties.add(tmpFA);
                        activityTime += tmpTime;
                    }
                }
            }

        }

    }
    // [END parse_dataset]

    // Initialize tab layout
    private void initializeTabLayout() {
        ViewPagerAdapter pagerAdapter =
                new ViewPagerAdapter(getSupportFragmentManager(), this);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        //disable tab indicator color
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(getApplicationContext(),android.R.color.transparent));
        // set custom icon for every tab
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(pagerAdapter.getTabView(i));
            }
        }
    }

    private void updateSport(){

        for(int i=0;i<fitnessProperties.size();++i){
            if(!checkIsExist(fitnessProperties.get(i).activityTimeStamp)) {
                Sport sp = new Sport();
                sp.setId(fitnessProperties.get(i).activityTimeStamp);
                sp.setUserID(Long.parseLong(LoginActivity.facebookUserID));
                sp.setTitle(fitnessProperties.get(i).activityName);
                sp.setCalorie(fitnessProperties.get(i).activityExpenditure);
                sp.setTotalTime(TimeUnit.SECONDS.toMillis(fitnessProperties.get(i).activityTime));
                sp.setDatetime(new Date().getTime());
                SD.insert(sp);
            }
        }
    }
    //true means id exist, false means id not exist
    private boolean checkIsExist(long id){
        List<Sport> result = SD.getAll();
        for(int j=0;j<result.size();++j){
            if(result.get(j).getId()==id){
                return true;
            }
        }
        return false;
    }

    // select specific tab
    private void selectTab(int index) {
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        tab.select();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent intent_home = new Intent();
            intent_home.setClass(HomeActivity.this, HomeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("BACK", 1);
            intent_home.putExtras(bundle);
            startActivity(intent_home);
            finish();
        } else if (id == R.id.food_list) {
            // Handle the camera action
            Intent intent_main = new Intent();
            intent_main.setClass(HomeActivity.this, MainActivity.class);
            startActivity(intent_main);
            finish();
            //Toast.makeText(this, "Open food list", Toast.LENGTH_SHORT).show();
        } //else if (id == R.id.calendar) {
//            Intent intent_calendar = new Intent();
//            intent_calendar.setClass(HomeActivity.this, CalendarActivity.class);
//            startActivity(intent_calendar);
//            finish();
        //Toast.makeText(this, "Open calendar", Toast.LENGTH_SHORT).show();
//        }
        else if (id == R.id.Import) {
            selectImage();
            //Toast.makeText(this, "Import food", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.message) {
            Intent intent_message = new Intent();
            intent_message.setClass(HomeActivity.this, MessageActivity.class);
            startActivity(intent_message);
            finish();
            //Toast.makeText(this, "Send message", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.setting_list) {
            Intent intent_setting = new Intent();
            intent_setting.setClass(HomeActivity.this, SettingsActivity.class);
            startActivity(intent_setting);
            finish();
        } else if (id == R.id.blood_pressure){
            Intent intent_blood_pressure = new Intent();
            intent_blood_pressure.setClass(HomeActivity.this, MyBloodPressure.class);
            startActivity(intent_blood_pressure);
            finish();
        } else if (id == R.id.mail){
            Intent intent_mail = new Intent();
            intent_mail.setClass(HomeActivity.this, MailActivity.class);
            startActivity(intent_mail);
            finish();
        } else if (id == R.id.new_calendar){
            Intent intent_new_calendar = new Intent();
            intent_new_calendar.setClass(HomeActivity.this, NewCalendarActivity.class);
            startActivity(intent_new_calendar);
            finish();
        } else if (id == R.id.chat) {
            Intent intent_chat = new Intent();
            intent_chat.setClass(HomeActivity.this, ChatBotActivity.class);
            startActivity(intent_chat);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectImage(){
        final CharSequence[] items = { "照相", "從相簿中選取", "取消" };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("新增食物");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                if (items[index].equals("照相")) {
                    if (activityIndex == HOME_ACTIVITY) {
                        /*Intent intent_camera = new Intent("com.example.nthucs.prototype.TAKE_PICT");
                        activity.startActivityForResult(intent_camera, SCAN_FOOD);*/
                        Intent result = new Intent();
                        result.putExtra(FROM_CAMERA, SCAN_FOOD);
                        result.setClass(activity, MainActivity.class);
                        activity.startActivity(result);
                        activity.finish();
                    } /*else {
                        // back to home activity
                        Intent result = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putInt("BACK", 1);
                        result.putExtra(FROM_CAMERA, SCAN_FOOD);
                        result.putExtras(bundle);
                        result.setClass(activity, MainActivity.class);
                        activity.startActivity(result);
                        activity.finish();
                    }*/
                } else if (items[index].equals("從相簿中選取")) {
                    if (activityIndex == HOME_ACTIVITY) {
                        /*Intent intent_gallery = new Intent("com.example.nthucs.prototype.TAKE_PHOTO");
                        activity.startActivityForResult(intent_gallery, TAKE_PHOTO);*/
                        Intent result = new Intent();
                        result.putExtra(FROM_GALLERY, TAKE_PHOTO);
                        result.setClass(activity, MainActivity.class);
                        activity.startActivity(result);
                        activity.finish();
                    }/* else {
                        // back to home activity
                        Intent result = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putInt("BACK", 1);
                        result.putExtra(FROM_GALLERY, TAKE_PHOTO);
                        result.putExtras(bundle);
                        result.setClass(activity, MainActivity.class);
                        activity.startActivity(result);
                        activity.finish();
                    }*/
                } else if (items[index].equals("取消")) {
                    dialog.dismiss();
//                    Intent intent = new Intent();
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("BACK", 1);
//                    intent.putExtras(bundle);
//                    intent.setClass(HomeActivity.this, HomeActivity.class);
//                    startActivity(intent);
                }
            }
        });
        builder.show();
    }

}
