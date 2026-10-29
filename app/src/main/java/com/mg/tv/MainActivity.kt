package com.mg.tv

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.mg.tv.adapter.AppCardAdapter
import com.mg.tv.data.AppRepository
import com.mg.tv.data.BrandColors
import com.mg.tv.data.FavoritesManager
import com.mg.tv.databinding.ActivityMainBinding
import com.mg.tv.model.AppInfo
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var appRepository: AppRepository
    private lateinit var appAdapter: AppCardAdapter

    private var currentHeroPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favoritesManager = FavoritesManager(this)
        appRepository = AppRepository(this, favoritesManager)

        setupHeader()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
        updateWifiName()
        loadApps()
    }

    // ────────────────────────────────────────────────────────────
    //  Header: pill buttons with focus animation
    // ────────────────────────────────────────────────────────────

    private fun setupHeader() {
        val pillViews = listOf(binding.btnInputs, binding.btnWifi, binding.btnSettings)

        for (pill in pillViews) {
            pill.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.animate().scaleX(1.08f).scaleY(1.08f).setDuration(120).start()
                    view.elevation = 6f
                } else {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                    view.elevation = 0f
                }
            }
        }

        binding.btnInputs.setOnClickListener { openTvInputs() }
        binding.btnWifi.setOnClickListener { openWifiSettings() }
        binding.btnSettings.setOnClickListener { openTvSettings() }
    }

    private fun openTvInputs() {
        // 1. Try system VIEW_INPUTS if available
        try {
            val intent = Intent("com.android.tv.action.VIEW_INPUTS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (packageManager.resolveActivity(intent, 0) != null) {
                startActivity(intent)
                return
            }
        } catch (_: Exception) { }

        // 2. Show native TV Inputs selection dialog
        showInputsDialog()
    }

    private fun showInputsDialog() {
        val inputItems = ArrayList<Pair<String, String>>() // Pair<DisplayLabel, InputId>

        try {
            val tvInputManager = getSystemService(android.content.Context.TV_INPUT_SERVICE) as? android.media.tv.TvInputManager
            val inputs = tvInputManager?.tvInputList ?: emptyList()
            for (info in inputs) {
                if (info.isPassthroughInput) {
                    val label = info.loadLabel(this)?.toString() ?: when {
                        info.id.contains("Hdmi1", ignoreCase = true) -> "HDMI 1"
                        info.id.contains("Hdmi2", ignoreCase = true) -> "HDMI 2"
                        info.id.contains("Hdmi3", ignoreCase = true) -> "HDMI 3"
                        info.id.contains("AV", ignoreCase = true) -> "AV"
                        else -> info.id.substringAfterLast('/')
                    }
                    inputItems.add(Pair(label, info.id))
                }
            }
        } catch (_: Exception) { }

        // Fallback to known hardware inputs on Mi TV 4A
        if (inputItems.isEmpty()) {
            inputItems.add(Pair("HDMI 1", "com.droidlogic.tvinput/.services.Hdmi1InputService/HW5"))
            inputItems.add(Pair("HDMI 2", "com.droidlogic.tvinput/.services.Hdmi2InputService/HW6"))
            inputItems.add(Pair("HDMI 3", "com.droidlogic.tvinput/.services.Hdmi3InputService/HW7"))
            inputItems.add(Pair("AV", "com.droidlogic.tvinput/.services.AV1InputService/HW1"))
        }

        val labels = inputItems.map { it.first }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_MiCleanLauncher_Dialog)
            .setTitle(R.string.inputs)
            .setItems(labels) { _, which ->
                val inputId = inputItems[which].second
                tuneToInput(inputId)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun tuneToInput(inputId: String) {
        try {
            val uri = android.media.tv.TvContract.buildChannelUriForPassthroughInput(inputId)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not tune to input: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            openTvSettings()
        }
    }

    private fun openTvSettings() {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Greeting & WiFi Name
    // ────────────────────────────────────────────────────────────

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greetingRes = when (hour) {
            in 5..11 -> R.string.greeting_morning
            in 12..16 -> R.string.greeting_afternoon
            in 17..21 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
        binding.tvGreeting.setText(greetingRes)
    }

    @Suppress("DEPRECATION")
    private fun updateWifiName() {
        try {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as? WifiManager
            val ssid = wifiManager?.connectionInfo?.ssid?.replace("\"", "")
            if (!ssid.isNullOrEmpty() && ssid != "<unknown ssid>") {
                binding.tvWifiName.text = ssid
            } else {
                binding.tvWifiName.text = getString(R.string.pill_wifi)
            }
        } catch (e: Exception) {
            binding.tvWifiName.text = getString(R.string.pill_wifi)
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Hero Ambient Spotlight Gradient (Apple TV style radial glow)
    // ────────────────────────────────────────────────────────────

    private fun updateHeroBanner(app: AppInfo) {
        // Update hero app label
        binding.tvHeroAppName.text = app.label

        if (app.packageName == currentHeroPackage) return
        currentHeroPackage = app.packageName

        // Render ambient radial spotlight glow
        val displayWidth = resources.displayMetrics.widthPixels.toFloat()
        val ambientDrawable = BrandColors.createRadialDrawable(app.packageName, displayWidth)

        binding.heroBackground.animate()
            .alpha(0.2f)
            .setDuration(120)
            .withEndAction {
                binding.heroBackground.background = ambientDrawable
                binding.heroBackground.animate()
                    .alpha(1.0f)
                    .setDuration(220)
                    .start()
            }
            .start()
    }

    // ────────────────────────────────────────────────────────────
    //  RecyclerView Setup: 4-Column Continuous Vertical Grid
    // ────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        appAdapter = AppCardAdapter(
            context = this,
            onAppFocused = { app -> updateHeroBanner(app) },
            onFavoriteToggled = { app ->
                val isNowFav = favoritesManager.toggleFavorite(app.packageName)
                val msg = if (isNowFav) getString(R.string.added_to_favorites) else getString(R.string.removed_from_favorites)
                Toast.makeText(this, "${app.label}: $msg", Toast.LENGTH_SHORT).show()
                loadApps()
            }
        )

        binding.rvAppsGrid.layoutManager = GridLayoutManager(this, 4)
        binding.rvAppsGrid.adapter = appAdapter
    }

    // ────────────────────────────────────────────────────────────
    //  Load Apps & Set Initial Focus
    // ────────────────────────────────────────────────────────────

    private fun loadApps() {
        val apps = appRepository.getInstalledApps()
        appAdapter.submitList(apps)

        if (apps.isNotEmpty()) {
            // Set initial hero label & background to first app (Netflix)
            updateHeroBanner(apps[0])

            // Focus first card on startup
            binding.rvAppsGrid.postDelayed({
                val firstCard = binding.rvAppsGrid.layoutManager?.findViewByPosition(0)
                val target = firstCard?.findViewById<View>(R.id.cardContainer) ?: firstCard
                target?.requestFocus()
            }, 100)
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Remote D-pad / Back Navigation
    // ────────────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // Back scrolls smoothly to top and focuses first card
        binding.rootScrollView.smoothScrollTo(0, 0)
        val firstCard = binding.rvAppsGrid.layoutManager?.findViewByPosition(0)
        val target = firstCard?.findViewById<View>(R.id.cardContainer) ?: firstCard
        target?.requestFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            @Suppress("DEPRECATION")
            onBackPressed()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
