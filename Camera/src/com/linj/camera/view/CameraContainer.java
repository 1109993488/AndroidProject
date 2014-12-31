package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.camera.view.CameraView.FlashMode;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;


/** 
 * @ClassName: CameraContainer 
 * @Description:  ������������ ��������󶨵�surfaceview�����պ����ʱͼƬView�;۽�View 
 * @author LinJ
 * @date 2014-12-31 ����9:38:52 
 *  
 */
public class CameraContainer extends FrameLayout implements PictureCallback{

	private final static String TAG="CameraContainer";

	/** ����󶨵�SurfaceView  */ 
	private CameraView mCameraView;
	
	/** �������ɵ�ͼƬ������һ�����Ƶ����½ǵĶ���Ч�������� */ 
	private TempImageView mTempImageView;
	
	/** ������Ļʱ��ʾ�ľ۽�ͼ��  */ 
	private FocusImageView mFocusImageView;
	
	/** �����Ƭ�ĸ�Ŀ¼ */ 
	private String mSavePath;
	
	/** ��Ƭ�ֽ���������  */ 
	private DataHandler mhandler;
	
	/** ���ռ����ӿڣ����������տ�ʼ�ͽ�����ִ����Ӧ����  */ 
	private TakePictureListener mListener;

	private SeekBar mSeekBar;
	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);

		mCameraView=new CameraView(context);
		FrameLayout.LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mCameraView.setLayoutParams(layoutParams);
		addView(mCameraView);

		mTempImageView=new TempImageView(context);
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mTempImageView.setLayoutParams(layoutParams);
		addView(mTempImageView);

		mFocusImageView=new FocusImageView(context);
		layoutParams=new LayoutParams(150,150);
		mFocusImageView.setLayoutParams(layoutParams);
		mFocusImageView.setFocusImg(R.drawable.focus);
		mFocusImageView.setFocusSucceedImg(R.drawable.focus_succeed);
		addView(mFocusImageView);
		
		mSeekBar=new SeekBar(context);
		mSeekBar.setMax(10);
	}



	/**  
	 *  ��ȡ��ǰ���������
	 *  @return   
	 */
	public FlashMode getFlashMode() {
		return mCameraView.getFlashMode();
	}

	/**  
	 *  �������������
	 *  @param flashMode   
	 */
	public void setFlashMode(FlashMode flashMode) {
		mCameraView.setFlashMode(flashMode);
	}

	/**
	 * �����ļ�����·��
	 * @param rootPath
	 */
	public void setRootPath(String rootPath){
		this.mSavePath=rootPath;

	}

	/**
	 * ���շ���
	 * @param callback
	 */
	public void takePicture(){
		mCameraView.takePicture(this,mListener);
	}

	/**  
	 * @Description: ���շ���
	 * @param @param listener ���ռ����ӿ�
	 * @return void    
	 * @throws 
	 */
	public void takePicture(TakePictureListener listener){
		this.mListener=listener;
		mCameraView.takePicture(this,mListener);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		if(mSavePath==null) throw new RuntimeException("mSavePath is null");
		if(mhandler==null) mhandler=new DataHandler();	
		mhandler.setMaxSize(200);
		Bitmap img=mhandler.save(data);

		//���´�Ԥ��ͼ��������һ�ε�����׼��
		mTempImageView.setListener(mListener);
		mTempImageView.setImageBitmap(img);
		mTempImageView.startAnimation(R.anim.tempview_show);
		camera.startPreview();
		if(mListener!=null) mListener.onTakePictureEnd();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//����UP�¼����ڴ���ʾ�۽�ͼƬ
		if (event.getAction()==KeyEvent.ACTION_UP) {
			//���þ۽�
			mCameraView.onFocus((int)event.getX(), (int)event.getY());

			mFocusImageView.show(event.getX(),event.getY());
		}
		return true;
	}


	/**
	 * ���շ��ص�byte���ݴ�����
	 * @author linj
	 *
	 */
	private final class DataHandler{
		/** ��ͼ���·��  */
		private String mThumbnailFolder;
		/** Сͼ���·�� */
		private String mImageFolder;
		/** ѹ�����ͼƬ���ֵ ��λKB*/
		private int maxSize=200;

		public DataHandler(){
			mImageFolder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, mSavePath);
			mThumbnailFolder=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, mSavePath);
			File folder=new File(mImageFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
			folder=new File(mThumbnailFolder);
			if(!folder.exists()){
				folder.mkdirs();
			}
		}

		/**
		 * ����ͼƬ
		 * @param ������ص��ļ���
		 * @return ���������ɵĴ�ͼ
		 */
		public Bitmap save(byte[] data){
			if(data!=null){
				//��������������ص�ͼƬ
				Bitmap bm=BitmapFactory.decodeByteArray(data, 0, data.length);
				//��������ͼ
				Bitmap thumbnail=ThumbnailUtils.extractThumbnail(bm, 213, 213);
				//�����µ��ļ���
				String imgName=FileOperateUtil.createFileNmae(".jpg");
				String imagePath=mImageFolder+File.separator+imgName;
				String thumbPath=mThumbnailFolder+File.separator+imgName;

				File file=new File(imagePath);  
				File thumFile=new File(thumbPath);
				try{
					//��ͼƬ��ͼ
					FileOutputStream fos=new FileOutputStream(file);
					ByteArrayOutputStream bos=compress(bm);
					fos.write(bos.toByteArray());
					fos.flush();
					fos.close();
					//��ͼƬСͼ
					BufferedOutputStream bufferos=new BufferedOutputStream(new FileOutputStream(thumFile));
					thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bufferos);
					bufferos.flush();
					bufferos.close();
					return bm; 
				}catch(Exception e){
					Log.e(TAG, e.toString());
					Toast.makeText(getContext(), "�������������ʧ��", Toast.LENGTH_SHORT).show();

				}
			}else{
				Toast.makeText(getContext(), "����ʧ�ܣ�������", Toast.LENGTH_SHORT).show();
			}
			return null;
		}

		/**
		 * ͼƬѹ������
		 * @param bitmap ͼƬ�ļ�
		 * @param max �ļ���С���ֵ
		 * @return ѹ������ֽ���
		 * @throws Exception
		 */
		public ByteArrayOutputStream compress(Bitmap bitmap){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// ����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
			int options = 99;
			while ( baos.toByteArray().length / 1024 > maxSize) { // ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ��
				options -= 3;// ÿ�ζ�����10
				//ѹ����С��0������ѹ��
				if (options<0) {
					break;
				}
				Log.i(TAG,baos.toByteArray().length / 1024+"");
				baos.reset();// ����baos�����baos
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// ����ѹ��options%����ѹ��������ݴ�ŵ�baos��
			}
			return baos;
		}

		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}
	}

	/** 
	 * @ClassName: TakePictureListener 
	 * @Description:  ���ռ����ӿڣ����������տ�ʼ�ͽ�����ִ����Ӧ����
	 * @author LinJ
	 * @date 2014-12-31 ����9:50:33 
	 *  
	 */
	public static interface TakePictureListener{

		/**  ���ս���ִ�еĶ������÷�������onPictureTaken����ִ�к󴥷� */
		public void onTakePictureEnd();

		/**  ��ʱͼƬ���������󴥷�*/
		public void onAnimtionEnd();
	}



}