package app.revanced.patches.music.account.handle

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.account.handle.fingerprints.AccountSwitcherAccessibilityLabelFingerprint
import app.revanced.patches.music.account.handle.fingerprints.NamesInactiveAccountThumbnailSizeFingerprint
import app.revanced.patches.music.utils.integrations.Constants.ACCOUNT
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getTargetIndexWithMethodReferenceName
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide handle",
    description = "Adds an option to hide the handle in the account menu.",
    dependencies = [
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
object HideHandlePatch : BytecodePatch(
    setOf(
        AccountSwitcherAccessibilityLabelFingerprint,
        NamesInactiveAccountThumbnailSizeFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        /**
         * Hide handle in account menu
         */
        AccountSwitcherAccessibilityLabelFingerprint.result?.let { result ->
            result.mutableMethod.apply {

                val textColorIndex = getTargetIndexWithMethodReferenceName("setTextColor")
                val setVisibilityIndex = getTargetIndexWithMethodReferenceName(textColorIndex, "setVisibility")
                val textViewInstruction = getInstruction<FiveRegisterInstruction>(setVisibilityIndex)

                replaceInstruction(
                    setVisibilityIndex,
                    "invoke-static {v${textViewInstruction.registerC}, v${textViewInstruction.registerD}}, $ACCOUNT->hideHandle(Landroid/widget/TextView;I)V"
                )
            }
        } ?: throw AccountSwitcherAccessibilityLabelFingerprint.exception

        /**
         * Hide handle in account switcher
         */
        NamesInactiveAccountThumbnailSizeFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.startIndex
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex, """
                        invoke-static {v$targetRegister}, $ACCOUNT->hideHandle(Z)Z
                        move-result v$targetRegister
                        """
                )
            }
        } ?: throw NamesInactiveAccountThumbnailSizeFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.ACCOUNT,
            "revanced_hide_handle",
            "true"
        )

    }
}
