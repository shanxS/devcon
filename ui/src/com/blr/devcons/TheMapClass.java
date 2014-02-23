package com.blr.devcons;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class TheMapClass extends Activity {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		setContentView(R.layout.themapclass);

		Button menu = (Button) findViewById(R.id.bMenu);
		menu.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent openStartingPoint = new Intent("android.intent.action.MENU");//takes the action name 
				startActivity(openStartingPoint);
				
			}
		});
		
		ImageView blip1 = (ImageView) findViewById(R.id.ivBlip1);
		ImageView blip2 = (ImageView) findViewById(R.id.ivBlip2);
		ImageView blip3 = (ImageView) findViewById(R.id.ivBlip3);
		blip1.setTranslationX(200);
		blip1.setTranslationY(400);

		blip2.setTranslationX(300);
		blip2.setTranslationY(700);

		blip3.setTranslationY(100);
		blip3.setTranslationY(500);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
