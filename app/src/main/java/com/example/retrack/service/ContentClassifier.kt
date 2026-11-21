package com.example.retrack.service

import android.view.accessibility.AccessibilityNodeInfo

object ContentClassifier {

    const val PLATFORM_YOUTUBE = "YouTube Shorts"
    const val PLATFORM_INSTAGRAM = "Instagram Reels"
    const val PLATFORM_TIKTOK = "TikTok"
    const val PLATFORM_FACEBOOK = "Facebook Reels"

    private const val CONTAINER_YOUTUBE_SHORTS = "reel_watch_player"
    private const val CONTAINER_INSTAGRAM_REELS = "clips_video_container"
    private const val CONTAINER_TIKTOK_VIEW_PAGER = "view_pager"
    private const val CONTAINER_FACEBOOK_REELS = "fb_shorts"

    private val SUPPORTED_PACKAGES = mapOf(
        "com.google.android.youtube" to PLATFORM_YOUTUBE,
        "com.instagram.android" to PLATFORM_INSTAGRAM,
        "com.zhiliaoapp.musically" to PLATFORM_TIKTOK,
        "com.ss.android.ugc.trill" to PLATFORM_TIKTOK,
        "com.facebook.katana" to PLATFORM_FACEBOOK
    )

    fun detectPlatform(packageName: String?): String? {
        if (packageName == null) return null
        return SUPPORTED_PACKAGES[packageName]
    }

    /**
     * Inspects rootInActiveWindow (never leaf event.source) to check if the user is currently inside a short-form video player.
     * Strictly targets actual video player container IDs and ignores generic tab/navigation labels.
     */
    fun isShortFormViewer(rootNode: AccessibilityNodeInfo?, packageName: String?): Boolean {
        if (rootNode == null || packageName == null) return false
        val platform = detectPlatform(packageName) ?: return false

        return when (platform) {
            PLATFORM_YOUTUBE -> hasContainerWithId(rootNode, CONTAINER_YOUTUBE_SHORTS)
            PLATFORM_INSTAGRAM -> hasContainerWithId(rootNode, CONTAINER_INSTAGRAM_REELS)
            PLATFORM_TIKTOK -> hasContainerWithId(rootNode, CONTAINER_TIKTOK_VIEW_PAGER)
            PLATFORM_FACEBOOK -> hasContainerWithId(rootNode, CONTAINER_FACEBOOK_REELS)
            else -> false
        }
    }

    private fun hasContainerWithId(node: AccessibilityNodeInfo, containerIdSubstring: String): Boolean {
        val resourceId = node.viewIdResourceName
        if (resourceId != null && resourceId.contains(containerIdSubstring, ignoreCase = true)) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (hasContainerWithId(child, containerIdSubstring)) {
                return true
            }
        }
        return false
    }

    /**
     * Extracts video signature derived strictly from creator handles or caption text nodes.
     * Returns null if no text is present. NEVER uses childCount or volatile system state.
     */
    fun extractVideoSignature(rootNode: AccessibilityNodeInfo?): String? {
        if (rootNode == null) return null
        val texts = mutableListOf<String>()
        collectNonVolatileTexts(rootNode, texts)

        if (texts.isEmpty()) return null
        // Sort/join deterministic stable text strings (creator handle or title text)
        val signatureContent = texts.sorted().joinToString("::")
        return if (signatureContent.isNotBlank()) signatureContent else null
    }

    private fun collectNonVolatileTexts(node: AccessibilityNodeInfo, outTexts: MutableList<String>) {
        val text = node.text?.toString()?.trim()
        val contentDesc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrEmpty() && isValidSignatureText(text)) {
            outTexts.add(text)
        } else if (!contentDesc.isNullOrEmpty() && isValidSignatureText(contentDesc)) {
            outTexts.add(contentDesc)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectNonVolatileTexts(child, outTexts)
        }
    }

    private fun isValidSignatureText(str: String): Boolean {
        // Exclude generic UI control strings or empty/numeric-only text like timestamps/counts
        if (str.length < 2) return false
        if (str.matches(Regex("^[0-9:\\s.,KMB]+$"))) return false
        val lower = str.lowercase()
        if (lower == "shorts" || lower == "reels" || lower == "home" || lower == "like" || lower == "share" || lower == "comment") {
            return false
        }
        return true
    }
}
