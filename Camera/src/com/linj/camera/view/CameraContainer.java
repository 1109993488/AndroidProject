package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.camera.view.CameraView.FlashMode;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


/** 
 * @ClassName: CameraContainer 
 * @Description:  ������������ ��������󶨵�surfaceview�����պ����ʱͼƬView�;۽�View 
 * @author LinJ
 * @date 2014-12-31 ����9:38:52 
 *  
 */
public class CameraContainer extends RelativeLayout implements PictureCallback
,OnSeekBarChangeListener,AutoFocusCallback{

	public final static String TAG="CameraContainer";

	/** ����󶨵�SurfaceView  */ 
	private CameraView mCameraView;

	/** �������ɵ�ͼƬ������һ�����Ƶ����½ǵĶ���Ч�������� */ 
	private TempImageView mTempImageView;

	/** ������Ļʱ��ʾ�ľ۽�ͼ��  */ 
	private FocusImageView mFocusImageView;

	/** ��ʾ¼����ʱ��TextView  */ 
	private TextView mRecordingInfoTextView;

	/** ��ʾˮӡͼ��  */ 
	private ImageView mWaterMarkImageView; 

	/** �����Ƭ�ĸ�Ŀ¼ */ 
	private String mSavePath;

	/** ��Ƭ�ֽ���������  */ 
	private DataHandler mDataHandler;

	/** ���ռ����ӿڣ����������տ�ʼ�ͽ�����ִ����Ӧ����  */ 
	private TakePictureListener mListener;

	/** ���ż����϶��� */ 
	private SeekBar mZoomSeekBar;

	/** ����ִ�ж�ʱ�����Handler����*/
	private Handler mHandler;
	private long mRecordStartTime;
	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);

		mHandler=new Handler();

		initView(context);
		setOnTouchListener(new TouchListener());
	}

	/**  
	 *  ��ʼ���ӿؼ�
	 *  @param context   
	 */
	private void initView(Context context) {
		mCameraView=new CameraView(context);
		RelativeLayout.LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mCameraView.setLayoutParams(layoutParams);
		addView(mCameraView);

		mTempImageView=new TempImageView(context);
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mTempImageView.setLayoutParams(layoutParams);
		addView(mTempImageView);

		mFocusImageView=new FocusImageView(context);
		layoutParams=new LayoutParams(150,150);
		mFocusImageView.setLayoutParams(layoutParams);
		mFocusImageView.setFocusImg(R.drawable.focus_focusing);
		mFocusImageView.setFocusSucceedImg(R.drawable.focus_focused);
		addView(mFocusImageView);

		mRecordingInfoTextView=new TextView(context);
		layoutParams=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.topMargin=50;
		layoutParams.rightMargin=50;
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		Drawable drawable=getResources().getDrawable(R.drawable.icon_rec);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),drawable.getMinimumHeight());
		mRecordingInfoTextView.setCompoundDrawables(drawable, null, null, null);
		mRecordingInfoTextView.setCompoundDrawablePadding(15);
		mRecordingInfoTextView.setText("00:10");
		mRecordingInfoTextView.setTextSize(20);
		mRecordingInfoTextView.setTextColor(Color.WHITE);
		mRecordingInfoTextView.setLayoutParams(layoutParams);
		mRecordingInfoTextView.setVisibility(View.GONE);
		addView(mRecordingInfoTextView);


		mWaterMarkImageView=new ImageView(context);
		mWaterMarkImageView.setImageResource(R.drawable.thumb_guide_tips_new);
		layoutParams=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mWaterMarkImageView.setLayoutParams(layoutParams);
		mWaterMarkImageView.setVisibility(View.GONE);
		addView(mWaterMarkImageView);

		//��ȡ��ǰ�����֧�ֵ�������ż���ֵС��0��ʾ��֧�����š���֧������ʱ�������϶�����
		int maxZoom=mCameraView.getMaxZoom();
		if(maxZoom>0){
			mZoomSeekBar=new SeekBar(context);
			mZoomSeekBar.setMax(maxZoom);
			layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			//����λ�������ײ�
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			//���õײ�Margin���˴���100�Ǻ�activity�����е�75���Աȣ�ҪתΪpx���������ĸ߶�
			layoutParams.bottomMargin=dip2px(100);
			layoutParams.rightMargin=100;
			layoutParams.leftMargin=100;
			mZoomSeekBar.setLayoutParams(layoutParams);
			mZoomSeekBar.setOnSeekBarChangeListener(this);
			addView(mZoomSeekBar);
			//����seekbar �ڴ����Ŵ���С����ʱ��ʾ
			mZoomSeekBar.setVisibility(View.GONE);
		}
	}


	public boolean startRecord(){
		mRecordStartTime=SystemClock.uptimeMillis();
		mRecordingInfoTextView.setVisibility(View.VISIBLE);
		mRecordingInfoTextView.setText("00:00");
		if(mCameraView.startRecord()){
			mHandler.postAtTime(recordRunnable, mRecordingInfoTextView, SystemClock.uptimeMillis()+1000);
			return true;
		}else {
			return false;
		}

	}
	Runnable recordRunnable=new Runnable() {	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mCameraView.isRecording()){
				long recordTime=SystemClock.uptimeMillis()-mRecordStartTime;
				SimpleDateFormat s=new SimpleDateFormat("mm:ss",Locale.getDefault());
				mRecordingInfoTextView.setText(s.format(new Date(recordTime)));
				mHandler.postAtTime(this,mRecordingInfoTextView, SystemClock.uptimeMillis());
			}else {
				mRecordingInfoTextView.setVisibility(View.GONE);
			}
		}
	};
	public void stopRecord(){
		mRecordingInfoTextView.setVisibility(View.GONE);
		mCameraView.stopRecord();
	}
	/**  
	*  �ı����ģʽ ������ģʽ��¼��ģʽ���л� ����ģʽ�ĳ�ʼ���ż���ͬ
	*  @param zoom   ���ż���
	*/
	public void switchMode(int zoom){
		mZoomSeekBar.setProgress(zoom);
		mCameraView.setZoom(zoom);
		//�Զ��Խ�
		mCameraView.onFocus(new Point(getWidth()/2, getHeight()/2), this);   
		//����ˮӡ
		mWaterMarkImageView.setVisibility(View.GONE);
	}
	
	public void setWaterMark(){
		if (mWaterMarkImageView.getVisibility()==View.VISIBLE) {
			mWaterMarkImageView.setVisibility(View.GONE);
		}else {
			mWaterMarkImageView.setVisibility(View.VISIBLE);
		}
	}
	
	/**  
	 *   ǰ�á���������ͷת��
	 */
	public void switchCamera(){
		mCameraView.switchCamera();
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
		if(mDataHandler==null) mDataHandler=new DataHandler();	
		mDataHandler.setMaxSize(200);
		Bitmap bm=mDataHandler.save(data);

		//���´�Ԥ��ͼ��������һ�ε�����׼��
		mTempImageView.setListener(mListener);
		mTempImageView.setImageBitmap(bm);
		mTempImageView.startAnimation(R.anim.tempview_show);
		camera.startPreview();
		if(mListener!=null) mListener.onTakePictureEnd(bm);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		mCameraView.setZoom(progress);
		mHandler.removeCallbacksAndMessages(mZoomSeekBar);
		//ZOOMģʽ�� �ڽ������������seekbar ����tokenΪmZoomSeekBar�������������ʱ�Ƴ�ǰһ����ʱ����
		mHandler.postAtTime(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mZoomSeekBar.setVisibility(View.GONE);
			}
		}, mZoomSeekBar,SystemClock.uptimeMillis()+2000);
	}



	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}



	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		//�۽�֮����ݽ���޸�ͼƬ
		if (success) {
			mFocusImageView.setImageResource(R.drawable.focus_focused);
		}else {
			//�۽�ʧ����ʾ��ͼƬ������δ�ҵ����ʵ���Դ����������ʾͬһ��ͼƬ
			mFocusImageView.setImageResource(R.drawable.focus_focus_failed);

		}
		//1�������View ����tokenΪmFocusImageView��ֹ����ɾ��
		mHandler.postAtTime(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mFocusImageView.setVisibility(View.GONE);
			}
		}, mFocusImageView,SystemClock.uptimeMillis()+1000);
	}

	private final class TouchListener implements OnTouchListener {

		/** ��¼��������Ƭģʽ���ǷŴ���С��Ƭģʽ */

		private static final int MODE_INIT = 0;
		/** �Ŵ���С��Ƭģʽ */
		private static final int MODE_ZOOM = 1;
		private int mode = MODE_INIT;// ��ʼ״̬ 

		/** ���ڼ�¼����ͼƬ�ƶ�������λ�� */

		private float startDis;


		@Override
		public boolean onTouch(View v, MotionEvent event) {
			/** ͨ�������㱣������λ MotionEvent.ACTION_MASK = 255 */
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// ��ָѹ����Ļ
			case MotionEvent.ACTION_DOWN:
				mode = MODE_INIT;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				//���mZoomSeekBarΪnull ��ʾ���豸��֧������ ֱ����������mode Moveָ��Ҳ�޷�ִ��
				if(mZoomSeekBar==null) return true;
				//�Ƴ�token����ΪmZoomSeekBar����ʱ����
				mHandler.removeCallbacksAndMessages(mZoomSeekBar);
				mZoomSeekBar.setVisibility(View.VISIBLE);

				mode = MODE_ZOOM;
				/** ����������ָ��ľ��� */
				startDis = distance(event);
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == MODE_ZOOM) {
					//ֻ��ͬʱ�����������ʱ���ִ��
					if(event.getPointerCount()<2) return true;
					float endDis = distance(event);// ��������
					//ÿ�仯10f zoom��1
					int scale=(int) ((endDis-startDis)/10f);
					if(scale>=1||scale<=-1){
						int zoom=mCameraView.getZoom()+scale;
						//zoom���ܳ�����Χ
						if(zoom>mCameraView.getMaxZoom()) zoom=mCameraView.getMaxZoom();
						if(zoom<0) zoom=0;
						mCameraView.setZoom(zoom);
						mZoomSeekBar.setProgress(zoom);
						//�����һ�εľ�����Ϊ��ǰ����
						startDis=endDis;
					}
				}
				break;
				// ��ָ�뿪��Ļ
			case MotionEvent.ACTION_UP:
				if(mode!=MODE_ZOOM){
					//���þ۽�
					Point point=new Point((int)event.getX(), (int)event.getY());
					mCameraView.onFocus(point,CameraContainer.this);
					mFocusImageView.show(point);
				}else {
					//ZOOMģʽ�� �ڽ������������seekbar ����tokenΪmZoomSeekBar�������������ʱ�Ƴ�ǰһ����ʱ����
					mHandler.postAtTime(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mZoomSeekBar.setVisibility(View.GONE);
						}
					}, mZoomSeekBar,SystemClock.uptimeMillis()+2000);
				}
				break;
			}
			return true;
		}
		/** ����������ָ��ľ��� */
		private float distance(MotionEvent event) {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			/** ʹ�ù��ɶ���������֮��ľ��� */
			return (float) Math.sqrt(dx * dx + dy * dy);
		}

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
		 * @return ���������ɵ�����ͼ
		 */
		public Bitmap save(byte[] data){
			if(data!=null){
				//��������������ص�ͼƬ
				Bitmap bm=BitmapFactory.decodeByteArray(data, 0, data.length);
				//��ȡ��ˮӡ��ͼƬ
				bm=getBitmapWithWaterMark(bm);
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

		private Bitmap getBitmapWithWaterMark(Bitmap bm) {
			// TODO Auto-generated method stub
			if(!(mWaterMarkImageView.getVisibility()==View.VISIBLE)){
				return bm;
			}
			Drawable mark=mWaterMarkImageView.getDrawable();
			Bitmap wBitmap=drawableToBitmap(mark);
			int w = bm.getWidth();

			int h = bm.getHeight();

			int ww = wBitmap.getWidth();

			int wh = wBitmap.getHeight();
			Bitmap newb = Bitmap.createBitmap( w, h, Config.ARGB_8888 );
			Canvas canvas=new Canvas(newb);
			  //draw src into

			canvas.drawBitmap( bm, 0, 0, null );//�� 0��0���꿪ʼ����src
			canvas.drawBitmap( wBitmap, w - ww + 5, h - wh + 5, null );//��src�����½ǻ���ˮӡ
			 //save all clip

			canvas.save( Canvas.ALL_SAVE_FLAG );//����

		    //store

			canvas.restore();//�洢
			bm.recycle();
			bm=null;
			wBitmap.recycle();
			wBitmap=null;
		    return newb;

		}
		public  Bitmap drawableToBitmap(Drawable drawable) {       
			Bitmap bitmap = Bitmap.createBitmap(
					drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight(),
					drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
							: Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			return bitmap;
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
		/**  
		 *���ս���ִ�еĶ������÷�������onPictureTaken����ִ�к󴥷�
		 *  @param bm �������ɵ�ͼƬ 
		 */
		public void onTakePictureEnd(Bitmap bm);

		/**  ��ʱͼƬ���������󴥷�
		 * @param bm �������ɵ�ͼƬ 
		 * */
		public void onAnimtionEnd(Bitmap bm);
	}

	/**  
	 * dipתpx
	 *  @param dipValue
	 *  @return   
	 */
	private  int dip2px(float dipValue){ 
		final float scale = getResources().getDisplayMetrics().density; 
		return (int)(dipValue * scale + 0.5f); 
	} 
}