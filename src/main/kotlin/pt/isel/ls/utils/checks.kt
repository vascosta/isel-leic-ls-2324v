package pt.isel.ls.utils

const val MAX_PLAYER_NAME_LENGTH = 25
const val MIN_PASSWORD_LENGTH = 6
const val MAX_PASSWORD_LENGTH = 20
const val MAX_DEVELOPER_NAME_LENGTH = 32
const val MAX_GAME_NAME_LENGTH = 64
val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{$MIN_PASSWORD_LENGTH,$MAX_PASSWORD_LENGTH}\$".toRegex()
val gameNameRegex = "^[A-Za-z0-9 ]+".toRegex()
val consecutiveSpaces = "\\s{2,}".toRegex()

fun checkPlayerName(name: String) {
    if (!checkName(name, MAX_PLAYER_NAME_LENGTH) || name.contains(' ')) throw InvalidPlayerNameException
}

fun checkQueryName(name: String?) {
    if (name.checkFormat()) throw InvalidGameNameException
}

fun checkQueryDeveloper(name: String?) {
    if (name.checkFormat()) throw InvalidDeveloperNameException
}

fun checkGameName(name: String) {
    if (!checkName(name, MAX_GAME_NAME_LENGTH))
        throw InvalidGameNameLengthException
    if (name != name.trim() || !name.matches(gameNameRegex) || name.contains(consecutiveSpaces))
        throw InvalidGameNameException
}

fun checkDeveloper(name: String) {
    if (!checkName(name, MAX_DEVELOPER_NAME_LENGTH)) throw InvalidDeveloperNameLengthException
    if (name != name.trim()) throw InvalidDeveloperNameException
}

fun checkName(name: String, maxValue: Int) = checkString(name) && name.length <= maxValue

fun checkEmail(email: String) {
    if (!checkString(email) || !email.matches(emailRegex)) throw InvalidEmailException
}

fun checkPassword(password: String) {
    if (!checkString(password) || !password.matches(passwordRegex)) throw InvalidPasswordException
}

fun checkId(id: Int, exception: Exception) {
    if (id <= 0) throw exception
}

fun checkString(string: String) = string.isNotEmpty()

private fun String?.checkFormat() = this != null && this != this.replace(" ", "")
