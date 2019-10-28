package com.tombspawn.skeleton.git

import com.google.gson.annotations.SerializedName

open class CredentialProvider(
    @SerializedName("ssh_file_path")
    var sshFilePath: String? = null,
    @SerializedName("passphrase")
    var passphrase: String? = null,
    @SerializedName("username")
    var username: String? = null,
    @SerializedName("password")
    var password: String? = null
) {

    fun isPresent(): Boolean {
        return !username.isNullOrEmpty() || !sshFilePath.isNullOrEmpty()
    }

    fun getErrorMessage(): String {
        return "Either set username and password or setup ssh with following block\nbot {\n    config {\n        git {\n " +
                "           credentials {\n                username = \"<username>\"\n                password = \"<password>\"\n          " +
                "      sshFilePath = \"<ssh file path>\"\n                passphrase = \"<passphrase>\"\n            }\n        }\n    }\n}"
    }
}