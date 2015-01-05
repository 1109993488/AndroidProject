package com.linj.album.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.camera.FileOperateUtil;
import com.example.camera.R;
import com.linj.imageloader.DisplayImageOptions;
import com.linj.imageloader.ImageLoader;
import com.linj.imageloader.displayer.RoundedBitmapDisplayer;


import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

/** 
 * @ClassName: AlbumView 
 * @Description:  ���View���̳���GridView����װ��Adapter��ͼƬ���ط���
 * @author LinJ
 * @date 2015-1-5 ����5:09:08 
 *  
 */
public class AlbumView extends GridView{
	/**  ͼƬ������ �Ż����˻���  */ 
	private ImageLoader imageLoader;
	/**  ����ͼƬ���ò��� */ 
	private DisplayImageOptions options;	
	/**  ��ǰ�Ƿ��ڱ༭״̬ trueΪ�༭ */ 
	private boolean mEditable;
	public AlbumView(Context context, AttributeSet attrs) {
		super(context, attrs);
		imageLoader= ImageLoader.getInstance(context);
		//��������ͼƬ���ز���
		DisplayImageOptions.Builder builder= new DisplayImageOptions.Builder();
		builder =builder
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageOnFail(R.drawable.ic_error)
				.cacheInMemory(true)//Ϊ���ڱ����޸�ͼƬ��ʱ����Сͼ�� �����ڴ��л���
				.cacheOnDisk(false)
				.displayer(new RoundedBitmapDisplayer(20));
		options=builder.build();
	}


	/**  
	 *  ����ͼƬ
	 *  @param rootPath ��Ŀ¼�ļ����� 
	 */
	public void loadAlbum(String rootPath){
		//��ȡ��Ŀ¼������ͼ�ļ���
		String thumbFolder=FileOperateUtil.getFolderPath(getContext(), FileOperateUtil.TYPE_THUMBNAIL, rootPath);
		List<File> files=FileOperateUtil.listFiles(thumbFolder, ".jpg");
		if(files!=null&&files.size()>0){
			List<String> paths=new ArrayList<String>();
			for (File file : files) {
				paths.add(file.getAbsolutePath());
			}
			setAdapter(new AlbumViewAdapter(paths));
		}
	}

	/**  
	 *  ȫѡͼƬ
	 *  @param listener ѡ��ͼƬ��ִ�еĻص�����   
	 */
	public void selectAll(AlbumView.OnCheckedChangeListener listener){
		((AlbumViewAdapter)getAdapter()).selectAll(listener);
	}
	/**  
	 * ȡ��ȫѡͼƬ
	 *  @param listener   ѡ��ͼƬ��ִ�еĻص�����  
	 */
	public void unSelectAll(AlbumView.OnCheckedChangeListener listener){
		((AlbumViewAdapter)getAdapter()).unSelectAll(listener);
	}

	/**  
	 * ���ÿɱ༭״̬
	 *  @param editable �Ƿ�ɱ༭   
	 */
	public void setEditable(boolean editable){
		mEditable=editable;
		((AlbumViewAdapter)getAdapter()).notifyDataSetChanged(null);
	}
	/**  
	 * ���ÿɱ༭״̬
	 *  @param editable �Ƿ�ɱ༭   
	 *  @param listener ѡ��ͼƬ��ִ�еĻص�����  
	 */
	public void setEditable(boolean editable,AlbumView.OnCheckedChangeListener listener){
		mEditable=editable;
		((AlbumViewAdapter)getAdapter()).notifyDataSetChanged(listener);
	}

	/**  
	 *  ��ȡ�ɱ༭״̬
	 *  @return   
	 */
	public boolean getEditable(){
		return mEditable;
	}

	/**  
	 *  ��ȡ��ǰѡ���ͼƬ·������
	 *  @return   
	 */
	public Set<String> getSelectedItems(){
		return ((AlbumViewAdapter)getAdapter()).getSelectedItems();
	}

	/** 
	 * @ClassName: OnCheckedChangeListener 
	 * @Description:  ͼƬѡ�к�ļ����ӿڣ�������activity�����ص�����
	 * @author LinJ
	 * @date 2015-1-5 ����5:13:43 
	 *  
	 */
	public interface OnCheckedChangeListener{
		public void onCheckedChanged(Set<String> set);
	}
	/** 
	 * @ClassName: AlbumViewAdapter 
	 * @Description:  ���GridView������
	 * @author LinJ
	 * @date 2015-1-5 ����5:14:14 
	 *  
	 */
	public class AlbumViewAdapter extends BaseAdapter
	{

		/** ���ص��ļ�·������ */ 
		List<String> mPaths;

		/**  ��ǰѡ�е��ļ��ļ��� */ 
		Set<String> itemSelectedSet=new HashSet<String>();

		/**  ѡ��ͼƬ��ִ�еĻص����� */ 
		AlbumView.OnCheckedChangeListener listener=null;


		public AlbumViewAdapter(List<String> paths) {
			super();
			this.mPaths = paths;
		}

		private class ViewHolder {
			ImageView imgThumbnail;//����ͼ
			CheckBox checkBox;//��ѡ��
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mPaths.size();
		}


		@Override
		public String getItem(int position) {
			// TODO Auto-generated method stub
			return mPaths.get(position);
		}


		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			AlbumItemView view = (AlbumItemView)convertView;
			if (view == null) {
				view = new AlbumItemView(getContext());
				holder = new ViewHolder();
				holder.imgThumbnail = (ImageView) view.findViewById(R.id.imgThumbnail);
				holder.checkBox=(CheckBox)view.findViewById(R.id.checkbox);
				holder.checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
				view.setTag(holder);
				view.setOnTouchListener(onTouchListener);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			Object tag=holder.imgThumbnail.getTag();
			String path=getItem(position);
			//�ļ���ͬʱ�����滻
			if(tag==null||!tag.equals(path)){
				imageLoader.loadImage(path, holder.imgThumbnail, options, false,getContext());
				holder.imgThumbnail.setTag(path);
				holder.checkBox.setTag(path);
			}
			if (mEditable){ 
				holder.checkBox.setVisibility(View.VISIBLE);
				//����Checkboxѡ��״̬
				holder.checkBox.setChecked(itemSelectedSet.contains(path));
			}
			else 
				holder.checkBox.setVisibility(View.GONE);
			return view;
		}




		/**  
		 * ���������ݸı�ʱ�����»���
		 *  @param listener   
		 */
		public void notifyDataSetChanged(AlbumView.OnCheckedChangeListener listener) {
			//����map
			itemSelectedSet=new HashSet<String>();
			this.listener=listener;
			super.notifyDataSetChanged();
		}
		/**  
		 * ѡ�������ļ�
		 *  @param listener   
		 */
		public void selectAll(AlbumView.OnCheckedChangeListener listener){
			for (String path : mPaths) {
				itemSelectedSet.add(path);
			}
			this.listener=listener;
			super.notifyDataSetChanged();
			if(listener!=null) listener.onCheckedChanged(itemSelectedSet);
		}

		/**  
		 *  ȡ��ѡ�������ļ�
		 *  @param listener   
		 */
		public void unSelectAll(AlbumView.OnCheckedChangeListener listener){
			notifyDataSetChanged(listener);
			if(listener!=null) listener.onCheckedChanged(itemSelectedSet);
		}
		/**  
		 * ��ȡ��ǰѡ���ļ��ļ���
		 *  @return   
		 */
		public Set<String> getSelectedItems(){
			return itemSelectedSet;
		}

		//Checkbox״̬�ı��������
		CompoundButton.OnCheckedChangeListener onCheckedChangeListener=new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(buttonView.getTag()==null) return;
				if (isChecked) itemSelectedSet.add(buttonView.getTag().toString());
				else itemSelectedSet.remove(buttonView.getTag().toString());
				if(listener!=null) listener.onCheckedChanged(itemSelectedSet);
			}
		};

		View.OnTouchListener onTouchListener=new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		};
		
		View.OnClickListener onClickListener=new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		};
	}
}
