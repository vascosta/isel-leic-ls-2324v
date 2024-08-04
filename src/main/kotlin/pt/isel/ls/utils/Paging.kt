package pt.isel.ls.utils

const val PAGING_BUILT_IN_LIMIT = 100

interface Paging {
    val skip: Int
    val limit: Int
    companion object {
        const val DEFAULT_SKIP = 0
        const val DEFAULT_LIMIT = PAGING_BUILT_IN_LIMIT
    }
}
