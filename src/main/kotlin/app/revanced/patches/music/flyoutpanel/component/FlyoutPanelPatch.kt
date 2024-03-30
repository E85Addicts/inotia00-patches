package app.revanced.patches.music.flyoutpanel.component

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.flyoutpanel.component.fingerprints.EndButtonsContainerFingerprint
import app.revanced.patches.music.flyoutpanel.component.fingerprints.SleepTimerFingerprint
import app.revanced.patches.music.flyoutpanel.shared.FlyoutPanelMenuItemPatch
import app.revanced.patches.music.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.music.utils.integrations.Constants.FLYOUT
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.EndButtonsContainer
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.patches.shared.litho.LithoFilterPatch
import app.revanced.util.exception
import app.revanced.util.getTargetIndex
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide flyout panel",
    description = "Adds options to hide flyout panel components.",
    dependencies = [
        FlyoutPanelMenuItemPatch::class,
        LithoFilterPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.apps.youtube.music",
            [
                "6.21.52",
                "6.22.52",
                "6.23.56",
                "6.25.53",
                "6.26.51",
                "6.27.54",
                "6.28.53",
                "6.29.58",
                "6.31.55",
                "6.33.52"
            ]
        )
    ]
)
@Suppress("unused")
object FlyoutPanelPatch : BytecodePatch(
    setOf(
        EndButtonsContainerFingerprint,
        SleepTimerFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        FlyoutPanelMenuItemPatch.hideComponents()

        EndButtonsContainerFingerprint.result?.let {
            it.mutableMethod.apply {
                val startIndex = getWideLiteralInstructionIndex(EndButtonsContainer)
                val targetIndex = getTargetIndex(startIndex, Opcode.MOVE_RESULT_OBJECT)
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex + 1,
                    "invoke-static {v$targetRegister}, $FLYOUT->hideLikeDislikeContainer(Landroid/view/View;)V"
                )
            }
        } ?: throw EndButtonsContainerFingerprint.exception

        /**
         * Forces sleep timer menu to be enabled.
         * This method may be desperate in the future.
         */
        SleepTimerFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstruction(
                    insertIndex,
                    "const/4 v$targetRegister, 0x1"
                )
            }
        }

        if (SettingsPatch.upward0636) {
            LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

            SettingsPatch.addMusicPreference(
                CategoryType.FLYOUT,
                "revanced_hide_flyout_panel_3_column_component",
                "false"
            )
        }

        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_add_to_queue",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_captions",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_delete_playlist",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_dismiss_queue",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_download",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_edit_playlist",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_go_to_album",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_go_to_artist",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_go_to_episode",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_go_to_podcast",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_help",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_like_dislike",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_play_next",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_quality",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_remove_from_library",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_remove_from_playlist",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_report",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_save_episode_for_later",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_save_to_library",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_save_to_playlist",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_share",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_shuffle_play",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_sleep_timer",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_start_radio",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_stats_for_nerds",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_subscribe",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithoutSummary(
            CategoryType.FLYOUT,
            "revanced_hide_flyout_panel_view_song_credit",
            "false"
        )
    }

    private const val FILTER_CLASS_DESCRIPTOR =
        "$COMPONENTS_PATH/PlayerFlyoutPanelsFilter;"
}
