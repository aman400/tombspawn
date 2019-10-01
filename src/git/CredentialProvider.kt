package com.ramukaka.git

open class CredentialProvider(var sshFilePath: String? = null,
                              var passphrase: String? = null,
                              var username: String? = null,
                              var password: String? = null) {

    fun isPresent(): Boolean {
        return !username.isNullOrEmpty() || !sshFilePath.isNullOrEmpty()
    }

    fun getErrorMessage(): String {
        return "Either set username and password or setup ssh with following block\nbot {\n    config {\n        git {\n " +
                "           credentials {\n                username = \"<username>\"\n                password = \"<password>\"\n          " +
                "      sshFilePath = \"<ssh file path>\"\n                passphrase = \"<passphrase>\"\n            }\n        }\n    }\n}"
    }
}