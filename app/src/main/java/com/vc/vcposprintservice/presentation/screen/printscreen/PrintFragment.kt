package com.vc.vcposprintservice.presentation.screen.printscreen

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vc.vcposprintservice.R
import com.vc.vcposprintservice.adapter.UsbDeviceAdapter
import com.vc.vcposprintservice.databinding.FragmentPrintBinding
import com.vc.vcposprintservice.domain.model.PrinterDevice
import com.vc.vcposprintservice.presentation.common.ToolBarEnum
import com.vc.vcposprintservice.presentation.common.ToolBarSettings
import com.vc.vcposprintservice.presentation.main.MainActivity
import com.vc.vcposprintservice.presentation.main.Navigation
import com.vc.vcposprintservice.presentation.main.ShareViewModel
import com.vc.vcposprintservice.utils.collectLatestLifecycleFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PrintFragment : Fragment() {

    private val logger: Logger = LoggerFactory.getLogger(PrintFragment::class.java)
    private var _binding: FragmentPrintBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UsbDeviceAdapter
    private val viewModel: PrintViewModel by viewModels()
    private val shareViewModel: ShareViewModel by activityViewModels()
    private lateinit var toolBarSettings: ToolBarSettings

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolBarSettings = context as ToolBarSettings
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolBarSettings.setUpToolBar(title = "VC POS Print Service", enum = ToolBarEnum.PRINT_FRAGMENT)
        addMenu()
        checkIfAuthWasSaved()
        checkIfAllFieldsAreFilled()
        collectState()
    }

    private fun addMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.print_bar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_log -> {
                        shareViewModel.onEvent(event = Navigation.LoggerScreen)
                        true
                    }

                    else -> {
                        false
                    }
                }

        })
    }

    private fun checkIfAuthWasSaved() {
        viewModel.onEvent(event = PrintScreenEvent.CheckAuthWasSave)
    }

    private fun checkIfAllFieldsAreFilled() {
        binding.buttonStart.setOnClickListener {
            viewModel.onEvent(
                event = PrintScreenEvent.CheckAuthenticationForm(
                    login = binding.loginEditText.text.toString(),
                    password = binding.passwordEditText.text.toString()
                )
            )
        }
    }

    private fun collectState() {
        collectLatestLifecycleFlow(viewModel.state) { state ->
            binding.loginLayout.error = state.loginError
            binding.passwordLayout.error = state.passwordError
            if (state.login.isNotBlank() && state.password.isNotBlank()) {
                checkUsbDeviceList()
            }
            if (state.auth != null) {
                with(binding) {
                    loginEditText.setText(state.login)
                    passwordEditText.setText(state.password)
                }
            }
            if (state.isPrinterDeviceIsSave) {
                shareViewModel.onEvent(event = Navigation.StartPrintService)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun checkUsbDeviceList() {
        val manager = requireActivity().getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList
        showUsbDeviceListDialog(usbDeviceList = deviceList.values.toList())
    }

    private fun showUsbDeviceListDialog(usbDeviceList: List<UsbDevice>) {
        logger.info("Просмотр доступных USB устройств")
        val dialogView = layoutInflater.inflate(R.layout.dialog_usb_devices, binding.root)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = UsbDeviceAdapter { usbDevice ->
            saveUsbDevice(usbDevice = usbDevice)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        adapter.setData(usbDevices = usbDeviceList)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()
            .show()
    }

    private fun saveUsbDevice(usbDevice: UsbDevice) {
        viewModel.onEvent(
            event = PrintScreenEvent.SavePrinter(
                PrinterDevice(
                    id = 1,
                    deviceId = usbDevice.deviceId,
                    vendorId = usbDevice.vendorId,
                    productName = usbDevice.productName,
                    manufactureName = usbDevice.manufacturerName
                )
            )
        )
        logger.info("Сохранение данных об устройстве ${usbDevice.productName} в бд")
    }
}