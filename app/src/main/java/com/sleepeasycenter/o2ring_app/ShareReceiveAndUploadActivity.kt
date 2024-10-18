package com.sleepeasycenter.o2ring_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import com.sleepeasycenter.o2ring_app.api.SleepEasyAPI
import com.sleepeasycenter.o2ring_app.databinding.ActivityShareReceiveAndUploadBinding
import com.sleepeasycenter.o2ring_app.utils.fileFromContentUri
import com.sleepeasycenter.o2ring_app.utils.readPatientId
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private class CurrentUploadableFile(var filetype: String, var mimetype: String, var file: File) {

}

class ShareReceiveAndUploadActivity : AppCompatActivity() {

    lateinit var binding: ActivityShareReceiveAndUploadBinding;

    val TAG: String = "ShareReceiveAndUploadActivity"

    private var currentFile: CurrentUploadableFile? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareReceiveAndUploadBinding.inflate(layoutInflater);
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar);
        actionBar?.setDisplayHomeAsUpEnabled(true)
        binding.btnUploadDataFromShare.setOnClickListener { x -> btnUpload_callback() }
        when {
            intent?.action == Intent.ACTION_SEND -> {
                Log.d(TAG, "Receiving mime type:" + intent.type);
                handleSendFile(intent)
            }

            else -> {

            }
        }

    }


    fun handleSendFile(intent: Intent) {

        IntentCompat.getParcelableExtra(
            intent,
            Intent.EXTRA_STREAM,
            Uri::class.java
        )?.let { data ->
            Log.d(TAG, "Received file:" + data.toString())
            val file = fileFromContentUri(this, data)
            binding.uploadTextOutput.setText("File: " + file.name);

            var filetype = "unknown"
            var mimetype = "unknown"
            if (intent.type == "application/vnd.ms-excel") {
                filetype = "csv"
                mimetype = "text/csv"
            } else if (intent.type == "application/pdf") {
                filetype = "pdf"
                mimetype = "application/pdf"
            } else if (intent.type?.contains("image") == true) {
                filetype = "image"
                mimetype = intent.type!!
            }

            currentFile = CurrentUploadableFile(filetype, mimetype, file);


            // file.readBytes()
        }
    }

    fun btnUpload_callback() {
        // Upload file using Retrofit
        // todo upload to database -> need to figure out authentication
        if (currentFile == null) {
            Log.d(TAG, "Tried to upload empty file!");
            return;
        }


        readPatientId(this)?.let { patient_id ->

            val filePart = MultipartBody.Part.createFormData(
                currentFile!!.filetype,
                currentFile!!.file.name,
                currentFile!!.file.asRequestBody(currentFile!!.mimetype.toMediaTypeOrNull())
            )
            val patientIdPart = MultipartBody.Part.createFormData("patient_id", patient_id);
            val call = SleepEasyAPI.getService().uploadO2RingData(filePart, patientIdPart)
            val context = this;
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                    res.errorBody()?.let { errBody ->
                        val s = errBody.string();
                        if (res.code() == 409){
                            Toast.makeText(
                                context,
                                "Data upload rejected! (File already uploaded!)",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else{
                            Log.d(TAG, "Upload Error Response:\n" + s)
                            Toast.makeText(
                                context,
                                "Error while uploading data:\n" + s,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    finish()
                    Toast.makeText(
                        context,
                        "File / data uploaded successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onFailure(call: Call<ResponseBody>, err: Throwable) {
                    Toast.makeText(
                        context,
                        "Unexpected error while uploading data:\n" + err.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                    throw err
                }

            })

            return
        }
        Toast.makeText(this, "Patient Id not configured!", Toast.LENGTH_LONG).show()

    }

    override fun onSupportNavigateUp(): Boolean {
        finish();
        return false;
    }
}