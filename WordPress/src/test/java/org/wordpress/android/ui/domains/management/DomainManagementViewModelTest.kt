package org.wordpress.android.ui.domains.management

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.analytics.AnalyticsTracker.Stat.DOMAIN_MANAGEMENT_MY_DOMAINS_SCREEN_DOMAIN_TAPPED
import org.wordpress.android.analytics.AnalyticsTracker.Stat.DOMAIN_MANAGEMENT_MY_DOMAINS_SCREEN_SHOWN
import org.wordpress.android.ui.domains.management.DomainManagementViewModel.ActionEvent
import org.wordpress.android.ui.domains.usecases.AllDomains
import org.wordpress.android.ui.domains.usecases.FetchAllDomainsUseCase
import org.wordpress.android.util.analytics.AnalyticsTrackerWrapper

@ExperimentalCoroutinesApi
class DomainManagementViewModelTest : BaseUnitTest() {
    @Mock
    private lateinit var analyticsTracker: AnalyticsTrackerWrapper

    @Mock
    lateinit var useCase: FetchAllDomainsUseCase

    private lateinit var viewModel: DomainManagementViewModel

    @Before
    fun setUp() = test {
        MockitoAnnotations.openMocks(this)
        whenever(useCase.execute()).thenReturn(AllDomains.Empty)
        viewModel = DomainManagementViewModel(testDispatcher(), analyticsTracker, useCase)
    }

    @Test
    fun `WHEN ViewModel initialized THEN track DOMAIN_MANAGEMENT_MY_DOMAINS_SCREEN_SHOWN event`() {
        verify(analyticsTracker).track(DOMAIN_MANAGEMENT_MY_DOMAINS_SCREEN_SHOWN)
    }

    @Test
    fun `WHEN a domain is tapped THEN track DOMAIN_MANAGEMENT_MY_DOMAINS_SCREEN_DOMAIN_TAPPED event`() {
        viewModel.onDomainTapped(testDomain)
        verify(analyticsTracker).track(DOMAIN_MANAGEMENT_MY_DOMAINS_SCREEN_DOMAIN_TAPPED)
    }

    @Test
    fun `WHEN a domain is tapped THEN send DomainTapped action event`() = testWithActionEvents { events ->
        viewModel.onDomainTapped(testDomain)
        advanceUntilIdle()
        assertThat(events.last()).isEqualTo(ActionEvent.DomainTapped(testDomain))
    }

    @Test
    fun `WHEN a navigation back button is tapped THEN send NavigateBackTapped action event`() =
        testWithActionEvents { events ->
            viewModel.onBackTapped()
            advanceUntilIdle()
            assertThat(events.last()).isEqualTo(ActionEvent.NavigateBackTapped)
        }

    private fun testWithActionEvents(block: suspend TestScope.(events: List<ActionEvent>) -> Unit) =
        test {
            val actionEvents = mutableListOf<ActionEvent>()
            val job = launch { viewModel.actionEvents.toList(actionEvents) }

            block(actionEvents)

            job.cancel()
        }

    companion object {
        private const val testDomain = "domain"
    }
}
