package com.sleepeasycenter.o2ring_app.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.oxy.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.lepu.blepro.observer.BleChangeObserver
import com.sleepeasycenter.o2ring_app.MainActivity
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.R
import com.sleepeasycenter.o2ring_app.Status
import com.sleepeasycenter.o2ring_app.adapters.DeviceFileListViewAdapter
import com.sleepeasycenter.o2ring_app.databinding.FragmentDeviceSessionHistoryBinding
import kotlinx.coroutines.launch

class DeviceSessionHistory: Fragment(), BleChangeObserver, DeviceFileListViewAdapter.OnItemClickListener {

    public val TAG: String = "DeviceSessionHistory"
    private var _binding: FragmentDeviceSessionHistoryBinding? = null;
    private val binding get() = _binding!!;

    private var adapter = DeviceFileListViewAdapter(arrayListOf(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDeviceSessionHistoryBinding.inflate(inflater, container, false);
        val recyclerView = binding.fileListRecyclerView;
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setAdapter(adapter);
        // Inflate the layout for this fragment
        val view = binding.root;


        //OximetryDeviceController.instance.rtTask.run()

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


    private fun init() {

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

    }

    override fun onBleStateChanged(model: Int, state: Int) {
        TODO("Not yet implemented")
    }

    override fun onItemClick(filename: String) {
        Toast.makeText(requireContext(), "Clicked: $filename", Toast.LENGTH_SHORT).show()

        val action = SessionHistoryFragmentDirections
            .actionSessionHistoryToDetailedSessionFragment(filename)

        findNavController()?.navigate(action)
    }
}