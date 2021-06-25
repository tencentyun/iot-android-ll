package com.tencent.iot.explorer.link.core.demo.video.activity

import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tencent.iot.explorer.link.core.demo.App
import com.tencent.iot.explorer.link.core.demo.R
import com.tencent.iot.explorer.link.core.demo.activity.BaseActivity
import com.tencent.iot.explorer.link.core.demo.video.Command
import com.tencent.iot.explorer.link.core.demo.video.adapter.DevsAdapter
import com.tencent.iot.explorer.link.core.demo.video.dialog.ListOptionsDialog
import com.tencent.iot.explorer.link.core.demo.video.dialog.ToastDialog
import com.tencent.iot.explorer.link.core.demo.video.entity.DevInfo
import com.tencent.iot.explorer.link.core.demo.video.entity.VideoProductInfo
import com.tencent.iot.video.link.consts.VideoConst
import com.tencent.xnet.XP2P
import com.tencent.xnet.XP2PCallback
import kotlinx.android.synthetic.main.fragment_video_device.*
import kotlinx.android.synthetic.main.title_layout.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.Runnable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private var countDownLatch = CountDownLatch(1)

class VideoNvrActivity : BaseActivity(), DevsAdapter.OnItemClicked, XP2PCallback,
    CoroutineScope by MainScope() {
    private var devs : MutableList<DevInfo> = ArrayList()
    private var adapter : DevsAdapter? = null
    private var videoProductInfo = VideoProductInfo()
    @Volatile
    private var queryJon: Job? = null

    override fun getContentView(): Int {
        return R.layout.activity_video_nvr_devs
    }

    override fun initView() {
        var devGridLayoutManager = GridLayoutManager(this@VideoNvrActivity, 2)
        adapter = DevsAdapter(this@VideoNvrActivity, devs)
        adapter?.let {
            it.setOnItemClicked(this)
            it.tipText = tv_tip_txt
        }
        tv_my_devs.visibility = View.GONE
        gv_devs.setLayoutManager(devGridLayoutManager)
        gv_devs.setAdapter(adapter)
        adapter?.radioComplete = radio_complete
        adapter?.radioEdit = radio_edit
        adapter?.switchBtnStatus(false)

        smart_refresh_layout.setEnableRefresh(true)
        smart_refresh_layout.setRefreshHeader(ClassicsHeader(this@VideoNvrActivity))
        smart_refresh_layout.setEnableLoadMore(false)
        smart_refresh_layout.setRefreshFooter(ClassicsFooter(this@VideoNvrActivity))

        App.data.accessInfo?.let {
            XP2P.setQcloudApiCred(it.accessId, it.accessToken)
            XP2P.setCallback(this)
        }

        var bundle = intent.getBundleExtra(VideoConst.VIDEO_NVR_INFO)
        bundle?.let {
            var devInfoStr = bundle.getString(VideoConst.VIDEO_NVR_INFO)
            if (TextUtils.isEmpty(devInfoStr)) return@let

            var devInfo = JSON.parseObject(devInfoStr, DevInfo::class.java)
            devInfo?.let {
                tv_title.setText(it.deviceName)
                showDev(it.deviceName)
            }
        }
    }

    private fun showDev(devName: String) {
        if (TextUtils.isEmpty(devName)) return
        queryJon?.let { it.cancel() }  // 先关闭上次未完成的协程

        Thread(Runnable {
            queryJon = launch (Dispatchers.Default){
                delay(100)

                App.data.accessInfo?.let {
                    countDownLatch = CountDownLatch(1)
                    var started = XP2P.startServiceWithXp2pInfo("${it.productId}/${devName}",
                        it.productId, devName, "")
                    Log.e("XXX", "showDev started " + started)
                    if (started != 0) {
                        launch(Dispatchers.Main) {
                            smart_refresh_layout?.finishRefresh()
                        }
                        return@launch
                    }

                    Log.e("XXX", "start wait")
                    countDownLatch.await(5, TimeUnit.SECONDS)
                    Log.e("XXX", "pass wait")
                    queryNvrDev(devName)
                }
            }
        }).start()
    }

    private fun queryNvrDev(devName: String) {
        App.data.accessInfo?.let {
            var nvrDevs = XP2P.postCommandRequestSync("${it.productId}/${devName}", Command.QUERY_NVR_DEVS.toByteArray(),
                Command.QUERY_NVR_DEVS.toByteArray().size.toLong(), 2 * 1000 * 1000)
            Log.e("XXX", "nvrDevs ${nvrDevs}")
            nvrDevs?.let {
                launch(Dispatchers.Main) {
                    smart_refresh_layout?.finishRefresh()
                    devs.clear()
                    var nvrDevs = JSONArray.parseArray(nvrDevs, DevInfo::class.java)
                    nvrDevs?.let { devs?.addAll(it) }
                    rg_edit_dev.visibility = View.VISIBLE
                    adapter?.videoProductInfo = videoProductInfo
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        cancel()
        App.data.accessInfo?.let {
            XP2P.stopService("${it.productId}/${tv_title.text}")
        }
        XP2P.setCallback(null)
        super.onDestroy()
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        radio_edit.setOnClickListener {
            var options = arrayListOf(getString(R.string.edit_devs_2_show))
            var dlg = ListOptionsDialog(this@VideoNvrActivity, options)
            dlg.show()
            dlg.setOnDismisListener {
                adapter?.switchBtnStatus(true)
            }
        }

        radio_complete.setOnClickListener {
            adapter?.switchBtnStatus(false)

            if (adapter?.checkedIds!!.size <= 0) {
                ToastDialog(this@VideoNvrActivity, ToastDialog.Type.WARNING, getString(R.string.at_least_one), 2000).show()
                return@setOnClickListener
            }
        }

        smart_refresh_layout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {}
            override fun onRefresh(refreshLayout: RefreshLayout) {
                rg_edit_dev.visibility = View.GONE
                try {
                    queryNvrDev(tv_title.text.toString())
                } catch (e: Exception) {
                    refreshLayout.finishRefresh()
                    e.printStackTrace()
                    ToastDialog(this@VideoNvrActivity, ToastDialog.Type.WARNING, getString(R.string.query_failed), 2000).show()
                }
            }
        })
    }

    override fun onItemMoreClicked(pos: Int, dev: DevInfo) {}
    override fun onItemCheckedClicked(pos: Int, checked: Boolean) {}

    override fun onItemCheckedLimited() {
        ToastDialog(this@VideoNvrActivity, ToastDialog.Type.WARNING, getString(R.string.devs_limit), 2000).show()
    }

    override fun onItemClicked(pos: Int, dev: DevInfo) {
        var options = arrayListOf(getString(R.string.preview), getString(R.string.playback))
        var dlg = ListOptionsDialog(this@VideoNvrActivity, options)
        dlg.show()
        dlg.setOnDismisListener {
            when(it) {
                0 -> { VideoPreviewActivity.startPreviewActivity(this@VideoNvrActivity, dev) }
                1 -> { VideoPlaybackActivity.startPlaybackActivity(this@VideoNvrActivity, dev) }
            }
        }
    }

    override fun fail(msg: String?, errorCode: Int) {}
    override fun commandRequest(id: String?, msg: String?) {}
    override fun xp2pEventNotify(id: String?, msg: String?, event: Int) {
        Log.e("XXX", "id ${id} msg ${msg} event ${event}")
        if (event == 1004) {
            countDownLatch.countDown()
        }
    }
    override fun avDataRecvHandle(id: String?, data: ByteArray?, len: Int) {}
    override fun avDataCloseHandle(id: String?, msg: String?, errorCode: Int) {}
}



