package com.linj.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;


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
import android.widget.FrameLayout;
import android.widget.Toast;


public class CameraContainer extends FrameLayout implements PictureCallback{
	private final static String TAG="CameraContainer";
	private CameraView cameraView;
	private TempImageView tempImageView;
	private String rootPath;
	private String THUMBNAIL_FOLDER;
	private String IMAGE_FOLDER;
	private DataHandler handler;
	public CameraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		handler=new DataHandler();
		cameraView=new CameraView(context);
		FrameLayout.LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		cameraView.setLayoutParams(layoutParams);
		addView(cameraView);

		tempImageView=new TempImageView(context);
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		tempImageView.setLayoutParams(layoutParams);

		addView(tempImageView);
	}


	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		if(rootPath==null)
			throw new RuntimeException("rootPathδ����");
		Bitmap img=handler.save(data);

		//���´�Ԥ��ͼ��������һ�ε�����׼��
		tempImageView.setImageBitmap(img);
		tempImageView.startAnimation(R.anim.tempview_show);
		camera.startPreview();
	}

	/**
	 * �����ļ�����·��
	 * @param rootPath
	 */
	public void setRootPath(String rootPath){
		this.rootPath=rootPath;
		IMAGE_FOLDER=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, rootPath);
		THUMBNAIL_FOLDER=FileOperateUtil.getFolderPath(getContext(),  FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		File folder=new File(IMAGE_FOLDER);
		if(!folder.exists()){
			folder.mkdirs();
		}
		folder=new File(THUMBNAIL_FOLDER);
		if(!folder.exists()){
			folder.mkdirs();
		}
	}

	/**
	 * ���պ���
	 * @param callback
	 */
	public void takePicture(){
		cameraView.takePicture(this);
	}

	/**
	 * ���շ��ص�byte���ݴ�����
	 * @author linj
	 *
	 */
	private final class DataHandler{
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
				String imagePath=THUMBNAIL_FOLDER+File.separator+imgName;
				String thumbPath=IMAGE_FOLDER+File.separator+imgName;

				File file=new File(imagePath);  
				File thumFile=new File(thumbPath);
				try{
					//��ͼƬ��ͼ
					FileOutputStream fos=new FileOutputStream(file);
					ByteArrayOutputStream bos=compress(bm, 200);
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
		public ByteArrayOutputStream compress(Bitmap bitmap,int max){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);// ����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
			int options = 100;
			while ( baos.toByteArray().length / 1024 > max) { // ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ��
				options -= 10;// ÿ�ζ�����10
				//ѹ����С��0������ѹ��
				if (options<0) {
					break;
				}
				baos.reset();// ����baos�����baos
				bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);// ����ѹ��options%����ѹ��������ݴ�ŵ�baos��
			}
			return baos;
		}
	}

}