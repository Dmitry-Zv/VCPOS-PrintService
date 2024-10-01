package com.vc.vcposprintservice.adapter

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.vc.vcposprintservice.R
import com.vc.vcposprintservice.databinding.ItemUsbDevicesBinding

class UsbDeviceAdapter(
    private val onItemClick: (UsbDevice) -> Unit
) : RecyclerView.Adapter<UsbDeviceAdapter.UsbDeviceViewHolder>() {

    inner class UsbDeviceViewHolder(private val binding: ItemUsbDevicesBinding) :
        ViewHolder(binding.root) {
        fun bind(usbDevice: UsbDevice) {
            with(binding) {
                when (usbDevice.getInterface(0).interfaceClass) {
                    UsbConstants.USB_CLASS_AUDIO -> usbClassImage.setImageResource(R.drawable.audio)
                    UsbConstants.USB_CLASS_HID -> usbClassImage.setImageResource(R.drawable.mouse_keyboard)
                    UsbConstants.USB_CLASS_PRINTER -> usbClassImage.setImageResource(R.drawable.ic_print_status_icon)
                    UsbConstants.USB_CLASS_PER_INTERFACE -> usbClassImage.setImageResource(R.drawable.ic_print_status_icon)
                    UsbConstants.USB_CLASS_MASS_STORAGE -> usbClassImage.setImageResource(R.drawable.usb_storage)
                    UsbConstants.USB_CLASS_COMM -> usbClassImage.setImageResource(R.drawable.scales)
                    else -> usbClassImage.setImageResource(R.drawable.usb_devices)
                }
                manufactureName.text = usbDevice.manufacturerName
                usbProductName.text = usbDevice.productName
                itemView.setOnClickListener {
                    onItemClick(usbDevice)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsbDeviceViewHolder {
        val binding =
            ItemUsbDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsbDeviceViewHolder(binding)
    }

    override fun getItemCount(): Int =
        listDiffer.currentList.size

    override fun onBindViewHolder(holder: UsbDeviceViewHolder, position: Int) {
        val usbDevice = listDiffer.currentList[position]
        holder.bind(usbDevice = usbDevice)
    }

    private val callback = object : DiffUtil.ItemCallback<UsbDevice>() {
        override fun areItemsTheSame(oldItem: UsbDevice, newItem: UsbDevice): Boolean =
            oldItem.deviceId == newItem.deviceId

        override fun areContentsTheSame(oldItem: UsbDevice, newItem: UsbDevice): Boolean =
            oldItem == newItem
    }

    private val listDiffer = AsyncListDiffer(this, callback)

    fun setData(usbDevices: List<UsbDevice>) {
        listDiffer.submitList(usbDevices)
    }

}