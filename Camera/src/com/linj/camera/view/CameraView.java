package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.camera.view.CameraContainer.TakePictureListener;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;


/** 
 * @ClassName: CameraView 
 * @Description: ������󶨵�SurfaceView ��װ�����շ���
 * @author LinJ
 * @date 2014-12-31 ����9:44:56 
 *  
 */
public class CameraView extends SurfaceView {

	public final static String TAG="CameraView";
	/** �͸�View�󶨵�Camera���� */
	private Camera mCamera;

	/** ��ǰ��������ͣ�Ĭ��Ϊ�ر� */ 
	private FlashMode mFlashMode=FlashMode.ON;

	/** ��ǰ���ż���  Ĭ��Ϊ0*/ 
	private int mZoom=0;

	/** ��ǰ��Ļ��ת�Ƕ�*/ 
	private int mOrientation=0;
	/** �Ƿ��ǰ�����,trueΪǰ��,falseΪ����  */ 
	private boolean mIsFrontCamera;
	/**  ¼���� */ 
	private MediaRecorder mMediaRecorder;
	/**  ������ã���¼��ǰ��¼������¼�������ָ�ԭ���� */ 
	private Camera.Parameters mParameters;
	/**  ¼����·�� ��������������ͼ*/ 
	private String mRecordPath=null;
	public CameraView(Context context){
		super(context);
		//��ʼ������
		getHolder().addCallback(callback);
		openCamera();
		mIsFrontCamera=false;
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//��ʼ������
		getHolder().addCallback(callback);
		openCamera();
		mIsFrontCamera=false;
	}

	private SurfaceHolder.Callback callback=new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if(mCamera==null){
					openCamera();
				}
				setCameraParameters();
				mCamera.setPreviewDisplay(getHolder());
			} catch (Exception e) {
				Toast.makeText(getContext(), "�����ʧ��", Toast.LENGTH_SHORT).show();
				Log.e(TAG,e.getMessage());
			}
			mCamera.startPreview();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			updateCameraOrientation();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			//ֹͣ¼��
			stopRecord();
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}

		}
	};

	public boolean isRecording(){
		return mMediaRecorder!=null;
	}
	/**  
	 *  ��ʼ¼��
	 *  @return ��ʼ¼���Ƿ�ɹ�   
	 */
	public boolean startRecord(){
		if(mCamera==null)
			openCamera();
		if (mCamera==null) {
			return false;
		}
		if(mMediaRecorder==null)
			mMediaRecorder = new MediaRecorder();
		else
			mMediaRecorder.reset();
		mParameters=mCamera.getParameters();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);
		mMediaRecorder
		.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder
		.setAudioSource(MediaRecorder.AudioSource.MIC);
		//����¼�����������Ӧ����Ҫ�˴�ȡһ����С��ʽ����Ƶ
		mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
		//���������Ƶ���򣬱��ڲ�����ʶ������������¼�ƣ���Ҫ��ת90��
		mMediaRecorder.setOrientationHint(90);
		String path=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_VEDIO, "test");
		File directory=new File(path);
		if(!directory.exists())
			directory.mkdirs();
		try {
			String name="vedio"+FileOperateUtil.createFileNmae(".3gp");
			mRecordPath=path+File.separator+name;
			File mRecAudioFile = new File(mRecordPath);
			mMediaRecorder.setOutputFile(mRecAudioFile
					.getAbsolutePath());
			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void stopRecord(){
		try {
			if(mMediaRecorder!=null){
				mMediaRecorder.stop();
				mMediaRecorder.reset();
				mMediaRecorder.release();
				mMediaRecorder=null;
				//������Ƶ������ͼ
				saveThumbnail();
			}
			if(mParameters!=null&&mCamera!=null){
				//�����������
				mCamera.reconnect();
				//ֹͣԤ����ע����������ȵ���ֹͣԤ�������ò�������Ч
				mCamera.stopPreview();
				//���ò���Ϊ¼��ǰ�Ĳ�������Ȼ���¼���ǵ��䣬����¼�ƺ�Ԥ��Ч�����ǵ��仭��
				mCamera.setParameters(mParameters);
				//���´�
				mCamera.startPreview();
				mParameters=null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveThumbnail() throws FileNotFoundException, IOException {
		if(mRecordPath!=null){
			//��������ͼ
			Bitmap bitmap=ThumbnailUtils.createVideoThumbnail(mRecordPath, Thumbnails.MICRO_KIND);
			if(bitmap!=null){
				String mThumbnailFolder=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, "test");
				File folder=new File(mThumbnailFolder);
				if(!folder.exists()){
					folder.mkdirs();
				}
				File file=new File(mRecordPath);
				file=new File(folder+File.separator+file.getName().replace("3gp", "jpg"));
				//��ͼƬСͼ
				BufferedOutputStream bufferos=new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bufferos);
				bufferos.flush();
				bufferos.close();
			}
			mRecordPath=null;
		}
	}

	/**  
	 *   ת��ǰ�úͺ��������
	 */
	public void switchCamera(){
		mIsFrontCamera=!mIsFrontCamera;
		openCamera();
		if(mCamera!=null){
			setCameraParameters();
			updateCameraOrientation();
			try {
				mCamera.setPreviewDisplay(getHolder());
				mCamera.startPreview();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**  
	 *   ���ݵ�ǰ�����״̬(ǰ�û����)���򿪶�Ӧ���
	 */
	private boolean openCamera()  {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

		if(mIsFrontCamera){
			Camera.CameraInfo cameraInfo=new CameraInfo();
			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if(cameraInfo.facing==Camera.CameraInfo.CAMERA_FACING_FRONT){
					try {
						mCamera=Camera.open(i);
					} catch (Exception e) {
						mCamera =null;
						return false;
					}

				}
			}
		}else {
			try {
				mCamera=Camera.open();
			} catch (Exception e) {
				mCamera =null;
				return false;
			}

		}
		return true;
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
		if(mCamera==null) return;
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
	 *  @param point ��������
	 */
	public void onFocus(Point point,AutoFocusCallback callback){
		Camera.Parameters parameters=mCamera.getParameters();
		//��֧�������Զ���۽�����ʹ���Զ��۽�������
		if (parameters.getMaxNumFocusAreas()<=0) {
			mCamera.autoFocus(callback);
			return;
		}
		List<Area> areas=new ArrayList<Camera.Area>();
		int left=point.x-300;
		int top=point.y-300;
		int right=point.x+300;
		int bottom=point.y+300;
		left=left<-1000?-1000:left;
		top=top<-1000?-1000:top;
		right=right>1000?1000:right;
		bottom=bottom>1000?1000:bottom;
		areas.add(new Area(new Rect(left,top,right,bottom), 100));
		parameters.setFocusAreas(areas);
		try {
			//����ʹ�õ�С���ֻ������þ۽������ʱ�򾭳�����쳣������־�����ǿ�ܲ���ַ���תint��ʱ������ˣ�
			//Ŀ����С���޸��˿�ܲ���뵼�£��ڴ�try������ʵ�ʾ۽�Ч��ûӰ��
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		mCamera.autoFocus(callback);
	}

	/**  
	 *  ��ȡ������ż������Ϊ40
	 *  @return   
	 */
	public int getMaxZoom(){
		if(mCamera==null) return -1;		
		Camera.Parameters parameters=mCamera.getParameters();
		if(!parameters.isZoomSupported()) return -1;
		return parameters.getMaxZoom()>40?40:parameters.getMaxZoom();
	}
	/**  
	 *  ����������ż���
	 *  @param zoom   
	 */
	public void setZoom(int zoom){
		if(mCamera==null) return;
		Camera.Parameters parameters;
		//ע��˴�Ϊ¼��ģʽ�µ�setZoom��ʽ����Camera.unlock֮�󣬵���getParameters����������android��ܵײ���쳣
		//stackoverflow�Ͽ����Ľ��������ڶ��߳�ͬʱ����Camera���µĳ�ͻ�������ڴ�ʹ��¼��ǰ�����mParameters��
		if(mParameters!=null)
			parameters=mParameters;
		else {
			parameters=mCamera.getParameters();
		}

		if(!parameters.isZoomSupported()) return;
		parameters.setZoom(zoom);
		mCamera.setParameters(parameters);
		mZoom=zoom;
	}
	public int getZoom(){
		return mZoom;
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
				//С��100W����
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
		//�Զ��۽�ģʽ
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(parameters);
		//���������ģʽ���˴���Ҫ������������ݻٺ����ؽ�������֮ǰ��״̬
		setFlashMode(mFlashMode);
		//�������ż���
		setZoom(mZoom);
		//������Ļ�������
		startOrientationChangeListener();
	}

	/**  
	 *   ������Ļ����ı�������� ��������Ļ�������л�ʱ�ı䱣���ͼƬ�ķ���  
	 */
	private  void startOrientationChangeListener() {  
		OrientationEventListener mOrEventListener = new OrientationEventListener(getContext()) {  
			@Override  
			public void onOrientationChanged(int rotation) { 

				if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)){
					rotation=0;
				}else if ((rotation > 45) && (rotation <= 135))  {
					rotation=90;
				}
				else if ((rotation > 135) && (rotation <= 225)) {
					rotation=180;
				} 
				else if((rotation > 225) && (rotation <= 315)) { 
					rotation=270;
				}else {
					rotation=0;
				}
				if(rotation==mOrientation)
					return;
				mOrientation=rotation;
				updateCameraOrientation();
			}  
		};  
		mOrEventListener.enable();  
	}  

	/**  
	 *   ���ݵ�ǰ�����޸ı���ͼƬ����ת�Ƕ�
	 */
	private void updateCameraOrientation(){
		if(mCamera!=null){
			Camera.Parameters parameters = mCamera.getParameters();
			//rotation����Ϊ 0��90��180��270��ˮƽ����Ϊ0��
			int rotation=90+mOrientation==360?0:90+mOrientation;
			//ǰ������ͷ��Ҫ�Դ�ֱ�������任��������Ƭ�ǵߵ���
			if(mIsFrontCamera){
				if(rotation==90) rotation=270;
				else if (rotation==270) rotation=90;
			}
			parameters.setRotation(rotation);//���ɵ�ͼƬת90��
			//Ԥ��ͼƬ��ת90��
			mCamera.setDisplayOrientation(90);//Ԥ��ת90��
			mCamera.setParameters(parameters);
		}
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