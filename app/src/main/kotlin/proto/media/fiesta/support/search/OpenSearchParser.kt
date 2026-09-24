package proto.media.fiesta.support.search

import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

object OpenSearchParser {

    private const val OPEN_SEARCH_NS = "http://a9.com/-/spec/opensearch/1.1/"
    private const val FIESTA_NS = "urn:fiesta:search:1"
    private const val SEARCH_TYPE = "text/html"
    private const val SUGGESTIONS_TYPE = "application/x-suggestions+json"
    private const val HTTPS = "https://"
    private val ID_PATTERN = Regex("^[a-z0-9][a-z0-9_-]{0,31}$")
    private val PLACEHOLDER = Regex("\\{[^}]*\\}")

    fun parse(input: InputStream): SearchEngineDescription {
        val root = readRoot(input)
        if (!root.isIn(OPEN_SEARCH_NS, "OpenSearchDescription")) {
            throw InvalidSearchEngineException("root must be OpenSearchDescription")
        }
        val children = childElements(root)
        requireUtf8Encoding(children)
        return SearchEngineDescription(
            id = readId(children),
            version = readVersion(children),
            shortName = readShortName(children),
            searchTemplate = readSearchTemplate(children),
            suggestionsTemplate = readSuggestionsTemplate(children),
            hosts = readHosts(children)
        )
    }

    private fun readRoot(input: InputStream): Element {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isExpandEntityReferences = false
        }
        return try {
            factory.newDocumentBuilder().parse(input).documentElement
        } catch (e: SAXException) {
            throw InvalidSearchEngineException("malformed XML: ${e.message}")
        } catch (e: IOException) {
            throw InvalidSearchEngineException("unreadable XML: ${e.message}")
        } catch (e: ParserConfigurationException) {
            throw InvalidSearchEngineException("XML parser unavailable: ${e.message}")
        }
    }

    private fun requireUtf8Encoding(children: List<Element>) {
        val encoding = firstText(children, OPEN_SEARCH_NS, "InputEncoding") ?: return
        if (!encoding.equals("UTF-8", ignoreCase = true)) {
            throw InvalidSearchEngineException("unsupported InputEncoding: $encoding")
        }
    }

    private fun readShortName(children: List<Element>): String =
        firstText(children, OPEN_SEARCH_NS, "ShortName")?.takeIf { it.isNotEmpty() }
            ?: throw InvalidSearchEngineException("ShortName required")

    private fun readSearchTemplate(children: List<Element>): String =
        readTemplates(children, SEARCH_TYPE).singleOrNull()
            ?: throw InvalidSearchEngineException("exactly one $SEARCH_TYPE Url required")

    private fun readSuggestionsTemplate(children: List<Element>): String? {
        val templates = readTemplates(children, SUGGESTIONS_TYPE)
        if (templates.size > 1) {
            throw InvalidSearchEngineException("at most one $SUGGESTIONS_TYPE Url allowed")
        }
        return templates.firstOrNull()
    }

    private fun readTemplates(children: List<Element>, type: String): List<String> =
        children
            .filter { it.isIn(OPEN_SEARCH_NS, "Url") && it.getAttribute("type") == type }
            .map { url ->
                if (childElements(url).any { it.localName == "Param" }) {
                    throw InvalidSearchEngineException("Url with Param is not supported")
                }
                url.getAttribute("template").also { requireValidTemplate(it) }
            }

    private fun requireValidTemplate(template: String) {
        if (!template.startsWith(HTTPS)) {
            throw InvalidSearchEngineException("template must start with $HTTPS: $template")
        }
        val placeholders = PLACEHOLDER.findAll(template).map { it.value }.toList()
        if (placeholders != listOf(DescribedSearchEngine.SEARCH_TERMS)) {
            throw InvalidSearchEngineException("template must contain only one {searchTerms}: $template")
        }
    }

    private fun readId(children: List<Element>): String {
        val id = firstText(children, FIESTA_NS, "Id")
            ?: throw InvalidSearchEngineException("cs:Id required")
        if (!ID_PATTERN.matches(id)) {
            throw InvalidSearchEngineException("invalid cs:Id: $id")
        }
        return id
    }

    private fun readVersion(children: List<Element>): Int {
        val text = firstText(children, FIESTA_NS, "Version") ?: return 1
        val version = text.toIntOrNull()
        if (version == null || version < 1) {
            throw InvalidSearchEngineException("invalid cs:Version: $text")
        }
        return version
    }

    private fun readHosts(children: List<Element>): List<String> =
        children
            .filter { it.isIn(FIESTA_NS, "Host") }
            .map { it.textContent.trim() }
            .onEach {
                if (!HostPattern.isValid(it)) throw InvalidSearchEngineException("invalid cs:Host: $it")
            }

    private fun firstText(children: List<Element>, namespace: String, name: String): String? =
        children.firstOrNull { it.isIn(namespace, name) }?.textContent?.trim()

    private fun childElements(parent: Element): List<Element> {
        val nodes = parent.childNodes
        return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
    }

    private fun Element.isIn(namespace: String, name: String): Boolean =
        namespaceURI == namespace && localName == name
}
