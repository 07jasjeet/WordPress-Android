package org.wordpress.android.ui.mysite

import androidx.lifecycle.MutableLiveData
import org.wordpress.android.analytics.AnalyticsTracker
import org.wordpress.android.ui.blaze.BlazeFeatureUtils
import org.wordpress.android.ui.blaze.BlazeFlowSource
import org.wordpress.android.viewmodel.Event
import org.wordpress.android.ui.mysite.MySiteUiState.PartialState.BlazeCardUpdate
import org.wordpress.android.ui.mysite.MySiteCardAndItemBuilderParams.BlazeCardBuilderParams.PromoteWithBlazeCardBuilderParams
import org.wordpress.android.ui.mysite.MySiteCardAndItemBuilderParams.BlazeCardBuilderParams.CampaignWithBlazeCardBuilderParams
import org.wordpress.android.ui.mysite.MySiteCardAndItemBuilderParams.BlazeCardBuilderParams
import javax.inject.Inject

class BlazeCardViewModelSlice @Inject constructor(
    private val blazeFeatureUtils: BlazeFeatureUtils,
    private val selectedSiteRepository: SelectedSiteRepository
) {
    private val _onNavigation = MutableLiveData<Event<SiteNavigationAction>>()
    val onNavigation = _onNavigation

    private val _refresh = MutableLiveData<Event<Boolean>>()
    val refresh = _refresh

    fun isSiteBlazeEligible() = blazeFeatureUtils.isSiteBlazeEligible(selectedSiteRepository.getSelectedSite()!!)

    fun getBlazeCardBuilderParams(blazeCardUpdate: BlazeCardUpdate?): BlazeCardBuilderParams? {
        return blazeCardUpdate?.let {
            if (it.blazeEligible) {
                it.campaign?.let { campaign ->
                    CampaignWithBlazeCardBuilderParams(
                        campaign = campaign,
                        onCreateCampaignClick = this::onCreateCampaignClick,
                        onCampaignClick = this::onCampaignClick,
                        onCardClick = this::onCampaignsCardClick,
                    )
                } ?: PromoteWithBlazeCardBuilderParams(
                    onClick = this::onPromoteWithBlazeCardClick,
                    onHideMenuItemClick = this::onPromoteWithBlazeCardHideMenuItemClick,
                    onMoreMenuClick = this::onPromoteWithBlazeCardMoreMenuClick
                )
            } else null
        }
    }

    private fun onCampaignsCardClick() {
        TODO("Not yet implemented")
    }

    private fun onCampaignClick() {
        TODO("Not yet implemented")
    }

    private fun onCreateCampaignClick() {
        blazeFeatureUtils.track(
            AnalyticsTracker.Stat.BLAZE_ENTRY_POINT_TAPPED,
            BlazeFlowSource.DASHBOARD_CARD
        )
        // todo: Add navigation to create campaign
    }

    fun onBlazeMenuItemClick(): SiteNavigationAction {
        blazeFeatureUtils.trackEntryPointTapped(BlazeFlowSource.MENU_ITEM)
        return SiteNavigationAction.OpenPromoteWithBlazeOverlay(BlazeFlowSource.MENU_ITEM)
    }

    private fun onPromoteWithBlazeCardMoreMenuClick() {
        blazeFeatureUtils.track(
            AnalyticsTracker.Stat.BLAZE_ENTRY_POINT_MENU_ACCESSED,
            BlazeFlowSource.DASHBOARD_CARD
        )
    }

    private fun onPromoteWithBlazeCardClick() {
        blazeFeatureUtils.trackEntryPointTapped(BlazeFlowSource.DASHBOARD_CARD)
        _onNavigation.value =
            Event(SiteNavigationAction.OpenPromoteWithBlazeOverlay(source = BlazeFlowSource.DASHBOARD_CARD))
    }

    private fun onPromoteWithBlazeCardHideMenuItemClick() {
        blazeFeatureUtils.track(
            AnalyticsTracker.Stat.BLAZE_ENTRY_POINT_HIDE_TAPPED,
            BlazeFlowSource.DASHBOARD_CARD
        )
        selectedSiteRepository.getSelectedSite()?.let {
            blazeFeatureUtils.hidePromoteWithBlazeCard(it.siteId)
        }
        _refresh.value = Event(true)
    }
}