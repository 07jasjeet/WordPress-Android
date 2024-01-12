package org.wordpress.android.ui.prefs.notifications

import android.content.Intent
import org.wordpress.android.analytics.AnalyticsTracker
import org.wordpress.android.datasets.ReaderBlogTable
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.generated.AccountActionBuilder
import org.wordpress.android.fluxc.store.AccountStore.AddOrDeleteSubscriptionPayload
import org.wordpress.android.fluxc.store.AccountStore.UpdateSubscriptionPayload

/** Any notification preference fragment that deals with **My Sites** or **My Followed Sites**
 * should implement this interface.*/
interface NotificationsMySitesSettingsFragment {
    var mNotificationUpdatedSite: String?
    var mPreviousEmailComments: Boolean
    var mPreviousEmailPosts: Boolean
    var mPreviousNotifyPosts: Boolean
    var mUpdateEmailPostsFirst: Boolean
    var mPreviousEmailPostsFrequency: String?
    var mUpdateSubscriptionFrequencyPayload: UpdateSubscriptionPayload?
    var mDispatcher: Dispatcher

    fun onMySiteSettingsChanged(data: Intent?) {
        if (data == null)
            return

        val notifyPosts = data.getBooleanExtra(NotificationSettingsFollowedDialog.KEY_NOTIFICATION_POSTS, false)
        val emailPosts = data.getBooleanExtra(NotificationSettingsFollowedDialog.KEY_EMAIL_POSTS, false)
        val emailPostsFrequency = data.getStringExtra(NotificationSettingsFollowedDialog.KEY_EMAIL_POSTS_FREQUENCY)
        val emailComments = data.getBooleanExtra(NotificationSettingsFollowedDialog.KEY_EMAIL_COMMENTS, false)
        if (notifyPosts != mPreviousNotifyPosts) {
            ReaderBlogTable.setNotificationsEnabledByBlogId(mNotificationUpdatedSite!!.toLong(), notifyPosts)
            val payload: AddOrDeleteSubscriptionPayload = if (notifyPosts) {
                AnalyticsTracker.track(AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_SETTINGS_ON)
                AddOrDeleteSubscriptionPayload(
                    mNotificationUpdatedSite!!,
                    AddOrDeleteSubscriptionPayload.SubscriptionAction.NEW
                )
            } else {
                AnalyticsTracker.track(AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_SETTINGS_OFF)
                AddOrDeleteSubscriptionPayload(
                    mNotificationUpdatedSite!!,
                    AddOrDeleteSubscriptionPayload.SubscriptionAction.DELETE
                )
            }
            mDispatcher.dispatch(AccountActionBuilder.newUpdateSubscriptionNotificationPostAction(payload))
        }
        if (emailPosts != mPreviousEmailPosts) {
            val payload: AddOrDeleteSubscriptionPayload = if (emailPosts) {
                AnalyticsTracker.track(AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_SETTINGS_EMAIL_ON)
                AddOrDeleteSubscriptionPayload(
                    mNotificationUpdatedSite!!,
                    AddOrDeleteSubscriptionPayload.SubscriptionAction.NEW
                )
            } else {
                AnalyticsTracker.track(AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_SETTINGS_EMAIL_OFF)
                AddOrDeleteSubscriptionPayload(
                    mNotificationUpdatedSite!!,
                    AddOrDeleteSubscriptionPayload.SubscriptionAction.DELETE
                )
            }
            mDispatcher.dispatch(AccountActionBuilder.newUpdateSubscriptionEmailPostAction(payload))
        }
        if (emailPostsFrequency != null && !emailPostsFrequency.equals(
                mPreviousEmailPostsFrequency,
                ignoreCase = true
            )
        ) {
            val subscriptionFrequency = getSubscriptionFrequencyFromString(emailPostsFrequency)
            mUpdateSubscriptionFrequencyPayload = UpdateSubscriptionPayload(
                mNotificationUpdatedSite!!,
                subscriptionFrequency
            )
            /*
             * The email post frequency update will be overridden by the email post update if the email post
             * frequency callback returns first.  Thus, the updates must be dispatched sequentially when the
             * email post update is switched from disabled to enabled.
             */
            if (emailPosts != mPreviousEmailPosts && emailPosts) {
                mUpdateEmailPostsFirst = true
            } else {
                mDispatcher.dispatch(
                    AccountActionBuilder.newUpdateSubscriptionEmailPostFrequencyAction(
                        mUpdateSubscriptionFrequencyPayload
                    )
                )
            }
        }
        if (emailComments != mPreviousEmailComments) {
            val payload: AddOrDeleteSubscriptionPayload = if (emailComments) {
                AnalyticsTracker.track(AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_SETTINGS_COMMENTS_ON)
                AddOrDeleteSubscriptionPayload(
                    mNotificationUpdatedSite!!,
                    AddOrDeleteSubscriptionPayload.SubscriptionAction.NEW
                )
            } else {
                AnalyticsTracker.track(AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_SETTINGS_COMMENTS_OFF)
                AddOrDeleteSubscriptionPayload(
                    mNotificationUpdatedSite!!,
                    AddOrDeleteSubscriptionPayload.SubscriptionAction.DELETE
                )
            }
            mDispatcher.dispatch(AccountActionBuilder.newUpdateSubscriptionEmailCommentAction(payload))
        }
    }

    fun getSubscriptionFrequencyFromString(s: String): UpdateSubscriptionPayload.SubscriptionFrequency {
        return if (s.equals(UpdateSubscriptionPayload.SubscriptionFrequency.DAILY.toString(), ignoreCase = true)) {
            AnalyticsTracker.track(AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_SETTINGS_EMAIL_DAILY)
            UpdateSubscriptionPayload.SubscriptionFrequency.DAILY
        } else if (s.equals(UpdateSubscriptionPayload.SubscriptionFrequency.WEEKLY.toString(), ignoreCase = true)) {
            AnalyticsTracker.track(AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_SETTINGS_EMAIL_WEEKLY)
            UpdateSubscriptionPayload.SubscriptionFrequency.WEEKLY
        } else {
            AnalyticsTracker.track(AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_SETTINGS_EMAIL_INSTANTLY)
            UpdateSubscriptionPayload.SubscriptionFrequency.INSTANTLY
        }
    }
}
