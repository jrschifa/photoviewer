package com.sarriaroman.PhotoViewer;

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
import android.os.AsyncTask;
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
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.UrlConnectionDownloader;

import org.apache.cordova.BuildHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;

import uk.co.senab.photoview.PhotoViewAttacher;

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
    private JSONObject headers;
    private JSONObject picassoOptions;
    private File tempImage;

	private String applicationId;
	private static final String TAG = "PhotoActivity";

    public static JSONArray rawArgs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.applicationId = (String) BuildHelper.getBuildConfigValue(this, "APPLICATION_ID");

		setContentView(getApplication().getResources().getIdentifier("photoviewer_photo", "layout", getApplication().getPackageName()));

		try {
            this.imageUrl = rawArgs.getString(0);
            this.titleText = rawArgs.getString(1);
            this.subTitleText = rawArgs.getString(2);
            this.maxWidth = rawArgs.getInt(3);
            this.maxHeight = rawArgs.getInt(4);
            this.menuItems = rawArgs.getJSONArray(5);
            this.headers = parseHeaders(rawArgs.optString(9));
            this.picassoOptions = rawArgs.optJSONObject(10);

            // Load the Views
            findViews();

            loadImage();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
	    Bitmap bmp = null;
	    try {
            File file = this.getLocalBitmapFileFromView(photo);
            bmp = BitmapFactory.decodeStream(new FileInputStream(file), null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

		if (bmp != null) {
			File path = this.getApplicationContext().getCacheDir();
			File file = writeFileToPath(path, bmp);

			Intent intent = new Intent(Intent.ACTION_SEND);

			Uri uri = FileProvider.getUriForFile(this.getApplicationContext(), this.applicationId + ".provider", file);

			intent.putExtra(Intent.EXTRA_STREAM, uri);
			intent.setType("image/*");
			intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			String title = menuItem.optString("title", "Share");
			startActivity(Intent.createChooser(intent, title));
		}
	}

	private void onDownloadAction(JSONObject menuItem) {
        Bitmap bmp = null;
        try {
            File file = this.getLocalBitmapFileFromView(photo);
            bmp = BitmapFactory.decodeStream(new FileInputStream(file), null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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
		getSupportActionBar().setTitle(this.titleText);

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

    private RequestCreator setOptions(RequestCreator picasso) throws JSONException {
        int width = (maxWidth != 0) ? this.maxWidth : MAX_WIDTH;
		int height = (maxHeight != 0) ? this.maxHeight : MAX_HEIGHT;
		int size = (int) Math.ceil(Math.sqrt(width * height));

        picasso.transform(new BitmapTransform(width, height));
        picasso.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE);
		picasso.resize(size, size);

        if(this.picassoOptions.has("fit") && this.picassoOptions.optBoolean("fit")) {
            picasso.fit();
        }

        if(this.picassoOptions.has("centerInside") && this.picassoOptions.optBoolean("centerInside")) {
            picasso.centerInside();
        }

        if(this.picassoOptions.has("centerCrop") && this.picassoOptions.optBoolean("centerCrop")) {
            picasso.centerCrop();
        }

        return picasso;
    }
    
    /**
     * Load the image using Picasso
     */
    private void loadImage() throws JSONException {
        if (this.imageUrl.startsWith("http") || this.imageUrl.startsWith("file")) {
            Picasso picasso;
            if (headers == null) {
                picasso = Picasso.with(PhotoActivity.this);
            } else {
                picasso = getImageLoader(this);
            }

            this.setOptions(picasso.load(this.imageUrl))
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
        } else if (this.imageUrl.startsWith("data:image")) {
            new AsyncTask<Void, Void, File>() {
                protected File doInBackground(Void... params) {
                    String base64Image = imageUrl.substring(imageUrl.indexOf(",") + 1);
                    return getLocalBitmapFileFromString(base64Image);
                }

                protected void onPostExecute(File file) {
                    tempImage = file;
                    Picasso picasso = Picasso.with(PhotoActivity.this);

                    try {
                        setOptions(picasso.load(tempImage))
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        } else {
            photo.setImageURI(Uri.parse(this.imageUrl));

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

    public void onDestroy() {
        if (tempImage != null) {
            tempImage.delete();
        }
        super.onDestroy();
    }

    private String getFileName() {
		return "share_image_" + System.currentTimeMillis() + ".png";
	}

    public File getLocalBitmapFileFromString(String base64) {
        File file;

        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), this.getFileName());
            file.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(file);
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            output.write(decoded);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            file = null;
        }
        return file;
    }

    /**
     * Create Local Image due to Restrictions
     *
     * @param imageView
     * @return
     */
    public File getLocalBitmapFileFromView(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp;

        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }

        // Store image to default external storage directory
        File file;

        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), this.getFileName());
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();

        } catch (IOException e) {
            file = null;
            e.printStackTrace();
        }
        return file;
    }

    private JSONObject parseHeaders(String headerString) {
        JSONObject headers = null;

        // Short circuit if headers is empty
        if (headerString == null || headerString.length() == 0) {
            return headers;
        }

        // headers should never be a JSON array, only a JSON object
        try {
            headers = new JSONObject(headerString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return headers;
    }

    private Picasso getImageLoader(Context ctx) {
        Picasso.Builder builder = new Picasso.Builder(ctx);

        builder.downloader(new UrlConnectionDownloader(ctx) {
            @Override
            protected HttpURLConnection openConnection(Uri uri) throws IOException {
                HttpURLConnection connection = super.openConnection(uri);
                Iterator<String> keyIter = headers.keys();
                String key = null;
                try {
                    while (keyIter.hasNext()) {
                        key = keyIter.next();
                        connection.setRequestProperty(key, headers.getString(key));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connection;
            }
        });

        return builder.build();
    }
}