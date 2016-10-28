package remix.myplayer.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import remix.myplayer.R;
import remix.myplayer.model.LrcInfo;
import remix.myplayer.model.MP3Item;
import remix.myplayer.ui.customview.LrcView;
import remix.myplayer.util.CommonUtil;
import remix.myplayer.lrc.SearchLRC;

/**
 * Created by Remix on 2015/12/2.
 */

/**
 * 歌词界面Fragment
 */
public class LrcFragment extends BaseFragment {
    //是否找到歌词的几种状态
    private static int UPDATE_LRC = 1;
    private static int NO_LRC = 2;
    private static int NO_NETWORK = 3;
    private static int SEARCHING = 4;
    private OnLrcViewFindListener onFindListener;
    private MP3Item mInfo;
    @BindView(R.id.lrc_view)
    LrcView mLrcView;
    //歌词列表
    private ArrayList<LrcInfo> mLrcList;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mLrcView == null)
                return;
            //是否正在搜索
            mLrcView.setSearching(msg.what == SEARCHING);
            if(msg.what == SEARCHING){
                return;
            }
            if(msg.what == UPDATE_LRC) {
                //更新歌词
                if(mLrcList != null) {
                    mLrcView.UpdateLrcList(mLrcList);
                }
            } else if (msg.what == NO_LRC) {
                //没有找到歌词
                mLrcView.UpdateLrcList(null);
            } else if (msg.what == NO_NETWORK) {
                //没用网络
                mLrcView.UpdateLrcList(null);
            }
        }
    };

    public void setOnFindListener(OnLrcViewFindListener l){
        onFindListener = l;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = LrcFragment.class.getSimpleName();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lrc,container,false);

        mUnBinder = ButterKnife.bind(this,rootView);
        onFindListener.onLrcViewFind(mLrcView);
        mInfo = (MP3Item)getArguments().getSerializable("MP3Item");
        UpdateLrc(mInfo);
        return rootView;
    }

    public void UpdateLrc(MP3Item mp3Item) {
        if(mp3Item == null)
            return;
        mInfo = mp3Item;
        new DownloadThread().start();
    }

    class DownloadThread extends Thread {
        @Override
        public void run() {
            if(!CommonUtil.isNetWorkConnected()) {
                mHandler.sendEmptyMessage(NO_NETWORK);
                return;
            }
            mHandler.sendEmptyMessage(SEARCHING);
            mLrcList = new SearchLRC(mInfo).getLrc();
            if(mLrcList == null) {
                mHandler.sendEmptyMessage(NO_LRC);
                return;
            }
            mHandler.sendEmptyMessage(UPDATE_LRC);
        }
    }

    public interface OnLrcViewFindListener{
        void onLrcViewFind(LrcView lrcView);
    }
}
