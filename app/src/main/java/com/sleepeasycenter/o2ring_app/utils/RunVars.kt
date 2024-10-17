package com.sleepeasycenter.o2ring_app.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

val _bleState = MutableLiveData<Boolean>().apply {
    value = false
}
val bleState: LiveData<Boolean> = _bleState
