package com.vc.vcposprintservice.presentation.screen.loggerscreen

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.vc.vcposprintservice.R
import com.vc.vcposprintservice.databinding.FragmentLoggerBinding
import com.vc.vcposprintservice.presentation.common.ToolBarEnum
import com.vc.vcposprintservice.presentation.common.ToolBarSettings
import com.vc.vcposprintservice.utils.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class LoggerFragment : Fragment() {

    private var _binding: FragmentLoggerBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: DatePickerDialog
    private val viewModel: LoggerViewModel by viewModels()
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
        _binding = FragmentLoggerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolBarSettings.setUpToolBar(title = "Logger", ToolBarEnum.LOGGER_FRAGMENT)
        dialog = DatePickerDialog(requireContext())
        dialog.setCancelable(false)
        addMenu()
        onPositiveButtonDatePickerClicked()
        collectLoggerState()
        readFromFile(formattedDate = null)
    }

    private fun addMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.logger_bar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.action_calendar -> {
                        dialog.show()
                        true
                    }

                    else -> {
                        false
                    }
                }

        }, viewLifecycleOwner)
    }

    private fun onPositiveButtonDatePickerClicked() {
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Принять") { dialogInterface, i ->
            viewModel.onEvent(event = LoggerEvent.OnDatePicked(datePicker = dialog.datePicker))
            dialogInterface.dismiss()
        }
    }

    private fun collectLoggerState() {
        collectLatestLifecycleFlow(viewModel.state) { state ->
            readFromFile(formattedDate = state.date)
        }
    }

    private fun readFromFile(formattedDate: String?) {
        lifecycleScope.launch {
            val logContent = withContext(Dispatchers.IO) {
                try {
                    val fileName = if (formattedDate != null) {
                        "log.$formattedDate.txt"
                    } else {
                        "log.txt"
                    }
                    val filePath = "${requireContext().filesDir.path}/logs/$fileName"
                    FileInputStream(filePath).use { inputStream ->
                        InputStreamReader(inputStream).use { isr ->
                            BufferedReader(isr).use { br ->
                                val sb = StringBuilder()
                                var line: String?
                                while (br.readLine().also { line = it } != null) {
                                    sb.insert(0, line + "\n")
                                }
                                sb.toString()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.message ?: "An error occurred"
                }
            }
            with(binding) {
                logTextView.text = logContent
                scrollView.scrollTo(0, 0)
            }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = if (formattedDate != null) {
                formattedDate
            } else {
                sdf.format(Date())
            }
            toolBarSettings.setUpToolBar(title = "Date: $date", enum = ToolBarEnum.LOGGER_FRAGMENT)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}