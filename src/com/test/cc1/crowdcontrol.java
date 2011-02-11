package com.test.cc1;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class crowdcontrol extends Activity {
	public ArrayList<String> ENTRIES = new ArrayList<String>();
	private LocationManager locationManager;
	public static String Username = "Eli3";
	public Location oldloc = new Location("init");
	public Location curloc = new Location("cur");
	public ListView lv1;
	public EditText ed;

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
    
    /*
    public void updateMsg(String msg) {
    	if (msg.equals("")) {
    		return;
    	}
    	ENTRIES.add(msg); //put the user's message on their own screen first, for a quicker feel
    	ed.setText("");
        lv1.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ENTRIES)); //display the new list item
    	String ourLat = Double.toString(curloc.getLatitude());
		String ourLon = Double.toString(curloc.getLongitude());
    	RestClient client = new RestClient("http://10.0.2.2:8888/cc1server");
        client.AddParam("content", msg);
        client.AddParam("latitude", ourLat);
        client.AddParam("longitude", ourLon);
        client.AddParam("username", Username);
        
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
        	response2list(response); //add any waiting queue entries to the user's list
        }
    }
    
    */
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
    	String[] resp = response.split("[|]");
    	for (String i : resp) {
    		ENTRIES.add(i);
    	}
    	lv1.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ENTRIES));
    }
}