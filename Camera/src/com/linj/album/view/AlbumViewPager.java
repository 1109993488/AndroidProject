package com.linj.album.view;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.album.view.MatrixImageView.OnMovingListener;
import com.linj.album.view.MatrixImageView.OnSingleTapListener;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.MatrixBitmapDisplayer;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/** 
* @ClassName: AlbumViewPager 
* @Description:  �Զ���viewpager  �Ż����¼�����
* @author LinJ
* @date 2015-1-9 ����5:33:33 
*  
*/
public class AlbumViewPager extends ViewPager implements OnMovingListener {
	public final static String TAG="AlbumViewPager";

	/**  ͼƬ������ �Ż����˻���  */ 
	private ImageLoader mImageLoader;
	/**  ����ͼƬ���ò��� */ 
	private DisplayImageOptions mOptions;	

	/**  ��ǰ�ӿؼ��Ƿ����϶�״̬  */ 
	private boolean mChildIsBeingDragged=false;

	private OnSingleTapListener onSingleTapListener;
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
				.displayer(new MatrixBitmapDisplayer());
		mOptions=builder.build();
	}


	/**  
	 *  ����ͼƬ
	 *  @param rootPath   ͼƬ��·��
	 */
	public void loadAlbum(String rootPath,String fileName,TextView view){
		//��ȡ��Ŀ¼������ͼ�ļ���
		String folder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_IMAGE, rootPath);
		List<File> files=FileOperateUtil.listFiles(folder, ".jpg");
		if(files!=null&&files.size()>0){
			List<String> paths=new ArrayList<String>();
			int currentItem=0;
			for (File file : files) {
				if(fileName!=null&&file.getName().equals(fileName))
					currentItem=files.indexOf(file);
				paths.add(file.getAbsolutePath());
			}
			setAdapter(new ViewPagerAdapter(paths));
			setCurrentItem(currentItem);
			view.setText((currentItem+1)+"/"+paths.size());
		}
		else {
			view.setText("0/0");
		}
	}

	
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if(mChildIsBeingDragged)
			return false;
		return super.onInterceptTouchEvent(arg0);
	}
	
	@Override
	public void startDrag() {
		// TODO Auto-generated method stub
		mChildIsBeingDragged=true;
	}


	@Override
	public void stopDrag() {
		// TODO Auto-generated method stub
		mChildIsBeingDragged=false;
	}

	public void setOnSingleTapListener(OnSingleTapListener onSingleTapListener) {
		this.onSingleTapListener = onSingleTapListener;
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
			MatrixImageView imageView = (MatrixImageView) imageLayout.findViewById(R.id.image);
			imageView.setOnMovingListener(AlbumViewPager.this);
			imageView.setOnSingleTapListener(onSingleTapListener);
			String path=paths.get(position);
			//			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			mImageLoader.loadImage(path, imageView, mOptions);
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
	
	
}
