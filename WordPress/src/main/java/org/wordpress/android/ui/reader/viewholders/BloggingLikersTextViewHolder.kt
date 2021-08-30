package org.wordpress.android.ui.reader.viewholders

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.UnderlineSpan
import android.view.ViewGroup
import org.wordpress.android.R
import org.wordpress.android.databinding.BloggerLikersTextItemBinding
import org.wordpress.android.ui.reader.adapters.FACE_ITEM_AVATAR_SIZE_DIMEN
import org.wordpress.android.ui.reader.adapters.FACE_ITEM_LEFT_OFFSET_DIMEN
import org.wordpress.android.ui.reader.adapters.TrainOfFacesItem.BloggersLikingTextItem
import org.wordpress.android.util.DisplayUtils
import org.wordpress.android.util.getColorFromAttribute
import org.wordpress.android.util.viewBinding

class BloggingLikersTextViewHolder(
    parent: ViewGroup,
    private val context: Context
) : TrainOfFacesViewHolder<BloggerLikersTextItemBinding>(parent.viewBinding(BloggerLikersTextItemBinding::inflate)) {
    fun bind(bloggersTextItem: BloggersLikingTextItem) = with(binding) {
        val position = adapterPosition

        if (position >= 0) {
            val displayWidth = DisplayUtils.getDisplayPixelWidth(context)
            val paddingWidth = 2 * context.resources.getDimensionPixelSize(R.dimen.reader_detail_margin)
            val avatarSize = context.resources.getDimensionPixelSize(FACE_ITEM_AVATAR_SIZE_DIMEN)
            val leftOffest = context.resources.getDimensionPixelSize(FACE_ITEM_LEFT_OFFSET_DIMEN)
            val facesWidth = position * avatarSize - (position - 1).coerceAtLeast(0) * leftOffest

            itemView.layoutParams.width = displayWidth - paddingWidth - facesWidth
        }

        numBloggers.text = with(bloggersTextItem) {
            SpannableString(text).formatWithSpan(itemView.context, text, closure)
        }
    }

    private fun SpannableString.formatWithSpan(context: Context, text: String, closure: String): Spannable {
        val start = 0
        val end = text.lastIndexOf(closure) - 1
        return if (end <= start || end >= text.length - 1) {
            this
        } else {
            this.apply {
                setSpan(
                        object : UnderlineSpan() {
                            override fun updateDrawState(ds: TextPaint) {
                                ds.isUnderlineText = true
                                ds.color = context.getColorFromAttribute(R.attr.colorPrimary)
                            }
                        },
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}
