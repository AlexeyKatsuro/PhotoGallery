package com.dedalexey.myapplication1111;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
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
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = PhotoGalleryFragment.class.getSimpleName();

    private int mPage=1;

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private PhotoAdapter mPhotoAdapter;
    private GridLayoutManager mGridLayoutManager;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private ProgressBar mProgressBar;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

//        Handler responseHandler = new Handler();
//        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
//        mThumbnailDownloader.setThumbnailDownloadListener(
//                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
//
//                    @Override
//                    public void onThumbnailDownloaded(PhotoHolder photoHolder,
//                                                      Bitmap bitmap) {
//                        Drawable drawable = new BitmapDrawable(getResources(),
//                                bitmap);
//                        photoHolder.bindDrawable(drawable);
//                    }
//                }
//        );
//        mThumbnailDownloader.start();
//        mThumbnailDownloader.getLooper();
//        Log.i(TAG,"Background thread started");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_photo_gallery_recycler_view);
        mProgressBar = (ProgressBar) v.findViewById(R.id.loading_indicator);
        //mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPhotoRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Adjust the columns to fit based on width of RecyclerView
                int width = mPhotoRecyclerView.getWidth();
                int COLUMN_SIZE =160;
                int mGridColumns = Math.round(width / (float)COLUMN_SIZE);
                mGridLayoutManager = new GridLayoutManager(getActivity(),mGridColumns);
                mPhotoRecyclerView.setLayoutManager(mGridLayoutManager);
                //setupAdapter();
               // setCurrentPageView();
            }
        });

        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(-1)) {
                   // Toast.makeText(getActivity(),"Start",Toast.LENGTH_SHORT).show();
                    mPage--;
                    if(mPage<1){
                        mPage=1;
                    }
                   // loadItems(mPage);
                }
                if (!recyclerView.canScrollVertically(1)) {
                    //Toast.makeText(getActivity(),"End",Toast.LENGTH_SHORT).show();
                    mPage++;
                    updateItems();
                }
            }
        });

        updateItems();
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       // mThumbnailDownloader.quit();
        //Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
       // mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery,menu);
        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG,"QuaryTextSubmit: " + query);

                mPage=0;
                QueryPreferences.setStoreQuery(getActivity(),query);
                searchView.onActionViewCollapsed();
                removeFocus();
                updateItems();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
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
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(getString(R.string.stop_polling));
        } else {
            toggleItem.setTitle(getString(R.string.start_polling));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                mPage=0;
                QueryPreferences.setStoreQuery(getActivity(),null);
                removeFocus();
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
    void removeFocus(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query,mPage).execute();
    }

    private void setupAdapter() {
        if (isAdded()) {

            mPhotoAdapter = new PhotoAdapter(mItems);
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        ImageView mItemImageView;
        GalleryItem mGalleryItem;
        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
        }
        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }
        public void bindGalleryItem(GalleryItem galleryItem){
            mGalleryItem=galleryItem;
            Picasso.with(getActivity())
                    .load(galleryItem.getUrl())
                    .placeholder(R.drawable.ic_action_download)
                    .into(mItemImageView);

        }

        @Override
        public void onClick(View v) {
            Intent i = PhotoPageActivity.newIntent(getActivity(),mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> items) {
            mGalleryItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            View view = inflater.inflate(R.layout.gallery_item,parent,false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item = mGalleryItems.get(position);
            holder.bindGalleryItem(item);
           // Drawable drawable = getResources().getDrawable(R.drawable.bill_up_close);
            //holder.bindDrawable(drawable);
            //mThumbnailDownloader.queueThumbnail(holder,item.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {
        private String mQuery;
        private int mPage;

        public FetchItemsTask(String query, int page) {
            mQuery = query;
            mPage = page;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            if (mQuery == null) {
                return new FlickrFetchr().setPage(mPage).fetchRecentPhotos();
            } else {
                return new FlickrFetchr().setPage(mPage).searchPhotos(mQuery);
            }
        }



        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items ;
            mProgressBar.setVisibility(View.GONE);
            setupAdapter();
        }

    }
}
