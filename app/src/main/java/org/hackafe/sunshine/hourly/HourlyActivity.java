package org.hackafe.sunshine.hourly;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import org.hackafe.sunshine.R;
import org.hackafe.sunshine.settings.SettingsActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HourlyActivity extends ActionBarActivity {
    private ShareActionProvider mShareActionProvider;
    boolean isDataLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new HourlyFragment())
                    .commit();
        }

        //showing activity logo
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_main_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        //set title
        getSupportActionBar().setTitle("Hourly forecast");
        //showing back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hourly, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(null);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_share:
                //TODO First time click not working properly (taking screenshot but no share dialog)
                setShareIntent();
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setShareIntent() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_container);

        if (!isDataLoading)
            new createScreenshotTask(linearLayout).execute();
    }

    public Uri takeScreenshot(LinearLayout linearLayout) {
        Uri uri;
        Bitmap myBitmap;

        //take screenshot
        View rootView = linearLayout.getRootView();
        rootView.setDrawingCacheEnabled(true);
        myBitmap = rootView.getDrawingCache();

        try {
            uri = saveImage(myBitmap);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return uri;
    }

    public Uri saveImage(Bitmap bitmap) throws IOException {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 30, bytes);
        File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Sunshine");
        fileDir.mkdirs();

        String fileName = "sh_" + System.currentTimeMillis() + ".png";

        File file = new File(fileDir, fileName);
        File[] files = fileDir.listFiles();
        for (File f : files) {
            f.delete();
        }
        file.createNewFile();
        FileOutputStream fo = new FileOutputStream(file);
        fo.write(bytes.toByteArray());
        fo.close();

        return Uri.fromFile(file);
    }

    private class createScreenshotTask extends AsyncTask<Void, Void, Uri> {
        private ProgressDialog pDialog;
        private LinearLayout linearLayout;

        createScreenshotTask(LinearLayout linearLayout) {
            this.linearLayout = linearLayout;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isDataLoading = true;

            pDialog = new ProgressDialog(HourlyActivity.this);
            pDialog.setMessage("Taking screenshot...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Uri doInBackground(Void... voids) {
            return takeScreenshot(linearLayout);
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            mShareActionProvider.setShareIntent(intent);

            pDialog.dismiss();

            isDataLoading = false;
        }
    }

}
