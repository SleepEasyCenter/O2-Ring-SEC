package com.sleepeasycenter.o2ring_app.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.R
import com.sleepeasycenter.o2ring_app.Status
import com.sleepeasycenter.o2ring_app.adapters.DeviceFileListViewAdapter
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeDashboardBinding
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [HomeDeviceDashboard.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeDeviceDashboard : Fragment() {

    public val TAG: String = "HomeDashboard"
    private var _binding: FragmentHomeDashboardBinding? = null;
    private val binding get() = _binding!!;

    private var adapter = DeviceFileListViewAdapter(arrayListOf())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false);
        val recyclerView = binding.fileListRecyclerView;
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setAdapter(adapter);
        // Inflate the layout for this fragment
        val view = binding.root;

        binding.btnUpload.isEnabled = false;
        binding.btnUpload.setOnClickListener {
            binding.btnUpload.isEnabled = false;
            requireActivity().lifecycleScope.launch {
                OximetryDeviceController.instance.uploadToServer(requireActivity())
            }
        }



        init()
        return view;
    }


    fun init() {
        OximetryDeviceController.instance.status.observe(
            viewLifecycleOwner,
            {
                when (it) {
                    Status.NEUTRAL -> binding.txtStatusText.setText("")
                    Status.DOWNLOADING -> binding.txtStatusText.setText("Downloading ${OximetryDeviceController.instance.filenames.value!!.size} files...")
                    Status.CONVERTING -> binding.txtStatusText.setText("Converting to CSV...")
                    Status.UPLOADING -> binding.txtStatusText.setText("Uploading...")
                }
                binding.btnUpload.isEnabled = it == Status.NEUTRAL;
            })
        OximetryDeviceController.instance.filenames.observe(viewLifecycleOwner, { newValue ->
            Log.d(TAG, "NEW VALUE: FILE COUNT: " + newValue.count())
            adapter.items = newValue.toCollection(ArrayList());
            adapter.notifyDataSetChanged()
        })

        OximetryDeviceController.instance.progress.observe(
            viewLifecycleOwner,
            { binding.barStatusProgress.progress = it })
        OximetryDeviceController.instance.progress_min.observe(
            viewLifecycleOwner,
            { binding.barStatusProgress.min = it })
        OximetryDeviceController.instance.progress_max.observe(
            viewLifecycleOwner,
            { binding.barStatusProgress.max = it })


//        OximetryDeviceController.instance.onReadFileProgress = { index, total ->
//            binding.txtStatusText.setText("Downloading files " + (index + 1) + "/$total...")
//            binding.barStatusProgress.min = 0;
//            binding.barStatusProgress.max = total;
//            binding.barStatusProgress.progress = index ;
//            // Don't allow file uploads when downloading files from ring.
//            binding.btnUpload.isEnabled = false;
//        }
//
//        OximetryDeviceController.instance.onFinishedReadingFiles = { oxyfiles ->
//            var fileCount = oxyfiles.size;
//            binding.btnUpload.isEnabled = true;
//
//            binding.txtStatusText.setText("Finished downloading $fileCount files.")
//            binding.barStatusProgress.progress =        binding.barStatusProgress.max ;
////            Log.d(TAG, "CSV CONVERTED:\n${convertToCsv(oxyfile)}")
////            oxyfiles[0].data[1].vector
//        }
    }


//     fun processAndUploadCsvFiles(){
//         CoroutineScope(MainScope()).launch {
//             var oxyfiles = OximetryDeviceController.instance.oxyfiles.value
//
//             var csvFiles: ArrayList<String> = arrayListOf();
//
//             for ((index, oxyFile) in oxyfiles!!.withIndex()) {
//                 binding.txtStatusText.setText("Converting to csv... (${index + 1} / ${oxyfiles.size})")
//                 binding.barStatusProgress.min = index;
//                 binding.barStatusProgress.max = oxyfiles.size;
//                 binding.barStatusProgress.progress = index;
//
//                 csvFiles += convertToCsv(oxyFile)
//                 binding.barStatusProgress.progress = index + 1;
//             }
//         }
//    }
}