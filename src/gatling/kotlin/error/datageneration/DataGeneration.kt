
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File


object DataGeneration {
    private val objectMapper = ObjectMapper()
    @JvmStatic
    fun main(args: Array<String>) {
        val apiResponse=readMapObjectFromFile("test/optimal.json")
        val content=apiResponse["content"] as List<Map<String,Any>>
        val outputFile = File("optimalQueries.txt")
        content.forEach{ item ->
            val query = item["query"] as String
            val queryWithoutNewLine=query.replace("\n", " ")
            outputFile.appendText(queryWithoutNewLine+"\n")
        }

    }

    fun readFile(path: String): String {
        val fileContent= DataGeneration::class.java.classLoader.getResource(path)?.readText() ?: ""
        return fileContent
    }

    fun readMapObjectFromFile(path: String) = objectMapper.readValue(
        readFile(path),
        object : TypeReference<Map<String, Any>>() {}
    )
}