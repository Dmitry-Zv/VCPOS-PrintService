package com.vc.vcposprintservice.presentation.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.vc.vcposprintservice.R
import com.vc.vcposprintservice.databinding.ActivityMainBinding
import com.vc.vcposprintservice.presentation.PrintService
import com.vc.vcposprintservice.presentation.common.ToolBarEnum
import com.vc.vcposprintservice.presentation.common.ToolBarSettings
import com.vc.vcposprintservice.presentation.screen.loggerscreen.LoggerFragment
import com.vc.vcposprintservice.presentation.screen.printscreen.PrintFragment
import com.vc.vcposprintservice.utils.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ToolBarSettings {

    private val logger: Logger = LoggerFactory.getLogger(MainActivity::class.java)
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ShareViewModel by viewModels()
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                logger.info("Разрешение POST_NOTIFICATIONS дано")
            } else {
                logger.info("Разрешение POST_NOTIFICATIONS отклоненно")
            }
            // if permission was denied, the service can still run only the notification won't be visible
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkAndRequestNotificationPermission()
        collectNavigation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.print_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_log -> {
                viewModel.onEvent(event = Navigation.LoggerScreen)
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }


    private fun collectNavigation() {
        collectLatestLifecycleFlow(viewModel.state) { navigation ->
            when (navigation) {
                Navigation.PrintScreen -> supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<PrintFragment>(R.id.fragment_container)
                }

                Navigation.StartPrintService -> startPrintService()
                Navigation.LoggerScreen -> supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<LoggerFragment>(R.id.fragment_container)
                }
            }

        }
    }

    private fun startPrintService() {
        val intent = Intent(this, PrintService::class.java).apply {
            action = PrintService.Action.START.toString()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
        logger.info("Запуск ${PrintService.Companion::class.java.name} сервиса")
        finish()
    }


    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )) {
                android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    logger.info("Разрешение POST_NOTIFICATIONS дано")
                }

                else -> {
                    logger.info("Получение разрешения POST_NOTIFICATION")
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun setUpToolBar(title: String, enum: ToolBarEnum) {
        binding.myToolbar.title = title
    }

}