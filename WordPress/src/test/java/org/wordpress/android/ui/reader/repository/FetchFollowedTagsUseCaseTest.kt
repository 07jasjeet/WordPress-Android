package org.wordpress.android.ui.reader.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.wordpress.android.BaseUnitTest
import org.wordpress.android.ui.reader.ReaderEvents.FollowedTagsChanged
import org.wordpress.android.ui.reader.repository.ReaderRepositoryCommunication.Error.NetworkUnavailable
import org.wordpress.android.ui.reader.repository.ReaderRepositoryCommunication.Error.RemoteRequestFailure
import org.wordpress.android.ui.reader.repository.ReaderRepositoryCommunication.Success
import org.wordpress.android.ui.reader.repository.usecases.tags.FetchFollowedTagsUseCase
import org.wordpress.android.ui.reader.services.update.ReaderUpdateLogic.UpdateTask.TAGS
import org.wordpress.android.ui.reader.services.update.wrapper.ReaderUpdateServiceStarterWrapper
import org.wordpress.android.util.EventBusWrapper
import org.wordpress.android.util.NetworkUtilsWrapper
import org.wordpress.android.viewmodel.ContextProvider
import java.util.EnumSet

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FetchFollowedTagsUseCaseTest : BaseUnitTest() {
    @Mock
    lateinit var contextProvider: ContextProvider

    @Mock
    lateinit var eventBusWrapper: EventBusWrapper

    @Mock
    lateinit var networkUtilsWrapper: NetworkUtilsWrapper

    @Mock
    lateinit var readerUpdateServiceStarterWrapper: ReaderUpdateServiceStarterWrapper

    private lateinit var useCase: FetchFollowedTagsUseCase

    @Before
    fun setUp() {
        useCase = FetchFollowedTagsUseCase(
            contextProvider,
            eventBusWrapper,
            networkUtilsWrapper,
            readerUpdateServiceStarterWrapper
        )
    }

    @Test
    fun `NetworkUnavailable returned when no network found`() = test {
        // Given
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(false)

        // When
        val result = useCase.fetch()

        // Then
        Assertions.assertThat(result).isEqualTo(NetworkUnavailable)
    }

    @Test
    fun `Success returned when FollowedTagsChanged event is posted with true`() = test {
        // Given
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(true)
        val event = FollowedTagsChanged(true)
        whenever(readerUpdateServiceStarterWrapper.startService(contextProvider.getContext(), EnumSet.of(TAGS)))
            .then { useCase.onFollowedTagsChanged(event) }

        // When
        val result = useCase.fetch()

        // Then
        Assertions.assertThat(result).isEqualTo(Success)
    }

    @Test
    fun `RemoteRequestFailure returned when FollowedTagsChanged event is posted with false`() = test {
        // Given
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(true)
        val event = FollowedTagsChanged(false)
        whenever(readerUpdateServiceStarterWrapper.startService(contextProvider.getContext(), EnumSet.of(TAGS)))
            .then { useCase.onFollowedTagsChanged(event) }

        // When
        val result = useCase.fetch()

        // Then
        Assertions.assertThat(result).isEqualTo(RemoteRequestFailure)
    }
}
