package com.example.cellidOcid;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivityOcid extends Activity {
    
	  TelephonyManager tm;	// global-> can be modified in onCreate() onPause() onResume() and class MyPhoneStateListener 
	  MyPhoneStateListener mylistener; // global
	  String mcc;// mobile country code
	  String mnc;// mobile network code
	  RadioButton openCellID;
	  RadioButton googleDB;
	  //EditText Login;
	 
	  EditText fieldCID;
	  EditText fieldLAC;
	  File root = null;
	  File file;
	  File fileDir ;
	  
	  List<NeighboringCellInfo> NeighboringList ;
	  List<Map<String,String>> NeighboringListDeal ;
	  GsmCellLocation curCellLocation;
	  
	  
	  int dBm;
	  
	  LocationManager loctionManager;
      String provider;
	  LocationListener locationListener ;
	  Location location;
		
      @Override
      public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        fieldCID = (EditText)findViewById(R.id.editCID);
        fieldLAC = (EditText)findViewById(R.id.editLAC);
        
        tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); // telephony manager instance 
        mylistener = new MyPhoneStateListener(); // phonestatelistener instance
        tm.listen(mylistener, PhoneStateListener.LISTEN_CELL_LOCATION|PhoneStateListener.LISTEN_SERVICE_STATE|PhoneStateListener.LISTEN_SIGNAL_STRENGTHS); // set the specific listeners
        
        //obtain the object of LocationManager from system service
      	loctionManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE); 
        
      	
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//fine accuracy
        criteria.setAltitudeRequired(false);//altitude not required
        criteria.setBearingRequired(false);// bearing not required
        criteria.setCostAllowed(true);//allow the network operator cost
        criteria.setPowerRequirement(Criteria.POWER_LOW);//low power consumption
      	// get the best provider in accuracy from the providers available
        provider = loctionManager.getBestProvider(criteria, true);
        locationListener = new LocationListener() {
	 		public void onStatusChanged(String provider, int status, Bundle extras) {
	 		}
	 		public void onProviderEnabled(String provider) {
	 			MainActivityOcid.this.location = loctionManager.getLastKnownLocation(provider);
	 		}
	 		public void onProviderDisabled(String provider) {
	 			MainActivityOcid.this.location=null;
	 		}
	 		//When location changes
	 		public void onLocationChanged(Location location) {
	 			MainActivityOcid.this.location=location;
	 		}
	 	};
	 	
        // create location manager
        ////////////////////////////////////////////////////
		TextView textneighboring = (TextView)findViewById(R.id.neighboring);  // create a textview instance,the textview class is already defined in the XML file.
        textneighboring.setSingleLine(false);   // multiple line textview widget
		NeighboringList = tm.getNeighboringCellInfo(); // using the tm instance, create nblist of type List
		
		String stringNeighboring = "Neighbouring Cell ID List \n";  // create a string abt nblist for printing out the cid and rssi
		
		root = Environment.getExternalStorageDirectory();  
		//if (root.canWrite()){
			
        fileDir = new File(root.getAbsolutePath()+"/fun/");  
        fileDir.mkdirs();  
        
        file= new File(fileDir, "itisfun.txt"); 
        
        try {
            FileWriter filewriter = new FileWriter(file,true);  
            BufferedWriter out = new BufferedWriter(filewriter);  
            out.write("the neighouring cell ids are: \r\n" );  
            out.flush();
            out.close();  
        }
        catch (IOException e) {
        	//Log.e("ERROR:---", "Could not write file to SDCard" + e.getMessage());
        	Toast.makeText(getApplicationContext(), "Could not write to sdcard0", Toast.LENGTH_SHORT).show();
        }
	        
		//}

        textneighboring.setText(stringNeighboring);        // set the text of the nb widget 
        
        Button buttonmap = (Button)findViewById(R.id.displayMap);  // create instance of the button widget
        
        Button log = (Button)findViewById(R.id.log);
        
        openCellID = (RadioButton)findViewById(R.id.radioButton1);
        googleDB = (RadioButton)findViewById(R.id.radioButton2);
       
        log.setOnClickListener(new View.OnClickListener(){
        	
        	public void onClick(View v){
        		
        		Toast.makeText(getApplicationContext(), "button log is clicked", Toast.LENGTH_SHORT).show();
        		
		       		 // write the cell and location info into the existing text file
		             try {  
		            	 if (!file.exists()){  
		            		 Toast.makeText(getApplicationContext(), "File Not Exist", Toast.LENGTH_SHORT).show();      
                         }
		            	 else {
	            		     FileWriter filewriter = new FileWriter(file,true);  
            			     BufferedWriter buf = new BufferedWriter( new FileWriter(file, true));
            			 
//		            		 
                             double[] currGPS = new double [2];
//                               currGPS = getGPS();
                             location=getLocationInfo();
                             if(location!=null){
                            	 currGPS[0]=location.getLatitude();
                            	 currGPS[1]=location.getLongitude();
                             }
                             buf.append( "current location GPS coordinates are " + currGPS[0] + " , " + currGPS[1] + "\r\n");
//   	                 		     
//			                	 buf.append("current cell id " + cid +"  " + rssi + " \r\n" );
//			                	 //get gpst returned
		                	 try {
								displayMap2();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								 e.printStackTrace();
								Toast.makeText(getApplicationContext(), "Can't get GPS coordinates", Toast.LENGTH_SHORT).show();
							}
		                	 
//			                	 buf.append("current cell tower gps coordiantes are " + cellGPS[0] + " , " + cellGPS[1] +"\r\n");
	            		     Toast.makeText(getApplicationContext(), "cell gps returned!", Toast.LENGTH_LONG).show();
	            			 Location curr = new Location("current location");
                             curr.setLatitude(currGPS[0]);
                             curr.setLongitude(currGPS[1]);
//                           buf.append("The distance between current location and the base station is" + distance + "\r\n");
		            		 
                             excelExport();
//			                 for (int i=0; i < NeighboringList.size(); i++){
//			            		 int rssi = NeighboringList.get(i).getRssi();
//			            		 String dBm = String.valueOf(-113 + 2 * rssi) + " dBm"; 
//			            		
//	                              
//			                	 buf.append(i + ". cell ID: "+ String.valueOf(NeighboringList.get(i).getCid()) + "  RSSI: "
//			                			          +  dBm 
//			                			          + " \r\n");
//			                	 if (i == NeighboringList.size()-1){
//			                		 Toast.makeText(getApplicationContext(), "All cell id recorded!", Toast.LENGTH_SHORT).show(); 
//			                	 }
//			                 }
			                 
			                 buf.append("\r\n\r\n\r\n"); 
			                 
		                	 buf.flush();
		                	 buf.close();	
		
		            	 }
 
        		     } catch (IOException e) {  
		                //Log.e("ERROR:---", "Could not write file to SDCard" + e.getMessage());  
	            	 Toast.makeText(getApplicationContext(), "could not write to sdcard-nb", Toast.LENGTH_SHORT).show();
	                 }  
//        		
//        		try {
//        			displayMap2(cid, lac, gpst);
//        		}
//        		catch (Exception e){
//        			Toast.makeText(getApplicationContext(), "Gps coordinates not returned", Toast.LENGTH_SHORT).show();
//        		}
       	}
        });
        
        buttonmap.setOnClickListener(new View.OnClickListener() {  // the parameter is the callback that will run
        	
			public void onClick(View v) {  
				
				Toast.makeText(getApplicationContext(), "button map is clicked", Toast.LENGTH_SHORT).show();
				// check Internet connection
				
//				if (!IsInternetAvailable()) 
//				{
//					Toast.makeText(getApplicationContext(), "NO INTERNET CONNECTION", Toast.LENGTH_LONG);
//					//return;
//				}
//				
////	            CellLocation.requestLocationUpdate();     // ??????????need to update cid or lac, or updated automatically
////	            cid = cellLocation.getCid();
////	            lac = cellLocation.getLac();
//				
//
//		cid = Integer.valueOf( fieldCID.getText().toString() );
//		lac = Integer.valueOf( fieldLAC.getText().toString() );
//				
//                try {
//                	if(openCellID.isChecked()){
//                       displayMap1(cid, lac);      // call displayMap(), pass the cid and lac in, and show the corresponding point on map               
//                	}
//                	else if(googleDB.isChecked()){
//                		if (!displayMap2(cid,lac,"333")){
//                			Toast.makeText(getApplicationContext(), "Location Not found!", Toast.LENGTH_SHORT).show();
//                		};
//                	}
//                } catch (Exception e) {  
//                	
//                    e.printStackTrace();
//                } 
//	            
//				
	  }
		});
   
    }
    
      private static File hasFile;  
      
      public static boolean filecheck(String filename) {  
          boolean flag = false;  
          File file = new File(filename);  
          if (file.exists()) {  
              flag = true;  
          }  
          setHasFile(file);  
          return flag;  
      }  
      
      /** 
       * @return the hasFile 
       */  
      public static File getHasFile() {  
          return hasFile;  
      }  
    
      /** 
       * @param hasFile 
       *            the hasFile to set 
       */  
      public static void setHasFile(File hasFile) {  
          MainActivityOcid.hasFile = hasFile;  
      }  
      
    public void excelExport() {
          try {  
        	  String filepath = "mnt/sdcard/urban.xls"; 
        	  
        	  List<String> headers = new ArrayList<String>();
          	  headers.add("Current GPS coordinates");
          	  for(int i=0;i<NeighboringListDeal.size();i++){
          		headers.add("Cell ID "+NeighboringListDeal.get(i).get("cellid")+" coordinates");
          		headers.add("RSSI of cid "+NeighboringListDeal.get(i).get("cellid")+" at current location");
          		headers.add("Distance to Cell ID "+NeighboringListDeal.get(i).get("cellid")+"(m)");
          		headers.add("Haversine to Cell ID "+NeighboringListDeal.get(i).get("cellid")+"(m)");
          	  }
          	  
          	  List<String> lines = new ArrayList<String>();
          	  if(location!=null){
          		lines.add(location.getLatitude()+","+location.getLongitude());
          		for(int i=0;i<NeighboringListDeal.size();i++){
          			lines.add(NeighboringListDeal.get(i).get("lat")+","+NeighboringListDeal.get(i).get("lng"));
          			lines.add(NeighboringListDeal.get(i).get("rssi"));
              		
              		Location curr = new Location("current location");
                      Location cell = new Location("cell location");
                      curr.setLatitude(location.getLatitude());
                      curr.setLongitude(location.getLongitude());
                      cell.setLatitude(Double.valueOf(NeighboringListDeal.get(i).get("lat")));
                      cell.setLongitude(Double.valueOf(NeighboringListDeal.get(i).get("lng")));
                       
              		double distance = curr.distanceTo(cell);
              		lines.add(""+distance);
              		
                      double haversine=Haversine.haversine(location.getLatitude(), location.getLongitude(), Double.valueOf(NeighboringListDeal.get(i).get("lat")), Double.valueOf(NeighboringListDeal.get(i).get("lng")));
                      lines.add(""+haversine);
              	}
          		
          	  }else{
          		lines.add("无");
          		for(int i=0;i<NeighboringListDeal.size();i++){
          			lines.add("None");
          			lines.add("None");
          			lines.add("None");
          			lines.add("None");
          		}
          	 }
          	  
          	 boolean has = MainActivityOcid.filecheck(filepath);  
             // if the file exists  
             if (has) { 
            	 Workbook book = Workbook.getWorkbook(new File(filepath));
             	 Sheet sheet = book.getSheet(0);  
             	 // get the row no.
                 int length = sheet.getRows(); 
                 WritableWorkbook wbook = Workbook.createWorkbook(new File(filepath), book); // Create the first book object  
                 WritableSheet sh = wbook.getSheet(0);// get the first object  
                 // start from the last row
                 for(short i=0;i<headers.size();i++){

         	    	 Label label = new Label(i, length, headers.get(i));  

         	    	sh.addCell(label);  
         	     }
             	 for(int j=0;j<lines.size();j++){
             		 Label label = new Label(j, length+1, lines.get(j));  

             		sh.addCell(label);  
             	 }
                 wbook.write();  
                 wbook.close(); 
             } else {  
            	 // create or open the .xls file
                 WritableWorkbook book = Workbook.createWorkbook(new File(filepath));  
                 book.setProtected(true); 
                 // create the first working sheet
                 WritableSheet sheet = book.createSheet("app-created", 0);  
         	     for(short i=0;i<headers.size();i++){
         	    	  
         	    	 Label label = new Label(i, 0, headers.get(i));  
         	    	 // add the defined cell to the sheet
                    sheet.addCell(label);  
         	     }
             	 for(int j=0;j<lines.size();j++){
             		 Label label = new Label(j, 1, lines.get(j));  
         	    	 // add the defined cell to the sheet
                    sheet.addCell(label);  
             	 }
                 // write and close the file
                 book.write();  
                 book.close();  
             }  
       } catch (Exception e) {  
             System.out.println(e);  
       }  
    }  
    
    @Override
    protected void onPause()
    {
      super.onPause();
      tm.listen(mylistener, PhoneStateListener.LISTEN_NONE);
   }
    
    @Override
    protected void onResume()
    {
       super.onResume();
       tm.listen(mylistener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }
 
    

    /* Start the PhoneState listener */
    private class MyPhoneStateListener extends PhoneStateListener
    {
    	void updateCellid (CellLocation location)
    	{
    		//retrieve a reference to an instance of TelephonyManager
    		curCellLocation = (GsmCellLocation) location;
            
            int cid = curCellLocation.getCid();
            int lac = curCellLocation.getLac();
            
            TextView textCID = (TextView)findViewById(R.id.cid);
            TextView textLAC = (TextView)findViewById(R.id.lac);
            textLAC.setText("local area code = " + String.valueOf(lac));
            textCID.setText("cell ID = " + String.valueOf(cid));
                        
            fieldCID.setText(String.valueOf(cid));
            fieldLAC.setText(String.valueOf(lac));
    	}
    	
       void updateSignalStrength(SignalStrength signalstrength)
       {
    	   int rssi = signalstrength.getGsmSignalStrength();
    	   dBm = -113 + 2 * rssi;
    	   TextView textRSSI = (TextView)findViewById(R.id.rssi);
    	   
    	   textRSSI.setText("RSSI = " + dBm);
       }

       void updateServiceState(String operator)
       {
   		
    	  mcc = operator != null && operator.length() >= 3 ? operator
				.substring(0, 3) : "";
		  mnc = operator != null && operator.length() >= 3 ? operator
				.substring(3) : "";
          
		  TextView textmcc = (TextView)findViewById(R.id.mcc);
		  textmcc.setText("MCC = " + mcc);		  
		  TextView textmnc = (TextView)findViewById(R.id.mnc);
		  textmnc.setText("MNC = " + mnc);
		  
       }

    	
      /* Get the Signal strength from the provider, each tiome there is an update */
      @Override
      public void onCellLocationChanged (CellLocation location)
      {
         super.onCellLocationChanged(location);
         updateCellid(location);      
      }
      
      @Override
      public void onSignalStrengthsChanged (SignalStrength signalstrength)
      {
    	  super.onSignalStrengthsChanged(signalstrength);
    	  updateSignalStrength(signalstrength);
      }
      
		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			super.onServiceStateChanged(serviceState);
			updateServiceState(serviceState.getOperatorNumeric());
		}

    };/* End of private Class */
    
    // using the google service to display the tower location on the map
    private void displayMap2() 
    {
    	NeighboringListDeal=new ArrayList<Map<String,String>>();
    	
    	//self
    	try {
    		int nbcid;
            String dBm_temp = dBm + " dBm";   /// covert the unit of rssi from ASU (arbitrary strength unit)
        	nbcid = curCellLocation.getCid();
        	
             String urlString = "http://www.google.com/glm/mmap";            
             //---open a connection to Google Maps API---
             URL url = new URL(urlString); 
             URLConnection conn = url.openConnection();
             HttpURLConnection httpConn = (HttpURLConnection) conn;    
             httpConn.setRequestMethod("POST");
             httpConn.setDoOutput(true); 
             httpConn.setDoInput(true);
             httpConn.connect(); 
             //---write some custom data to Google Maps API---
             OutputStream outputStream = httpConn.getOutputStream();
             WriteData(outputStream, nbcid, curCellLocation.getLac());       
             // ---get the response---
             InputStream inputStream = httpConn.getInputStream();  
             DataInputStream dataInputStream = new DataInputStream(inputStream);
             
             //---interpret the response obtained---
             dataInputStream.readShort();
             dataInputStream.readByte();
             int code = dataInputStream.readInt();
             if (code == 0) {
                 double lat = (double) dataInputStream.readInt() / 1000000D;
                 double lng = (double) dataInputStream.readInt() / 1000000D;
                 dataInputStream.readInt();
                 dataInputStream.readInt();
                 dataInputStream.readUTF();
                 
               //  cellGPS = new double[2];
                 Map<String,String> tem=new HashMap<String,String>();
                 tem.put("lat", String.valueOf(lat));
                 tem.put("lng", String.valueOf(lng));
                 tem.put("rssi", dBm_temp);
                 tem.put("cellid", String.valueOf(nbcid));
                 NeighboringListDeal.add(tem);
                 Toast.makeText(getApplicationContext(), "cellGPS assigned!", Toast.LENGTH_LONG).show();
               //  ---display Google Maps---
                 String uriString = "geo:" + lat
                     + "," + lng+ "?q=" + lat + "," + lng;
             }
//				    }    
    	} catch (IOException e) {
    		int nbcid;
            String dBm_temp = dBm + " dBm";   /// covert the unit of rssi from ASU (arbitrary strength unit)
        	nbcid = curCellLocation.getCid();
        	
			 Map<String,String> tem=new HashMap<String,String>();
             tem.put("lat", String.valueOf("0"));
             tem.put("lng", String.valueOf("0"));
             tem.put("rssi", dBm_temp);
             tem.put("cellid", String.valueOf(nbcid));
             NeighboringListDeal.add(tem);
             Toast.makeText(getApplicationContext(), "error in network connection!", Toast.LENGTH_LONG).show();
			 e.printStackTrace();
		}
    	
    	//neihboring
    	try{
    		for(int i=0; i < NeighboringList.size(); i++){                         // for each cid in the nblist, print out description and value
	   	       	 	int rssi = NeighboringList.get(i).getRssi();
	   	       	 	String dBm_temp = String.valueOf(-113 + 2 * rssi) + " dBm";   /// covert the unit of rssi from ASU (arbitrary strength unit)
	   	       	 	int nbcid = NeighboringList.get(i).getCid();
   		           	  
   		       	     String urlString = "http://www.google.com/glm/mmap";            
	   		       	 URL url = new URL(urlString); 
		             URLConnection conn = url.openConnection();
		             HttpURLConnection httpConn = (HttpURLConnection) conn;    
		             httpConn.setRequestMethod("POST");
		             httpConn.setDoOutput(true); 
		             httpConn.setDoInput(true);
		             httpConn.connect(); 
		             //---write some custom data to Google Maps API---
		             OutputStream outputStream = httpConn.getOutputStream();
   		              WriteData(outputStream, nbcid, NeighboringList.get(i).getLac());       
   		           // ---get the response---
			             InputStream inputStream = httpConn.getInputStream();  
			             DataInputStream dataInputStream = new DataInputStream(inputStream);
			             
			             //---interpret the response obtained---
			             dataInputStream.readShort();
			             dataInputStream.readByte();
			             int code = dataInputStream.readInt();
   		              if (code == 0) {
   		                 double lat = (double) dataInputStream.readInt() / 1000000D;
   		                 double lng = (double) dataInputStream.readInt() / 1000000D;
   		                 dataInputStream.readInt();
   		                 dataInputStream.readInt();
   		                 dataInputStream.readUTF();
   		                 
   		                 // cellGPS = new double[2];
   		                 Map<String,String> tem=new HashMap<String,String>();
   		                 tem.put("lat", String.valueOf(lat));
   		                 tem.put("lng", String.valueOf(lng));
   		                 tem.put("rssi", dBm_temp);
   		                 tem.put("cellid", String.valueOf(nbcid));
   		                 NeighboringListDeal.add(tem);
   		                 Toast.makeText(getApplicationContext(), "cellGPS assigned!", Toast.LENGTH_LONG).show();
   		               //  ---display Google Maps---
   		                 String uriString = "geo:" + lat
   		                     + "," + lng+ "?q=" + lat + "," + lng;
   		       	 }
//		    		}
		        }
			} catch (IOException e) {
				for(int i=0; i < NeighboringList.size(); i++){  
					int rssi = NeighboringList.get(i).getRssi();
	   	       	 	String dBm_temp = String.valueOf(-113 + 2 * rssi) + " dBm";   /// covert the unit of rssi from ASU (arbitrary strength unit)
	   	       	 	int nbcid = NeighboringList.get(i).getCid();
	   	       	 	
					 Map<String,String> tem=new HashMap<String,String>();
	                 tem.put("lat", String.valueOf("0"));
	                 tem.put("lng", String.valueOf("0"));
	                 tem.put("rssi", dBm_temp);
	                 tem.put("cellid", String.valueOf(nbcid));
	                 NeighboringListDeal.add(tem);
				}
				Toast.makeText(getApplicationContext(), "Connection Error!", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
	           
    }
    
    private void WriteData(OutputStream out, int cellID, int lac) throws IOException
    {    	
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeShort(21);
        dataOutputStream.writeLong(0);
        dataOutputStream.writeUTF("en");
        dataOutputStream.writeUTF("Android");
        dataOutputStream.writeUTF("1.0");
        dataOutputStream.writeUTF("Web");
        dataOutputStream.writeByte(27);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(3);
        dataOutputStream.writeUTF("");

        dataOutputStream.writeInt(cellID);  
        dataOutputStream.writeInt(lac);     

        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.writeInt(0);
        dataOutputStream.flush();    	
    }
    
    // check if the internet connection is available
    private boolean IsInternetAvailable()
    {
    	// ONLY CHECK FOR WIFI
    	boolean haveconnectedwifi = false;
    	
        ConnectivityManager connectivitymanager	= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netinfo = connectivitymanager.getAllNetworkInfo();
        
        for (NetworkInfo ni : netinfo){
        	if ("WIFI".equals(ni.getTypeName()))
        		if (ni.isConnected())
        			haveconnectedwifi = true;
        		
        }
		return haveconnectedwifi;
       
    }
    

  	 public Location getLocationInfo(){
       	//String provider = LocationManager.GPS_PROVIDER;
           Location location=null;
           //check if GPS is on
           if (loctionManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
              //listen to the change of location
              loctionManager.requestLocationUpdates(provider, 60000, 1, locationListener);
              //get the last known location
              location = loctionManager.getLastKnownLocation(provider);
       	 }else{
       		//obatin the current status of GPS
       		 Intent gpsIntent = new Intent();
              gpsIntent.setClassName("com.android.settings",
                      "com.android.settings.widget.SettingsAppWidgetProvider");
              gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
              gpsIntent.setData(Uri.parse("custom:3"));
              try {
                  PendingIntent.getBroadcast(this, 0, gpsIntent, 0).send();
              } catch (CanceledException e) {
                  e.printStackTrace();
              }
       	}
           return location;
   	}
}
