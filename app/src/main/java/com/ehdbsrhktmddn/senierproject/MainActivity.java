package com.ehdbsrhktmddn.senierproject;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.*;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity<BluetoothSPP> extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태

    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터

    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋

    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스

    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓

    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림

    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림

    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드

    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼

    private int readBufferPosition; // 버퍼 내 문자 저장 위치

    public TextView textViewDust;

    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


    int pariedDeviceCount;
    String[] word;

    private TextView textViewReceive, textViewReceive2, textViewID; // 수신 된 데이터를 표시하기 위한 텍스트 뷰

    //private EditText editTextSend; // 송신 할 데이터를 작성하기 위한 에딧 텍스트

    private Button buttonSend, buttonSend2; // 송신하기 위한 버튼


    String h;
    int gongi;
    double mise;

    int swtich = 0;

    private ImageView imageView1, imageView2;

    private BluetoothSPP bt;
    public ConstraintLayout mLayout;

    //View line;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("IOT 자동환기 시스템");
        showDialogForLocationServiceSetting();
        mLayout = (ConstraintLayout) findViewById(R.id.mLayout);
        mLayout.setBackgroundColor(Color.rgb(135, 206, 235));
        textViewReceive = (TextView) findViewById(R.id.textView15);
        textViewReceive2 = (TextView) findViewById(R.id.textView16);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonSend2 = (Button) findViewById(R.id.buttonSend2);
        imageView1 = (ImageView) findViewById(R.id.imageView);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        textViewDust = (TextView) findViewById(R.id.textView14);
        textViewID = (TextView) findViewById(R.id.textViewID);

//
//

        //gps 수신값띄우기

        gpsTracker = new GpsTracker(MainActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();
        final String address = getCurrentAddress(latitude, longitude);
//                textview_address.setText(address);

        final FineDustMeasure fm = FineDustMeasure.getInstance();
        String region = "대한민국 서울특별시 구로구 구로5동 544";
        final String temp[] = FineDustMeasure.getRealRegion(region);

        for (String t : temp) {
            Log.d("BBBBBBBBBBBBBBBBBB : ", t);
        }

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                FineDustMeasure.FineDustVO vo = fm.GetFineDust_pm(temp);
                String result = address + "\n" + vo.pm10 + "\n" + vo.region + "\n" + vo.dateTime + "\n";
                /*Message message = handler.obtainMessage();

                Bundle bundle = new Bundle();
                bundle.putString("msg", result);
                message.setData(bundle);
                handler.sendMessage(message);*/

                textViewDust.setText(vo.pm10.concat("㎍/m³"));
                textViewID.setText(address);
//
            }
        });

        thread1.start();


        //알람


        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                if (swtich != 1) {
                    sendData("1");

                    swtich = 1;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendData("3");
                        }
                    }, 4000);
                }

            }

        });


        buttonSend2.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                sendData("2");

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendData("3");
                        createNotification2();
                        swtich = 0;
                    }
                }, 4000);

            }

        });

        // 블루투스 활성화하기

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정


        if (bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때

            // 여기에 처리 할 코드를 작성하세요.

        } else { // 디바이스가 블루투스를 지원 할 때

            if (bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)

                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출

            } else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)

                // 블루투스를 활성화 하기 위한 다이얼로그 출력

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                // 선택한 값이 onActivityResult 함수에서 콜백된다.

                startActivityForResult(intent, REQUEST_ENABLE_BT);

            }


        }
    }

    private void sendData(String text) {
        // 문자열에 개행문자("\n")를 추가해줍니다.

        text += "\n";
        try {

            // 데이터 송신

            outputStream.write(text.getBytes());

            Log.d("버튼눌림", "잘됨");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_ENABLE_BT:

                if (requestCode == RESULT_OK) { // '사용'을 눌렀을 때

                    selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출

                } else { // '취소'를 눌렀을 때

                    // 여기에 처리 할 코드를 작성하세요.

                }

                break;

        }

    }*/

    private void selectBluetoothDevice() {

        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.

        devices = bluetoothAdapter.getBondedDevices();

        // 페어링 된 디바이스의 크기를 저장

        pariedDeviceCount = devices.size();

        // 페어링 되어있는 장치가 없는 경우

        if (pariedDeviceCount == 0) {

            // 페어링을 하기위한 함수 호출

        }

        // 페어링 되어있는 장치가 있는 경우

        else {

            // 디바이스를 선택하기 위한 다이얼로그 생성

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");

            // 페어링 된 각각의 디바이스의 이름과 주소를 저장

            List<String> list = new ArrayList<>();

            // 모든 디바이스의 이름을 리스트에 추가

            for (BluetoothDevice bluetoothDevice : devices) {

                list.add(bluetoothDevice.getName());

            }

            list.add("취소");


            // List를 CharSequence 배열로 변경

            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);

            list.toArray(new CharSequence[list.size()]);


            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너

            builder.setItems(charSequences, new DialogInterface.OnClickListener() {

                @Override

                public void onClick(DialogInterface dialog, int which) {

                    // 해당 디바이스와 연결하는 함수 호출

                    connectDevice(charSequences[which].toString());

                }

            });


            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정

            builder.setCancelable(false);

            // 다이얼로그 생성

            AlertDialog alertDialog = builder.create();

            alertDialog.show();

        }

    }

    private void connectDevice(String deviceName) {
        // 페어링 된 디바이스들을 모두 탐색

        for (BluetoothDevice tempDevice : devices) {

            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료

            if (deviceName.equals(tempDevice.getName())) {

                bluetoothDevice = tempDevice;

                break;

            }

        }

        // UUID 생성

        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성

        try {

            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);

            bluetoothSocket.connect();

            // 데이터 송,수신 스트림을 얻어옵니다.

            outputStream = bluetoothSocket.getOutputStream();

            inputStream = bluetoothSocket.getInputStream();

            // 데이터 수신 함수 호출

            receiveData();

        } catch (IOException e) {

            e.printStackTrace();

        }


    }

    private void receiveData() {
        final Handler handler = new Handler();

        // 데이터를 수신하기 위한 버퍼를 생성
        Log.d("수신", "잘됨");

        readBufferPosition = 0;

        readBuffer = new byte[1024];


        // 데이터를 수신하기 위한 쓰레드 생성

        workerThread = new Thread(new Runnable() {

            @Override

            public void run() {

                while (!(Thread.currentThread().isInterrupted())) {

                    try {

                        // 데이터를 수신했는지 확인합니다.

                        int byteAvailable = inputStream.available();

                        // 데이터가 수신 된 경우

                        if (byteAvailable > 0) {

                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.

                            byte[] bytes = new byte[byteAvailable];

                            inputStream.read(bytes);

                            // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.

                            for (int i = 0; i < byteAvailable; i++) {

                                byte tempByte = bytes[i];

                                // 개행문자를 기준으로 받음(한줄)

                                if (tempByte == '\n') {

                                    // readBuffer 배열을 encodedBytes로 복사

                                    byte[] encodedBytes = new byte[readBufferPosition];

                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    // 인코딩 된 바이트 배열을 문자열로 변환

                                    final String text = new String(encodedBytes, "US-ASCII");

                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {

                                        @Override

                                        public void run() {

                                            // 텍스트 뷰에 출력

                                            //textViewReceive.append(text + "\n");

                                            word = text.split(",");
                                            gongi = Integer.parseInt(word[0]);

                                            mise = Double.valueOf(word[1]);


                                            textViewReceive.setText(word[0] + " ppm");
                                            textViewReceive2.setText(word[1] + "㎍/m³");

                                            // 배경색 변경
                                            if (((gongi >= 101) && (gongi < 251)) || ((mise >= 81) && (mise < 151))) {           //나쁨
                                                mLayout.setBackgroundColor(Color.rgb(244, 174, 114));
                                            } else if (gongi >= 251 || mise >= 151.0) {                                     //매우나쁨
                                                mLayout.setBackgroundColor(Color.rgb(255, 0, 0));
                                            } else if (((gongi >= 0) && (gongi < 51)) || ((mise >= 0) && (mise < 31))) {              //  좋음
                                                mLayout.setBackgroundColor(Color.rgb(255, 192, 203));
                                            } else if (((gongi >= 51) && (gongi < 101)) || ((mise >= 31) && (mise < 81))) {             //보통
                                                mLayout.setBackgroundColor(Color.rgb(135, 206, 235));
                                            }

                                            // 공기질 얼굴 변경
                                            if ((gongi >= 101) && (gongi < 251)) {
                                                imageView1.setImageResource(R.drawable.soso);
                                            } else if (gongi >= 251) {
                                                imageView1.setImageResource(R.drawable.fuck);
                                            } else if ((gongi >= 0) && (gongi < 51)) {
                                                imageView1.setImageResource(R.drawable.happy);
                                            } else if ((gongi >= 51) && (gongi < 101)) {
                                                imageView1.setImageResource(R.drawable.good);
                                            }

                                            // 미세먼지 얼굴 변경
                                            if ((mise >= 81) && (mise < 151)) {
                                                imageView2.setImageResource(R.drawable.soso);
                                            } else if (mise >= 151.0) {
                                                imageView2.setImageResource(R.drawable.fuck);
                                            } else if ((mise >= 0) && (mise < 31)) {
                                                imageView2.setImageResource(R.drawable.happy);
                                            } else if ((mise >= 31) && (mise < 81)) {
                                                imageView2.setImageResource(R.drawable.good);
                                            }


                                            if ((gongi >= 101 || mise >= 81.0) && swtich == 0) {

                                                sendData("1");
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        sendData("3");
                                                        createNotification1();
                                                        swtich = 1;
                                                    }
                                                }, 10000);
                                            }


                                            if (((gongi >= 0) && (gongi < 51)) && ((mise >= 0) && (mise < 31)) && swtich == 1) {

                                                sendData("1");
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        sendData("3");
                                                        createNotification2();
                                                        swtich = 0;
                                                    }
                                                }, 10000);
                                            }


                                        }

                                    });

                                } // 개행 문자가 아닐 경우
                                else {

                                    readBuffer[readBufferPosition++] = tempByte;

                                }

                            }

                        }

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                    try {

                        // 1초마다 받아옴

                        Thread.sleep(1000);

                    } catch (InterruptedException e) {

                        e.printStackTrace();

                    }

                }

            }

        });

        workerThread.start();

    }

    //푸쉬알람
    private void createNotification1() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("창문 작동 알림");
        builder.setContentText("창문이 열렸습니다.");

        builder.setColor(Color.RED);
        // 사용자가 탭을 클릭하면 자동 제거
        builder.setAutoCancel(true);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // id값은
        // 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(1, builder.build());
    }

    //창문 닫는 푸쉬버튼
    private void createNotification2() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("창문 작동 알림");
        builder.setContentText("창문이 닫혔습니다.");

        builder.setColor(Color.RED);
        // 사용자가 탭을 클릭하면 자동 제거
        builder.setAutoCancel(true);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // id값은
        // 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(2, builder.build());
    }

//    @Override
//    public void run() {
//        FineDustMeasure Dust = FineDustMeasure.getInstance();
//        double b1 = Dust.GetFineDust_pm("chungbuk","pm10");
//
//        String b2 = Double.toString(b1);
//        textViewDust.setText(b2);
//
//    }


    // gps 부분

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    void checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        Log.d("에러", "에러");
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }


    public String getCurrentAddress(double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
//            Toast.makeText(this, "경도 : " + latitude, Toast.LENGTH_LONG).show();
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        Log.d("안됨", "안됨");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //showDialogForLocationServiceSetting();

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;

        }

//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//
//            case REQUEST_ENABLE_BT:
//
//                if (requestCode == RESULT_OK) { // '사용'을 눌렀을 때
//
//                    selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
//
//                } else { // '취소'를 눌렀을 때
//
//                    // 여기에 처리 할 코드를 작성하세요.
//
//                }
//
//                break;
//
//        }


    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


}