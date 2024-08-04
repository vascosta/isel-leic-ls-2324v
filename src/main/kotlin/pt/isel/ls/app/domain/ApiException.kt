package pt.isel.ls.app.domain

import kotlinx.serialization.Serializable
import pt.isel.ls.utils.GameAlreadyExistsException
import pt.isel.ls.utils.GameNotFoundException
import pt.isel.ls.utils.InvalidBodyInputException
import pt.isel.ls.utils.InvalidCapacityException
import pt.isel.ls.utils.InvalidDateException
import pt.isel.ls.utils.InvalidDeveloperNameException
import pt.isel.ls.utils.InvalidDeveloperNameLengthException
import pt.isel.ls.utils.InvalidEmailException
import pt.isel.ls.utils.InvalidGameIdException
import pt.isel.ls.utils.InvalidGameNameException
import pt.isel.ls.utils.InvalidGameNameLengthException
import pt.isel.ls.utils.InvalidGenreException
import pt.isel.ls.utils.InvalidPasswordException
import pt.isel.ls.utils.InvalidPlayerIdException
import pt.isel.ls.utils.InvalidPlayerNameException
import pt.isel.ls.utils.InvalidSessionIdException
import pt.isel.ls.utils.InvalidStateException
import pt.isel.ls.utils.PlayerAlreadyExistsException
import pt.isel.ls.utils.PlayerNotFoundException
import pt.isel.ls.utils.PlayerNotInSessionException
import pt.isel.ls.utils.PlayerNotLoggedInException
import pt.isel.ls.utils.PlayerNotTheSessionHostException
import pt.isel.ls.utils.SessionDoesntExistException
import pt.isel.ls.utils.SessionHasExpiredException
import pt.isel.ls.utils.SessionHasPlayerException
import pt.isel.ls.utils.SessionHostCantBeRemovedException
import pt.isel.ls.utils.SessionIsFullException

@Serializable
class ApiException(val message: String) {
    companion object {
        val NOT_FOUND = listOf(
            SessionDoesntExistException,
            PlayerNotFoundException,
            GameNotFoundException
        )
        val BAD_REQUEST = listOf(
            InvalidPlayerNameException,
            InvalidEmailException,
            InvalidPasswordException,
            InvalidGameNameException,
            InvalidGameNameLengthException,
            InvalidDeveloperNameException,
            InvalidDeveloperNameLengthException,
            InvalidGenreException,
            InvalidBodyInputException,
            InvalidDateException,
            InvalidGameIdException,
            InvalidCapacityException,
            InvalidSessionIdException,
            InvalidPlayerIdException,
            InvalidStateException,
            SessionIsFullException,
            SessionHasPlayerException,
            SessionHasExpiredException,
            SessionHostCantBeRemovedException,
            PlayerAlreadyExistsException,
            PlayerNotInSessionException,
            GameAlreadyExistsException
        )
        val FORBIDDEN = listOf(
            PlayerNotTheSessionHostException
        )
        val UNAUTHORIZED = listOf(
            PlayerNotLoggedInException
        )
    }
}
