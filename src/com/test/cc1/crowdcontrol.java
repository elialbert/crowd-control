package com.test.cc1;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
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
	public static long Radius = 50;
	public long oldRad = 50;
	public ArrayList<Userkey> uk = new ArrayList<Userkey>(); //arraylist of user->key tuples
	public int menuChoice;
	public Location oldloc = new Location("init");
	public Location curloc = new Location("cur");
	public Location sendloc = new Location("send");
	public ListView lv1;
	public EditText ed;
	public String tosend;
	
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
	    	menuChoice = R.id.user;
	        getUserInput(R.id.user);
	        return true;
	    case R.id.radius:
	    	menuChoice = R.id.radius;
	    	getUserInput(R.id.radius);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void getUserInput(int itemId) { //this is clunky fix/factor it
		final FrameLayout fl = new FrameLayout(this);
        final EditText input = new EditText(this);
        String title = "";
        input.setGravity(Gravity.CENTER);
        if (itemId == R.id.user) 
        	title = "Enter username";
        if (itemId == R.id.radius)
        	title = "Enter radius of hearing";

        fl.addView(input, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        input.setText("");
        new AlertDialog.Builder(this)
        	.setView(fl)
        	.setTitle(title)
        	.setPositiveButton("OK", new DialogInterface.OnClickListener(){
        		@Override
        		public void onClick(DialogInterface d, int which) {
        			d.dismiss();
        			doMenu(input.getText().toString());       			
        		}
        	})
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
        		@Override
        		public void onClick(DialogInterface d, int which) {
        			d.dismiss();
        			menuChoice = 0;
        		}
        	}).create().show();
	}
	
	public void doMenu(String input) {
		if (menuChoice == R.id.user) {
			boolean found = false;
	        if (input.equals("")) {
	        	return;
	        }
	        Username = input; //set the new username no matter what
	        for (Userkey i : uk) {
				if (i.getUsername().equals(input)) {
					Key = i.getKey(); //client has saved key
					found = true;
					break;
				}
			}
			if (!found) {
				Userkey userkey = new Userkey(input, new Long(0)); //init from clientside with key=0
				Key = userkey.getKey();
				uk.add(userkey); //put the new tuple in the array
			}
			setTitle(Username + ", Radius set to " + Radius);
			return;
		}
		
		if (menuChoice == R.id.radius) {
	    	//if (input.matches("[/d+]")) {
	    	try {
	    		Radius = Long.valueOf(input);
	    		setTitle(Username + ", radius set to " + Radius);
	    		return;
	    	}
	    	catch (Exception e) {
	    		Radius = 50;
	    		setTitle(Username + ", radius error, radius set to 50");
	    	}
	    	//}
	    	return;
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,);
        //TextView title(TextView) = findViewById(android.R.id.title);
        
        lv1 = /*getListView(); */(ListView)findViewById(R.id.outputListView);
        ed = (EditText)findViewById(R.id.edtInput);
        //set up recurring geolocation updates
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoLoc(this));
		sendloc = null;
		oldloc.setLatitude(71.0);
		oldloc.setLongitude(-122.0);
		curloc.setLatitude(71.0);
		curloc.setLongitude(-122.0);
		tosend = "";

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
                  //update(String.valueOf(ed.getText()), null);
                	sendloc = curloc; //make sure we send the current location over as well
                	tosend = String.valueOf(ed.getText());
            		new serverTask().execute(tosend);
                	return true;
                }
                return false;
            }

        });
    }
    
    
    public class serverTask extends AsyncTask<String, Void, String> {
    	
    	//display the user's message
    	@Override
    	public void onPreExecute() {
    		if ((tosend != null) && !tosend.matches("^\\s*$")) {
    			ENTRIES.add(Username + ": " + tosend); //put the user's message on their own screen first, for a quicker feel
    			tosend = "";
    			ed.setText("");
    			lv1.setAdapter(new ArrayAdapter<String>(crowdcontrol.this, R.layout.list_item, ENTRIES)); //display the new list item
    		}
    	}
    	
    	//update the server and get back any waiting messages
        protected String doInBackground(String... inparams) {
            return update(inparams[0], sendloc);
        }
        
        //send any result to be printed
        protected void onPostExecute(String result) {
        	Log.w("SERVERTASK", result);
        	Log.w("post", "in thread?");
            crowdcontrol.this.response2list(result);
        }
    }
    
    
    
    //this called by GeoLoc, updates location and sends to server if loc has moved a bunch
    public void updateLoc(Location loc) {
    	if (oldloc.distanceTo(loc) < 10) { //see if we've moved 10 meters
    		curloc = loc; //update the curloc, but still compare to the oldloc
    		sendloc = null;
    	}
    	else {
    		curloc = loc;
    		sendloc = curloc;
    	}
    	new serverTask().execute(""); //hit the server using the gps loop as our driver loop
    }
    
    
    public String update(String msg, Location loc) {
    	//RestClient client = new RestClient("http://10.0.2.2:8888/cc1server"); //get that server
    	RestClient client = new RestClient("http://crowd-control.appspot.com/cc1server"); //get that server
    	String ourLat = "";
    	String ourLon = "";
    	
    	//MESSAGE SENDING
    	if ((msg != null) && !msg.matches("^\\s*$")) { //we have a real message to send
    		
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
        
        //send the radius if it has changed
        if (oldRad != Radius) {
        	client.AddParam("radius", String.valueOf(Radius));
        	oldRad = Radius;
        }
        
        try
        {
            client.Execute(RequestMethod.GET);
        }
        catch (Exception e)
        {
            ENTRIES.add(e.getMessage());
        }
 
        String response = client.getResponse();
        return response;
        /*if ((response != null) && !response.matches("^\\s*$")) { //this should only run when there were waiting messages on the server
        	response2list(response); //add any queue entries to the user's list
        */
    }
    
    public void response2list(String response) {
    	if ((response != null) && !response.matches("^\\s*$")) {
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
		    	for (int j = 0; j < resp.length - 1; j ++) {
		    		String i = resp[j];
		    		ENTRIES.add(i);
		    	}
		    	lv1.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ENTRIES));
		    	Log.w("THREADSARG", "not past here");
	    	}
	    	Log.w("THREADSARG", "or here either");
    	}
    }
}