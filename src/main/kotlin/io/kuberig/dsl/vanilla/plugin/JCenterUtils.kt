package io.kuberig.dsl.vanilla.plugin

import com.mashape.unirest.http.Unirest

fun jcenterExists(moduleName: String, version: String): Boolean {
    val response = Unirest.get("https://api.bintray.com/packages/teyckmans/rigeldev-oss-maven/{module}")
            .routeParam("module", moduleName)
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