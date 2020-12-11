package org.wordpress.android.ui.jetpack.backup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.backup_download_activity.*
import org.wordpress.android.R
import org.wordpress.android.WordPress
import org.wordpress.android.ui.LocaleAwareActivity
import org.wordpress.android.ui.jetpack.backup.BackupDownloadStep.COMPLETE
import org.wordpress.android.ui.jetpack.backup.BackupDownloadStep.DETAILS
import org.wordpress.android.ui.jetpack.backup.BackupDownloadStep.PROGRESS
import org.wordpress.android.ui.jetpack.backup.BackupDownloadViewModel.BackupDownloadWizardState.BackupDownloadCanceled
import org.wordpress.android.ui.jetpack.backup.BackupDownloadViewModel.BackupDownloadWizardState.BackupDownloadCompleted
import org.wordpress.android.ui.jetpack.backup.BackupDownloadViewModel.BackupDownloadWizardState.BackupDownloadInProgress
import org.wordpress.android.ui.jetpack.backup.details.BackupDownloadDetailsFragment
import org.wordpress.android.ui.utils.UiHelpers
import org.wordpress.android.util.wizard.WizardNavigationTarget
import javax.inject.Inject

class BackupDownloadActivity : LocaleAwareActivity() {
    // todo: annmarie add listeners if needed
    // todo: annmarie get the values from the bundle for site & activityId
    @Inject internal lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject internal lateinit var uiHelpers: UiHelpers
    private lateinit var viewModel: BackupDownloadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WordPress).component().inject(this)
        setContentView(R.layout.backup_download_activity)

        setSupportActionBar(toolbar_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViewModel(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.writeToBundle(outState)
    }

    private fun initViewModel(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(BackupDownloadViewModel::class.java)

        viewModel.navigationTargetObservable
                .observe(this, { target ->
                    target?.let {
                        showStep(target)
                    }
                })

        viewModel.screenTitleObservable.observe(this, { title ->
            supportActionBar?.title = getString(title)
            // Change the activity title for accessibility purposes
            this.title = getString(title)
        })

        // todo: annmarie - may there are more states?
        // Canceled, Running, Complete -> (Running = kick off status)
        viewModel.wizardFinishedObservable.observe(this, { backupDownloadWizardState ->
            backupDownloadWizardState?.let {
                val intent = Intent()
                val (backupDownloadCreated, activityId) = when (backupDownloadWizardState) {
                    // teh request was canceled
                    is BackupDownloadCanceled -> Pair(false, null)
                    is BackupDownloadInProgress -> {
                        // The request is in progress and user didn't want to wait around
                        Pair(true, backupDownloadWizardState.activityId)
                    }
                    is BackupDownloadCompleted -> Pair(true, backupDownloadWizardState.activityId)
                }
                // todo: annmarie what information do I need to send back - just to kick off status
                // intent.putExtra(SOME_KEY_THAT_DESCRIBES_THE_ID, activityId )
                setResult(if (backupDownloadCreated) RESULT_OK else RESULT_CANCELED, intent)
                finish()
            }
        })

        viewModel.exitFlowObservable.observe(this, {
            setResult(Activity.RESULT_CANCELED)
            finish()
        })
        viewModel.onBackPressedObservable.observe(this, {
            super.onBackPressed()
        })
        viewModel.start(savedInstanceState)
    }

    private fun showStep(target: WizardNavigationTarget<BackupDownloadStep, BackupDownloadState>) {
        val fragment = when (target.wizardStep) {
            DETAILS -> BackupDownloadDetailsFragment.newInstance()
            PROGRESS -> BackupDownloadDetailsFragment.newInstance()
            COMPLETE -> BackupDownloadDetailsFragment.newInstance()
        }
        slideInFragment(fragment, target.wizardStep.toString())
    }

    private fun slideInFragment(fragment: Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) != null) {
            transaction.addToBackStack(null).setCustomAnimations(
                    R.anim.activity_slide_in_from_right, R.anim.activity_slide_out_to_left,
                    R.anim.activity_slide_in_from_left, R.anim.activity_slide_out_to_right
            )
        }
        transaction.replace(R.id.fragment_container, fragment, tag)
        transaction.commit()
    }
}
