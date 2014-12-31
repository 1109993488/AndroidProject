package com.linj.camera.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.camera.R;
import com.linj.camera.view.CameraContainer.TakePictureListener;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;


/** 
 * @ClassName: CameraView 
 * @Description: ������󶨵�SurfaceView ��װ�����շ���
 * @author LinJ
 * @date 2014-12-31 ����9:44:56 
 *  
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback
,AutoFocusCallback{

	private final static String TAG="CameraView";

	/** �͸�View�󶨵�Camera���� */
	private Camera mCamera;

	/** ��ǰ��������ͣ�Ĭ��Ϊ�ر� */ 
	private FlashMode mFlashMode=FlashMode.OFF;

	public CameraView(Context context){
		super(context);
		//��ʼ������
		getHolder().addCallback(this);
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//��ʼ������
		getHolder().addCallback(this);
	}


	/**  
	 *  ��ȡ��ǰ���������
	 *  @return   
	 */
	public FlashMode getFlashMode() {
		return mFlashMode;
	}

	/**  
	 *  �������������
	 *  @param flashMode   
	 */
	public void setFlashMode(FlashMode flashMode) {
		if(flashMode==mFlashMode) return;
		mFlashMode = flashMode;
		Camera.Parameters parameters=mCamera.getParameters();
		switch (flashMode) {
		case ON:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			break;
		case AUTO:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			break;
		case TORCH:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			break;
		default:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			break;
		}
		mCamera.setParameters(parameters);
	}

	public void takePicture(PictureCallback callback,TakePictureListener listener){
		mCamera.takePicture(null, null, callback);
	}

	/**  
	 * �ֶ��۽� 
	 *  @param x ������x����
	 *  @param y   ������y����
	 */
	public void onFocus(int x,int y){
		Camera.Parameters parameters=mCamera.getParameters();
		//��֧�������Զ���۽�����ʹ���Զ��۽�������
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(this);
			return;
		}
		List<Area> areas=new ArrayList<Camera.Area>();
		int left=x-300;
		int top=y-300;
		int right=x+300;
		int bottom=y+300;
		areas.add(new Area(new Rect(left,top,right,bottom), 100));
		parameters.setFocusAreas(areas);
		try {
			//����ʹ�õ�С���ֻ������þ۽������ʱ�򾭳�����쳣������־�����ǿ�ܲ���ַ���תint��ʱ������ˣ�
			//Ŀ����С���޸��˿�ܲ���뵼�£��ڴ�try������ʵ�ʾ۽�Ч��ûӰ��
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			// TODO: handle exception
		}
		mCamera.autoFocus(this);
	}



	/**
	 * �������������
	 */
	private void setCameraParameters(){
		Camera.Parameters parameters = mCamera.getParameters();
		// ѡ����ʵ�Ԥ���ߴ�   
		List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			//Ԥ��ͼƬ��С
			parameters.setPreviewSize(cameraSize.width, cameraSize.height);
		}

		//�������ɵ�ͼƬ��С
		sizeList = parameters.getSupportedPictureSizes();
		if (sizeList.size()>0) {
			Size cameraSize=sizeList.get(0);
			for (Size size : sizeList) {
				//С��500W����
				if (size.width*size.height<100*10000) {
					cameraSize=size;
					break;
				}
			}
			parameters.setPictureSize(cameraSize.width, cameraSize.height);
		}
		//����ͼƬ��ʽ
		parameters.setPictureFormat(ImageFormat.JPEG);       
		parameters.setJpegQuality(100);
		parameters.setJpegThumbnailQuality(100);
		//�Զ�У׼
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//		if(parameters.isZoomSupported())
//           parameters.setZoom(parameters.getMaxZoom());
//		parameters.
		mCamera.setParameters(parameters);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera = Camera.open();
			setCameraParameters();
			mCamera.setPreviewDisplay(getHolder());
		} catch (IOException e) {
			Toast.makeText(getContext(), "�����ʧ��", Toast.LENGTH_SHORT).show();
			Log.e(TAG,e.getMessage());
		}
		mCamera.startPreview();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		Camera.Parameters parameters = mCamera.getParameters();
		Log.i(TAG, getResources().getConfiguration().orientation+"");
		//�ж���Ļ����
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mCamera.setDisplayOrientation(90);//Ԥ��ת90��
			parameters.set("rotation", 90);//���ɵ�ͼƬת90��
			
		}
		else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			parameters.set("orientation", "landscape");
			parameters.set("rotation", 0);
		}
		mCamera.setParameters(parameters);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		if (mCamera != null) {
			mCamera.stopPreview();
		}
		mCamera.release();
		mCamera = null;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {

	}

	/** 
	 * @Description: ���������ö�� Ĭ��Ϊ�ر�
	 */
	public enum FlashMode{
		/** ON:����ʱ�������   */ 
		ON,
		/** OFF�����������  */ 
		OFF,
		/** AUTO��ϵͳ�����Ƿ�������  */ 
		AUTO,
		/** TORCH��һֱ�������  */ 
		TORCH
	}
}