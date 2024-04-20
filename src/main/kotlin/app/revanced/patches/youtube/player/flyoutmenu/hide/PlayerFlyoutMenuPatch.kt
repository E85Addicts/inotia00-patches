package app.revanced.patches.youtube.player.flyoutmenu.hide

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patches.shared.litho.LithoFilterPatch
import app.revanced.patches.youtube.player.flyoutmenu.hide.fingerprints.AdvancedQualityBottomSheetFingerprint
import app.revanced.patches.youtube.player.flyoutmenu.hide.fingerprints.CaptionsBottomSheetFingerprint
import app.revanced.patches.youtube.utils.fingerprints.QualityMenuViewInflateFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.youtube.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.youtube.utils.integrations.Constants.PLAYER_CLASS_DESCRIPTOR
import app.revanced.patches.youtube.utils.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.BottomSheetFooterText
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.literalInstructionViewHook
import app.revanced.util.patch.BaseBytecodePatch

@Suppress("unused")
object PlayerFlyoutMenuPatch : BaseBytecodePatch(
    name = "Hide player flyout menu",
    description = "Adds options to hide player flyout menu components.",
    dependencies = setOf(
        LithoFilterPatch::class,
        PlayerTypeHookPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ),
    compatiblePackages = COMPATIBLE_PACKAGE,
    fingerprints = setOf(
        AdvancedQualityBottomSheetFingerprint,
        CaptionsBottomSheetFingerprint,
        QualityMenuViewInflateFingerprint
    )
) {
    private const val PANELS_FILTER_CLASS_DESCRIPTOR =
        "$COMPONENTS_PATH/PlayerFlyoutMenuFilter;"

    override fun execute(context: BytecodeContext) {
        arrayOf(
            AdvancedQualityBottomSheetFingerprint to "hideFooterQuality",
            CaptionsBottomSheetFingerprint to "hideFooterCaptions",
            QualityMenuViewInflateFingerprint to "hideFooterQuality"
        ).map { (fingerprint, name) ->
            fingerprint.literalInstructionViewHook(BottomSheetFooterText, "$PLAYER_CLASS_DESCRIPTOR->$name(Landroid/view/View;)V")
        }

        LithoFilterPatch.addFilter(PANELS_FILTER_CLASS_DESCRIPTOR)

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE_SCREEN: PLAYER",
                "PREFERENCE_SCREENS: FLYOUT_MENU",
                "SETTINGS: HIDE_PLAYER_FLYOUT_MENU"
            )
        )

        SettingsPatch.updatePatchStatus(this)
    }
}