package com.photogallery.lgd.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/20.
 */

public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
//        new FetchItemsTask().execute();
        updateItems();

//        //服务启动
//        Intent i = PollService.newIntent(getActivity());
//        getActivity().startService(i);

//        PollService.setServiceAlarm(getActivity(), true);//启动定时器

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);

        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                }
        );

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setupAdapter();

        return v;
    }

    //调用清理方法
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Log.d(TAG, "QueryTextSubmit: " + s);
                QueryPreferences.setStoredQuery(getActivity(), s);//存储用户提交的查询信息
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        //菜单项切换
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);//清楚查询信息
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();//在FetchItemsTask中使用保存的查询信息
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //        private TextView mTitleTextView;
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);

//            mTitleTextView = (TextView) itemView;
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
        }

//        public void bindGalleryItem(GalleryItem item){
//            mTitleTextView.setText(item.toString());
//        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem){
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
//            Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPageUri());
            Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//            TextView textView = new TextView(getActivity());
//            return new PhotoHolder(textView);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            photoHolder.bindGalleryItem(galleryItem);
//            photoHolder.bindGalleryItem(galleryItem);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            photoHolder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        private String mQuery;

        public FetchItemsTask(String query){
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            /*
            try {
                String result = new FlickrFetchr().getUrlString("https://www.bignerdranch.com");
                Log.i(TAG, "Fetched contents of URL: " + result);
            } catch (IOException ioe) {
                Log.e(TAG, "Filed to fetch URL: ", ioe);
            }
            */
//            return new FlickrFetchr().fetchItems();
//            return null;

//            String query = "robot";
//            if (query == null){
            if (mQuery == null){
                return new FlickrFetchr().fetchRecentPhotos();
            }else {
//                return new FlickrFetchr().searchPhotos(query);
                return new FlickrFetchr().searchPhotos(mQuery);
            }

        }

        //接收doInBackground方法返回的数据，更新视图
        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }
}
