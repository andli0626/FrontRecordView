package org.crazyit.sound;

import java.io.File;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class RecordVideo extends Activity implements OnClickListener,SurfaceHolder.Callback {
	// 程序中的两个按钮
	Button record, stop;
	// 系统的视频文件
	File videoFile;
	MediaRecorder 	mediaRecorder;
	Camera 			mCamera;
	int 			mCurrentCamIndex = 0;
	// 显示视频预览的SurfaceView
	SurfaceView surfaceview;
	boolean previewing;
	// 记录是否正在进行录制
	private boolean isRecording = false;

	// 用来显示视频的一个接口，我靠不用还不行，也就是说用mediarecorder录制视频还得给个界面看
	// 想偷偷录视频的同学可以考虑别的办法。。嗯需要实现这个接口的Callback接口
	private SurfaceHolder mSurfaceHolder;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 去掉标题栏
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 设置全屏
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 设置横屏显示
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// 选择支持半透明模式,在有surfaceview的activity中使用。
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		
		setContentView(R.layout.main);
		
		// 获取程序界面中的两个按钮
		record = (Button) findViewById(R.id.record);
		stop   = (Button) findViewById(R.id.stop);
		// 让stop按钮不可用。
		stop.setEnabled(false);
		record.setOnClickListener(this);
		stop  .setOnClickListener(this);
		
		// 获取程序界面中的SurfaceView
		surfaceview = (SurfaceView) this.findViewById(R.id.sView);
		// holder加入回调接口
		surfaceview.getHolder().addCallback(this);
		// 【必须设置】设置Surface不需要自己维护缓冲区
		surfaceview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// 设置分辨率
		// surfaceview.getHolder().setFixedSize(320, 280);
		// 设置该组件让屏幕不会自动关闭
		surfaceview.getHolder().setKeepScreenOn(true);
	}

	@Override
	public void onClick(View source) {
		switch (source.getId()) {
		// 单击录制按钮
		case R.id.record:
			if (!Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				Toast.makeText(RecordVideo.this, "SD卡不存在，请插入SD卡！",Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				// 创建mediarecorder对象
				mediaRecorder = new MediaRecorder();
				mediaRecorder.reset();
				// 【必须设置】解锁摄像头，否则报错，无法进行视频录制
				mCamera.unlock();
				mediaRecorder.setCamera(mCamera);
				// 设置从麦克风采集声音
				 mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				// 设置从摄像头采集图像
				mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				
				/**** 设置视频文件的输出格式（必须在设置声音编码格式、图像编码格式之前设置） ****/
				
				//输出格式
				//mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
				mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				
				/**** 设置 声音，图像编码格式 ****/
				// 声音编码格式
				mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
				// 图像编码格式
				// mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);//不合法
				mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
				
				/**** 设置 视频分辨率，视频帧率（必须放在设置编码和格式的后面，否则报错） ****/
				// 视频分辨率 设置值可能导致无法录制
				// mediaRecorder.setVideoSize(320, 480);
				// mediaRecorder.setVideoSize(176, 144);
				// 视频帧率 每秒 4帧
				// mediaRecorder.setVideoFrameRate(4);
				mediaRecorder.setVideoFrameRate(20);
				
				// 保存视频
				// 创建保存录制视频的视频文件
				// videoFile = new File(Environment.getExternalStorageDirectory().getCanonicalFile() + "/myvideo.mp4");
				// mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
				mediaRecorder.setOutputFile("/sdcard/front_vedio.3gp");
				
				// 指定使用SurfaceView来预览视频
				//mediaRecorder.setPreviewDisplay(surfaceview.getHolder().getSurface()); 
				mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface()); 
				
				// 准备录制
				mediaRecorder.prepare();
				// 开始录制
				mediaRecorder.start();
				
				// 让record按钮不可用。
				record.setEnabled(false);
				// 让stop按钮可用。
				stop.setEnabled(true);
				
				isRecording = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		// 单击停止按钮
		case R.id.stop:
			// 如果正在进行录制
			if (isRecording) {
				// 停止录制
				mediaRecorder.stop();
				// 释放资源
				mediaRecorder.release();
				mediaRecorder = null;
				// 让record按钮可用。
				record.setEnabled(true);
				// 让stop按钮不可用。
				stop.setEnabled(false);
			}
			break;
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
		mSurfaceHolder = holder;
		if (previewing) {
			mCamera.stopPreview();
			previewing = false;
		}

		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			previewing = true;
			setCameraDisplayOrientation(RecordVideo.this, mCurrentCamIndex, mCamera);
		} catch (Exception e) {}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
		mSurfaceHolder = holder;
		mCamera = openFrontCamera();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// surfaceDestroyed的时候同时对象设置为null
		surfaceview 	= null;
		mSurfaceHolder 	= null;
		mediaRecorder 	= null;
		
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		previewing = false;
	}
	
	@Override
	protected void onDestroy() {
		isRecording = false;
		super.onDestroy();
	}
	
	// 打开前置摄像头
	private Camera openFrontCamera() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
					mCurrentCamIndex = camIdx;
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
		return cam;
	}
	// 根据横竖屏自动调节preview方向，Starting from API level 14, this method can be called when preview is active.
	// 需要android 4.0以上
	private static void setCameraDisplayOrientation(Activity activity,int cameraId, Camera camera) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

		// degrees the angle that the picture will be rotated clockwise. Valid
		// values are 0, 90, 180, and 270.
		// The starting position is 0 (landscape).
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else {
			// back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}
}
