package contentgraph.datageneration

data class CCReport(
    val content: CCContent
){
    data class CCContent(
        val query: String
    )
}
