package com.btx.local.localthumbnails;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.btx.abero.LocalThumbnails;
import com.btx.abero.media.FileInfo;
import com.btx.abero.media.FileListActivity;
import com.btx.abero.media.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private LocalThumbnails localThumbnails;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      /*  gridView = findViewById(R.id.grid);
        localThumbnails = new LocalThumbnails.Builder().create(this);

        MyAdapter myAdapter = new MyAdapter();
        gridView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();*/

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileType(FileType.PICTURE.value());
        fileInfo.setPath("/mnt/sdcard/PICS/2018-08-04");
        fileInfo.setLastModified(System.currentTimeMillis());
        fileInfo.setName("图片");

        FileListActivity.startFilelistActivity(this, fileInfo);

    }

    private class MyAdapter extends BaseAdapter {

        private List<String> list;

        public MyAdapter() {
            list = new ArrayList<>();
            ///sdcard/PICS/2018-07-23
            File file = new File("/sdcard/DCIM/Camera");
            File[] files = file.listFiles();
            for (File f : files)
                if (f.getAbsolutePath().contains("jpg") || f.getAbsolutePath().contains("mp4") || f.getAbsolutePath().contains("wav")) {
                    list.add(f.getAbsolutePath());
                }
            Log.i(TAG, "MyAdapter: size=" + list.size());

            // 视频其他信息的查询条件
            String[] mediaColumns = {MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA, MediaStore.Video.Media.DURATION};
            Uri uri = Uri.fromFile(new File(list.get(0)));
            Log.i(TAG, "uri =" + uri);
            Cursor cursor = getContentResolver().query(uri, mediaColumns, null, null, null);
            Log.i(TAG, "cursor size=" + cursor.getCount());
            /*getContentResolver().query()
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media
                            .EXTERNAL_CONTENT_URI,
                    mediaColumns, null, null, null);*/
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            ViewHolder holder = null;
            View item = null;

            Log.i(TAG, "getView: " + position);
            if (null == convertView) {
                Log.i(TAG, "create view");
                holder = new ViewHolder();
                item = LayoutInflater.from(MainActivity.this).inflate(R.layout.item, parent, false);
                holder.view = item.findViewById(R.id.image);
                holder.text = item.findViewById(R.id.text);
                item.setTag(holder);
            } else {
                Log.i(TAG, "reuse view");
                holder = (ViewHolder) convertView.getTag();
                item = convertView;
            }

            holder.text.setText("pos=" + position);
            String path = list.get(position);
            if (path.equals(holder.view.getTag())) {
                localThumbnails.load(list.get(position), holder.view);
            } else {
                holder.view.setTag(path);
                holder.view.setImageBitmap(null);
                localThumbnails.load(list.get(position), holder.view);
            }

            return item;

        }


    }

    static class ViewHolder {
        ImageView view;
        TextView text;
    }
}
