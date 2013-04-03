package com.sample.socialshare;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.AccountAPI;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.net.RequestListener;

public class weiboUpload extends Activity{
	private Weibo mWeibo;
    private static final String REDIRECT_URL = "http://www.sina.com";
	private String bitmapPath;
	private String profileImageUrl;
	private Context context;
	private Bitmap userProfile;
	private ImageView userDiaplay;
	private long userId;
	private Button postPhotoButton;
	private ImageButton loginButton;
	private int logState = 0;
    public static Oauth2AccessToken accessToken;
	private ProgressDialog dialog;
	
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		context = this.getBaseContext();
		Intent i = getIntent();
	    bitmapPath = i.getStringExtra("bitmapPath");
		setContentView(R.layout.wb); 
		
		Resources res = getResources();
		mWeibo = Weibo.getInstance(res.getString(R.string.wb_app_id), REDIRECT_URL);
        loginButton = (ImageButton) findViewById(R.id.wbLogin);
        userDiaplay = (ImageView)findViewById(R.id.wbProfile);
        
        /**
         * automatically log in
         */
        mWeibo.authorize(weiboUpload.this, new AuthDialogListener());
        
        loginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	if(logState == 0){
            		mWeibo.authorize(weiboUpload.this, new AuthDialogListener());
            	}
            	else{
            		AccessTokenKeeper.clear(weiboUpload.this);
            		logState = 0;
            		userDiaplay.setImageResource(R.drawable.wbprofile);
            		CookieSyncManager.createInstance(weiboUpload.this.context);
            		CookieManager.getInstance().removeAllCookie();
            		loginButton.setImageResource(R.drawable.wb_login_up);
            	}
            }
            
        });
        
        /**
         * post photo
         */
        postPhotoButton = (Button)findViewById(R.id.wbUpload);
        postPhotoButton.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		postPhoto();
        	}
        });
	}
	
	private void postPhoto(){
		StatusesAPI api = new StatusesAPI(weiboUpload.accessToken);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String currentDateandTime = sdf.format(new Date());
		
		String user_input="#upload_test#"+currentDateandTime;
		
		if(logState == 1)
		{
			runOnUiThread(new Runnable() {
				public void run() {
					showDialog(0);
				}
			});
			api.upload(user_input, bitmapPath, "0.0", "0.0", new RequestListener(){
			@Override
			public void onIOException(IOException arg0){
				//TODO
			}
			
			public void onError(WeiboException arg0){
				/**
				 * update ui in a runnable
				 */				
				runOnUiThread(new Runnable() {
					public void run() {
						dialog.dismiss();
						Toast.makeText(weiboUpload.this, "Unsuccessful, you may repeat!", Toast.LENGTH_SHORT)
	                        .show();
					}
				});
			}
			
			public void onComplete(String arg0){
				/**
				 * update ui in a runnable
				 */
				runOnUiThread(new Runnable() {
					public void run() {
						dialog.dismiss();
						Toast.makeText(weiboUpload.this, "Upload Successful", Toast.LENGTH_SHORT)
                        .show();
					}
				});
			}
		});}else{
			/**
			 * update ui in a runnable
			 */
			runOnUiThread(new Runnable() {
				public void run() {
					
					Toast.makeText(weiboUpload.this, "Log In Please!", Toast.LENGTH_SHORT)
                    .show();
				}
			});
		}
	}
	
	public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void getProfilePhoto(){
    	
    	AccountAPI account = new AccountAPI(weiboUpload.accessToken);
    	account.getUid(new accoutListener());	
    	
    	
    }
    
    /**
     * display progress bar
     */
    @Override
    protected Dialog onCreateDialog(int id) {
    	
    	if(id == 0)
    	{
    		dialog = new ProgressDialog(this);
    		dialog.setMessage("Uploading...");
    		dialog.setIndeterminate(true);
    		dialog.setCancelable(true);
    		return dialog;
    	}else{
    		return null;
    	}
    }
    
    class accoutListener implements RequestListener{

		@Override
		public void onComplete(String result) {
            try {
				JSONObject ja = new JSONObject(result);

				userId = ja.getLong("uid");
				UsersAPI user = new UsersAPI(weiboUpload.accessToken);
		    	user.show(userId, new RequestListener(){

					@Override
					public void onComplete(String result) {
						// TODO Auto-generated method stub
						 // CONVERT RESPONSE STRING TO JSON ARRAY
			            try {
							JSONObject ja = new JSONObject(result);

							profileImageUrl = ja.getString("avatar_large");
							userProfile = getBitmapFromURL(profileImageUrl);
							
							runOnUiThread(new Runnable() {
		    					public void run() {
		    							if(profileImageUrl != null){
		    									
		    									userDiaplay.setImageBitmap(userProfile);
		    							}
		    							}});
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			 
					}

					@Override
					public void onError(WeiboException arg0) {

						
					}

					@Override
					public void onIOException(IOException arg0) {

					}
					});
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 
		}

		@Override
		public void onError(WeiboException arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onIOException(IOException arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    class AuthDialogListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            String token = values.getString("access_token");
            String expires_in = values.getString("expires_in");
            weiboUpload.accessToken = new Oauth2AccessToken(token, expires_in);
            if (weiboUpload.accessToken.isSessionValid()) {

            	AccessTokenKeeper.keepAccessToken(weiboUpload.this,
                        accessToken);
                getProfilePhoto();
        		logState = 1;
        		runOnUiThread(new Runnable() {
					public void run() {
        		loginButton.setImageResource(R.drawable.wb_logout_up);
					}
        		});
                Toast.makeText(weiboUpload.this, "Login Succefully", Toast.LENGTH_SHORT)
                        .show();  

            }
        }

        @Override
        public void onError(WeiboDialogError e) {
            Toast.makeText(getApplicationContext(),
                    "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), "Auth cancel",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(getApplicationContext(),
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }

    }
    
}
