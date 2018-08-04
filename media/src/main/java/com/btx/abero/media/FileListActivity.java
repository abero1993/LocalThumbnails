package com.btx.abero.media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.btx.abero.LocalThumbnails;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by abero on 2018/4/25.
 */

public class FileListActivity extends AppCompatActivity {

    private static final String TAG = "FileListActivity";
    public static final String EXTRA_KEY = "fileinfo_key";
    private ListView mListView;
    private List<FileInfo> mFileInfoList;
    private FileInfo mFileInfo;


    public static void startFilelistActivity(AppCompatActivity activity, FileInfo fileInfo) {
        if (null == activity || fileInfo == null)
            throw new NullPointerException("activity or fileinfo can not be null");

        Intent intent = new Intent(activity, FileListActivity.class);
        intent.putExtra(EXTRA_KEY, fileInfo);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);
        mFileInfo = (FileInfo) getIntent().getSerializableExtra(EXTRA_KEY);
        Toolbar toolbar = findViewById(R.id.file_toolbar);
        toolbar.setTitle(R.string.video);
        if (mFileInfo != null) {
            if (FileType.VIDEO.value() == mFileInfo.getFileType())
                toolbar.setTitle(R.string.video);
            else if (FileType.AUDIO.value() == mFileInfo.getFileType())
                toolbar.setTitle(R.string.audio);
            else if (FileType.PICTURE.value() == mFileInfo.getFileType())
                toolbar.setTitle(R.string.picture);
        }


    }

    private void initList() {
        mListView = findViewById(R.id.file_listview);
        mFileInfoList = new ArrayList<>();
        if (mFileInfo != null) {
            File file = new File(mFileInfo.getPath());
            Log.i(TAG, "floder path=" + mFileInfo.getPath());
            File[] files = file.listFiles(new OmxFileFilter(mFileInfo.getFileType()));
            if (files != null) {
                for (File f : files) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileType(mFileInfo.getFileType());
                    fileInfo.setPath(f.getPath());
                    fileInfo.setName(f.getName());
                    fileInfo.setLastModified(f.lastModified());
                    mFileInfoList.add(fileInfo);
                }
            }
        }

        mListView.setAdapter(new FileAdapter(this, mFileInfoList));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FileInfo fileInfo = mFileInfoList.get(i);
                if (FileType.VIDEO.value() == fileInfo.getFileType()) {
                    VideoActivity.intentTo(FileListActivity.this, fileInfo.getPath(), fileInfo.getName());
                } else if (FileType.PICTURE.value() == fileInfo.getFileType()) {
                    Rect rect = new Rect();
                    view.getGlobalVisibleRect(rect);
                    PicViewActivity.startPicViewActivity(FileListActivity.this, fileInfo.getPath(), rect);
                } else if (FileType.AUDIO.value() == fileInfo.getFileType()) {
                    AudioPlayActivity.startAudioPlayActivity(FileListActivity.this, fileInfo.getPath());
                }
            }
        });


    }

    static class FileAdapter extends BaseAdapter {
        private Context mContext;
        private List<FileInfo> mList;
        private LocalThumbnails mLocalThumbnails;

        public FileAdapter(Context context, List<FileInfo> list) {
            mContext = context;
            mList = list;
            mLocalThumbnails = new LocalThumbnails.Builder().create(context);
        }

        @Override
        public int getCount() {
            return null == mList ? 0 : mList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view != null) {
                viewHolder = (ViewHolder) view.getTag();
            } else {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(mContext).inflate(R.layout.file_list_item, viewGroup, false);
                viewHolder.logo = view.findViewById(R.id.file_item_logo);
                viewHolder.name = view.findViewById(R.id.file_item_name);
                viewHolder.time = view.findViewById(R.id.file_item_time);
                view.setTag(viewHolder);
            }

            FileInfo fileInfo = mList.get(i);
            mLocalThumbnails.load(fileInfo.getPath(), viewHolder.logo);
            viewHolder.name.setText(fileInfo.getName());
            SimpleDateFormat bartDateFormat = new SimpleDateFormat("EEEE-MMMM-dd-yyyy");
            Date date = new Date(fileInfo.getLastModified());
            viewHolder.time.setText(bartDateFormat.format(date));
            Log.i(TAG, "file name=" + fileInfo.getName() + " time=" + fileInfo.getLastModified());

            return view;
        }

        static class ViewHolder {
            ImageView logo;
            TextView name;
            TextView time;
        }

    }


    static class OmxFileFilter implements FileFilter {
        private String mSuffix;

        public OmxFileFilter(int type) {
            if (FileType.VIDEO.value() == type)
                mSuffix = ".mp4";
            else if (FileType.AUDIO.value() == type)
                mSuffix = ".wav";
            else if (FileType.PICTURE.value() == type)
                mSuffix = ".jpg";
        }

        @Override
        public boolean accept(File file) {
            if (file.getName().endsWith(mSuffix))
                return true;
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initList();
    }
}
