package app.revanced.patches.youtube.layout.branding.icon

import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.stringOption
import app.revanced.patches.youtube.utils.compatibility.Constants.COMPATIBLE_PACKAGE
import app.revanced.patches.youtube.utils.patch.PatchList.CUSTOM_BRANDING_ICON_FOR_YOUTUBE
import app.revanced.patches.youtube.utils.settings.ResourceUtils.updatePatchStatusIcon
import app.revanced.patches.youtube.utils.settings.settingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.copyFile
import app.revanced.util.copyResources
import app.revanced.util.copyXmlNode
import app.revanced.util.getResourceGroup
import app.revanced.util.underBarOrThrow

private const val DEFAULT_ICON = "revancify_blue"

private val availableIcon = mapOf(
    "AFN Blue" to "afn_blue",
    "AFN Red" to "afn_red",
    "MMT" to "mmt",
    "Revancify Blue" to DEFAULT_ICON,
    "Revancify Red" to "revancify_red",
    "YouTube" to "youtube"
)

private val sizeArray = arrayOf(
    "xxxhdpi",
    "xxhdpi",
    "xhdpi",
    "hdpi",
    "mdpi"
)

private val drawableDirectories = sizeArray.map { "drawable-$it" }

private val mipmapDirectories = sizeArray.map { "mipmap-$it" }

private val launcherIconResourceFileNames = arrayOf(
    "adaptiveproduct_youtube_background_color_108",
    "adaptiveproduct_youtube_foreground_color_108",
    "ic_launcher",
    "ic_launcher_round"
).map { "$it.png" }.toTypedArray()

private val splashIconResourceFileNames = arrayOf(
    "product_logo_youtube_color_24",
    "product_logo_youtube_color_36",
    "product_logo_youtube_color_144",
    "product_logo_youtube_color_192"
).map { "$it.png" }.toTypedArray()

private val oldSplashAnimationResourceFileNames = arrayOf(
    "\$\$avd_anim__1__0",
    "\$\$avd_anim__1__1",
    "\$\$avd_anim__2__0",
    "\$\$avd_anim__2__1",
    "\$\$avd_anim__3__0",
    "\$\$avd_anim__3__1",
    "\$avd_anim__0",
    "\$avd_anim__1",
    "\$avd_anim__2",
    "\$avd_anim__3",
    "\$avd_anim__4",
    "avd_anim"
).map { "$it.xml" }.toTypedArray()

private val launcherIconResourceGroups =
    mipmapDirectories.getResourceGroup(launcherIconResourceFileNames)

private val splashIconResourceGroups =
    drawableDirectories.getResourceGroup(splashIconResourceFileNames)

private val oldSplashAnimationResourceGroups =
    listOf("drawable").getResourceGroup(oldSplashAnimationResourceFileNames)

@Suppress("unused")
val customBrandingIconPatch = resourcePatch(
    CUSTOM_BRANDING_ICON_FOR_YOUTUBE.title,
    CUSTOM_BRANDING_ICON_FOR_YOUTUBE.summary,
) {
    compatibleWith(COMPATIBLE_PACKAGE)

    dependsOn(settingsPatch)


    val appIconOption = stringOption(
        key = "appIcon",
        default = DEFAULT_ICON,
        values = availableIcon,
        title = "App icon",
        description = """
            The icon to apply to the app.
            
            If a path to a folder is provided, the folder must contain the following folders:

            ${mipmapDirectories.joinToString("\n") { "- $it" }}

            Each of these folders must contain the following files:

            ${launcherIconResourceFileNames.joinToString("\n") { "- $it" }}
            """.trimIndentMultiline(),
        required = true,
    )

    val changeSplashIconOption by booleanOption(
        key = "changeSplashIcon",
        default = true,
        title = "Change splash icons",
        description = "Apply the custom branding icon to the splash screen.",
        required = true
    )

    val restoreOldSplashAnimationOption by booleanOption(
        key = "restoreOldSplashAnimation",
        default = true,
        title = "Restore old splash animation",
        description = "Restore the old style splash animation.",
        required = true,
    )

    execute {
        // Check patch options first.
        val appIcon = appIconOption.underBarOrThrow()

        val appIconResourcePath = "youtube/branding/$appIcon"


        // Check if a custom path is used in the patch options.
        if (!availableIcon.containsValue(appIcon)) {
            val copiedFiles = copyFile(
                launcherIconResourceGroups,
                appIcon,
                "WARNING: Invalid app icon path: $appIcon. Does not apply patches."
            )
            if (copiedFiles)
                updatePatchStatusIcon("custom")
        } else {
            // Change launcher icon.
            launcherIconResourceGroups.let { resourceGroups ->
                resourceGroups.forEach {
                    copyResources("$appIconResourcePath/launcher", it)
                }
            }

            // Change monochrome icon.
            arrayOf(
                ResourceGroup(
                    "drawable",
                    "adaptive_monochrome_ic_youtube_launcher.xml"
                )
            ).forEach { resourceGroup ->
                copyResources("$appIconResourcePath/monochrome", resourceGroup)
            }

            // Change splash icon.
            if (changeSplashIconOption == true) {
                splashIconResourceGroups.let { resourceGroups ->
                    resourceGroups.forEach {
                        copyResources("$appIconResourcePath/splash", it)
                    }
                }
            }

            // Change splash screen.
            if (restoreOldSplashAnimationOption == true) {
                oldSplashAnimationResourceGroups.let { resourceGroups ->
                    resourceGroups.forEach {
                        copyResources("$appIconResourcePath/splash", it)
                    }
                }

                copyXmlNode(
                    "$appIconResourcePath/splash",
                    "values-v31/styles.xml",
                    "resources"
                )
            }

            updatePatchStatusIcon(appIcon)
        }
    }
}
