package pt.isel.ls.utils

// Common

object InvalidBodyInputException : Exception("Invalid body input")

// Player
object InvalidPlayerIdException : Exception("Player ID must be a positive number")
object InvalidPlayerNameException : Exception("Name must have at most $MAX_PLAYER_NAME_LENGTH characters and no spaces")
object InvalidEmailException : Exception("Invalid email format")
object InvalidPasswordException : Exception("Password must have between $MIN_PASSWORD_LENGTH and $MAX_PASSWORD_LENGTH characters and contain at least one letter and one digit")
object PlayerNotLoggedInException : Exception("Player not logged in")
object PlayerAlreadyExistsException : Exception("Player already exists")
object PlayerNotFoundException : Exception("Player not found")
object PlayerNotInSessionException : Exception("Player is not in the session")
object PlayerNotTheSessionHostException : Exception("Player is not the host of the session")

// Game
object InvalidGameIdException : Exception("Game ID must be a positive number")
object InvalidGameNameLengthException : Exception("Name must have at most $MAX_GAME_NAME_LENGTH characters")
object InvalidGameNameException : Exception("Invalid game name format")
object InvalidDeveloperNameException : Exception("Invalid developer name format")
object InvalidDeveloperNameLengthException : Exception("Developer name must have at most $MAX_DEVELOPER_NAME_LENGTH characters")
object InvalidGenreException : Exception("Invalid genre")
object GameAlreadyExistsException : Exception("Game already exists")
object GameNotFoundException : Exception("Game Not Found")

//  Session
object InvalidCapacityException : Exception("Session capacity must be a positive number and greater or equal than players in session")
object InvalidSessionIdException : Exception("Session ID must be a positive number")
object InvalidDateException : Exception("Invalid date")
object InvalidStateException : Exception("State must either be 'open' or 'closed'")
object SessionDoesntExistException : Exception("Session not found")
object SessionIsFullException : Exception("Session is full")
object SessionHasPlayerException : Exception("Session already has player")
object SessionHostCantBeRemovedException : Exception("Host can't be removed from session")
object SessionHasExpiredException : Exception("Session has expired")
