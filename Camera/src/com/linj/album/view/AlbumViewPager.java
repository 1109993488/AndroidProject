package com.linj.album.view;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.camera.AlbumItemAty;
import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.album.view.MatrixImageView.OnMovingListener;
import com.linj.album.view.MatrixImageView.OnSingleTapListener;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.MatrixBitmapDisplayer;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
		String thumbnailFolder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		//��ȡͼƬ�ļ���ͼ
		List<File> imageList=FileOperateUtil.listFiles(folder, ".jpg");
		//��ȡ��Ƶ�ļ�����ͼ
		List<File> videoList=FileOperateUtil.listFiles(thumbnailFolder, ".jpg","video");
		List<File> files=new ArrayList<File>();
		//����Ƶ�ļ�����ͼ����ͼƬ��ͼ�б���
		if(videoList!=null&&videoList.size()>0){
			files.addAll(videoList);
		}
		if(imageList!=null&&imageList.size()>0){
			files.addAll(imageList);
		}
		FileOperateUtil.sortList(files, false);
		if(files.size()>0){
			List<String> paths=new ArrayList<String>();
			int currentItem=0;
			for (File file : files) {
				if(fileName!=null&&file.getName().equals(fileName))
					currentItem=imageList.indexOf(file);
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

	/**  
	 *  ɾ����ǰ��
	 *  @return  ����ǰλ��/��������
	 */
	public String deleteCurrentPath(){
		return ((ViewPagerAdapter)getAdapter()).deleteCurrentItem(getCurrentItem());

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
			//��Ϊֱ�Ӽӵ�viewGroup�»ᵼ�·��ص�viewΪviewGroup
			View imageLayout = inflate(getContext(),R.layout.item_album_pager, null);
			viewGroup.addView(imageLayout);
			assert imageLayout != null;
			MatrixImageView imageView = (MatrixImageView) imageLayout.findViewById(R.id.image);
			imageView.setOnMovingListener(AlbumViewPager.this);
			imageView.setOnSingleTapListener(onSingleTapListener);
			String path=paths.get(position);
			//final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			
			ImageButton videoIcon=(ImageButton)imageLayout.findViewById(R.id.videoicon);
			if(path.contains("video")){
				videoIcon.setVisibility(View.VISIBLE);
			}else {			
				videoIcon.setVisibility(View.GONE);
			}
			videoIcon.setOnClickListener(playVideoListener);
			videoIcon.setTag(path);
			imageLayout.setTag(path);
			mImageLoader.loadImage(path, imageView, mOptions);
			return imageLayout;
		}

		OnClickListener playVideoListener=new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String path=v.getTag().toString();
				path=path.replace(getContext().getResources().getString(R.string.Thumbnail),
						getContext().getResources().getString(R.string.Video));
				path=path.replace(".jpg", ".3gp");
				((AlbumItemAty)getContext()).playVideo(path);
			}
		};

		@Override
		public int getItemPosition(Object object) {
			//��notifyDataSetChangedʱ����None�����»���
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int arg1, Object object) {
			((ViewPager) container).removeView((View) object);  
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;			
		}

		//�Զ����ȡ��ǰview����                              
		public String deleteCurrentItem(int position) {
			String path=paths.get(position);
			if(path!=null) {
				FileOperateUtil.deleteSourceFile(path, getContext());
				paths.remove(path);
				notifyDataSetChanged();
				if(paths.size()>0)
					return (getCurrentItem()+1)+"/"+paths.size();
				else {
					return "0/0";
				}
			}
			return null;
		}
	}


}
