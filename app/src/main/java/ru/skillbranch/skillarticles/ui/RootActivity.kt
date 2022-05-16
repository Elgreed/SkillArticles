package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.core.view.forEach
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.viewmodels.*

class RootActivity : AppCompatActivity() {
    private val viewModel: RootViewModel by viewModels()
    lateinit var viewBinding: ActivityRootBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.toolbar)

        // setup nav controller
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        // setup action bar navigation
        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.nav_articles,
                R.id.nav_bookmarks,
                R.id.nav_transcriptions,
                R.id.nav_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfig)
        viewBinding.navView.setOnItemSelectedListener {
            viewModel.topLevelNavigate(it.itemId)
            true
        }

        viewModel.observeNavigation(this, ::handleNavigation)

        navController.addOnDestinationChangedListener { _, destination, args ->
            Log.e("RootActivity", "change destination $destination")
            viewBinding.navView.menu.forEach { item ->
                if (destination.matchDestination(item.itemId)) {
                    item.isChecked = true
                }

                // implement select profile icon if auth flow is open
            }
        }
    }

    // for navigate on pres back arrow in action bar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun renderNotification(notify: Notify) {
        val snackbar =
            Snackbar.make(viewBinding.coordinatorContainer, notify.message, Snackbar.LENGTH_LONG)
                .setAnchorView(findViewById(R.id.bottombar) ?: viewBinding.navView)

        when (notify) {
            is Notify.ActionMessage -> {
                val (_, label, handler) = notify

                with(snackbar) {
                    setActionTextColor(getColor(R.color.color_accent_dark))
                    setAction(label) { handler.invoke() }
                }
            }

            is Notify.ErrorMessage -> {
                val (_, label, handler) = notify

                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    handler ?: return@with
                    setAction(label) { handler.invoke() }
                }
            }

            else -> {}
        }

        snackbar.show()
    }

    fun handleNavigation(cmd: NavCommand) {
        when (cmd) {
            is NavCommand.Action -> navController.navigate(cmd.action)
            is NavCommand.Builder -> navController.navigate(
                cmd.destination,
                cmd.args,
                cmd.options,
                cmd.extras
            )
            is NavCommand.TopLevel -> {
                val popBackStack = navController.popBackStack(cmd.destination, false)
                if (!popBackStack) navController.navigate(cmd.destination, null, cmd.options)
            }
        }
    }

    private fun NavDestination.matchDestination(@IdRes resId: Int) =
        hierarchy.any { it.id == resId }
}
