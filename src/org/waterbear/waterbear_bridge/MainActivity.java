package org.waterbear.waterbear_bridge;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private TextView log;
	private Button sendTest; 
	private BtInterface bt = null;
	private UDPServer udpServer = null;
	private long lastTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        this.log = (TextView) this.findViewById(R.id.log);

        this.sendTest = (Button) findViewById(R.id.send_test);
        this.sendTest.setOnClickListener(this);
        
        this.bt = new BtInterface(handlerStatus, handlerBT);
        this.bt.connect();
        
        this.udpServer = new UDPServer(handlerStatus, handlerUDP);
        this.udpServer.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.bt.close();
		this.udpServer.close();
	}

    final Handler handlerStatus = new Handler() {
        public void handleMessage(Message msg) {
            int co = msg.arg1;
            if (co == 0) {
            	log.append((String) msg.obj);
            } 
        	else if (co == 1) {
            	Exception e = (Exception) msg.obj;
            	StringBuilder errorMsg = 
            			new StringBuilder("Bluetooth Error: \n").append(e.toString());
            	Toast.makeText(
            			MainActivity.this, errorMsg, Toast.LENGTH_LONG)
            			.show();
            }
        	else if (co == 2) {
            	Exception e = (Exception) msg.obj;
            	StringBuilder errorMsg = 
            			new StringBuilder("UDP Error: \n").append(e.toString());
            	Toast.makeText(
            			MainActivity.this, errorMsg, Toast.LENGTH_LONG)
            			.show();
            }

        }
    };
    
	final Handler handlerBT = new Handler() {
        public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData");
            
            long t = System.currentTimeMillis();
            if(t - lastTime > 100) { // Pour éviter que les messages soit coupés
                log.append("\n");
				lastTime = System.currentTimeMillis();
			}
            log.append(data);
        }
    };
    
	final Handler handlerUDP = new Handler() {
        public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData");
        	log.append(data);
        	bt.sendData(data);
        }
	};

	@Override
	public void onClick(View v) {
		if (v == this.sendTest) {
			bt.sendData("Blaise aime les penis ! (Ca c'est malin...)\n");
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
