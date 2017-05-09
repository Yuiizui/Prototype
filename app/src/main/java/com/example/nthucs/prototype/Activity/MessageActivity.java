package com.example.nthucs.prototype.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nthucs.prototype.MessageList.Commit;
import com.example.nthucs.prototype.MessageList.MessageAdapter;
import com.example.nthucs.prototype.R;
import com.example.nthucs.prototype.TabsBar.TabsController;
import com.example.nthucs.prototype.TabsBar.ViewPagerAdapter;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by admin on 2016/7/16.
 */
public class MessageActivity extends AppCompatActivity
            implements NavigationView.OnNavigationItemSelectedListener{

    private LoginButton loginButton;
    private CallbackManager mCallbackManager;
    private ProfileTracker mProfileTracker;
    private Menu menu;
    private AccessToken accessToken;
    public ArrayList<Commit> arrayOfCommit;
    public ListView listView;
    private Activity activity = MessageActivity.this;
    private int activityIndex = 3;
    private static final int MESSAGE_ACTIVITY = 3;
    private static final int SCAN_FOOD = 2;
    private static final int TAKE_PHOTO = 3;
    private static final String FROM_CAMERA = "scan_food";
    private static final String FROM_GALLERY = "take_photo";

    String httpUrl,postID,userName;
    static Bitmap img;

    // element for the bottom of the tab content
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Message");
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_message_nav);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//
//        View headerView = navigationView.getHeaderView(0);
//        TextView facebookUsername = (TextView) headerView.findViewById(R.id.Facebook_name);
//        facebookUsername.setText("Hello, "+LoginActivity.facebookName);
//        ProfilePictureView profilePictureView = (ProfilePictureView) headerView.findViewById(R.id.Facebook_profile_picture);
//        profilePictureView.setProfileId(LoginActivity.facebookUserID);

        doLoggedThings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        System.out.println(" onCreateOptionsMenu");
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(mCallbackManager.onActivityResult(requestCode, resultCode, data)) {
//            return;
//        }
    }

    // Initialize tab layout
    private void initializeTabLayout() {
        ViewPagerAdapter pagerAdapter =
                new ViewPagerAdapter(getSupportFragmentManager(), this);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        // set custom icon for every tab
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(pagerAdapter.getTabView(i));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println(" onResume");
        // Always select tab 3
        //selectTab(3);
    }

    // select specific tab
    private void selectTab(int index) {
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        tab.select();
    }


    private void doLoggedThings(){

        setContentView(R.layout.activity_message_nav);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        // Attach the adapter to a ListView
        listView = (ListView) findViewById(R.id.messageList);

        //check whether user logged
        if (Profile.getCurrentProfile() == null) {
            mProfileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                    // profile2 is the new profile
                    mProfileTracker.stopTracking();
                    userName = profile2.getName();
                }
            };
        }
        else{
            userName = Profile.getCurrentProfile().getName();
        }
        queryGraphAPI();


        // initialize tabLayout and viewPager
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        //initializeTabLayout();
        // call function to active tabs listener
        //TabsController tabsController = new TabsController(3, MessageActivity.this, tabLayout, viewPager);
        //tabsController.processTabLayout();

        //selectTab(3);

    }

    private void updateMenuTitles(String name) {

        MenuItem loginMenuItem = menu.findItem(R.id.login);
        loginMenuItem.setTitle(name);
    }

    private void queryGraphAPI(){
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        // Insert your code here
                        System.out.println("oncomplete");
                        System.out.println("accessToken "+accessToken);
                        updateMenuTitles(userName);
                        arrayOfCommit = new ArrayList<>();
                        System.out.println("reponse"+response.getJSONObject());
                        arrayOfCommit = handleJSON(response.getJSONObject());
                        // Create the adapter to convert the array to views
                        MessageAdapter adapter = new MessageAdapter(getApplicationContext(), arrayOfCommit);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                System.out.println("arraylist "+arrayOfCommit.get(position).getPostID());
                                postID = arrayOfCommit.get(position).getPostID();
                                Intent intent_chat = new Intent(getApplicationContext(), ChatRoomActivity.class);
                                intent_chat.putExtra("postID",postID);
                                startActivity(intent_chat);
                            }
                        });
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "application,comments,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private ArrayList handleJSON(JSONObject jsonObject){
        ArrayList<Commit> data = new ArrayList<>();
        try{
            JSONArray responseData = jsonObject.getJSONArray("data");
            for(int i=0;i<responseData.length();i++){
                JSONObject jsonTmp = responseData.getJSONObject(i);
                if(jsonTmp.optJSONObject("application")!=null){
                    if(jsonTmp.optJSONObject("application").getString("name").equals("Prototype")){
                        //posted img
                        httpUrl = jsonTmp.getString("picture");
                        Thread thread = new Thread(mutiThread);
                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(jsonTmp.optJSONObject("comments")!=null) {
                            //person 's comments
                            JSONArray comments = jsonTmp.getJSONObject("comments").getJSONArray("data");
                            int lastIndex = comments.length() - 1;
                            //person's name
                            String personName = comments.getJSONObject(lastIndex).getJSONObject("from").getString("name");
                            //person's comment
                            String personComment = comments.getJSONObject(lastIndex).getString("message");
                            //postID
                            String postID = jsonTmp.getString("id");
                            Commit commit = new Commit(img, personName, personComment,postID);
                            data.add(commit);
                        }
                    }
                }
            }
        }
        catch (JSONException e){
            System.out.println("JSONEXCEPTION "+e.toString());
            e.printStackTrace();
        }
        return data;
    }

    public static void getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            img = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            // Log exception
            System.out.println(e.toString());
        }
    }
    private Runnable mutiThread = new Runnable(){
        public void run(){
            // 運行網路連線的程式
            getBitmapFromURL(httpUrl);
        }
    };


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent intent_home = new Intent();
            intent_home.setClass(MessageActivity.this, HomeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("BACK", 1);
            intent_home.putExtras(bundle);
            startActivity(intent_home);
            finish();
        }
        else if (id == R.id.food_list) {
            // Handle the camera action
            Intent intent_main = new Intent();
            intent_main.setClass(MessageActivity.this, MainActivity.class);
            startActivity(intent_main);
            finish();
            //Toast.makeText(this, "Open food list", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.calendar) {
            Intent intent_calendar = new Intent();
            intent_calendar.setClass(MessageActivity.this, CalendarActivity.class);
            startActivity(intent_calendar);
            finish();
            //Toast.makeText(this, "Open calendar", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.Import) {
            selectImage();
            //Toast.makeText(this, "Import food", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.message) {
            Intent intent_message = new Intent();
            intent_message.setClass(MessageActivity.this, MessageActivity.class);
            startActivity(intent_message);
            finish();
            //Toast.makeText(this, "Send message", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.setting_list) {
            Intent intent_setting = new Intent();
            intent_setting.setClass(MessageActivity.this, SettingsActivity.class);
            startActivity(intent_setting);
            finish();
        } else if (id == R.id.blood_pressure){
            Intent intent_blood_pressure = new Intent();
            intent_blood_pressure.setClass(MessageActivity.this, MyBloodPressure.class);
            startActivity(intent_blood_pressure);
            finish();
        } else if (id == R.id.mail){
            Intent intent_mail = new Intent();
            intent_mail.setClass(MessageActivity.this, MailActivity.class);
            startActivity(intent_mail);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectImage(){
        final CharSequence[] items = { "Take with Camera", "Choose from Gallery", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                if (items[index].equals("Take with Camera")) {
                    if (activityIndex == MESSAGE_ACTIVITY) {
                        Intent intent_camera = new Intent("com.example.nthucs.prototype.TAKE_PICT");

                        activity.startActivityForResult(intent_camera, SCAN_FOOD);
                    } else {
                        // back to setting activity
                        Intent result = new Intent();
                        result.putExtra(FROM_CAMERA, SCAN_FOOD);
                        result.setClass(activity, MessageActivity.class);
                        activity.startActivity(result);
                        activity.finish();
                    }
                } else if (items[index].equals("Choose from Gallery")) {
                    if (activityIndex == MESSAGE_ACTIVITY) {
                        Intent intent_gallery = new Intent("com.example.nthucs.prototype.TAKE_PHOTO");
                        //intent_gallery.putParcelableArrayListExtra(calDATA, foodCalList);
                        activity.startActivityForResult(intent_gallery, TAKE_PHOTO);
                    } else {
                        // back to setting activity
                        Intent result = new Intent();
                        result.putExtra(FROM_GALLERY, TAKE_PHOTO);
                        result.setClass(activity, MessageActivity.class);
                        activity.startActivity(result);
                        activity.finish();
                    }
                } else if (items[index].equals("Cancel")) {
                    dialog.dismiss();
                    Intent intent = new Intent();
                    intent.setClass(MessageActivity.this, MessageActivity.class);
                    startActivity(intent);
                }
            }
        });
        builder.show();
    }
}
