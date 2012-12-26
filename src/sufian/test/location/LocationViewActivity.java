package sufian.test.location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

public class LocationViewActivity extends Activity implements LocationListener, Runnable {
	private TextView gpsLat, gpsLon;
	private LocationManager locationManager;
	private File logFile;
	private FileWriter writer;
	private SimpleDateFormat formatter;
	private int interval = 1000 * 60 * 1;
	private boolean running;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_view);
		gpsLat = (TextView) findViewById(R.id.gpsLatitude);
		gpsLon = (TextView) findViewById(R.id.gpsLongitude);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, this);

		String path = Environment.getExternalStorageDirectory().getPath(); 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		File folder = new File(path + "/GPSLogs");
		if(!folder.exists()) folder.mkdirs();
		logFile = new File(path + "/GPSLogs/GPSLog_" + sdf.format(new Date())+ ".txt");

		try {
			writer = new FileWriter(logFile);
			writer.write("time, latitude, longitude\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		formatter = new SimpleDateFormat("HH-mm-ss");
		running = true;
		new Thread(this).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(this);
		running = false;
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = (double) (location.getLatitude());
		double lon = (double) (location.getLongitude());

		if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
			gpsLat.setText(String.valueOf(lat));
			gpsLon.setText(String.valueOf(lon));
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void run() {
		while(running) {
			Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			try {
				writer.write(formatter.format(new Date()) + ", " +
						String.format("%.6f, %.6f\n", loc.getLatitude(), loc.getLongitude()));
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
} 
