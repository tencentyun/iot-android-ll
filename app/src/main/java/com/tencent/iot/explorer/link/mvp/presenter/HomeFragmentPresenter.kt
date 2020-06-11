package com.mvp.presenter

import android.text.TextUtils
import com.kitlink.App
import com.kitlink.entity.DeviceEntity
import com.mvp.ParentPresenter
import com.mvp.model.HomeFragmentModel
import com.mvp.view.HomeFragmentView

/**
 * 设备页面
 */
class HomeFragmentPresenter(view: HomeFragmentView) :
    ParentPresenter<HomeFragmentModel, HomeFragmentView>(view) {

    override fun getIModel(view: HomeFragmentView): HomeFragmentModel {
        return HomeFragmentModel(view)
    }

    /**
     * 获得列表中的数据
     */
    fun getDeviceEntity(position: Int): DeviceEntity {
        return model!!.getDeviceEntity(position)
    }

    /**
     * 请求设备列表
     */
    fun loadDeviceList() {
        model?.run {
            if (deviceListEnd){
                loadShareDeviceList()
            }else{
                loadDeviceList()
            }
        }
    }

    /**
     *  刷新家庭列表
     */
    fun refreshFamilyList() {
        model?.refreshFamilyList()
    }

    /**
     *  刷新房间列表
     */
    fun refreshRoomList() {
        model?.refreshRoomList()
    }

    /**
     * 请求设备列表
     */
    fun refreshDeviceList() {
        if (TextUtils.isEmpty(App.data.getCurrentFamily().FamilyId) || App.data.familyList.size <= 0) {
            model?.refreshFamilyList()
        } else {
            model?.refreshDeviceList()
        }
    }

    /**
     * 获取共享的设备
     */
    fun loadShareDeviceList() {
        model?.loadShareDeviceList()
    }

    /**
     * 切换家庭
     */
    fun tabFamily(position: Int) {
        model?.tabFamily(position)
    }

    /**
     * 切换房间
     */
    fun tabRoom(position: Int) {
        model?.tabRoom(position)
    }

}