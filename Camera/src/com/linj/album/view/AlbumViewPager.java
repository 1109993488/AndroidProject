package com.linj.album.view;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.FadeInBitmapDisplayer;
import com.linj.imageloader.displayer.RoundedBitmapDisplayer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/***
 * �Զ���viewpager  ��page�ı�ʱ���ݾɵ�position���µ�position
 * @author Administrator
 *
 */
public class AlbumViewPager extends ViewPager  {
	int cPosition=0;//��ʾ��ǰ��position
	boolean lock=false;//�Ƿ�����viewpager ��lock=trueʱ �޷�����viewpager

	/**  ͼƬ������ �Ż����˻���  */ 
	private ImageLoader mImageLoader;
	/**  ����ͼƬ���ò��� */ 
	private DisplayImageOptions mOptions;	


	public AlbumViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageLoader= ImageLoader.getInstance(context);
		//��������ͼƬ���ز���
		DisplayImageOptions.Builder builder= new DisplayImageOptions.Builder();
		builder =builder
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageOnFail(R.drawable.ic_error)
				.cacheInMemory(true)
				.cacheOnDisk(false)
				.displayer(new FadeInBitmapDisplayer());
		mOptions=builder.build();
	}




	/**  
	 *  ����ͼƬ
	 *  @param rootPath   ͼƬ��·��
	 */
	public void loadAlbum(String rootPath){
		//��ȡ��Ŀ¼������ͼ�ļ���
		String folder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, rootPath);
		List<File> files=FileOperateUtil.listFiles(folder, ".jpg");
		if(files!=null&&files.size()>0){
			List<String> paths=new ArrayList<String>();
			for (File file : files) {
				paths.add(file.getAbsolutePath());
			}
			setAdapter(new ViewPagerAdapter(paths));
		}
	}

	public class ViewPagerAdapter extends PagerAdapter {
		private List<String> paths;//��ͼ��ַ ���Ϊ����ͼƬ ��Ϊ��ͼurl
		private View mCurrentView;
		public ViewPagerAdapter(List<String> paths){
			this.paths=paths;
		}

		@Override
		public int getCount() {
			return paths.size();
		}

		@Override
		public Object instantiateItem(ViewGroup viewGroup, int position) {
			//ע�⣬���ﲻ���Լ�inflate��ʱ��ֱ����ӵ�viewGroup�£�����Ҫ��addView�������
			View imageLayout = inflate(getContext(),R.layout.item_album_pager, null);
			viewGroup.addView(imageLayout);
			assert imageLayout != null;
			ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
			String path=paths.get(position);
			//			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			mImageLoader.loadImage(path, imageView, mOptions, false, getContext());
//			imageView.setImageBitmap(BitmapFactory.decodeFile(path));
			return imageLayout;
		}




		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;			
		}



		//���õ�ǰ��view
		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			mCurrentView = (View)object;
		}
		//�Զ����ȡ��ǰview����                              
		public View getPrimaryItem() {
			return mCurrentView;
		}
	}

	/**
	 * �Զ���page�ı�ʱ�ļ����¼�
	 * @param myPageChangeListener
	 */
	public void setOnPageChangeListener(final MyPageChangeListener myPageChangeListener){

		//ʵ��ԭ�е�OnPageChangeListener
		setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// ʵ��ԭ�е�onPageSelected�¼�
				myPageChangeListener.onPageSelected(position);
				//ʵ���Զ����onPageChanged�¼�
				myPageChangeListener.onPageChanged(cPosition,position);
				//���µ�ǰpositionΪoldPosition
				cPosition=position;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				myPageChangeListener.onPageScrolled(arg0,arg1,arg2);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				myPageChangeListener.onPageScrollStateChanged(arg0);
			}
		});
	}

	public void setLock(boolean islock) {
		this.lock=islock;
	}

	@Override  
	public boolean onTouchEvent(MotionEvent arg0) {  
		// ����ʱ ��Χfalse ����׽touch�¼� ʹviewpager�޷�����
		return super.onTouchEvent(arg0);
	}  

	public interface MyPageChangeListener extends OnPageChangeListener{
		/**
		 * 
		 * @param oldPosition �ƶ�ǰ��position ����ֵΪ-1ʱ ��ʾ��һ������ ��δ�ƶ���
		 * @param newPosition �ƶ����position
		 */
		public void onPageChanged(int oldPosition,int newPosition);   
	}

}
