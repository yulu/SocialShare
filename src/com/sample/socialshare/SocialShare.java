package com.sample.socialshare;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class SocialShare extends Activity {
	private static final int SELECT_PICTURE = 1;
	private String selectedImagePath; 
	private ImageView img; 
	Button facebookButton;
	Button weiboButton;

	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.main); 
		
		/**
		 * The facebook and weibo upload buttons, triger new activity for upload
		 */
		facebookButton = (Button)findViewById(R.id.facebook_upload);
		weiboButton = (Button)findViewById(R.id.weibo_upload);
		
		facebookButton.setVisibility(View.INVISIBLE);
		facebookButton.setEnabled(false);
		weiboButton.setVisibility(View.INVISIBLE);
		weiboButton.setEnabled(false);
		
		facebookButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0){
				Intent intent = new Intent(SocialShare.this, facebookUpload.class);
				intent.putExtra("bitmapPath", selectedImagePath);
				startActivity(intent);
			}
		});
		
		weiboButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0){
				Intent intent = new Intent(SocialShare.this, weiboUpload.class);
				intent.putExtra("bitmapPath", selectedImagePath);
				startActivity(intent);
				
			}
		});
		
		/**
		 * Load image from gallary button
		 */
		img = (ImageView)findViewById(R.id.select_image); 
		((Button) findViewById(R.id.button)) .setOnClickListener(new OnClickListener() { 
			public void onClick(View arg0) { 
				Intent intent = new Intent(); 
				intent.setType("image/*"); 
				intent.setAction(Intent.ACTION_GET_CONTENT); 
				startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PICTURE); 
			} 
		}); 
	} 

	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		if (resultCode == RESULT_OK) { 
			if (requestCode == SELECT_PICTURE) { 
				Uri selectedImageUri = data.getData(); 
				selectedImagePath = getPath(selectedImageUri); 
				img.setImageURI(selectedImageUri); 
				
				facebookButton.setVisibility(View.VISIBLE);
				facebookButton.setEnabled(true);
				weiboButton.setVisibility(View.VISIBLE);
				weiboButton.setEnabled(true);
			} 
		} 
	} 

	protected String getPath(Uri uri) { 
		String[] projection = { MediaStore.Images.Media.DATA }; 
		Cursor cursor = managedQuery(uri, projection, null, null, null); 
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
		cursor.moveToFirst(); 
		return cursor.getString(column_index); 
	} 

}
