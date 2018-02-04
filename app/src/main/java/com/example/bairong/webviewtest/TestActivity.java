package com.example.bairong.webviewtest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private final static String TAG = "IcsTestActivity";
    private final static String ALBUM_PATH
            = Environment.getExternalStorageDirectory() + "/download_test/";
    private ImageView mImageView;
    private ImageView mImgRead;
    private Button mBtnSave;
    private Button mBtnRead;
    private ProgressDialog mSaveDialog = null;
    private Bitmap mBitmap;
    private String mFileName;
    private String mSaveMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mImageView = (ImageView) findViewById(R.id.imgSource);
        mImgRead = (ImageView) findViewById(R.id.imgRead);
        mBtnSave = (Button) findViewById(R.id.btnSave);
        mBtnRead = (Button) findViewById(R.id.btnRead);
        if(judgeFile(ALBUM_PATH,"test.jpg")) {
            Toast.makeText(TestActivity.this,"存在",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(TestActivity.this,"不存在",Toast.LENGTH_LONG).show();
        }
        new Thread(connectNet).start();
        // 下载图片
        mBtnSave.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mSaveDialog = ProgressDialog.show(TestActivity.this, "保存图片", "图片正在保存中，请稍等...", true);
                new Thread(saveFileRunnable).start();
            }
        });
        mBtnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    readFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Get image from newwork
     *
     * @param path The path of image
     * @return byte[]
     * @throws Exception
     */
    public byte[] getImage(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        InputStream inStream = conn.getInputStream();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return readStream(inStream);
        }
        return null;
    }

    /**
     * Get image from newwork
     *
     * @param path The path of image
     * @return InputStream
     * @throws Exception
     */
    public InputStream getImageStream(String path) throws Exception {

        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream();
        }
        return null;
    }

    /**
     * Get data from stream
     *
     * @param inStream
     * @return byte[]
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * 保存文件
     *
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public void saveFile(Bitmap bm, String fileName) throws IOException {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            File dirFile = new File(ALBUM_PATH);
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            File myCaptureFile = new File(ALBUM_PATH + fileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        }
    }

    /**
     * 读取文件
     *
     * @throws IOException
     */
    public void readFile() throws IOException {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            File dirFile = new File(ALBUM_PATH);
            if (!dirFile.exists()) {
                Toast.makeText(TestActivity.this, "文件不存在！", Toast.LENGTH_LONG).show();
            } else {
//                File myCaptureFile = new File(ALBUM_PATH + fileName);
//                InputStream bos = new InputStream(new FileInputStream(myCaptureFile));
//                bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
//                bos.flush();
//                bos.close();
//                if(bmpDefaultPic==null)
                Bitmap bmpDefaultPic = BitmapFactory.decodeFile(ALBUM_PATH + "test.jpg", null);
                mImgRead.setImageBitmap(bmpDefaultPic);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    saveFile(mBitmap, mFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Permission Denied
                Toast.makeText(TestActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Runnable saveFileRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                saveFile(mBitmap, mFileName);
                mSaveMessage = "图片保存成功！";
            } catch (IOException e) {
                mSaveMessage = "图片保存失败！";
                e.printStackTrace();
            }
            messageHandler.sendMessage(messageHandler.obtainMessage());
        }
    };

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mSaveDialog.dismiss();
            Log.d(TAG, mSaveMessage);
            Toast.makeText(TestActivity.this, mSaveMessage, Toast.LENGTH_SHORT).show();
        }
    };

    /*
    * 连接网络
    * 由于在4.0中不允许在主线程中访问网络，所以需要在子线程中访问
    */
    private Runnable connectNet = new Runnable() {
        @Override
        public void run() {
            try {
                String filePath = "http://img.my.csdn.net/uploads/201402/24/1393242467_3999.jpg";
                mFileName = "test.jpg";

                //以下是取得图片的两种方法
                //////////////// 方法1：取得的是byte数组, 从byte数组生成bitmap
//                byte[] data = getImage(filePath);
//                if(data!=null){
//                    mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);// bitmap
//                }else{
//                    Toast.makeText(TestActivity.this, "Image error!", 1).show();
//                }
                ////////////////////////////////////////////////////////

                //******** 方法2：取得的是InputStream，直接从InputStream生成bitmap ***********/
                mBitmap = BitmapFactory.decodeStream(getImageStream(filePath));
                //********************************************************************/

                // 发送消息，通知handler在主线程中更新UI
                connectHanlder.sendEmptyMessage(0);
                Log.d(TAG, "set image ...");
            } catch (Exception e) {
                Toast.makeText(TestActivity.this, "无法链接网络！", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        }
    };

    private Handler connectHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "display image");
            // 更新UI，显示图片
            if (mBitmap != null) {
                mImageView.setImageBitmap(mBitmap);// display image
            }
        }
    };

    /**
     * 获取指定目录内所有文件路径
     * @param dirPath 需要查询的文件目录
     * @param _type 查询类型，比如mp3什么的
     */
    public  boolean judgeFile(String dirPath, String _type) {
        boolean tag=false;
        File f = new File(dirPath);
        if (!f.exists()) {//判断路径是否存在
            return false;
        }
        File[] files = f.listFiles();
        if(files==null){//判断权限
            return false;
        }
        for (File _file : files) {//遍历目录
            if(_file.isFile() && _file.getName().equals(_type)){
                tag=true;
                break;
            } else if(_file.isDirectory()){//查询子目录
                judgeFile(_file.getAbsolutePath(), _type);
            } else{
                tag=false;
            }
        }
        return  tag;
    }

}
