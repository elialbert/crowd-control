package com.test.cc1;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class crowdcontrol extends Activity {
	public ArrayList<String> ENTRIES = new ArrayList<String>();
	private LocationManager locationManager;
	public static String Username = "Nobody";
	public long Key = 0;
	
	public ArrayList<Userkey> uk = new ArrayList<Userkey>(); //arraylist of user->key tuples
	
	public Location oldloc = new Location("init");
	public Location curloc = new Location("cur");
	public ListView lv1;
	public EditText ed;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.cc_menu, menu);
	    return true;
	}
 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.user:
	        getUser();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void getUser() {
		final FrameLayout fl = new FrameLayout(this);
        final EditText input = new EditText(this);
        input.setGravity(Gravity.CENTER);

        fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        input.setText("");
        new AlertDialog.Builder(this)
        	.setView(fl)
        	.setTitle("Enter Username")
        	.setPositiveButton("OK", new DialogInterface.OnClickListener(){
        		@Override
        		public void onClick(DialogInterface d, int which) {
        			boolean found = false;
        			d.dismiss();
        			String curname = input.getText().toString();
        			Username = curname; //set the new username no matter what
        			for (Userkey i : uk) {
        				if (i.getUsername().equals(curname)) {
        					Key = i.getKey();
        					found = true;
        					break;
        				}
        			}
        			if (!found) {
        				Userkey userkey = new Userkey(curname, new Long(0));
        				Key = userkey.getKey();
        				uk.add(userkey); //put the new tuple in the array
        			}
        		}
        	})
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
        		@Override
        		public void onClick(DialogInterface d, int which) {
        			d.dismiss();
        		}
        	}).create().show();
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        lv1 = /*getListView(); */(ListView)findViewById(R.id.outputListView);
        ed = (EditText)findViewById(R.id.edtInput);
        //set up recurring geolocation updates
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoLoc(this));
		oldloc.setLatitude(71.0);
		oldloc.setLongitude(-122.0);
		curloc.setLatitude(71.0);
		curloc.setLongitude(-122.0);
        ENTRIES.add("test1");

        lv1.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ENTRIES));

        //ListView lv = getListView();
        lv1.setTextFilterEnabled(true);

        lv1.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
            // When clicked, show a toast with the TextView text
            Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                Toast.LENGTH_SHORT).show();
          }
         });
        
        //final EditText edittext = (EditText) findViewById(R.id.edtInput);
        ed.setOnKeyListener(new OnKeyListener() {
            @Override
        	public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                  // Perform action on key press
                  update(String.valueOf(ed.getText()), null);
                  return true;
                }
                return false;
            }

        });
    }
    
    //this called by GeoLoc, updates location and sends to server if loc has moved a bunch
    public void updateLoc(Location loc) {
    	if (oldloc.distanceTo(loc) < 10) { //see if we've moved 10 meters
    		curloc = loc; //update the curloc, but still compare to the oldloc
    		return;
    	}
    	else {
    		update("", loc);
    	}
    }
    
    public void update(String msg, Location loc) {
    	RestClient client = new RestClient("http://10.0.2.2:8888/cc1server"); //get that server
    	String ourLat = "";
    	String ourLon = "";
    	
    	//MESSAGE SENDING
    	if ((msg != null) && !msg.matches("^\\s*$")) { //we have a real message to send
    		ENTRIES.add(msg); //put the user's message on their own screen first, for a quicker feel
        	ed.setText("");
            lv1.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ENTRIES)); //display the new list item
            client.AddParam("content", msg);
    	}
    	else { //no message to send
    		client.AddParam("content", "");
    	}
    	
    	
    	//LOCATION UPDATING
    	if (loc == null) {
    		ourLat = Double.toString(curloc.getLatitude());
    		ourLon = Double.toString(curloc.getLongitude());
    	}
    	else { //only get here if we're updating location specifically
    		//if (oldloc.distanceTo(loc) < 10) { //see if we've moved 10 meters
        	//	curloc = loc;
    		//}
        	ourLat = Double.toString(loc.getLatitude());
    		ourLon = Double.toString(loc.getLongitude());
            
            oldloc = loc; //set the new previous location
            curloc = loc;
    	}
    	
    	
    	client.AddParam("latitude", ourLat);
        client.AddParam("longitude", ourLon);
        client.AddParam("username", Username);
        client.AddParam("key", String.valueOf(Key));
        
        try
        {
            client.Execute(RequestMethod.GET);
        }
        catch (Exception e)
        {
            ENTRIES.add(e.getMessage());
        }
 
        String response = client.getResponse();
        if ((response != null) && !response.matches("^\\s*$")) { //this should only run when there were waiting messages on the server
        	response2list(response); //add any queue entries to the user's list
        }

    }
    
    public void response2list(String response) {
    	String[] respf = response.split("[~]");
    	if (respf.length == 2) {
    		Key = Long.valueOf(respf[0]);
    		for (Userkey i : uk) {
    			if (i.getUsername().equals(Username)) {
    				i.setKey(Key);
    				break;
    			}
    		}
    		
    		response = respf[1];
    		Log.w("keystuff", " " + respf.length + " " + Key);
    	}
    	if ((response != null) && !response.matches("^\\s*$")) {
	    	String[] resp = response.split("[|]");
			Log.w("keystuff", "response: " + response + " " + resp + " " + resp.length);
	    	for (String i : resp) {
	    		ENTRIES.add(i);
	    	}
	    	lv1.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ENTRIES));
    	}
    }
}