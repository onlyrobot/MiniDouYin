package com.pengyao.minidouyin;

import android.Manifest;
import android.content.Intent;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.pengyao.minidouyin.bean.PostVideoResponse;
import com.pengyao.minidouyin.tool.ResourceUtils;
import com.pengyao.minidouyin.tool.RetrofitManager;
import com.pengyao.minidouyin.tool.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.pengyao.minidouyin.tool.Utils.MEDIA_TYPE_IMAGE;
import static com.pengyao.minidouyin.tool.Utils.MEDIA_TYPE_VIDEO;
import static com.pengyao.minidouyin.tool.Utils.getOutputMediaFile;

public class Capsture extends AppCompatActivity implements View.OnClickListener{

    private SurfaceView mSurfaceView;
    private Button playButton;
    private View cs_view;
    private Button cancelButton;
    private Button submitButton;
    private Button recordButton;
    private MaterialProgressBar progressBar;
    private Camera mCamera;
    private Camera.Parameters parameters;
    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;
    private MediaRecorder mMediaRecorder = null;
    private float oldDist = 1f;
    private boolean isRecording = false;
    private int rotationDegree = 0;
    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private String[] mPermissionsArrays = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private final static int REQUEST_PERMISSION = 123;
    private static final int PICK_IMAGE = 1;

    private MediaPlayer player = null;
    private String path;
    private Call<PostVideoResponse> postCall;
    private MultipartBody.Part video;
    private MultipartBody.Part coverImage;

    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_capsture);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(mPermissionsArrays, REQUEST_PERMISSION);
        }

        mSurfaceView = findViewById(R.id.mSurfaceView);
        playButton = findViewById(R.id.mBtnPlay);
        playButton.setOnClickListener(this);
        cancelButton = findViewById(R.id.mBtnCancle);
        cancelButton.setOnClickListener(this);
        submitButton = findViewById(R.id.mBtnSubmit);
        submitButton.setOnClickListener(this);
        recordButton = findViewById(R.id.btn_record);
        recordButton.setOnClickListener(this);
        progressBar = findViewById(R.id.mProgress);
        cs_view = findViewById(R.id.mLlRecordOp);
        initCamera();

//        findViewById(R.id.btn_picture).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //todo 拍一张照片
//                mCamera.takePicture(null, null, mPicture);
//            }
//        });
//        findViewById(R.id.btn_facing).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //todo 切换前后摄像头
//                if (CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                    mCamera = Capsture.this.getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
//                } else {
//                    mCamera = Capsture.this.getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
//                }
//                try {
//                    mCamera.setPreviewDisplay(surfaceHolder);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mCamera.startPreview();
//            }
//        });
    }

    public void initCamera(){
        mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        final SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCameraAndPreview();
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.mBtnPlay:
                player.start();
                playButton.setVisibility(View.INVISIBLE);
                playButton.requestLayout();
                break;
            case R.id.btn_record:
                if (isRecording) {
                    Toast.makeText(Capsture.this, "停止录制", Toast.LENGTH_LONG).show();
                    Capsture.this.releaseMediaRecorder();
                    isRecording = false;
                    releaseCameraAndPreview();
                    startPreview(mSurfaceView.getHolder());
                    if(progressBar.getVisibility() == View.VISIBLE){
                        progressBar.setVisibility(View.INVISIBLE);
                        progressBar.requestLayout();
                    }
                } else {
                    if (Capsture.this.prepareVideoRecorder()) {
                        mMediaRecorder.start();
                        isRecording = true;
                        Toast.makeText(Capsture.this, "开始录制", Toast.LENGTH_LONG).show();
                        progressBar.setMax(10);
                        progressBar.setProgress(-1);
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.requestLayout();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progressBar.getProgress() + 1);
                                if(progressBar.getProgress() == progressBar.getMax()){
                                    progressBar.setVisibility(View.INVISIBLE);
                                    progressBar.requestLayout();
                                    recordButton.performClick();
                                }else if(isRecording){
                                    handler.postDelayed(this, 1000);
                                }
                            }
                        });
                    }else{
                        releaseMediaRecorder();
                    }
                }
                break;
            case R.id.mBtnCancle:
                View view = findViewById(R.id.mLlRecordOp);
                view.setVisibility(View.INVISIBLE);
                view.requestLayout();
                playButton.setVisibility(View.INVISIBLE);
                playButton.requestLayout();
                player.stop();
                player.release();
                player = null;
                mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                try {
                    mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File videoFile = new File(path);
                if (videoFile.exists() && videoFile.isFile()) {
                    videoFile.delete();
                }
                mCamera.startPreview();
                break;
            case R.id.mBtnSubmit:
                View view1 = findViewById(R.id.mLlRecordOp);
                view1.setVisibility(View.INVISIBLE);
                view1.requestLayout();
                playButton.setVisibility(View.INVISIBLE);
                playButton.requestLayout();
                Uri videoUri = Uri.fromFile(new File(path));
                video = getMultipartFromUri("video", videoUri);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == PICK_IMAGE) {
                coverImage = getMultipartFromUri("cover_image", data.getData());
                Retrofit retrofit = RetrofitManager.get("http://test.androidcamp.bytedance.com/");
                Call<PostVideoResponse> postVideoResponseCall = retrofit.
                        create(Service.class).post("1120172773", "彭瑶", coverImage, video);
                postCall = postVideoResponseCall;
                postVideoResponseCall.enqueue(new Callback<PostVideoResponse>() {
                    @Override
                    public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                        Toast.makeText(Capsture.this, "Video posted!", Toast.LENGTH_LONG).show();
                        postCall = null;
                    }
                    @Override
                    public void onFailure(Call<PostVideoResponse> call, Throwable t) {
                        postCall = null;
                    }
                });
            }
        }
    }
    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(Capsture.this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }
    private void startPreview(SurfaceHolder holder) {
        cs_view.setVisibility(View.VISIBLE);
        cs_view.requestLayout();

        if(player == null){
            player = new MediaPlayer();
        }
        player.setDisplay(holder);
        try{
            player.setDataSource(path);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playButton.setVisibility(View.VISIBLE);
                playButton.requestLayout();
            }
        });
        player.start();
    }


    private static float getFingerDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void cameraZoom(boolean isZoomIn, Camera camera) {
        parameters = mCamera.getParameters();
        if (parameters.isZoomSupported()) {
            int maxZoom = parameters.getMaxZoom();
            int currentZoom = parameters.getZoom();
            if (isZoomIn && currentZoom < maxZoom) {
                currentZoom+=2;
                if (currentZoom > maxZoom) {
                    currentZoom = maxZoom;
                }
            } else if (currentZoom > 0) {
                currentZoom-=2;
                if (currentZoom < 0) {
                    currentZoom = 0;
                }
            }
            parameters.setZoom(currentZoom);
            camera.setParameters(parameters);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            return true;
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerDistance(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerDistance(event);
                    if (newDist > oldDist) {
                        cameraZoom(true, mCamera);
                    } else if (newDist < oldDist) {
                        cameraZoom(false, mCamera);
                    }
                    oldDist = newDist;
                    break;
            }
        }
        return true;
    }
    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder
        releaseMediaRecorder();
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFile(path = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);
        try{
            mMediaRecorder.prepare();
        }catch (Exception e){
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                Log.d("hello", "null() called");
                fos.close();
            } catch (IOException e) {
                Log.d("mPicture", "Error accessing file: " + e.getMessage());
            }
            mCamera.startPreview();
        }
    };
    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        rotationDegree = getCameraDisplayOrientation(position);
        cam.setDisplayOrientation(rotationDegree);
        return cam;
    }
    private void releaseMediaRecorder() {
        if(mMediaRecorder != null){
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }
    private void releaseCameraAndPreview() {
        if(mCamera != null){
            mCamera.lock();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    private void releasePlayerAndPreview(){
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
        if(playButton.getVisibility() != View.INVISIBLE){
            playButton.setVisibility(View.INVISIBLE);
        }
        if(cs_view.getVisibility() != View.INVISIBLE){
            cs_view.setVisibility(View.INVISIBLE);
        }
    }
    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    @Override
    public void onResume(){
        super.onResume();
        releasePlayerAndPreview();
        releaseMediaRecorder();
        if(mCamera == null){
            mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            try{
                mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
