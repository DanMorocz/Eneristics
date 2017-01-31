/*******************************************************************************
ls
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kura.example.ble.tisensortag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.bluetooth.BluetoothAdapter;
import org.eclipse.kura.bluetooth.BluetoothDevice;
import org.eclipse.kura.bluetooth.BluetoothGattCharacteristic;
import org.eclipse.kura.bluetooth.BluetoothGattService;
import org.eclipse.kura.bluetooth.BluetoothLeScanListener;
import org.eclipse.kura.bluetooth.BluetoothService;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;


public class BluetoothLe implements ConfigurableComponent, CloudClientListener, BluetoothLeScanListener {
	
	public static final String MONGO_READINGS_DB = "readings";
	public static final String MONGO_WEATHER_COLLECTION = "weather";
	public static final String MONGO_DBJSON_COLLECTION = "dbjson";
	private static final String MONGO_INSERT_TIME_KEY = "insert_time";
	
    private static final Logger s_logger = LoggerFactory.getLogger(BluetoothLe.class);

    private final String APP_ID = "BLE_APP_V1";
    private final String PROPERTY_SCAN = "scan_enable";
    private final String PROPERTY_SCANTIME = "scan_time";
    private final String PROPERTY_PERIOD = "period";
    private final String PROPERTY_TEMP = "enableTermometer";
    private final String PROPERTY_ACC = "enableAccelerometer";
    private final String PROPERTY_HUM = "enableHygrometer";
    private final String PROPERTY_MAG = "enableMagnetometer";
    private final String PROPERTY_PRES = "enableBarometer";
    private final String PROPERTY_GYRO = "enableGyroscope";
    private final String PROPERTY_OPTO = "enableLuxometer";
    private final String PROPERTY_BUTTONS = "enableButtons";
    private final String PROPERTY_REDLED = "switchOnRedLed";
    private final String PROPERTY_GREENLED = "switchOnGreenLed";
    private final String PROPERTY_BUZZER = "switchOnBuzzer";
    private final String PROPERTY_TOPIC = "publishTopic";
    private final String PROPERTY_INAME = "iname";

    private CloudService m_cloudService;
    private static CloudClient m_cloudClient;
    private List<TiSensorTag> m_tiSensorTagList;
    private BluetoothService m_bluetoothService;
    private BluetoothAdapter m_bluetoothAdapter;
    private List<BluetoothGattService> m_bluetoothGattServices;
    private ScheduledExecutorService m_worker;
    private ScheduledFuture<?> m_handle;

    private int m_period = 10;
    private int m_scantime = 5;
    private static String m_topic = "data";
    private long m_startTime;
    private boolean m_connected = false;
    private String iname = "hci0";
    private boolean enableScan = false;
    private boolean enableTemp = false;
    private boolean enableAcc = false;
    private boolean enableHum = false;
    private boolean enableMag = false;
    private boolean enablePres = false;
    private boolean enableGyro = false;
    private boolean enableOpto = false;
    private boolean enableButtons = false;
    private boolean enableRedLed = false;
    private boolean enableGreenLed = false;
    private boolean enableBuzzer = false;

    public void setCloudService(CloudService cloudService) {
        this.m_cloudService = cloudService;
    }

    public void unsetCloudService(CloudService cloudService) {
        this.m_cloudService = null;
    }

    public void setBluetoothService(BluetoothService bluetoothService) {
        this.m_bluetoothService = bluetoothService;
    }

    public void unsetBluetoothService(BluetoothService bluetoothService) {
        this.m_bluetoothService = null;
    }

    // --------------------------------------------------------------------
    //
    // Activation APIs
    //
    // --------------------------------------------------------------------
    protected void activate(ComponentContext context, Map<String, Object> properties) {
        s_logger.info("Activating BluetoothLe example...");

        if (properties != null) {
            if (properties.get(this.PROPERTY_SCAN) != null) {
                this.enableScan = (Boolean) properties.get(this.PROPERTY_SCAN);
            }
            if (properties.get(this.PROPERTY_SCANTIME) != null) {
                this.m_scantime = (Integer) properties.get(this.PROPERTY_SCANTIME);
            }
            if (properties.get(this.PROPERTY_PERIOD) != null) {
                this.m_period = (Integer) properties.get(this.PROPERTY_PERIOD);
            }
            if (properties.get(this.PROPERTY_TEMP) != null) {
                this.enableTemp = (Boolean) properties.get(this.PROPERTY_TEMP);
            }
            if (properties.get(this.PROPERTY_ACC) != null) {
                this.enableAcc = (Boolean) properties.get(this.PROPERTY_ACC);
            }
            if (properties.get(this.PROPERTY_HUM) != null) {
                this.enableHum = (Boolean) properties.get(this.PROPERTY_HUM);
            }
            if (properties.get(this.PROPERTY_MAG) != null) {
                this.enableMag = (Boolean) properties.get(this.PROPERTY_MAG);
            }
            if (properties.get(this.PROPERTY_PRES) != null) {
                this.enablePres = (Boolean) properties.get(this.PROPERTY_PRES);
            }
            if (properties.get(this.PROPERTY_GYRO) != null) {
                this.enableGyro = (Boolean) properties.get(this.PROPERTY_GYRO);
            }
            if (properties.get(this.PROPERTY_OPTO) != null) {
                this.enableOpto = (Boolean) properties.get(this.PROPERTY_OPTO);
            }
            if (properties.get(this.PROPERTY_BUTTONS) != null) {
                this.enableButtons = (Boolean) properties.get(this.PROPERTY_BUTTONS);
            }
            if (properties.get(this.PROPERTY_REDLED) != null) {
                this.enableRedLed = (Boolean) properties.get(this.PROPERTY_REDLED);
            }
            if (properties.get(this.PROPERTY_GREENLED) != null) {
                this.enableGreenLed = (Boolean) properties.get(this.PROPERTY_GREENLED);
            }
            if (properties.get(this.PROPERTY_BUZZER) != null) {
                this.enableBuzzer = (Boolean) properties.get(this.PROPERTY_BUZZER);
            }
            if (properties.get(this.PROPERTY_TOPIC) != null) {
                m_topic = (String) properties.get(this.PROPERTY_TOPIC);
            }
            if (properties.get(this.PROPERTY_INAME) != null) {
                this.iname = (String) properties.get(this.PROPERTY_INAME);
            }
        }

        this.m_tiSensorTagList = new ArrayList<TiSensorTag>();

        try {
            m_cloudClient = this.m_cloudService.newCloudClient(this.APP_ID);
            m_cloudClient.addCloudClientListener(this);
        } catch (KuraException e1) {
            s_logger.error("Error starting component", e1);
            throw new ComponentException(e1);
        }

        if (this.enableScan) {

            this.m_worker = Executors.newSingleThreadScheduledExecutor();

            try {

                // Get Bluetooth adapter and ensure it is enabled
                this.m_bluetoothAdapter = this.m_bluetoothService.getBluetoothAdapter(this.iname);
                if (this.m_bluetoothAdapter != null) {
                    s_logger.info("Bluetooth adapter interface => " + this.iname);
                    s_logger.info("Bluetooth adapter address => " + this.m_bluetoothAdapter.getAddress());
                    s_logger.info("Bluetooth adapter le enabled => " + this.m_bluetoothAdapter.isLeReady());

                    if (!this.m_bluetoothAdapter.isEnabled()) {
                        s_logger.info("Enabling bluetooth adapter...");
                        this.m_bluetoothAdapter.enable();
                        s_logger.info("Bluetooth adapter address => " + this.m_bluetoothAdapter.getAddress());
                    }
                    this.m_startTime = 0;
                    this.m_connected = false;
                    this.m_handle = this.m_worker.scheduleAtFixedRate(new Runnable() {

                        @Override
                        public void run() {
                            checkScan();
                        }
                    }, 0, 1, TimeUnit.SECONDS);
                } else {
                    s_logger.warn("No Bluetooth adapter found ...");
                }
            } catch (Exception e) {
                s_logger.error("Error starting component", e);
                throw new ComponentException(e);
            }
        }
    }

    protected void deactivate(ComponentContext context) {

        s_logger.debug("Deactivating BluetoothLe...");
        if (this.m_bluetoothAdapter != null && this.m_bluetoothAdapter.isScanning()) {
            s_logger.debug("m_bluetoothAdapter.isScanning");
            this.m_bluetoothAdapter.killLeScan();
        }

        // disconnect SensorTags
        for (TiSensorTag tiSensorTag : this.m_tiSensorTagList) {
            if (tiSensorTag != null) {
                tiSensorTag.disconnect();
            }
        }
        this.m_tiSensorTagList.clear();

        // cancel a current worker handle if one if active
        if (this.m_handle != null) {
            this.m_handle.cancel(true);
        }

        // shutting down the worker and cleaning up the properties
        if (this.m_worker != null) {
            this.m_worker.shutdown();
        }

        // cancel bluetoothAdapter
        this.m_bluetoothAdapter = null;

        // Releasing the CloudApplicationClient
        s_logger.info("Releasing CloudApplicationClient for {}...", this.APP_ID);
        if (m_cloudClient != null) {
            m_cloudClient.release();
        }

        s_logger.debug("Deactivating BluetoothLe... Done.");
    }

    protected void updated(Map<String, Object> properties) {

        if (properties != null) {
            if (properties.get(this.PROPERTY_SCAN) != null) {
                this.enableScan = (Boolean) properties.get(this.PROPERTY_SCAN);
            }
            if (properties.get(this.PROPERTY_SCANTIME) != null) {
                this.m_scantime = (Integer) properties.get(this.PROPERTY_SCANTIME);
            }
            if (properties.get(this.PROPERTY_PERIOD) != null) {
                this.m_period = (Integer) properties.get(this.PROPERTY_PERIOD);
            }
            if (properties.get(this.PROPERTY_TEMP) != null) {
                this.enableTemp = (Boolean) properties.get(this.PROPERTY_TEMP);
            }
            if (properties.get(this.PROPERTY_ACC) != null) {
                this.enableAcc = (Boolean) properties.get(this.PROPERTY_ACC);
            }
            if (properties.get(this.PROPERTY_HUM) != null) {
                this.enableHum = (Boolean) properties.get(this.PROPERTY_HUM);
            }
            if (properties.get(this.PROPERTY_MAG) != null) {
                this.enableMag = (Boolean) properties.get(this.PROPERTY_MAG);
            }
            if (properties.get(this.PROPERTY_PRES) != null) {
                this.enablePres = (Boolean) properties.get(this.PROPERTY_PRES);
            }
            if (properties.get(this.PROPERTY_GYRO) != null) {
                this.enableGyro = (Boolean) properties.get(this.PROPERTY_GYRO);
            }
            if (properties.get(this.PROPERTY_OPTO) != null) {
                this.enableOpto = (Boolean) properties.get(this.PROPERTY_OPTO);
            }
            if (properties.get(this.PROPERTY_BUTTONS) != null) {
                this.enableButtons = (Boolean) properties.get(this.PROPERTY_BUTTONS);
            }
            if (properties.get(this.PROPERTY_REDLED) != null) {
                this.enableRedLed = (Boolean) properties.get(this.PROPERTY_REDLED);
            }
            if (properties.get(this.PROPERTY_GREENLED) != null) {
                this.enableGreenLed = (Boolean) properties.get(this.PROPERTY_GREENLED);
            }
            if (properties.get(this.PROPERTY_BUZZER) != null) {
                this.enableBuzzer = (Boolean) properties.get(this.PROPERTY_BUZZER);
            }
            if (properties.get(this.PROPERTY_TOPIC) != null) {
                m_topic = (String) properties.get(this.PROPERTY_TOPIC);
            }
            if (properties.get(this.PROPERTY_INAME) != null) {
                this.iname = (String) properties.get(this.PROPERTY_INAME);
            }
        }

        try {
            s_logger.debug("Deactivating BluetoothLe...");
            if (this.m_bluetoothAdapter != null && this.m_bluetoothAdapter.isScanning()) {
                s_logger.debug("m_bluetoothAdapter.isScanning");
                this.m_bluetoothAdapter.killLeScan();
            }

            // disconnect SensorTags
            for (TiSensorTag tiSensorTag : this.m_tiSensorTagList) {
                if (tiSensorTag != null) {
                    tiSensorTag.disconnect();
                }
            }
            this.m_tiSensorTagList.clear();

            // cancel a current worker handle if one is active
            if (this.m_handle != null) {
                this.m_handle.cancel(true);
            }

            // shutting down the worker and cleaning up the properties
            if (this.m_worker != null) {
                this.m_worker.shutdown();
            }

            // cancel bluetoothAdapter
            this.m_bluetoothAdapter = null;

            if (this.enableScan) {
                // re-create the worker
                this.m_worker = Executors.newSingleThreadScheduledExecutor();

                // Get Bluetooth adapter and ensure it is enabled
                this.m_bluetoothAdapter = this.m_bluetoothService.getBluetoothAdapter(this.iname);
                if (this.m_bluetoothAdapter != null) {
                    s_logger.info("Bluetooth adapter interface => " + this.iname);
                    s_logger.info("Bluetooth adapter address => " + this.m_bluetoothAdapter.getAddress());
                    s_logger.info("Bluetooth adapter le enabled => " + this.m_bluetoothAdapter.isLeReady());

                    if (!this.m_bluetoothAdapter.isEnabled()) {
                        s_logger.info("Enabling bluetooth adapter...");
                        this.m_bluetoothAdapter.enable();
                        s_logger.info("Bluetooth adapter address => " + this.m_bluetoothAdapter.getAddress());
                    }
                    this.m_startTime = 0;
                    this.m_connected = false;
                    this.m_handle = this.m_worker.scheduleAtFixedRate(new Runnable() {

                        @Override
                        public void run() {
                            checkScan();
                        }
                    }, 0, 1, TimeUnit.SECONDS);
                } else {
                    s_logger.warn("No Bluetooth adapter found ...");
                }
            }
        } catch (Exception e) {
            s_logger.error("Error starting component", e);
            throw new ComponentException(e);
        }

        s_logger.debug("Updating Bluetooth Service... Done.");
    }

    // --------------------------------------------------------------------
    //
    // Main task executed every second
    //
    // --------------------------------------------------------------------

    void checkScan() {

        // Scan for devices
        if (this.m_bluetoothAdapter.isScanning()) {
            s_logger.info("m_bluetoothAdapter.isScanning");
            if (System.currentTimeMillis() - this.m_startTime >= this.m_scantime * 1000) {
                this.m_bluetoothAdapter.killLeScan();
            }
        } else {
            if (System.currentTimeMillis() - this.m_startTime >= this.m_period * 1000) {
                s_logger.info("startLeScan");
                this.m_bluetoothAdapter.startLeScan(this);
                this.m_startTime = System.currentTimeMillis();
            }
        }

    }

    // --------------------------------------------------------------------
    //
    // Private Methods
    //
    // --------------------------------------------------------------------

    protected static void doPublishKeys(String address, Object key) {
        KuraPayload payload = new KuraPayload();
        payload.setTimestamp(new Date());
        payload.addMetric("key", key);
        try {
            m_cloudClient.publish(m_topic + "/" + address + "/keys", payload, 0, false);
        } catch (Exception e) {
            s_logger.error("Can't publish message, " + "keys", e);
        }

    }

    private void doServicesDiscovery(TiSensorTag tiSensorTag) {
        s_logger.info("Starting services discovery...");
        this.m_bluetoothGattServices = tiSensorTag.discoverServices();
        for (BluetoothGattService bgs : this.m_bluetoothGattServices) {
            s_logger.info(
                    "Service UUID: " + bgs.getUuid() + "  :  " + bgs.getStartHandle() + "  :  " + bgs.getEndHandle());
        }
    }

    private void doCharacteristicsDiscovery(TiSensorTag tiSensorTag) {
        List<BluetoothGattCharacteristic> lbgc = tiSensorTag.getCharacteristics("0x0001", "0x0100");
        for (BluetoothGattCharacteristic bgc : lbgc) {
            s_logger.info(
                    "Characteristics uuid : " + bgc.getUuid() + " : " + bgc.getHandle() + " : " + bgc.getValueHandle());
        }
    }

    private boolean searchSensorTagList(String address) {

        for (TiSensorTag tiSensorTag : this.m_tiSensorTagList) {
            if (tiSensorTag.getBluetoothDevice().getAdress().equals(address)) {
                return true;
            }
        }
        return false;
    }

    // --------------------------------------------------------------------
    //
    // BluetoothLeScanListener APIs
    //
    // --------------------------------------------------------------------
    @Override
    public void onScanFailed(int errorCode) {
        s_logger.error("Error during scan");

    }

    @Override
    public void onScanResults(List<BluetoothDevice> scanResults) {

        
    	
    	// Scan for TI SensorTag
    	for (BluetoothDevice bluetoothDevice : scanResults) {
            s_logger.info("Address " + bluetoothDevice.getAdress() + " Name " + bluetoothDevice.getName());

            if (bluetoothDevice.getName().contains("SensorTag")) {
                s_logger.info("TI SensorTag " + bluetoothDevice.getAdress() + " found.");
                
                
                if (!searchSensorTagList(bluetoothDevice.getAdress())) {
                    TiSensorTag tiSensorTag = new TiSensorTag(bluetoothDevice);
                    this.m_tiSensorTagList.add(tiSensorTag);
                }
            } else {
                s_logger.info("Found device = " + bluetoothDevice.getAdress());
            }
        }

        s_logger.debug("Found " + this.m_tiSensorTagList.size() + " SensorTags");

        // connect to TiSensorTags
        for (TiSensorTag myTiSensorTag : this.m_tiSensorTagList) {

            if (!myTiSensorTag.isConnected()) {
                s_logger.info("Connecting to TiSensorTag...");
                this.m_connected = myTiSensorTag.connect(this.iname);
            } else {
                s_logger.info("TiSensorTag already connected!");
                this.m_connected = true;
            }

            if (this.m_connected) {

                KuraPayload payload = new KuraPayload();
                payload.setTimestamp(new Date());
                if (myTiSensorTag.getCC2650()) {
                    payload.addMetric("Type", "CC2650");
                } else {
                    payload.addMetric("Type", "CC2541");
                }

                // Test
                // doServicesDiscovery(myTiSensorTag);
                // doCharacteristicsDiscovery(myTiSensorTag);

                myTiSensorTag.setFirmwareRevision(myTiSensorTag.firmwareRevision());
                
                String humidity_0 = "0";
               
                if (this.enableHum) {
                    myTiSensorTag.enableHygrometer();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    float humidity = myTiSensorTag.readHumidity();
                    s_logger.info("Humidity: " + ""+humidity);

                    payload.addMetric("humidity", ""+humidity);
                    humidity_0 = ""+humidity;
                    
                }
                String timestampOut = ""+System.currentTimeMillis();
                if (this.enableTemp) {
                    myTiSensorTag.enableTermometer();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    double[] temperatures = myTiSensorTag.readTemperature();

                    s_logger.info("Ambient: " + temperatures[0] + " Target: " + temperatures[1]);
                    s_logger.info("Latest Testing ");

                    payload.addMetric("ambient", ""+temperatures[0]);//changing to string jan 23
                    payload.addMetric("target", ""+temperatures[1]);//changing to string jan 23
                    payload.addMetric("timestamp", ""+timestampOut);//changing to string jan 23
                    payload.addMetric("device_id", ""+myTiSensorTag.getBluetoothDevice().getAdress());
                    
                    String temperatures_0 = ""+temperatures[0];
                    String temperatures_1 = ""+temperatures[1];
                    timestampOut = ""+System.currentTimeMillis();
                    
                    
                 //***********************************
            		/*Runtime rt = Runtime.getRuntime();
            		String[] commands = {"java","-jar", "/home/pi/TestMongoInsert.jar", myTiSensorTag.getBluetoothDevice().getAdress(), timestampOut , temperatures_0 ,humidity_0};
            		Process proc = null;
            		try {
            			proc = rt.exec(commands);
            		} catch (IOException e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		} 
            		BufferedReader stdInput = new BufferedReader(new 
            			     InputStreamReader(proc.getInputStream()));

            			BufferedReader stdError = new BufferedReader(new 
            			     InputStreamReader(proc.getErrorStream()));

            			// read the output from the command
            			System.out.println("Here is the standard output of the command:\n");
            			String s = null;
            			try {
            				while ((s = stdInput.readLine()) != null) {
            				   // System.out.println(s);
            				    s_logger.info("Console Log of NC " + s );
            				}
            			} catch (IOException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			}

            			// read any errors from the attempted command
            			System.out.println("Here is the standard error of the command (if any):\n");
            			try {
            				while ((s = stdError.readLine()) != null) {
            				   // System.out.println(s);
            				    s_logger.info("Console Log of NC " + s);
            				}
            			} catch (IOException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			}
*/
            			Runtime rtweather = Runtime.getRuntime();
                		String[] commandsweather = {"java","-jar", "/home/pi/TestMongoWeather.jar"};
                		Process procweather = null;
                		try {
                			procweather = rtweather.exec(commandsweather);
                		} catch (IOException e) {
                			// TODO Auto-generated catch block
                			e.printStackTrace();
                		} 
                		//*****************************************
                    
                }

//                if (this.enableAcc) {
//                    if (myTiSensorTag.getCC2650()) {
//                        // Reduce period to 500ms (for a bug on SensorTag firmware :-)) and enable accelerometer with
//                        // range 8g
//                        myTiSensorTag.setAccelerometerPeriod("32");
//                        myTiSensorTag.enableAccelerometer("3802");
//                    } else {
//                        myTiSensorTag.enableAccelerometer("01");
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    double[] acceleration = myTiSensorTag.readAcceleration();
//
//                    s_logger.info(
//                            "Acc X: " + acceleration[0] + " Acc Y: " + acceleration[1] + " Acc Z: " + acceleration[2]);
//
//                    payload.addMetric("Acceleration X", acceleration[0]);
//                    payload.addMetric("Acceleration Y", acceleration[1]);
//                    payload.addMetric("Acceleration Z", acceleration[2]);
//                }
//
              
                
                
                
                
//
//                if (this.enableMag) {
//                    // Reduce period to 500ms (for a bug on SensorTag firmware :-)) and enable magnetometer
//                    myTiSensorTag.setMagnetometerPeriod("32");
//                    if (myTiSensorTag.getCC2650()) {
//                        myTiSensorTag.enableMagnetometer("4000");
//                    } else {
//                        myTiSensorTag.enableMagnetometer("");
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    float[] magneticField = myTiSensorTag.readMagneticField();
//
//                    s_logger.info("Mag X: " + magneticField[0] + " Mag Y: " + magneticField[1] + " Mag Z: "
//                            + magneticField[2]);
//
//                    payload.addMetric("Magnetic X", magneticField[0]);
//                    payload.addMetric("Magnetic Y", magneticField[1]);
//                    payload.addMetric("Magnetic Z", magneticField[2]);
//
//                }
//
//                if (this.enablePres) {
//                    // Calibrate pressure sensor
//                    myTiSensorTag.calibrateBarometer();
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    myTiSensorTag.readCalibrationBarometer();
//
//                    // Read pressure
//                    myTiSensorTag.enableBarometer();
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    double pressure = myTiSensorTag.readPressure();
//
//                    s_logger.info("Pre : " + pressure);
//
//                    payload.addMetric("Pressure", pressure);
//                }
//
//                if (this.enableGyro) {
//                    if (myTiSensorTag.getCC2650()) {
//                        // Reduce period to 500ms (for a bug on SensorTag firmware :-)) and enable gyroscope
//                        myTiSensorTag.setGyroscopePeriod("32");
//                        myTiSensorTag.enableGyroscope("0700");
//                    } else {
//                        myTiSensorTag.enableGyroscope("07");
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    float[] gyroscope = myTiSensorTag.readGyroscope();
//
//                    s_logger.info("Gyro X: " + gyroscope[0] + " Gyro Y: " + gyroscope[1] + " Gyro Z: " + gyroscope[2]);
//
//                    payload.addMetric("Gyro X", gyroscope[0]);
//                    payload.addMetric("Gyro Y", gyroscope[1]);
//                    payload.addMetric("Gyro Z", gyroscope[2]);
//
//                }
//
//                if (this.enableOpto) {
//                    myTiSensorTag.enableLuxometer();
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    double light = myTiSensorTag.readLight();
//                    s_logger.info("Light: " + light);
//
//                    payload.addMetric("Light", light);
//                }
//
//                if (this.enableButtons) {
//                    // For buttons only enable notifications
//                    myTiSensorTag.enableKeysNotification();
//                }
//
//                if (this.enableRedLed) {
//                    myTiSensorTag.switchOnRedLed();
//                } else {
//                    myTiSensorTag.switchOffRedLed();
//                }
//
//                if (this.enableGreenLed) {
//                    myTiSensorTag.switchOnGreenLed();
//                } else {
//                    myTiSensorTag.switchOffGreenLed();
//                }
//
//                if (this.enableBuzzer) {
//                    myTiSensorTag.switchOnBuzzer();
//                } else {
//                    myTiSensorTag.switchOffBuzzer();
//                }
                s_logger.info("Latest Testing myTi is ebabled ");
                myTiSensorTag.enableIOService();

                try {
                    // Publish to Mongo only if there are metrics to be published!
                	
                      //SENSOR DATA
                		//build a new Document Wrapper object. One for each sensortag.
                        s_logger.info("Latest Testing building SensorWrapper ");
                        try{
                            MongoSensorDocWrapper sensorWrapper = new MongoSensorDocWrapper(s_logger);
                           // s_logger.info("WRAPPER: " + sensorWrapper.toString());
                		   //publish dummy data to the MongoDB for this sensortag for testing
                		   //ACTUAL USE: wrapper.publish(kuraPayload)
                           sensorWrapper.publish(payload);
                           //sensorWrapper.publish();//for test only
                            } catch (Exception e) {	
                        	s_logger.info(e.toString());
                        }
                		
                		//WEATHER DATA
                		//build a model of the current data from weather server, including timestamp
                        //s_logger.info("CREATE WEATHER WRAPPPER ");
                        //MongoWeatherWrapper weatherWrapper = new MongoWeatherWrapper();
                        //s_logger.info("WEATHER WRAPPPER CREATED ");
                		
                		//publish the weather wrapper, which already contains a timestamped poll of the weather server
                        //s_logger.info("PUBLISH MONGO WEATHER WRAPPPER ");
                        //weatherWrapper.publishMongo();
                        //s_logger.info(" MONGO WEATHER WRAPPPER PUBLISHED ");
                		
                		/* s_logger.info("Latest Testing about to MQTT publish ");
                         if (!payload.metricNames().isEmpty()) {
                             m_cloudClient.publish(m_topic + "/" + myTiSensorTag.getBluetoothDevice().getAdress(), payload,
                                     0, false);*/
                	
                	//*************************
                        
                        
                     s_logger.info("**** PUBLISHUNG MQTT PAYLOAD *******v1 ");
                     if (!payload.metricNames().isEmpty()) {
                         m_cloudClient.publish(m_topic + "/" + myTiSensorTag.getBluetoothDevice().getAdress(), payloadToByteArray(payload),
                                 0, false, 0);
                         
                         m_cloudClient.publish(m_topic + "/" + myTiSensorTag.getBluetoothDevice().getAdress(), getLastWeatherBytes(),
                                 0, false, 0);
                         
                         //UPDATE: Publish dbjson to AWS
                         m_cloudClient.publish(m_topic + "/" + "<DBJASON TOPIC NAME>", getLastDBJSONBytes(),
                                 0, false, 0);
                     
                          
                    /* s_logger.info("**** PUBLISHING PLAIN TEXT WEATHER *******v1 ");
	                 m_cloudClient.publish(m_topic + "/" + myTiSensorTag.getBluetoothDevice().getAdress(), getWeatherXML().getBytes(),
	                             0, false, 0);*/
                             
                 /*    try {
                    	 s_logger.info("**** PUBLISHING MQTT WEATHER *******v1 ");
    	                 m_cloudClient.publish(m_topic + "/" + myTiSensorTag.getBluetoothDevice().getAdress(), new WeatherDocument().getBytes(),
    	                             0, false, 0);
                     } catch(Exception e) {
                    	 s_logger.info("**** PUBLISHING Exception! *******v1 " + e.getCause().getMessage());
                     }*/
                    
                        
                	
                	/*s_logger.info("HELLO HELLO ");
                	String hello = "hello";   
                         if (!payload.metricNames().isEmpty()) {
                                 m_cloudClient.publish(m_topic + "/" + myTiSensorTag.getBluetoothDevice().getAdress(), hello.getBytes(),
                                         0, false, 0);*/
                             
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                s_logger.info("Cannot connect to TI SensorTag " + myTiSensorTag.getBluetoothDevice().getAdress() + ".");
            }
        }
    }
    
    /**
     * @author Dan Morocz
     * @return byte[] of the most recent item in the dbjson collection.
     */
    private byte[] getLastDBJSONBytes() {
    	MongoClient mongo = StaticMongo.getInstance(s_logger);
		DB db = new DB(mongo, MONGO_READINGS_DB);
		JSONObject document = new JSONObject();
		
		//public Cursor aggregate(List<? extends DBObject> pipeline, AggregationOptions options)
		//DBCursor lastThreePolls = new DBCursor(db.getCollection(SENSOR_COLLECTION), DBObject query, DBObject fields, ReadPreference readPreference);
			
		BasicDBObject queryParams = new BasicDBObject();
		queryParams.append(MONGO_INSERT_TIME_KEY, -1);
		DBCursor polls = db.getCollection(MONGO_DBJSON_COLLECTION).find().sort( queryParams );
		
		//get the most recent poll from the collection
		int counter = 0;
		String output = "";
		int numPolls = 1;
		while(polls.hasNext() && counter++<numPolls) {
			DBObject obj = polls.next();
			
			try {
	        	document.put("sensortags", (obj.get("sensortags")).toString());
	        	document.put("weather", (obj.get("weather")).toString());
	        	document.put(MONGO_INSERT_TIME_KEY, (obj.get(MONGO_INSERT_TIME_KEY)).toString());	        	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return document.toString().getBytes(StandardCharsets.UTF_8);
	}
    
    /**
     * @author Dan Morocz
     * @return byte[] of the most recent fetch of weather data in the mondoDB weather collection.
     */
    private byte[] getLastWeatherBytes() {
    	MongoClient mongo = StaticMongo.getInstance(s_logger);
		DB db = new DB(mongo, MONGO_READINGS_DB);
		JSONObject document = new JSONObject();
		
		//public Cursor aggregate(List<? extends DBObject> pipeline, AggregationOptions options)
		//DBCursor lastThreePolls = new DBCursor(db.getCollection(SENSOR_COLLECTION), DBObject query, DBObject fields, ReadPreference readPreference);
			
		BasicDBObject queryParams = new BasicDBObject();
		queryParams.append("timestamp", -1);
		DBCursor polls = db.getCollection(MONGO_WEATHER_COLLECTION).find().sort( queryParams );
		
		//get the most recent poll from the collection
		int counter = 0;
		String output = "";
		int numPolls = 1;
		while(polls.hasNext() && counter++<numPolls) {
			DBObject obj = polls.next();
			
			try {
	        	document.put("timestamp", (obj.get("timestamp")).toString());
	        	document.put("temperature", (obj.get("temperature")).toString());
	        	document.put("windChill", (obj.get("windChill")).toString());
	        	document.put("pressure", (obj.get("pressure")).toString());
	        	document.put("humidity", (obj.get("humidity")).toString());
	        	document.put("windSpeed", (obj.get("windSpeed")).toString());
	        	document.put("windUnits", (obj.get("windUnits")).toString());
	        	document.put("windDirection", (obj.get("windDirection")).toString());
	        	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return document.toString().getBytes(StandardCharsets.UTF_8);
	}

	/**
     * @author Dan Morocz
     * @return
     * @throws Exception
     * Grab the XML from the weather URL and return it.
     */
    public String getWeatherXML() throws Exception{
    	URL weatherURL = new URL("http://dd.weatheroffice.ec.gc.ca/citypage_weather/xml/ON/s0000430_e.xml");
        URLConnection yc = weatherURL.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                                    yc.getInputStream()));
        String inputLine;
        String weatherXML = "";
        while ((inputLine = in.readLine()) != null) 
            weatherXML += inputLine;
        in.close();
        
        return weatherXML;
    }
    
    /**
     * @author Dan Morocz
     * @param payload Payload to convert to JSON object as a byte[].
     * @return payload in the form of a JSON Object, cast to a byte[].
     */
    private static byte[] payloadToByteArray(KuraPayload payload) {
		
		JSONObject obj = new JSONObject();

        try {
			obj.put("Timestamp", payload.getMetric("Timestamp"));
			obj.put("Type", payload.getMetric("Type"));
			obj.put("Humidity", payload.getMetric("Humidity"));
			obj.put("Temperature", payload.getMetric("Ambient"));
			obj.put("Device_id", payload.getMetric("Device_id"));
			//obj.put("Ambient", payload.getMetric("Ambient"));
			//obj.put("Target", payload.getMetric("Target"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return obj.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    

    // --------------------------------------------------------------------
    //
    // CloudClientListener APIs
    //
    // --------------------------------------------------------------------
    @Override
    public void onControlMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {

    }

    @Override
    public void onMessageArrived(String deviceId, String appTopic, KuraPayload msg, int qos, boolean retain) {

    }

    @Override
    public void onConnectionLost() {

    }

    @Override
    public void onConnectionEstablished() {

    }

    @Override
    public void onMessageConfirmed(int messageId, String appTopic) {

    }

    @Override
    public void onMessagePublished(int messageId, String appTopic) {

    }

}
