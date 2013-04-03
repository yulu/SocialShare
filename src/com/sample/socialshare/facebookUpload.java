package com.sample.socialshare;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;

public class facebookUpload extends Activity{
	/**
	 * Attributes
	 */
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private final String PENDING_ACTION_BUNDLE_KEY = "com.sample.socialshare.facebookupload:PendingAction";
	
	private Button fbUploadButton;
	private LoginButton loginButton;
	private ProfilePictureView profilePictureView;
	private PendingAction pendingAction = PendingAction.NONE;
	private GraphUser user;
	private enum PendingAction{NONE, POST_PHOTO};
	private UiLifecycleHelper uiHelper;
	private String bitmapPath;
	private ProgressDialog dialog;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);      
        }
        
        Intent i = getIntent();
        bitmapPath = i.getStringExtra("bitmapPath");
        
		setContentView(R.layout.fb); 
		
		/**
         * Log in 
         */
        loginButton = (LoginButton) findViewById(R.id.fbLogin);
        loginButton.setPublishPermissions(PERMISSIONS);
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                facebookUpload.this.user = user;
                updateUI();
            }
        });
        //request permission as log in
       
        
        /**
         * profile information
         */
        profilePictureView = (ProfilePictureView) findViewById(R.id.fbPicture);
        
        /**
         * upload button
         */
        fbUploadButton = (Button) findViewById(R.id.fbUpload);
        fbUploadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	performPublish(PendingAction.POST_PHOTO);
            }
        });
		
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        updateUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    /**
     * @param login session
     * @param state
     * @param exception
     */
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
                new AlertDialog.Builder(facebookUpload.this)
                    .setTitle("cancel")
                    .setMessage("permission not granted")
                    .setPositiveButton("ok", null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
        updateUI();
    }
    
    /**
     * update UI with profile photo and greeting
     */
    private void updateUI() {
        Session session = Session.getActiveSession();
        boolean enableButtons = (session != null && session.isOpened());

        fbUploadButton.setEnabled(enableButtons);

        if (enableButtons && user != null) {
            profilePictureView.setProfileId(user.getId());
        } else {
            profilePictureView.setProfileId(null);
        }
    }
    
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        switch (previouslyPendingAction) {
            case POST_PHOTO:
                postPhoto();
                break;
        }
    }
    
    private void postPhoto() {
        if (hasPublishPermission()) {
            Bitmap image = BitmapFactory.decodeFile(bitmapPath);

            Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), image, new Request.Callback() {
                @Override
                public void onCompleted(Response response) {
                    showPublishResult(response.getError());
                    dialog.dismiss();
                }
      
            });
            //add progress bar here!
            showDialog(0);
            request.executeAsync();
            
        } else {
            pendingAction = PendingAction.POST_PHOTO;
        }
    }
    
    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }

    private void performPublish(PendingAction action) {
        Session session = Session.getActiveSession();
        if (session != null) {
           // pendingAction = action;
            if (hasPublishPermission()) {
                // We can do the action right away.
        		pendingAction = PendingAction.POST_PHOTO;
                handlePendingAction();
            } 
        }
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
    
    /**
     * display upload result
     * @param error
     */
    private void showPublishResult(FacebookRequestError error) {

        if (error == null) {
            Toast.makeText(facebookUpload.this, "Upload Successful", Toast.LENGTH_SHORT)
            .show();
        } else {
            Toast.makeText(facebookUpload.this, "Upload Error, Try Again", Toast.LENGTH_SHORT)
            .show();
        }

    }
    
	
}
