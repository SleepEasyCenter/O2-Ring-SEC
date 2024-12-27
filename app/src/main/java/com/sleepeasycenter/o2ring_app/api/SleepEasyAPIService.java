package com.sleepeasycenter.o2ring_app.api;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SleepEasyAPIService {
    @Multipart
    @POST("/staff/o2ring-data/upload.php")
    Call<ResponseBody> uploadO2RingData(@Part MultipartBody.Part filePart, @Part MultipartBody.Part patient_id);
}

