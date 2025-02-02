package org.thoughtcrime.securesms.badges.gifts.viewgift.sent

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveDataReactiveStreams
import org.signal.core.util.DimensionUnit
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.badges.gifts.viewgift.ViewGiftRepository
import org.thoughtcrime.securesms.badges.models.BadgeDisplay112
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsAdapter
import org.thoughtcrime.securesms.components.settings.DSLSettingsBottomSheetFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.database.model.MmsMessageRecord
import org.thoughtcrime.securesms.database.model.databaseprotos.GiftBadge
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.util.BottomSheetUtil

/**
 * Handles all interactions for received gift badges.
 */
class ViewSentGiftBottomSheet : DSLSettingsBottomSheetFragment() {

  companion object {
    private const val ARG_GIFT_BADGE = "arg.gift.badge"
    private const val ARG_SENT_TO = "arg.sent.to"

    @JvmStatic
    fun show(fragmentManager: FragmentManager, messageRecord: MmsMessageRecord) {
      ViewSentGiftBottomSheet().apply {
        arguments = Bundle().apply {
          putParcelable(ARG_SENT_TO, messageRecord.recipient.id)
          putByteArray(ARG_GIFT_BADGE, messageRecord.giftBadge!!.toByteArray())
        }
        show(fragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
      }
    }
  }

  private val sentTo: RecipientId
    get() = requireArguments().getParcelable(ARG_SENT_TO)!!

  private val giftBadge: GiftBadge
    get() = GiftBadge.parseFrom(requireArguments().getByteArray(ARG_GIFT_BADGE))

  private val viewModel: ViewSentGiftViewModel by viewModels(
    factoryProducer = { ViewSentGiftViewModel.Factory(sentTo, giftBadge, ViewGiftRepository()) }
  )

  override fun bindAdapter(adapter: DSLSettingsAdapter) {
    BadgeDisplay112.register(adapter)

    LiveDataReactiveStreams.fromPublisher(viewModel.state).observe(viewLifecycleOwner) { state ->
      adapter.submitList(getConfiguration(state).toMappingModelList())
    }
  }

  private fun getConfiguration(state: ViewSentGiftState): DSLConfiguration {
    return configure {
      noPadTextPref(
        title = DSLSettingsText.from(
          stringId = R.string.ViewSentGiftBottomSheet__thanks_for_your_support,
          DSLSettingsText.CenterModifier, DSLSettingsText.Title2BoldModifier
        )
      )

      space(DimensionUnit.DP.toPixels(8f).toInt())

      if (state.recipient != null) {
        noPadTextPref(
          title = DSLSettingsText.from(
            charSequence = getString(R.string.ViewSentGiftBottomSheet__youve_gifted_a_badge, state.recipient.getDisplayName(requireContext())),
            DSLSettingsText.CenterModifier
          )
        )

        space(DimensionUnit.DP.toPixels(30f).toInt())
      }

      if (state.badge != null) {
        customPref(
          BadgeDisplay112.Model(
            badge = state.badge
          )
        )
      }
    }
  }
}
