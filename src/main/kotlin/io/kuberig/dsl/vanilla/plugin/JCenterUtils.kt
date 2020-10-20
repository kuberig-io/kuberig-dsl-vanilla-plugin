package io.kuberig.dsl.vanilla.plugin

import com.mashape.unirest.http.Unirest

fun jcenterExists(packageName: String, version: String, bintrayUser: String, bintrayApiKey: String): Boolean {

    Thread.sleep(250)

    val response = Unirest.get("https://api.bintray.com/packages/teyckmans/rigeldev-oss-maven/{package}")
            .routeParam("package", packageName)
            .basicAuth(bintrayUser, bintrayApiKey)
            .asJson()

    if (response.status == 404) {
        return false
    }

    val `object` = response.body.getObject()
    val versions = `object`.getJSONArray("versions")

    for (i in 0 until versions.length()) {
        val availableVersion = versions.getString(i)
        if (availableVersion == version) {
            return true
        }
    }

    return false
}