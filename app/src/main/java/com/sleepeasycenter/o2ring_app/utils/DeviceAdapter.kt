package com.sleepeasycenter.o2ring_app.utils

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sleepeasycenter.o2ring_app.R
import com.lepu.blepro.objs.Bluetooth

class DeviceAdapter(layoutResId: Int, data: MutableList<Bluetooth>?) : BaseQuickAdapter<Bluetooth, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: Bluetooth) {
        holder.setText(R.id.device_item_name, item.name + "  " + item.macAddr)
    }
}