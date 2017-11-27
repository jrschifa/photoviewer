package com.sarriaroman.PhotoViewer;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoActivity extends AppCompatActivity {
	private static final int ACTION_NONE = 0;
	private static final int ACTION_DOWNLOAD = 1;
	private static final int ACTION_SHARE = 2;
	private static final int ACTION_COPY_LINK = 3;

	private static final ArrayMap<Integer, String> iconMap = new ArrayMap<Integer, String>();
	static {
		iconMap.put(ACTION_DOWNLOAD, "ic_file_download_white");
		iconMap.put(ACTION_SHARE, "ic_share_white");
		iconMap.put(ACTION_COPY_LINK, "ic_link_white");
	}

	private static final int MAX_WIDTH = 1024;
	private static final int MAX_HEIGHT = 1024;

	private PhotoViewAttacher mAttacher;
	private ImageView photo;
	private Toolbar toolbar;
	private TextView subTitle;

	private String imageUrl;
	private JSONArray menuItems;
	private String titleText;
	private String subTitleText;
	private int maxWidth;
	private int maxHeight;

	private static final String FILE_PROVIDER_PACKAGE_ID = "com.sarriaroman.PhotoViewer.fileprovider";
	private static final String TAG = "PhotoActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getApplication().getResources().getIdentifier("activity_photo", "layout", getApplication().getPackageName()));

		Intent intent = this.getIntent();

		imageUrl = intent.getStringExtra("url");
		titleText = intent.getStringExtra("title");
		subTitleText = intent.getStringExtra("subtitle");
		maxWidth = intent.getIntExtra("maxWidth", 0);
		maxHeight = intent.getIntExtra("maxHeight", 0);
		String menuArray = intent.getStringExtra("menu");

		try {
			menuItems = new JSONArray(menuArray);
		} catch (JSONException e) {
			e.printStackTrace();
			menuItems = new JSONArray();
		}

		// Load the Views
		findViews();

		loadImage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		Application application = getApplication();
		Resources resources = application.getResources();
		String packageName = application.getPackageName();

		for (int i = 0; i < menuItems.length(); i++) {
			JSONObject menuItem = menuItems.optJSONObject(i);
			MenuItem item = menu.add(Menu.NONE, i, Menu.NONE, menuItem.optString("title"));
			String iconPath = menuItem.optString("icon");
			if (!iconPath.isEmpty()) {
				try {
					item.setIcon(Drawable.createFromStream(getAssets().open(iconPath), null));
				} catch (IOException e) {
					Log.e(TAG, "icon from asset drawable", e);
				}
			} else {
				String icon = this.iconMap.get(menuItem.optInt("action"));
				item.setIcon(resources.getIdentifier(icon, "id", packageName));
			}

			item.setShowAsAction(menuItem.optInt("showAs"));
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();

		try {
			JSONObject currentItem = menuItems.getJSONObject(itemId);
			int action = currentItem.getInt("action");

			if (action == this.ACTION_DOWNLOAD) {
				this.onDownloadAction(currentItem);
			} else if (action == this.ACTION_SHARE) {
				this.onShareAction(currentItem);
			} else if (action == this.ACTION_COPY_LINK) {
				this.onCopyLinkAction(currentItem);
			}
		} catch (JSONException e) {
			Log.e(TAG, "Error: ", e);
		}

		return super.onOptionsItemSelected(item);
	}

	private void onCopyLinkAction(JSONObject menuItem) {
		ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(imageUrl, imageUrl);
		clipboard.setPrimaryClip(clip);

		Toast.makeText(getActivity(), "Copied", Toast.LENGTH_LONG).show();
	}

	private void onShareAction(JSONObject menuItem) {
		Bitmap bmp = this.getLocalBitmap(photo);

		if (bmp != null) {
			File path = this.getApplicationContext().getCacheDir();
			File file = writeFileToPath(path, bmp);

			Intent intent = new Intent(Intent.ACTION_SEND);

			Uri uri = FileProvider.getUriForFile(this.getApplicationContext(), FILE_PROVIDER_PACKAGE_ID, file);

			intent.putExtra(Intent.EXTRA_STREAM, uri);
			intent.setType("image/*");
			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			String title = menuItem.optString("title", "Share");
			startActivity(Intent.createChooser(intent, title));
		}
	}

	private void onDownloadAction(JSONObject menuItem) {
		Bitmap bmp = getLocalBitmap(photo);

		if (bmp != null) {
			File path = Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS);
			File file = this.writeFileToPath(path, bmp);

			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
			getApplication().getApplicationContext().sendBroadcast(intent);

			Toast.makeText(getActivity(), "Download Completed", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Find and Connect Views
	 *
	 */
	private void findViews() {
		Application application = getApplication();
		Resources resources = application.getResources();
		String packageName = application.getPackageName();

		// Photo Container
		photo = (ImageView) findViewById( resources.getIdentifier("photoView", "id", packageName) );
		mAttacher = new PhotoViewAttacher(photo);

		// ToolBar
		toolbar = (Toolbar) findViewById( resources.getIdentifier("toolbar", "id", packageName) ); // Attaching the layout to the toolbar object
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(titleText);

		// SubTitle
		subTitle = (TextView) findViewById( resources.getIdentifier("subtitleView", "id", packageName) );
		subTitle.setText(Html.fromHtml(this.subTitleText));
	}

	/**
	 * Get the current Activity
	 *
	 * @return
	 */
	private Activity getActivity() {
		return this;
	}

	/**
	 * Hide Loading when showing the photo. Update the PhotoView Attacher
	 */
	private void hideLoadingAndUpdate() {
		photo.setVisibility(View.VISIBLE);
		mAttacher.update();
	}

	/**
	 * Load the image using Picasso
	 *
	 */
	private void loadImage() {
		if( imageUrl.startsWith("http") || imageUrl.startsWith("file") ) {
			int width = (maxWidth != 0) ? maxWidth : MAX_WIDTH;
			int height = (maxHeight != 0) ? maxHeight : MAX_HEIGHT;
			int size = (int) Math.ceil(Math.sqrt(width * height));

			Picasso.with(this)
				.load(imageUrl)
				.transform(new BitmapTransform(width, height))
				.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
				.resize(size, size)
				.centerInside()
				.into(photo, new com.squareup.picasso.Callback() {
					@Override
					public void onSuccess() {
						hideLoadingAndUpdate();
					}

					@Override
					public void onError() {
						Toast.makeText(getActivity(), "Error loading image.", Toast.LENGTH_LONG).show();

						finish();
					}
				});
		} else if ( imageUrl.startsWith("data:image")){
            String base64String = imageUrl.substring(imageUrl.indexOf(",")+1);
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            photo.setImageBitmap(decodedByte);

            hideLoadingAndUpdate();
        } else {
            photo.setImageURI(Uri.parse(imageUrl));

            hideLoadingAndUpdate();
        }
	}

	private File writeFileToPath(File path, Bitmap bmp) {
		try {
			File file = new File(path, this.getFileName());

			path.mkdirs();

			FileOutputStream out = new FileOutputStream(file);
			bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();

			return file;
		} catch(FileNotFoundException e) {
			Log.e(TAG, "File not found: ", e);
		} catch (IOException e) {
			Log.e(TAG, "IO: ", e);
		}

		return null;
	}

	private String getFileName() {
		return "share_image_" + System.currentTimeMillis() + ".png";
	}

	private Bitmap getLocalBitmap(ImageView imageView) {
		Drawable drawable = imageView.getDrawable();

		if (drawable instanceof BitmapDrawable){
			return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
		}

		return null;
	}
}