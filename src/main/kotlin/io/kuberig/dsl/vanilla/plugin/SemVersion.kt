package io.kuberig.dsl.vanilla.plugin

data class SemVersion(
        private val majorVersion: Int,
        private val minorVersion: Int,
        private val patchVersion: Int
) {
    fun isHigher(otherVersion: SemVersion) : Boolean {
        if (majorVersion > otherVersion.majorVersion) {
            return true
        } else if (majorVersion == otherVersion.majorVersion) {
            if (minorVersion > otherVersion.minorVersion) {
                return true
            } else if (minorVersion == otherVersion.minorVersion) {
                return patchVersion > otherVersion.patchVersion
            }
        }

        return false
    }

    companion object {
        fun fromTagName(tagName: String): SemVersion? {
            val tagSemVersionPart: String = if (tagName.startsWith("v")) {
                tagName.substring(1)
            } else {
                tagName
            }

            val parts = tagSemVersionPart.split("\\.".toRegex()).toTypedArray()

            return if (parts.size != 3) {
                null
            } else SemVersion(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())

        }

        fun fromProjectName(rootProjectName: String, subProjectName: String): SemVersion {
            return fromTagName(subProjectName.substringAfter("$rootProjectName-"))!!
        }
    }
}

