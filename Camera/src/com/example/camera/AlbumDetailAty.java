package com.example.camera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linj.album.view.AlbumViewPager;
import com.linj.album.view.AlbumViewPager.MyPageChangeListener;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.MatrixBitmapDisplayer;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.FloatMath;
import android.util.Log;


import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
/**
 * ͼƬ���� 
 * @author Administrator
 *
 */
public class AlbumDetailAty extends Activity {
	private String mSaveRoot;
	private AlbumViewPager pagerPhoto;//��ʾ��ͼ
	private TextView txtTitle;
	private int oldPosition=0;//��ǰѡ����ļ����
	private boolean isWeb;//�Ƿ�����ͼƬ
	List<String> urls=new ArrayList<String>();//���viewpager����ͼƬ���ӵ����� 
	Bitmap lastEditBitmap;//��ʾ���һ�ű༭��ͼƬ ��viewpager�л�ʱ��Ϊnull
	Button save;//���水ť
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.bigphoto);
		pagerPhoto=(AlbumViewPager)findViewById(R.id.pagerPhoto);
		mSaveRoot="test";
//		pagerPhoto.setOnPageChangeListener2(new PhotoPageChangeListener());//view�ı��¼�

		//��������ͼƬ���ز���
		pagerPhoto.loadAlbum(mSaveRoot);

	}

	/**
	 * ����adapter����
	 * @param urls
	 * @param ROOTFOLDER
	 * @param CHILDFOLDER
	 */
	protected void loadAlbum() {
		//ͼƬ�ļ�Ŀ¼
		final String imageFolder=FileOperateUtil.getFolderPath(this, FileOperateUtil.TYPE_IMAGE, mSaveRoot);
		File folder=new File(imageFolder);
		List<File> files=FileOperateUtil.listFiles(folder, ".jpg");
		
		txtTitle.setText((oldPosition+1)+"/"+urls.size());
		pagerPhoto.setCurrentItem(oldPosition);


	}


//	protected void setBigImages(File picDir, final List<String> urls) {
//		//��ͼ��viewpager��ʽ��ʾ
//		final File[] files=picDir.listFiles(new FileFilter() {
//
//			@Override
//			public boolean accept(File pathname) {
//				// TODO Auto-generated method stub
//				String name=pathname.getName().toLowerCase();
//				//���˵������ļ�������ͼ
//				if (name.indexOf(".jpg")>0) {
//					urls.add(pathname.getPath());
//					return true;
//				}
//				else {
//					return false;
//				}
//			}
//		});
//		if (files!=null) {
//			pagerPhoto.setAdapter(new ViewPagerAdapter(urls,BigPhotoAty.this));// �������ViewPagerҳ���������
//			Intent intent=getIntent();
//			Bundle bundle=intent.getExtras();
//			if(bundle!=null){
//				String tag=bundle.getString("path");
//				for (int i=0;i<files.length;i++) {
//					if (files[i].getPath().equals(tag)) {
//						oldPosition=i;
//						break;
//					}
//				}
//			}
//			txtTitle.setText((oldPosition+1)+"/"+files.length);
//			pagerPhoto.setCurrentItem(oldPosition);
//		}
//		else {
//			Toast.makeText(getApplicationContext(), "û�п�ʶ���ͼƬ", Toast.LENGTH_SHORT).show();
//			finish();
//		}
//	}

	/**
	 * ��ViewPager��ҳ���״̬�����ı�ʱ����
	 * 
	 * @author Administrator
	 * 
	 */
	private class PhotoPageChangeListener implements MyPageChangeListener {
		/**
		 * This method will be invoked when a new page becomes selected.
		 * position: Position index of the new selected page.
		 */


		public void onPageSelected(int position) {
			oldPosition = position;
			txtTitle.setText((oldPosition+1)+"/"+urls.size());
			//�����һ�α༭ͼƬ��Ϊ��
			lastEditBitmap=null;
			//���ر��水ť
			save.setVisibility(View.INVISIBLE);
		}

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageChanged(int oldPosition, int newPosition) {
			//������ͼƬ ִ��ͼƬ��ԭ����
//			if (!isWeb) {
//				View oldView= ((ViewPagerAdapter) pagerPhoto.getAdapter()).getPrimaryItem();;
//				// TODO Auto-generated method stub
//				if (oldView!=null&&oldPosition>=0&&oldPosition<=urls.size()-1) {
//					ImageView img=(ImageView)oldView.findViewById(R.id.image);  
//					Bitmap bitmap= BitmapFactory.decodeFile(urls.get(oldPosition));
//					img.setImageBitmap(bitmap);//ͼƬ��ԭ
//				}
//			}
		}
	}



	
	

	private final class TouchListener implements OnTouchListener {
		ImageView imageView;
		public TouchListener(){

		}
		public TouchListener( ImageView imageView){
			this.imageView=imageView; 
		}
		/** ��¼��������Ƭģʽ���ǷŴ���С��Ƭģʽ */
		private int mode = 0;// ��ʼ״̬ 
		/** ������Ƭģʽ */
		private static final int MODE_DRAG = 1;
		/** �Ŵ���С��Ƭģʽ */
		private static final int MODE_ZOOM = 2;

		/** ���ڼ�¼��ʼʱ�������λ�� */
		private PointF startPoint = new PointF();
		/** ���ڼ�¼����ͼƬ�ƶ�������λ�� */
		private Matrix matrix = new Matrix();
		/** ���ڼ�¼ͼƬҪ��������ʱ�������λ�� */
		private Matrix currentMatrix = new Matrix();

		/** ������ָ�Ŀ�ʼ���� */
		private float startDis;
		/** ������ָ���м�� */
		private PointF midPoint;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			/** ͨ�������㱣������λ MotionEvent.ACTION_MASK = 255 */
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// ��ָѹ����Ļ
			case MotionEvent.ACTION_DOWN:
				mode = MODE_DRAG;
				// ��¼ImageView��ǰ���ƶ�λ��
				currentMatrix.set(imageView.getImageMatrix());
				startPoint.set(event.getX(), event.getY());
				break;
				// ��ָ����Ļ���ƶ������¼��ᱻ���ϴ���
			case MotionEvent.ACTION_MOVE:
				// ����ͼƬ
				if (mode == MODE_DRAG) {
					float dx = event.getX() - startPoint.x; // �õ�x����ƶ�����
					float dy = event.getY() - startPoint.y; // �õ�x����ƶ�����
					// ��û���ƶ�֮ǰ��λ���Ͻ����ƶ�
					matrix.set(currentMatrix);
					matrix.postTranslate(dx, dy);
				}
				// �Ŵ���СͼƬ
				else if (mode == MODE_ZOOM) {
					float endDis = distance(event);// ��������
					if (endDis > 10f) { // ������ָ��£��һ���ʱ�����ش���10
						float scale = endDis / startDis;// �õ����ű���
						matrix.set(currentMatrix);
						matrix.postScale(scale, scale,midPoint.x,midPoint.y);
					}
				}
				break;
				// ��ָ�뿪��Ļ
			case MotionEvent.ACTION_UP:
				// �������뿪��Ļ��������Ļ�ϻ��д���(��ָ)
			case MotionEvent.ACTION_POINTER_UP:
				mode = 0;
				break;
				// ����Ļ���Ѿ��д���(��ָ)������һ������ѹ����Ļ
			case MotionEvent.ACTION_POINTER_DOWN:
				mode = MODE_ZOOM;
				/** ����������ָ��ľ��� */
				startDis = distance(event);
				/** ����������ָ����м�� */
				if (startDis > 10f) { // ������ָ��£��һ���ʱ�����ش���10
					midPoint = mid(event);
					//��¼��ǰImageView�����ű���
					currentMatrix.set(imageView.getImageMatrix());
				}
				break;
			}
			imageView.setImageMatrix(matrix);
			return true;
		}

		/** ����������ָ��ľ��� */
		private float distance(MotionEvent event) {
			float dx = event.getX(1) - event.getX(0);
			float dy = event.getY(1) - event.getY(0);
			/** ʹ�ù��ɶ���������֮��ľ��� */
			return FloatMath.sqrt(dx * dx + dy * dy);
		}

		/** ����������ָ����м�� */
		private PointF mid(MotionEvent event) {
			float midX = (event.getX(1) + event.getX(0)) / 2;
			float midY = (event.getY(1) + event.getY(0)) / 2;
			return new PointF(midX, midY);
		}

	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
	}

	public void btn_click(View view) {
		switch (view.getId()) {
//		case R.id.imgBtnback:
//			finish();
//			break;
//		case R.id.btnTitleRight:
//			if (isWeb) {
//				finish();
//			}
//			else {
//				if (lastEditBitmap!=null) {
//					//������ͼƬ
//					saveNewPhoto(lastEditBitmap,urls.get(pagerPhoto.getCurrentItem()));
//				}
//			}
//			break;
//		case R.id.btnRotateRight:
//			rotateBitmap(view.getId());
//			break;
//		case R.id.btnRotateLeft:
//			rotateBitmap(view.getId());
//			break;
//		case R.id.btnRotateLeftRight:
//			rotateBitmap(view.getId());
//			break;
//		case R.id.btnRotateUpDown:
//			rotateBitmap(view.getId());
//			break;
		default:
			break;
		}
	}
//	/**
//	 * ����ͼƬ
//	 * @param lastEditBitmap2  �޸ĺ��ͼƬ
//	 * @param string ·��
//	 */
//	private void saveNewPhoto(Bitmap lastEditBitmap2, String path) {
//		// TODO Auto-generated method stub
//		File myCaptureFile=new File(path);
//		//��������ͼ
//		Bitmap thumbnail=ThumbnailUtils.extractThumbnail(lastEditBitmap2, 213, 213);
//		//��ȡ����ͼ·��
//		String tPath=myCaptureFile.getParentFile().getPath()+"/"+getString(R.string.ImageThumbnail)+myCaptureFile.getName();//СͼΪ��ͼĿ¼������ͼ�ļ���
//		File thumFile=new File(tPath);
//		try{
//			//��ͼƬ��ͼ
//			BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(myCaptureFile));
//			lastEditBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//			bos.flush();
//			bos.close();
//			//��ͼƬСͼ
//			bos=new BufferedOutputStream(new FileOutputStream(thumFile));
//			thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//			bos.flush();
//			bos.close();
//			Toast.makeText(this, "�޸ĳɹ�", Toast.LENGTH_SHORT).show();
//		}catch(Exception e){
//			Log.i("pic", e.toString());
//		}
//	}

//	class onDoubleClick implements View.OnTouchListener{ 
//		ImageView imageView;
//		public onDoubleClick(ImageView imageView) {
//			this.imageView=imageView;
//		}
//		int count = 0;   
//		int firClick = 0;   
//		int secClick = 0;   
//		@Override    
//		public boolean onTouch(View v, MotionEvent event) {     
//			if(MotionEvent.ACTION_DOWN == event.getAction()){  
//				Log.i("", count+"");
//				count++;     
//				if(count == 1){     
//					firClick = (int) System.currentTimeMillis();           
//				} else if (count == 2){     
//					secClick = (int) System.currentTimeMillis();     
//					if(secClick - firClick < 1000){     
//						Toast.makeText(getApplicationContext(), "˫��", 1).show();
//					}     
//					count = 0;     
//					firClick = 0;     
//					secClick = 0;  
//					imageView.setOnTouchListener(new TouchListener(imageView));
//					imageView.setScaleType(ScaleType.MATRIX);
//					Matrix matrix=new Matrix();
//
//					matrix.postScale(2, 2);
//					imageView.setImageMatrix(matrix);
//					pagerPhoto.setLock(true);
//				} 
//
//			}     
//			return false;
//		}     
//	}   
//
//
//	private void rotateBitmap(int id) {
//
//		View imageLayout= ((ViewPagerAdapter) pagerPhoto.getAdapter()).getPrimaryItem();;
//		ImageView img=(ImageView)imageLayout.findViewById(R.id.image);  
//		//��ǰ�༭ͼƬΪ��ʱ ����ͼƬ ��������༭��ǰͼƬ
//		if (lastEditBitmap==null) {
//			lastEditBitmap= BitmapFactory.decodeFile(urls.get(pagerPhoto.getCurrentItem()));
//		}
//		//		 Getting width & height of the given image.  
//		int w = lastEditBitmap.getWidth();  
//		int h = lastEditBitmap.getHeight();  
//		// Setting post rotate to 90  
//		Matrix mtx = new Matrix();  
//		switch (id) {
//		case R.id.btnRotateRight:
//			mtx.postRotate(90);  
//			break;
//		case R.id.btnRotateLeft:
//			mtx.postRotate(-90);  
//			break;
//		case R.id.btnRotateLeftRight:
//			mtx.postScale(-1,1);  
//			break;
//		case R.id.btnRotateUpDown:
//			mtx.postScale(1, -1);//���·�ת
//			break;
//		}
//		// Rotating Bitmap  
//		Bitmap rotatedBMP = Bitmap.createBitmap(lastEditBitmap, 0, 0, w, h, mtx, true);  
//		//���༭���ͼƬ����Ϊ��ǰ�༭ͼƬ
//		lastEditBitmap=rotatedBMP;
//		//���浱ǰview
//
//		//��ʾ���水ť
//		save.setVisibility(View.VISIBLE);
//		img.setImageBitmap(rotatedBMP);   
//	}

}
