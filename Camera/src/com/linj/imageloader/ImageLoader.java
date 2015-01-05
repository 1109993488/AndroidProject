package com.linj.imageloader;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import com.linj.imageloader.ImageSizeUtil.ImageSize;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;


public class ImageLoader {
	/**
	 * ��������
	 */
	private static ImageLoader mInstance;
	/**
	 * ��Ϣ����
	 */
	private LinkedBlockingDeque<Runnable> mTaskQueue;

	/**
	 * ͼƬ����ĺ��Ķ���
	 */
	private LruCache<String, Bitmap> mLruCache;
	/**
	 * �̳߳�
	 */
	private ExecutorService mThreadPool;
	private static final int DEAFULT_THREAD_COUNT = 1;
	/**
	 * ���еĵ��ȷ�ʽ
	 */
	private Type mType = Type.LIFO;

	/**
	 * ��̨��ѯ�߳�
	 */
	private Thread mPoolThread;
	/**
	 * UI�߳��е�Handler
	 */
	private Handler mUIHandler;

	private Semaphore mSemaphoreThreadPool;

	private static final String TAG = "ImageLoader";

	public enum Type
	{
		FIFO, LIFO;
	}
	public static ImageLoader getInstance(Context context)
	{
		if (mInstance == null)
		{
			synchronized (ImageLoader.class)
			{
				if (mInstance == null)
				{
					mInstance = new ImageLoader(DEAFULT_THREAD_COUNT, Type.LIFO,context);
				}
			}
		}
		return mInstance;
	}
	private ImageLoader(int threadCount, Type type,Context context)
	{
		init(threadCount, type,context);
	}
	public static ImageLoader getInstance(int threadCount, Type type,Context context)
	{
		if (mInstance == null)
		{
			synchronized (ImageLoader.class)
			{
				if (mInstance == null)
				{

					mInstance = new ImageLoader(threadCount, type,context);
				}
			}
		}
		return mInstance;
	}

	/**
	 * ��ʼ��
	 * 
	 * @param threadCount
	 * @param type
	 */
	private void init(int threadCount, Type type,Context context)
	{
		// ��ȡ����Ӧ�õ��������ڴ�
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheMemory = maxMemory / 8;
		mLruCache = new LruCache<String, Bitmap>(cacheMemory){
			@Override
			protected int sizeOf(String key, Bitmap value)
			{
				//				return value.getAllocationByteCount();
				return value.getRowBytes() * value.getHeight(); //�ɰ汾����
			}

		};

		// �����̳߳�
		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mType = type;
		mSemaphoreThreadPool = new Semaphore(threadCount,true);
		mTaskQueue = new LinkedBlockingDeque<Runnable>();	
		initBackThread();
	}
	/**
	 * ��ʼ����̨��ѯ�߳�
	 */
	private void initBackThread()
	{
		// ��̨��ѯ�߳�
		mPoolThread = new Thread()
		{
			@Override
			public void run()
			{
				while(true){
					try {
						// ��ȡһ���źţ���δ�����źţ������߳�
						mSemaphoreThreadPool.acquire();
						// �̳߳�ȥȡ��һ���������ִ�У����������Ϊ��ʱ�������߳�
						Runnable runnable=getTask();
						//ʹ���̳߳�ִ������
						mThreadPool.execute(runnable);	
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}


			};
		};

		mPoolThread.start();
	}

	/**
	 * ����pathΪimageview����ͼƬ
	 * 
	 * @param path
	 * @param imageView
	 * @throws InterruptedException 
	 */
	public void loadImage(final String path, final ImageView imageView,final DisplayImageOptions options,
			final boolean isFromNet,final Context context) 
	{
		options.displayer.display(BitmapFactory.decodeResource(context.getResources(),options.imageResOnLoading), imageView);
		if (mUIHandler == null)
		{
			mUIHandler = new Handler(context.getMainLooper())
			{
				public void handleMessage(Message msg)
				{
					// ��ȡ�õ�ͼƬ��Ϊimageview�ص�����ͼƬ
					ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
					Bitmap bm = holder.bitmap;
					ImageView view = holder.imageView;
					if(bm!=null)
						options.displayer.display(bm, view);
					else {
						options.displayer.display(BitmapFactory.decodeResource(context.getResources(),options.imageResOnFail), view);
					}
				};
			};
		}

		// ����path�ڻ����л�ȡbitmap
		Bitmap bm = getBitmapFromLruCache(path);

		if (bm != null)
		{
			refreashBitmap(path, imageView, bm);
		} else{
			addTask(buildTask(path, imageView,options, isFromNet));
		}

	}

	/**
	 * ���ݴ���Ĳ������½�һ������
	 * 
	 * @param path
	 * @param imageView
	 * @param isFromNet
	 * @return
	 */
	private Runnable buildTask(final String path, final ImageView imageView,final DisplayImageOptions options,
			final boolean isFromNet)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				Bitmap bm = null;
				if (isFromNet)
				{
					//��ȥ�����ļ��в���
					File file = getDiskCacheDir(imageView.getContext(),
							md5(path));
					// ����ڻ����ļ��з���
					if (file.exists()){
						bm = loadImageFromLocal(file.getAbsolutePath(),
								imageView);
					} else{
						// ����Ƿ���Ӳ�̻���
						if (options.cacheOnDisk){
							boolean downloadState = DownloadImgUtils
									.downloadImgByUrl(path, file);
							if (downloadState){
								bm = loadImageFromLocal(file.getAbsolutePath(),
										imageView);
							}
						} else{
							bm = DownloadImgUtils.downloadImgByUrl(path,
									imageView);
						}
					}
				} else{
					bm = loadImageFromLocal(path, imageView);
				}
				// �Ƿ����ڴ��л���
				if (options.cacheInMemory) {
					addBitmapToLruCache(path, bm);
				}
				//������Ϣ��UI�߳�
				refreashBitmap(path, imageView, bm);
				//�ͷ��ź�
				mSemaphoreThreadPool.release();
			}


		};
	}

	private Bitmap loadImageFromLocal(final String path,
			final ImageView imageView)
	{
		Bitmap bm;
		// ����ͼƬ
		// ͼƬ��ѹ��
		// 1�����ͼƬ��Ҫ��ʾ�Ĵ�С
		ImageSize imageSize = ImageSizeUtil.getImageViewSize(imageView);
		// 2��ѹ��ͼƬ
		bm = decodeSampledBitmapFromPath(path, imageSize.width,
				imageSize.height);
		return bm;
	}


	/**
	 * ����ǩ�������࣬���ַ����ֽ�����
	 * 
	 * @param str
	 * @return
	 */
	public String md5(String str)
	{
		byte[] digest = null;
		try
		{
			MessageDigest md = MessageDigest.getInstance("md5");
			digest = md.digest(str.getBytes());
			return bytes2hex02(digest);

		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ��ʽ��
	 * 
	 * @param bytes
	 * @return
	 */
	public String bytes2hex02(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		String tmp = null;
		for (byte b : bytes)
		{
			// ��ÿ���ֽ���0xFF���������㣬Ȼ��ת��Ϊ10���ƣ�Ȼ�������Integer��ת��Ϊ16����
			tmp = Integer.toHexString(0xFF & b);
			if (tmp.length() == 1)// ÿ���ֽ�8Ϊ��תΪ16���Ʊ�־��2��16����λ
			{
				tmp = "0" + tmp;
			}
			sb.append(tmp);
		}

		return sb.toString();

	}

	private void refreashBitmap(final String path, final ImageView imageView,
			Bitmap bm){
		Message message = Message.obtain();
		ImgBeanHolder holder = new ImgBeanHolder();
		holder.bitmap = bm;
		holder.path = path;
		holder.imageView = imageView;
		message.obj = holder;
		mUIHandler.sendMessage(message);
	}

	/**
	 * ��ͼƬ����LruCache
	 * 
	 * @param path
	 * @param bm
	 */
	protected void addBitmapToLruCache(String path, Bitmap bm)
	{
		if (getBitmapFromLruCache(path) == null){
			if (bm != null)
				mLruCache.put(path, bm);
		}
	}

	/**
	 * ����ͼƬ��Ҫ��ʾ�Ŀ�͸߶�ͼƬ����ѹ��
	 * 
	 * @param path
	 * @param width
	 * @param height
	 * @return
	 */
	protected Bitmap decodeSampledBitmapFromPath(String path, int width,
			int height)
	{
		// ���ͼƬ�Ŀ�͸ߣ�������ͼƬ���ص��ڴ���
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		options.inSampleSize = ImageSizeUtil.caculateInSampleSize(options,
				width, height);

		// ʹ�û�õ���InSampleSize�ٴν���ͼƬ
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}



	/**
	 * ��û���ͼƬ�ĵ�ַ
	 * 
	 * @param context
	 * @param uniqueName
	 * @return
	 */
	public File getDiskCacheDir(Context context, String uniqueName)
	{
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()))
		{
			cachePath = context.getExternalCacheDir().getPath();
		} else
		{
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * ����path�ڻ����л�ȡbitmap
	 * 
	 * @param key
	 * @return
	 */
	private Bitmap getBitmapFromLruCache(String key)
	{
		return mLruCache.get(key);
	}


	/**
	 * ���������ȡ��һ��������������Ϊ��ʱ���������÷���
	 * 
	 * @return
	 * @throws InterruptedException 
	 */
	private Runnable getTask() throws InterruptedException
	{
		if (mType == Type.FIFO)
		{
			return mTaskQueue.takeFirst();
		} else 
		{
			return mTaskQueue.takeLast();
		}
	}
	/**
	 * ��������������
	 * @param runnable
	 * @throws InterruptedException
	 */
	private  void addTask(Runnable runnable)
	{
		try {
			mTaskQueue.put(runnable);
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}

	}
	private class ImgBeanHolder
	{
		Bitmap bitmap;
		ImageView imageView;
		String path;
	}
}
