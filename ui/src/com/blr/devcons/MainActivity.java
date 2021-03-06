package com.blr.devcons;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	ListBeaconsActivity lba = new ListBeaconsActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView iv=(ImageView)findViewById(R.id.ivLogo);
       iv.setImageResource(R.drawable.ic_launcher);
       final TextView tv = (TextView)findViewById(R.id.ed);
       Thread timer = new Thread(){
			public void run(){
				try{
					while (!lba.status) {
						tv.setText("no");
					}

					tv.setText("yes");
					Intent openStartingPoint = new Intent("android.intent.action.MENU");//takes the action name 
					startActivity(openStartingPoint);
					
				}catch(Exception e){
					e.printStackTrace();
					
				}finally{
					
				}
				
			}
			
		};
		timer.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}

/**
 * Displays list of found beacons sorted by RSSI.
 * Starts new activity with selected beacon if activity was provided.
 *
 * @author wiktorgworek@google.com (Wiktor Gworek)
 */
class ListBeaconsActivity extends Activity {
	
  public Boolean status = false; 

  private static final String TAG = ListBeaconsActivity.class.getSimpleName();

  public static final String EXTRAS_TARGET_ACTIVITY = "extrasTargetActivity";
  public static final String EXTRAS_BEACON = "extrasBeacon";

  private static final int REQUEST_ENABLE_BT = 1234;
  private static final String ESTIMOTE_BEACON_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
  private static final String ESTIMOTE_IOS_PROXIMITY_UUID = "8492E75F-4FD6-469D-B132-043FE94921D8";
  private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

  private BeaconManager beaconManager;
  private LeDeviceListAdapter adapter;
  private TextView message;
  Boolean flag = true;

  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    // Configure device list.
    adapter = new LeDeviceListAdapter(this);
    ListView list = (ListView) findViewById(R.id.ed);
    list.setAdapter(adapter);
    list.setOnItemClickListener(createOnItemClickListener());

    // Configure verbose debug logging.
    L.enableDebugLogging(true);

    // Configure BeaconManager.
    beaconManager = new BeaconManager(this);
    beaconManager.setRangingListener(new BeaconManager.RangingListener() {
      @Override
      public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
        // Note that results are not delivered on UI thread.
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // Note that beacons reported here are already sorted by estimated
            // distance between device and beacon.
            List<Beacon> estimoteBeacons = filterBeacons(beacons);
            if (estimoteBeacons.size() > 0)
            	status = true;
            else
            	status = false;
            
            adapter.replaceWith(estimoteBeacons);
            
            
          }
        });
      }
    });
  }

  private List<Beacon> filterBeacons(List<Beacon> beacons) {
    List<Beacon> filteredBeacons = new ArrayList<Beacon>(beacons.size());
    for (Beacon beacon : beacons) {
      if (beacon.getProximityUUID().equalsIgnoreCase(ESTIMOTE_BEACON_PROXIMITY_UUID)
          || beacon.getProximityUUID().equalsIgnoreCase(ESTIMOTE_IOS_PROXIMITY_UUID)) {
        filteredBeacons.add(beacon);
      }
    }
    return filteredBeacons;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem refreshItem = menu.findItem(R.id.ed);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    beaconManager.disconnect();

    super.onDestroy();
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Check if device supports Bluetooth Low Energy.
    if (!beaconManager.hasBluetooth()) {
      Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
      return;
    }

    // If Bluetooth is not enabled, let user enable it.
    if (!beaconManager.isBluetoothEnabled()) {
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    } else {
      connectToService();
    }
  }

  @Override
  protected void onStop() {
    try {
      beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
    } catch (RemoteException e) {
      Log.d(TAG, "Error while stopping ranging", e);
    }

    super.onStop();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_ENABLE_BT) {
      if (resultCode == Activity.RESULT_OK) {
        connectToService();
      } else {
        Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
        getActionBar().setSubtitle("Bluetooth not enabled");
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void connectToService() {
    getActionBar().setSubtitle("Scanning...");
    adapter.replaceWith(Collections.<Beacon>emptyList());
    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
      @Override
      public void onServiceReady() {
        try {
          beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (RemoteException e) {
          Toast.makeText(ListBeaconsActivity.this, "Cannot start ranging, something terrible happened",
              Toast.LENGTH_LONG).show();
          Log.e(TAG, "Cannot start ranging", e);
        }
      }
    });
  }

  private AdapterView.OnItemClickListener createOnItemClickListener() {
    return new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY) != null) {
          try {
            Class<?> clazz = Class.forName(getIntent().getStringExtra(EXTRAS_TARGET_ACTIVITY));
            Intent intent = new Intent(ListBeaconsActivity.this, clazz);
            intent.putExtra(EXTRAS_BEACON, adapter.getItem(position));
            startActivity(intent);
          } catch (ClassNotFoundException e) {
            Log.e(TAG, "Finding class by name failed", e);
          }
        }
      }
    };
  }

}
