package pt.isel.ls.session.domain

import pt.isel.ls.utils.Paging

data class SessionQuery(override val skip: Int, override val limit: Int, val optionals: HashMap<String, String>) :
    Paging
