package com.alan.roundimageview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alan.myimageview.R;

public class ActCamera extends Activity implements OnClickListener {
	public static final int ACTIVITY_RET_FROM_PICK = 1001;
	public static final int ACTIVITY_RET_FROM_CAMERA = 1002;
	public static final int ACTIVITY_RET_FROM_CROP = 1003;
	public static final int PHOTO_OK = 1004;
	public static final String PHOTO_CROP = "iscrop";
	public static final String PHOTO_PATH = "photo_path";
	private boolean mIsCrop=true;
	private Button mTakePhoto;
	private Button mPickPhoto;
	private Button mCancel;
	private Button mLookHead;
//	private ImageButton mBack;
//	private ImageButton mSure;
	private ImageView mImage;
	private LinearLayout mLayoutChoose;
	private LinearLayout mLayoutPreview;
	private View mEmptyView;

	private String mTmpName;
	private String mUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_camera);
		overridePendingTransition(R.anim.push_bottom_in, R.anim.push_top_out);

		mTakePhoto = (Button) findViewById(R.id.id_btn_takePhoto);
		mPickPhoto = (Button) findViewById(R.id.id_btn_pickPhoto);
		mCancel = (Button) findViewById(R.id.id_btn_cancel);
		mLookHead = (Button) findViewById(R.id.id_btn_lookhead);
//		mBack = (ImageButton) findViewById(R.id.id_imgbtn_cameraBack);
//		mSure = (ImageButton) findViewById(R.id.id_imgbtn_cameraSure);
		mImage = (ImageView) findViewById(R.id.id_img_preview);
		mLayoutChoose = (LinearLayout) findViewById(R.id.id_layout_choose);
		mLayoutPreview = (LinearLayout) findViewById(R.id.id_layout_preview);

		mTakePhoto.setOnClickListener(this);
		mPickPhoto.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mLookHead.setOnClickListener(this);
//		mBack.setOnClickListener(this);
//		mSure.setOnClickListener(this);
		mEmptyView = findViewById(R.id.view);
		mEmptyView.setOnClickListener(this);

		Intent intent = getIntent();
		if (intent != null && intent.hasExtra(PHOTO_CROP)) {
			mIsCrop = intent.getBooleanExtra(PHOTO_CROP, true);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mTakePhoto) {
			mLayoutPreview.setVisibility(View.VISIBLE);
			mLayoutChoose.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.GONE);
			mTmpName = FileUtils.getExternalCacheDir(this).getPath() + System.currentTimeMillis()
					+ ".jpg";
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mTmpName)));
			startActivityForResult(intent, ACTIVITY_RET_FROM_CAMERA);
		} else if (v == mPickPhoto) {
			mLayoutPreview.setVisibility(View.VISIBLE);
			mLayoutChoose.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.GONE);
			Intent intent = new Intent(Intent.ACTION_PICK, null);
			intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			startActivityForResult(intent, ACTIVITY_RET_FROM_PICK);
		} else if (v == mCancel) {
			if (!TextUtils.isEmpty(mUrl)) {
				File file = new File(mUrl);
				if (file.exists()) {
					file.delete();
				}
			}
			onBackPressed();
		} 
//		else if (v == mBack) {
//			onBackPressed();
//		} else if (v == mSure) {
//			if (!TextUtils.isEmpty(mUrl)) {
//				Intent intent = new Intent();
//				intent.putExtra(PHOTO_PATH, mUrl);
//				setResult(PHOTO_OK, intent);
//			}
//			onBackPressed();
//		}
		else if (v.getId() == R.id.view) {
			onBackPressed();
		} else if (v == mLookHead) {
//			showActivity(ActShowHead.class, true);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ACTIVITY_RET_FROM_PICK: // 相册
			if (data != null) {
				if (mIsCrop) {
					startPhotoCrop(data.getData());
				} else {
					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor actualimagecursor = managedQuery(data.getData(), proj, null, null, null);
					int actual_image_column_index = actualimagecursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					actualimagecursor.moveToFirst();
					String img_path = actualimagecursor.getString(actual_image_column_index);
					// File file =new File(img_path);
					// if (!file.exists()) {
					// file.mkdirs();
					// }
					
					Bitmap bitmap = BitmapUtil.getimage(img_path);
					mImage.setImageBitmap(bitmap);
//					storeImageData(new File(img_path), false);
				}
			} else {
				onBackPressed();
			}
			break;

		case ACTIVITY_RET_FROM_CAMERA: // 拍照
			if (null != mTmpName) {
				File temp = new File(mTmpName);
				if (temp.isFile() && temp.exists()) {
					if (mIsCrop) {
						startPhotoCrop(Uri.fromFile(temp));
					} else {
						storeImageData(temp, true);
					}
				} else {
					onBackPressed();
				}
			} else {
//				ToastUtil.showMessage("修改失败");
				onBackPressed();
			}
			break;

		case ACTIVITY_RET_FROM_CROP:
			// 选择头像后返回操作
			if (resultCode == RESULT_CANCELED) {
				onBackPressed();
			} else if (resultCode == RESULT_OK) {
				storeImageData(data);
			}
			break;
		}
	}

	private void storeImageData(Intent data) {
		if (data == null) {
			return;
		}
		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			BufferedOutputStream stream;
			try {
				File file = null;
				if (!TextUtils.isEmpty(mTmpName)) {
					file = new File(mTmpName);
					if (file.exists()) {
						file.delete();
					}
				}
				mUrl = FileUtils.getExternalCacheDir(this).getPath() + "/"
						+ System.currentTimeMillis() + ".jpg";
				file = new File(mUrl);
				file.createNewFile();
				stream = new BufferedOutputStream(new FileOutputStream(file));
				photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
				stream.flush();
				stream.close();
				photo.recycle();
				Bitmap bitmap = BitmapUtil.getFitBitmap(mUrl);
				mImage.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
//			ToastUtil.showMessage(R.string.get_data_fail);
			onBackPressed();
		}
	}

	private void storeImageData(File file, boolean isDelete) {

		String path = file.getPath();
		if (!file.exists() || !file.isFile()) {
//			ToastUtil.showMessage(R.string.get_data_fail);
			onBackPressed();
			return;
		}
		Bitmap photo = null;
		try {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			options.inSampleSize = BitmapUtil.calculateInSampleSize(options, 250, 250);
			options.inJustDecodeBounds = false;
			photo = BitmapFactory.decodeFile(path, options);
		} catch (OutOfMemoryError e) {

		}
		int angle = readPictureDegree(path);
		if (angle != 0) {
			Matrix m = new Matrix();
			m.setRotate(angle);
			photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), m, true);
		}
		if (photo != null) {
			BufferedOutputStream stream;
			try {
				if (isDelete) {
					file.delete();
				}
				mUrl = FileUtils.getExternalCacheDir(this).getPath() + "/"
						+ System.currentTimeMillis() + ".jpg";
				file = new File(mUrl);
				file.createNewFile();
				stream = new BufferedOutputStream(new FileOutputStream(file));
				photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				stream.flush();
				stream.close();
				photo.recycle();
				Bitmap bitmap = BitmapUtil.getimage(mUrl);
				mImage.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
//			ToastUtil.showMessage(R.string.get_data_fail);
			onBackPressed();
		}
	}

	private void startPhotoCrop(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 96);
		intent.putExtra("outputY", 96);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, ACTIVITY_RET_FROM_CROP);
	}

	/**
	 * 读取图片属性：旋转的角度
	 * @param path
	 *        图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.push_top_in, R.anim.push_bottom_out);
	}

//	public class UploadTask extends AsyncTask<String, Void, String> {
//
//		@Override
//		protected String doInBackground(String... params) {
//			String string = "";
//			try {
//				File file = new File(params[0]);
//				if (file.exists()) {
//					string = upLoadTest(HttpUrl.domain + HttpUrl.UPLOADAVATAR, params[0]);
//				} else {
//
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			return string;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			super.onPostExecute(result);
//			if (!StringUtil.isEmpty(result)) {
//				try {
//					BaseDataEntity entity = (BaseDataEntity) JSON.parseObject(result,
//							BaseDataEntity.class);
//					if (entity != null) {
//						if (!StringUtil.isEmpty(entity.getData())) {
//							Toast.makeText(SCSDBaseApplication.getAppContext(), "修改头像成功",
//									Toast.LENGTH_LONG).show();
//						} else {
//							Toast.makeText(SCSDBaseApplication.getAppContext(), "修改头像失败",
//									Toast.LENGTH_LONG).show();
//						}
//					} else {
//						Toast.makeText(SCSDBaseApplication.getAppContext(), "修改头像失败", Toast.LENGTH_LONG)
//								.show();
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//					Toast.makeText(SCSDBaseApplication.getAppContext(), "修改头像失败", Toast.LENGTH_LONG)
//							.show();
//				}
//			} else {
//				Toast.makeText(SCSDBaseApplication.getAppContext(), "修改头像失败", Toast.LENGTH_LONG).show();
//			}
//		}
//
//	}
//
//	private String upLoadTest(String url, String filePath) {
//		HttpClient httpclient = new DefaultHttpClient();
//		String strResult = "";
//		HttpPost httppost = new HttpPost(url);
//		File file = new File(filePath);
//		MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create(); // 文件传输
//		try {
//			mBuilder.addBinaryBody("file", new FileInputStream(file),
//					ContentType.APPLICATION_OCTET_STREAM, ".jpg");
//			mBuilder.addTextBody("UserId", ShareCookie.getUserId());
//			httppost.setEntity(mBuilder.build());
//			httppost.setHeader("User-Agent", "Tiangou-Android");
//			HttpResponse response = httpclient.execute(httppost);
//			int statusCode = response.getStatusLine().getStatusCode();
//			if (statusCode == HttpStatus.SC_OK) {
//				strResult = EntityUtils.toString(response.getEntity());
//				return strResult;
//			} else {
//				return strResult;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return strResult;
//
//	}

//	private void upPhoto(File file) {
//		showWaitingDialog();
//		HttpClientAsync client = HttpClientAsync.getInstance();
//		HttpParams param = new HttpParams();
//		param.put("UserId", ShareCookie.getUserId());
//		param.put("ImgData", file);
//		client.setmToken(ShareCookie.getToken());
//		client.post(HttpUrl.getUrl(HttpUrl.UPLOADAVATAR), param, HttpUrl.CONTENT_TEXT,
//				new HttpCallBack() {
//					@Override
//					public void onHttpSuccess(Object arg0) {
//						dismissWaitingDialog();
//						BaseEntity entity;
//						try {
//							entity = (BaseEntity) JSON.parseObject(arg0.toString(),
//									BaseEntity.class);
//							if (null != entity) {
//								ToastUtil.showMessage(entity.getMessage());
//								if (entity.getState() == 1) {
//									finish();
//								}
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						 
//					}
//
//					@Override
//					public void onHttpStarted() {
//
//					}
//
//					@Override
//					public void onHttpFailure(Exception arg0, String arg1) {
//						dismissWaitingDialog();
//						if (!StringUtil.isEmpty(arg1)) {
//							ToastUtil.showMessage(arg1);
//						}
//					}
//				});// ,
//	}
}
