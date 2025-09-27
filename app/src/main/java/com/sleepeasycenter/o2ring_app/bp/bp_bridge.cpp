#include <jni.h>
#include <stdint.h>
#include "mega_bp_step.h"      // declares mega_bp_step / init / terminate
#include "rtwtypes.h"          // boolean_T, uint8_T etc.

static bool g_inited = false;

extern "C" JNIEXPORT void JNICALL
Java_com_sleepeasycenter_o2ring_1app_BloodPressureNative_init(JNIEnv*, jobject) {
  if (!g_inited) {
    mega_bp_step_initialize();
    g_inited = true;
  }
}

extern "C" JNIEXPORT void JNICALL
Java_com_sleepeasycenter_o2ring_1app_BloodPressureNative_release(JNIEnv*, jobject) {
  if (g_inited) {
    mega_bp_step_terminate();
    g_inited = false;
  }
}

// calcBp returns a 2-byte array: [SBP, DBP]
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_sleepeasycenter_o2ring_1app_BloodPressureNative_calcBp(
    JNIEnv* env, jobject /*thiz*/,
    jint ppg,          // raw PPG sample (int)
    jshort acc1, jshort acc2, jshort acc3,  // motion (or 0 if unused)
    jbyte sbpCalib,    // unsigned char in C (0..255)
    jbyte dbpCalib,    // unsigned char in C (0..255)
    jboolean useAcc,   // whether to use acc signals
    jbyte sampRate,    // 150 hz
    jboolean resetFlag // true to reset internal state
) {
  if (!g_inited) {
    mega_bp_step_initialize();
    g_inited = true;
  }

  unsigned char sbp_out = 0;
  unsigned char dbp_out = 0;

  mega_bp_step(
    (int)ppg,
    (short)acc1, (short)acc2, (short)acc3,
    (unsigned char)sbpCalib,
    (unsigned char)dbpCalib,
    (boolean_T)(useAcc ? 1 : 0),
    (unsigned char)sampRate,
    (boolean_T)(resetFlag ? 1 : 0),
    &sbp_out, &dbp_out
  );

  jbyte out[2];
  out[0] = (jbyte)sbp_out; // JNI bytes are signed; keep as raw byte
  out[1] = (jbyte)dbp_out;

  jbyteArray arr = env->NewByteArray(2);
  env->SetByteArrayRegion(arr, 0, 2, out);
  return arr;
}
