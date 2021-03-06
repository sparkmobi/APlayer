package remix.myplayer.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import remix.myplayer.Global;
import remix.myplayer.R;
import remix.myplayer.bean.mp3.Song;
import remix.myplayer.helper.MusicServiceRemote;
import remix.myplayer.misc.asynctask.WrappedAsyncTaskLoader;
import remix.myplayer.misc.interfaces.OnItemClickListener;
import remix.myplayer.service.Command;
import remix.myplayer.ui.adapter.PlayQueueAdapter;
import remix.myplayer.ui.widget.fastcroll_recyclerview.LocationRecyclerView;
import remix.myplayer.util.DensityUtil;
import remix.myplayer.util.PlayListUtil;

import static remix.myplayer.service.MusicService.EXTRA_POSITION;
import static remix.myplayer.util.MusicUtil.makeCmdIntent;
import static remix.myplayer.util.Util.sendLocalBroadcast;

/**
 * Created by Remix on 2015/12/6.
 */

/**
 * 正在播放列表Dialog
 */
public class PlayQueueDialog extends BaseDialogActivity implements LoaderManager.LoaderCallbacks<List<Song>> {
    @BindView(R.id.bottom_actionbar_play_list)
    LocationRecyclerView mRecyclerView;
    private PlayQueueAdapter mAdapter;
    private static int LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_playqueue);
        ButterKnife.bind(this);

        mAdapter = new PlayQueueAdapter(this, R.layout.item_playqueue);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                sendLocalBroadcast(makeCmdIntent(Command.PLAYSELECTEDSONG)
                        .putExtra(EXTRA_POSITION, position));

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //初始化LoaderManager
        getSupportLoaderManager().initLoader(LOADER_ID++, null, this);
        //改变播放列表高度，并置于底部
        Window w = getWindow();
//        w.setWindowAnimations(R.style.AnimBottom);
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.height = DensityUtil.dip2px(mContext, 354);
        lp.width = metrics.widthPixels;
        w.setAttributes(lp);
        w.setGravity(Gravity.BOTTOM);
    }

    public PlayQueueAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    protected void onStart() {
        super.onStart();
        overridePendingTransition(R.anim.slide_bottom_in, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_bottom_out);
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new AsyncPlayQueueSongLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, final List<Song> data) {
        if (data == null)
            return;
        mAdapter.setData(data);
        final int currentId = MusicServiceRemote.getCurrentSong().getId();
        if (currentId < 0)
            return;
        mRecyclerView.smoothScrollToCurrentSong(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        if (mAdapter != null)
            mAdapter.setData(null);
    }

    @Override
    public void onMediaStoreChanged() {
        if (mHasPermission) {
            getSupportLoaderManager().initLoader(LOADER_ID++, null, this);
        } else {
            if (mAdapter != null)
                mAdapter.setData(null);
        }
    }

    @Override
    public void onPermissionChanged(boolean has) {
        onMediaStoreChanged();
    }

    @Override
    public void onPlayListChanged() {
        onMediaStoreChanged();
    }

    private static class AsyncPlayQueueSongLoader extends WrappedAsyncTaskLoader<List<Song>> {
        private AsyncPlayQueueSongLoader(Context context) {
            super(context);
        }

        @Override
        public List<Song> loadInBackground() {

            List<Integer> ids = PlayListUtil.getSongIds(Global.PlayQueueID);
            if (ids == null || ids.isEmpty())
                return new ArrayList<>();
            return PlayListUtil.getMP3ListByIds(ids, Global.PlayQueueID);
        }
    }
}
