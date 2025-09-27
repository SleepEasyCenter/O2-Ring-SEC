package com.sleepeasycenter.o2ring_app

object BloodPressureNative {
  init {
    System.loadLibrary("bp_native")
  }

  external fun init()
  external fun release()

  // returns ByteArray(size=2): [SBP, DBP]
  external fun calcBp(
          ppg: Int,
          acc1: Short,
          acc2: Short,
          acc3: Short,
          sbpCalib: Byte,
          dbpCalib: Byte,
          useAcc: Boolean,
          sampRate: Byte,
          reset: Boolean
  ): ByteArray
}
