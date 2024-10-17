package com.sleepeasycenter.o2ring_app.utils

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sleepeasycenter.o2ring_app.R

class EcgAdapter(layoutResId: Int, data: MutableList<EcgData>?) : BaseQuickAdapter<EcgData, BaseViewHolder>(layoutResId, data) {

    override fun convert(holder: BaseViewHolder, item: EcgData) {
        holder.setText(R.id.device_item_name, "${item.fileName} duration : ${item.duration} s")
    }
}