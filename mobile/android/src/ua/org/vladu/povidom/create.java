package ua.org.vladu.povidom;

import ua.org.vladu.povidom.tempPhoto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class create extends TabActivity implements OnClickListener
{
	Button startButton;
	ImageView preview;
	ProgressDialog progress1;
	ImageView chosenImageView;
	Button choosePicture;
	String mState;
	Random rand = new Random();
	Bitmap bmp;
	Bitmap alteredBitmap;
	Bitmap alteredBitmap1;
	Bitmap bm;
	Uri imageFileUri;
	Canvas canvas;
	Paint paint;
	Matrix matrix;
	int isStarted=0;
	int position=0;
	EditText txt, txt2; 
	String img, img1; 
	ProgressDialog dialog; String s5 = "";  String st1 = "", st2=""; LocationManager lm; LocationListener ll; 
	Uri uri1; String address = null; List<NameValuePair> nameValuePairs;
	private String array_spinner[]; int isPhotoSet = 0;
	Spinner s; int res0;
	ProgressBar mProgress;
	ProgressDialog progreeDialog; Uri imgUri;
	Location loc; String bestProvider;
	int res;
	private JSONObject jObject;
	int prevOrientation;
	AlertDialog alert; AlertDialog.Builder builder;
	boolean exitCode = false;
	ImageButton  home, upload;
	TabHost tabs;
	TextView gpsstatus;
	Button setGps;
	LinearLayout gpsAuto, gpsManual;
	Spinner sp1, sp2;
	EditText street;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	MapView mapView;
	boolean mapIsSet = false;
	TabHost tabHost;
	double st1Raw;
	double st2Raw;
	
	/** Register for the updates when Activity is in foreground */
	@Override
	protected void onResume() {
		super.onResume();
		if (bestProvider != null) {
			lm.requestLocationUpdates(bestProvider, 0, 0, ll);
		}
		tempPhoto tmp = new tempPhoto(this.getBaseContext());
	    SQLiteDatabase sqlDb = tmp.getReadableDatabase(); 
	    Cursor curs = sqlDb.rawQuery("select IMG from img;", null);
	    curs.moveToFirst();
	    if (curs.getCount() > 0 )
	    {
	    	byte[] bitmapData = curs.getBlob(0);
	        Bitmap bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
	        chosenImageView.setImageBitmap(bmp);
	    }
		curs.close();
		sqlDb.close();
	}


	@Override
	protected void onPause() {
		super.onPause();
		if (bestProvider != null) {
			lm.removeUpdates(ll);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) 
	    {
	    	home();
	    }
	    return exitCode;
	}

	
	public void home() {
		builder = new AlertDialog.Builder(create.this);
     	builder.setMessage("Звіт не збережено. Вийти?")
     	       .setCancelable(false)
     	       .setPositiveButton("Так", new DialogInterface.OnClickListener() {
     	           public void onClick(DialogInterface dialog, int id) {
     	        	   finish();
     	           }
     	       })
     	       .setNegativeButton("Ні", new DialogInterface.OnClickListener() {
     	           public void onClick(DialogInterface dialog, int id) {
     	                dialog.cancel();
     	                exitCode = false;
     	           }
     	       });
     	builder.show();
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create);
        
        home = (ImageButton) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() 
        {        
        	public void onClick(View v) 
        	{
        		home();
        	}
        });
        
        Resources res = getResources(); 
        tabHost = getTabHost(); 
        TabHost.TabSpec spec;  

        spec = tabHost.newTabSpec("edit").setIndicator("",
                          res.getDrawable(R.drawable.tabedit))
                      .setContent(R.id.t2);
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("photo").setIndicator("",
                          res.getDrawable(R.drawable.tabcamera))
                      .setContent(R.id.t3);
        tabHost.addTab(spec);
        
        spec = tabHost.newTabSpec("geo").setIndicator("",
                res.getDrawable(R.drawable.tabcompass))
            .setContent(R.id.t4);
        
        tabHost.addTab(spec);
                
        getTabWidget().getChildAt(2).setOnClickListener(new OnClickListener() { 
            public void onClick(View v) { 

                Log.d("qwe", getTabHost().getCurrentTabTag());

                if (getTabHost().getCurrentTabTag().equals("geo")) { 

                	Log.d("qwe", "yeah");
                	
	               	 GeoPoint point2 = new GeoPoint(50.441327,30.545998);
	               	 if (mapIsSet == true) {
	               		point2 = new GeoPoint(st1Raw, st2Raw);
	               	 }
	               	 
	               	mapView.getOverlays().clear();
	               	
	                 MapController mapController = mapView.getController();
	                 mapController.setZoom(16);
	                 mapController.setCenter(point2);
	                 
	                 MapOverlay mmapOverlay = new MapOverlay(create.this, point2);
	                 List<Overlay> listOfOverlays = mapView.getOverlays();
	                 listOfOverlays.add(mmapOverlay);
	                 mapView.invalidate();
                	
                } else {
                	tabHost.setCurrentTabByTag("geo");
                }
            } 
        });

        mapView = (MapView) findViewById(R.id.mapmain);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        //mapView.setUseDataConnection(false);
        GeoPoint point2 = new GeoPoint(50.441327,30.545998); // temporary default value for Kyiv

        MapController mapController = mapView.getController();
        mapController.setZoom(15);
        mapController.setCenter(point2);
        
        MapOverlay mmapOverlay = new MapOverlay(this, point2);
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.add(mmapOverlay);
        mapView.invalidate();
        
        
        upload = (ImageButton) findViewById(R.id.upload);
        chosenImageView = (ImageView) this.findViewById(R.id.PreviewImage);

        //GPS 
    	lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	Criteria criteria = new Criteria();
		//bestProvider = lm.getBestProvider(criteria, true);
        //if ( !lm.isProviderEnabled(bestProvider ) )
    	if ( lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
        {
        	bestProvider = lm.getBestProvider(criteria, true);
        	if ( lm.isProviderEnabled( bestProvider) ) {
        		ll = new mylocationlistener();
        		lm.requestLocationUpdates(bestProvider, 0, 0, ll);
        	}

        } 
        array_spinner=new String [8];
        array_spinner[0]="Яма на дорозі";
        array_spinner[1]="Відкритий каналізаційний люк";
        array_spinner[2]="Переповнений смітник";
        array_spinner[3]="Ігрові автомати в житлових районах";
        array_spinner[4]="Неприбрані під'їзди";
        array_spinner[5]="Звалища сміття посеред міста";
        array_spinner[6]="Руйнування архітектури";
        array_spinner[7]="Інші скарги";
        s = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinneritem, array_spinner);
        adapter.setDropDownViewResource(R.layout.selecteditem);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			public void onItemSelected(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {
				s5 = String.valueOf(arg3);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				Toast.makeText(create.this, "No selected", Toast.LENGTH_LONG).show();
			}

        });
        
        txt = (EditText) this.findViewById(R.id.Text);
        String tx = txt.getText().toString();
        upload.setOnClickListener(new View.OnClickListener() 
        {        
        	public void onClick(View v) 
        	{
        		tempPhoto tmp5 = new tempPhoto(getBaseContext());
        		SQLiteDatabase sqlDb5 = tmp5.getReadableDatabase(); 
        		Cursor curs5 = sqlDb5.rawQuery("select IMG from img;", null);
        		curs5.moveToFirst();
        		if (curs5.getCount() == 0 )
        		{
        			curs5.close();
        			sqlDb5.close();
        			Toast.makeText(create.this, R.string.pleaseMakePhoto, Toast.LENGTH_LONG).show();
        			tabHost.setCurrentTabByTag("tag2");
        			return;
        		}
        		curs5.close();
        		sqlDb5.close();
    	    	String tx = txt.getText().toString();

	           	if ( mapIsSet == false )
	            {
	           		Toast.makeText(create.this, "Необхідні координати вашого місцеположення", Toast.LENGTH_LONG).show();
             		tabHost.setCurrentTabByTag("tag3");
	           		return;
	            }
	           	else if (st1.compareTo("") != 0 && st2.compareTo("") != 0)
        		{	          	   
        			// Check internet connection	
	        		ConnectivityManager connec =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	        		if (connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() || connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) //      ConnectivityManager.TYPE_WIFI   
	        		{
	            		prevOrientation = getRequestedOrientation();
	            		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) 
	            		{
		                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		                } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) 
		                {
		                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		                } else 
		                {
		                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		                }	                
	        			progreeDialog = ProgressDialog.show(create.this, "", "Зачекайте...", true, false);
	        			Send send = new Send(st1, st2, s5, tx);
	        		    httpThread httpthread = new httpThread(send);
	        		    httpthread.start();
	            	}  
	            	else    
	            	{
	            		Toast.makeText(create.this, R.string.checkConnection , Toast.LENGTH_LONG).show();
	            		int res = save();
	            		if (res == 0)
	            		{
	            			Toast.makeText(create.this, R.string.reportSaved, Toast.LENGTH_LONG).show();
	            			finish();
	            		}
	            	}
        		}
            }
        });
        chosenImageView.setOnClickListener(this);
        
        // open select dialog (must move to separate function      
        String FILENAME = "settings";
		String string = "1";
		String st01 = "";
		try 
		{
			StringBuilder inb = new StringBuilder();
			FileInputStream fis = openFileInput("settings");
			int ch;
			while((ch = fis.read()) != -1)
			    inb.append((char)ch);
			st01 = inb.toString();
			fis.close();
		} catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		
 		tempPhoto tmp5 = new tempPhoto(getBaseContext());
	    SQLiteDatabase sqlDb5 = tmp5.getReadableDatabase(); 
	    Cursor curs5 = sqlDb5.rawQuery("select IMG from img;", null);
	    curs5.moveToFirst();

	    if (curs5.getCount() > 0 )
	    {
	        byte[] bitmapData = curs5.getBlob(0);
	        Bitmap bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
	        chosenImageView.setImageBitmap(bmp);
		}
	    else if (st01.compareTo("1") == 0)
        {
	    	Intent intent1 = new Intent(create.this, qwe.class);
        	startActivity(intent1);
       		string = "0";
			FileOutputStream fos;
			try 
			{
				fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
				fos.write(string.getBytes());
				fos.close();
			} catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
        } 
        curs5.close();
	    sqlDb5.close();
    }
    
	public void onClick(View v) 
	{
		startCamera();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) 
	{
		txt = (EditText) this.findViewById(R.id.Text);
		savedInstanceState.putString("MyString", txt.getText().toString());
		savedInstanceState.putInt("pos", s.getSelectedItemPosition());
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		super.onRestoreInstanceState(savedInstanceState);
		txt = (EditText) this.findViewById(R.id.Text);
		String myString = savedInstanceState.getString("MyString");
		chosenImageView = (ImageView) this.findViewById(R.id.PreviewImage);
		tempPhoto tmp = new tempPhoto(this.getBaseContext());
		SQLiteDatabase sqlDb = tmp.getReadableDatabase();       
		Cursor curs = sqlDb.rawQuery("select IMG from img;", null);
		curs.moveToFirst();
		if (curs.getCount() > 0 )
		{
			byte[] bitmapData = curs.getBlob(0);
            Bitmap bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
            chosenImageView.setImageBitmap(bmp);
		}
		curs.close();
		sqlDb.close();      
		txt.setText(myString);
		int p = savedInstanceState.getInt("pos");
		s.setSelection(p);	  
	}

	public String getDate()
	{
        Calendar c = Calendar.getInstance(); 
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String month = String.valueOf(c.get(Calendar.MONTH));
        String year = String.valueOf(c.get(Calendar.YEAR));
        String hours = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        String minutes = String.valueOf(c.get(Calendar.MINUTE));
        String date = day + "/" + month + "/" + year + " " + hours + ":" + minutes;
        return date;
	}
	
	public void startCamera()
	{
		Intent intent = new Intent(create.this, qwe.class);
		startActivity(intent); 
	}
	
	
	public int save()
	{
		String tx = txt.getText().toString();
		tempPhoto tmp = new tempPhoto(this.getBaseContext());
	    SQLiteDatabase sqlDb = tmp.getReadableDatabase(); 
	    Cursor curs = sqlDb.rawQuery("select IMG from img;", null);
	    curs.moveToFirst();
	    if (curs.getCount() == 0 )
	    {
			Toast.makeText(create.this, R.string.pleaseMakePhoto, Toast.LENGTH_LONG).show();
			return -1;
		}
	    curs.close();
	    sqlDb.close();
		if (st1.compareTo("") != 0 && st2.compareTo("") != 0)
		{	
            tx = txt.getText().toString();
       	    if (tx.trim().length() < 5)
       	    {
       	    	Toast.makeText(create.this, R.string.enterMsgMin, Toast.LENGTH_LONG).show();  
       	    	return -1;
       	    }
       	    tempPhoto tmp5 = new tempPhoto(this.getBaseContext());
       	    SQLiteDatabase sqlDb5 = tmp5.getReadableDatabase(); 
       	    byte[] bitmapData = null;
	        Cursor curs5 = sqlDb5.rawQuery("select IMG from img;", null);
	        curs5.moveToFirst();
	        if (curs5.getCount() > 0 )
	        {
	        	bitmapData = curs5.getBlob(0);
	        }
	        curs5.close();
	        sqlDb5.close();
	        
	        // db 
	  		reports r0 = new reports(this.getBaseContext());
	  		SQLiteDatabase sqlDb1 = r0.getWritableDatabase();
            ContentValues values = new ContentValues();
	        values.put("TXT", tx);
	        values.put("URL", "");
	        values.put("LATITUDE", st1);
	        values.put("LONGTITUDE", st2);
	        values.put("CATEGORY", s5);
	        values.put("IMAGE", bitmapData);
	        values.put("STATUS", "saved");
	        values.put("DATEOFCREATION", getDate());
	        values.put("DROPHASH", "");
	        values.put("DATEUNTILREMOVE", "");
	        values.put("CITY", "");
	        values.put("STREET", "");
	        long n = sqlDb1.insert("r0", null, values);
	        sqlDb1.close();
		
	        tempPhoto tmp1 = new tempPhoto(this.getBaseContext());
	        SQLiteDatabase sqlDb2 = tmp.getWritableDatabase(); 
	        sqlDb2.execSQL("delete from img");
		    sqlDb2.close();
			return 0;
		}
		else
		{
			Toast.makeText(create.this, R.string.checkGps, Toast.LENGTH_LONG).show(); 
			return -1;
		}
	}
	
	// setting up menus
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) 
    {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.create, menu);
	    return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
	    	case R.id.quitMenu:
	    		builder = new AlertDialog.Builder(create.this);
		     	builder.setMessage("Звіт не збережено. Вийти?")
		     	       .setCancelable(false)
		     	       .setPositiveButton("Так", new DialogInterface.OnClickListener() {
		     	           public void onClick(DialogInterface dialog, int id) {
		     	        	   tempPhoto tmp = new tempPhoto(getBaseContext());
		     	 	        SQLiteDatabase sqlDb1 = tmp.getWritableDatabase(); 
		     	 	        sqlDb1.execSQL("delete from img");
		     	 		    sqlDb1.close();
		     	 		    finish();

		     	           }
		     	       })
		     	       .setNegativeButton("Ні", new DialogInterface.OnClickListener() {
		     	           public void onClick(DialogInterface dialog, int id) {
		     	                dialog.cancel();
		     	                exitCode = false;
		     	           }
		     	       });
		     	builder.show();
		     	return true;
	    	case R.id.takeMenu:
	    		startCamera();
	    		return true;
	    	case R.id.saveMenu:
	    		int res = save();
	    		if (res == 0)
	    		{
	    			Toast.makeText(create.this, R.string.reportSaved, Toast.LENGTH_LONG).show();
	    			finish();
	    		}
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    	} 
		}
	
	 public class reports extends SQLiteOpenHelper 
	 {
		    private static final int DATABASE_VERSION = 2;
		    private static final String DICTIONARY_TABLE_NAME = "r0";
		    private static final String DICTIONARY_TABLE_CREATE =
		                "CREATE TABLE " + DICTIONARY_TABLE_NAME + " (" +
		                "ID" + " integer primary key autoincrement, " +
		                "TXT" + " TEXT, " +
		                "URL" + " TEXT, " +
		                "LATITUDE" + " TEXT, " +
		                "LONGTITUDE" + " TEXT, " +
		                "CATEGORY" + " TEXT, " +
		                "IMAGE" + " BLOB, " +
		                "STATUS" + " TEXT, " +
		                "DATEOFCREATION" + " TEXT, " +
		                "DROPHASH" + " TEXT, " +
		                "DATEUNTILREMOVE" + " REAL, " +
		                "CITY" + " TEXT, " +
		                "STREET" + " TEXT);";
		    
			private static final String DATABASE_NAME = "reports30";

		    reports(Context context) 
		    {
		        super(context, DATABASE_NAME, null, DATABASE_VERSION);
		    }

		    @Override
		    public void onCreate(SQLiteDatabase db) 
		    {
		        db.execSQL(DICTIONARY_TABLE_CREATE);
		    }

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
			{
				// TODO Auto-generated method stub
			}
		}
	 
	 	public void quit()
	 	{
	 		finish();
	 	}

	 	private class httpThread extends Thread 
	 	{
	        private Send send;
	        
	        public httpThread(Send send) 
	        {
	           this.send = send;
	        }

	        @Override
	        public void run() 
	        {         
	            res = send.send();
	            handler.sendEmptyMessage(0);
	        }

	        private Handler handler = new Handler() 
	        {
	            @Override
	            public void handleMessage(Message msg) 
	            {
	                progreeDialog.dismiss();
	                setRequestedOrientation(prevOrientation);
	                if (res == -1)
	                {
	                	Toast.makeText(create.this, R.string.serverError, Toast.LENGTH_LONG).show();
	                	int res = save();
            			if (res == 0)
            			{
            				Toast.makeText(create.this, R.string.reportSaved, Toast.LENGTH_LONG).show();
            			}
	                }
	                else if (res == 0)
	                {
	                	Toast.makeText(create.this, R.string.reportUploaded, Toast.LENGTH_LONG).show();
	                }
	                quit();
	            }
	        };
	    }
 
	 	private class Send  
	 	{		 
	 		String st1, st2, s5, tx;
	        public Send(String st1, String st2, String s5, String tx) 
	        {
	        	this.st1 = st1;
	        	this.st2 = st2;
	        	this.s5 = s5;
	        	this.tx = tx;
	        }

	        public int send()
	        {
	          	//HTTP POST 
	       		HttpParams httpParameters = new BasicHttpParams();
	            int timeoutConnection = 5000;
	            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
	            int timeoutSocket = 25000;
	            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

	            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

	            Log.d("joss", "HTTP init");
	            
	            HttpContext localContext = new BasicHttpContext();
	            HttpPost httpPost = new HttpPost("http://povidom-vladu.org.ua/index/post/");
	               
		        String path = Environment.getExternalStorageDirectory().toString();
		        img=path+"/test2.jpg";
				OutputStream fOut = null;
				File f1 = new File(path, "test2.jpg");
				try 				
				{
					fOut = new FileOutputStream(f1);
				} catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}

				tempPhoto tmp = new tempPhoto(create.this.getBaseContext());
				
				Log.d("joss", "get data from database");
				
	            SQLiteDatabase sqlDb = tmp.getReadableDatabase(); 
	               
	            Cursor curs = sqlDb.rawQuery("select IMG from img;", null);
	            curs.moveToFirst();

	            if (curs.getCount() > 0 )
	            {
	            	byte[] bitmapData = curs.getBlob(0);
	                Bitmap bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
	                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
	            }
	         		
	            curs.close();
	         	sqlDb.close();
	               	               
	         	// Add your data  
	            nameValuePairs = new ArrayList<NameValuePair>(5);  
	       	    nameValuePairs.add(new BasicNameValuePair("msg",this.tx));  
	       	    nameValuePairs.add(new BasicNameValuePair("lat", this.st1));
	       	    nameValuePairs.add(new BasicNameValuePair("long", this.st2));
	       	    nameValuePairs.add(new BasicNameValuePair("img", img));
	       	    nameValuePairs.add(new BasicNameValuePair("type", this.s5));
	              
	            try 
	            {
	            	MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	            	for(int index=0; index < nameValuePairs.size(); index++) 
	                {
	            		if(nameValuePairs.get(index).getName().equalsIgnoreCase("img")) 
	                    {
	            			entity.addPart(nameValuePairs.get(index).getName(), new FileBody(f1));
	                    } else 
	                    {
	                        entity.addPart(nameValuePairs.get(index).getName(), new StringBody(nameValuePairs.get(index).getValue()));
	                    }
	                }
	                httpPost.setEntity(entity);
	                Log.d("joss", "runnning HTTP request");
	                HttpResponse response = httpClient.execute(httpPost, localContext);
	                Log.d("joss", "HTTP DONE!!");    
	                f1.delete();
	              	String result = "";
	              	InputStream in = response.getEntity().getContent();
	              	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	                StringBuilder str = new StringBuilder();
	                String line = null;
	                while((line = reader.readLine()) != null)
	                {
	                    str.append(line + "\n");
	                }
	                in.close();
	                result = str.toString();
	                String status = "Null";
	                String url0 = "Null";
	                String drophash = "Null";
                	try 
                	{
						jObject = new JSONObject(result);
						status = jObject.getString("status");
						url0 = jObject.getString("url");
						drophash = jObject.getString("drophash");
						Log.d("joss", "parameters parsing DONE!!");
					} catch (JSONException e) 
					{
						e.printStackTrace();
					} 
	                
	                if (status.compareTo("OK") == 0) //(result.substring(0, 2).compareTo("OK") == 0)
	                {               	
	                	String url = url0; //result.substring(3, result.length());
		            	tempPhoto tmp2 = new tempPhoto(create.this.getBaseContext());
		       	      	SQLiteDatabase sqlDb2 = tmp2.getReadableDatabase(); 
		       	      	byte[] bitmapData = null;
		       	      	Cursor curs2 = sqlDb2.rawQuery("select IMG from img;", null);
		       	      	curs2.moveToFirst();
		       	      	if (curs2.getCount() > 0 )
		       	      	{
		       	      		bitmapData = curs2.getBlob(0);
		       	        }
		       	      	curs2.close();
		       	      	sqlDb2.close();
		       	      	long unixTime = System.currentTimeMillis() / 1000L;
		       	      	reports r0 = new reports(create.this.getBaseContext());
		       	      	SQLiteDatabase sqlDb1 = r0.getWritableDatabase();
		       	        ContentValues values = new ContentValues();
		       	        values.put("TXT", tx);
		       	        values.put("URL", url);
		       	        values.put("LATITUDE", st1);
		       	        values.put("LONGTITUDE", st2);
		       	        values.put("CATEGORY", s5);
		       	        values.put("IMAGE", "");
		       	        values.put("STATUS", "uploaded");
		       	        values.put("DATEOFCREATION", getDate());
		       	        values.put("DROPHASH", drophash);
		       	        values.put("DATEUNTILREMOVE", unixTime);
		       	        values.put("CITY", "");
		       	     	values.put("STREET", "");
		       	        long n = sqlDb1.insert("r0", null, values);
		       	        sqlDb1.close();
		       	        
		       	        tempPhoto tmp3 = new tempPhoto(create.this.getBaseContext());
		       	        SQLiteDatabase sqlDb3 = tmp3.getWritableDatabase(); 
		       	        sqlDb3.execSQL("delete from img");
		       		    sqlDb3.close();
		       		    return 0;  
	                } 
	                else
	                {
	               	 	return -1;   
	                }
	            } catch (IOException e) 
	            {
	                   e.printStackTrace();
	            } 
	            return 1; 
	        }
	    }
	 	
	 	private class mylocationlistener implements LocationListener 
	    {
	        public void onLocationChanged(Location location) 
	        {
	            if (location != null && mapIsSet == false) 
	            {
	            	loc = location;
	         		st1 = String.valueOf(location.getLatitude());
	         		st2 = String.valueOf(location.getLongitude());
	         		st1Raw = location.getLatitude();
	         		st2Raw = location.getLongitude();
	            	Log.d("LOCATION CHANGED", location.getLatitude() + "");
	            	Log.d("LOCATION CHANGED", location.getLongitude() + "");
	            	//Toast.makeText(create.this, "Зловили спутнік!" , Toast.LENGTH_LONG).show();
	            	
	            	 GeoPoint point2 = new GeoPoint(location.getLatitude(), location.getLongitude());

	                 MapController mapController = mapView.getController();
	                 mapController.setZoom(16);
	                 mapController.setCenter(point2);
	                 
	                 mapView.getOverlays().clear();
	                 mapIsSet = true;
	                 
	                 MapOverlay mmapOverlay = new MapOverlay(create.this, point2);
	                 List<Overlay> listOfOverlays = mapView.getOverlays();
	                 listOfOverlays.add(mmapOverlay);
	                 mapView.invalidate();
	            }
	        }
	        public void onProviderDisabled(String provider) 
	        {
	        }
	        public void onProviderEnabled(String provider) 
	        {
	        }
	        public void onStatusChanged(String provider, int status, Bundle extras) 
	        {
	        }
	    }
	 
	 	public int send()
		{
	        String tx = txt.getText().toString();	   
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 25000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

	        HttpContext localContext = new BasicHttpContext();
	        HttpPost httpPost = new HttpPost("http://povidom-vladu.org.ua/index/post/");
	        
	        String path = Environment.getExternalStorageDirectory().toString();
	        img=path+"/test.jpg";
	        File f1 = new File(path, "test.jpg");
	        
	        // Add your data  

        	s5="0";
	        nameValuePairs = new ArrayList<NameValuePair>(5);  
		    nameValuePairs.add(new BasicNameValuePair("msg",tx));  
		    nameValuePairs.add(new BasicNameValuePair("lat", st1));
		    nameValuePairs.add(new BasicNameValuePair("long", st2));
		    nameValuePairs.add(new BasicNameValuePair("img", img));
		    nameValuePairs.add(new BasicNameValuePair("type", s5));
	        try 
	        {
	            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	            for(int index=0; index < nameValuePairs.size(); index++) 
	            {
	                if(nameValuePairs.get(index).getName().equalsIgnoreCase("img")) 
	                {
	                    entity.addPart(nameValuePairs.get(index).getName(), new FileBody(f1));
	                } else 
	                {
	                    entity.addPart(nameValuePairs.get(index).getName(), new StringBody(nameValuePairs.get(index).getValue()));
	                }
	            }
	            httpPost.setEntity(entity);
                HttpResponse response = httpClient.execute(httpPost, localContext);	            
	       	 	String result = "";
	       	 	InputStream in = response.getEntity().getContent();
	       	 	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	       	 	StringBuilder str = new StringBuilder();
	       	 	String line = null;
	       	 	while((line = reader.readLine()) != null)
	       	 	{
	       	 		str.append(line + "\n");
	       	 	}
	       	 	in.close();
	       	 	result = str.toString();
	       	 	if (result.substring(0, 2).compareTo("OK") == 0)
	       	 	{
	       	 		String url = result.substring(3, result.length());
	       	 		Intent intent = new Intent(create.this, list.class);
	       	 		Bundle b = new Bundle();
	       	 		b.putString ("url", url);
	       	 		b.putString ("msg", tx);
	       	 		intent.putExtras(b);
	       	 		startActivity(intent);
	       	 		
	       	 		tempPhoto tmp = new tempPhoto(this.getBaseContext());
	       	 		SQLiteDatabase sqlDb1 = tmp.getWritableDatabase(); 
	       	 		sqlDb1.execSQL("delete from img");
	       	 		sqlDb1.close();
	        	
	       	 		String path1 = Environment.getExternalStorageDirectory().toString();
	       	 		File f12 = new File(path1, "test.jpg");
	       	 		if (f12.length() > 0)
	       	 		{
	       	 			f12.delete();
	       	 		} 	 	        
	       	 		finish();
	       	 	}    
	       	 	else
	       	 	{
	       	 		Toast.makeText(create.this, result, Toast.LENGTH_LONG).show();
	       	 		Toast.makeText(create.this, R.string.serverError, Toast.LENGTH_LONG).show();  
	       	 	}
	        	} catch (IOException e) 
	        	{
	        		e.printStackTrace();
	        		Toast.makeText(create.this, e.getMessage() , Toast.LENGTH_LONG).show();
	        	}
	        	return 0;
			}
	 	
	 	 public class MapOverlay extends org.osmdroid.views.overlay.Overlay {

	     	GeoPoint point;
	         public MapOverlay(Context ctx, GeoPoint point) {
	             super(ctx);
	             this.point = point; 
	             // TODO Auto-generated constructor stub
	         }

	         @Override
	         public boolean onTouchEvent(MotionEvent event, MapView mapView) 
	         {
	             if (event.getAction() == 1) {                
	                 GeoPoint p = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
	                 //Toast.makeText(getBaseContext(),  p.getLatitudeE6() / 1E6 + "," +  p.getLongitudeE6() /1E6 , Toast.LENGTH_SHORT).show();
	                 this.point = p;
	                 
	                 st1Raw = p.getLatitudeE6() / 1E6;
	                 st2Raw = p.getLongitudeE6() /1E6;
	                 st1 = String.valueOf(st1Raw);
	                 st2 = String.valueOf(st2Raw);
	                 
	                 //mapView.getController().setCenter(p);
	                 mapView.invalidate();
	                 
	                 mapIsSet = true;
	             }                            
	             return false;
	         }        
	         
	         @Override
	         protected void draw(Canvas pC, MapView pOsmv, boolean shadow) {   
	         	if (shadow) {
	                 return;
	         	}
	         	
	         	Bitmap bmp  = BitmapFactory.decodeResource(getResources(), R.drawable.marker_default);
	         	int bmpWidth = bmp.getWidth();
	         	int bmpHeight = bmp.getHeight();
	         	
	             Paint lp3;
	             lp3 = new Paint();
	             lp3.setColor(Color.RED);
	             lp3.setAntiAlias(true);
	             lp3.setStyle(Style.FILL);
	             lp3.setStrokeWidth(1);
	             lp3.setTextAlign(Paint.Align.LEFT);
	             lp3.setTextSize(12);
	             final Rect viewportRect = new Rect();
	             final Projection projection = pOsmv.getProjection();
	             viewportRect.set(projection.getScreenRect());
	             
	             Point p01 = pOsmv.getProjection().toPixels(this.point, null);
	             //pC.drawCircle(p01.x, p01.y, 5, lp3);
	             pC.drawBitmap(bmp, p01.x - (bmpWidth), p01.y - (bmpHeight), lp3);
	             pOsmv.invalidate();
	             
		         // temporary have to limit zoom level 
	             // see: http://stackoverflow.com/questions/4146395/set-minimum-zoom-level-for-mapview
	             // TODO: revise this to disable zoom button instead of just reverting zoom state
		         if (pOsmv.getZoomLevel() > 16) {
		         	pOsmv.getController().setZoom(16);
		         }
	         }

	     }
	}