package com.potato.jarvis.tools

/** Canonical client-side description of the server tool contract. */
data class ToolSpec(
    val name: String,
    val description: String,
    val risk: Int,
    val timeoutSeconds: Double = 30.0,
    val retryable: Boolean = false,
)

class ToolRegistry {
    private val definitions = listOf(
        ToolSpec("get_time", "Get the current local server time.", 0, 30.0, true),
        ToolSpec("remember", "Store user information in long-term memory.", 1),
        ToolSpec("read_note", "Read a named private note.", 1, 30.0, true),
        ToolSpec("write_note", "Create or replace a named private note.", 2),
        ToolSpec("list_notes", "List private notes.", 0, 30.0, true),
        ToolSpec("delete_note", "Delete a private note.", 3),
        ToolSpec("read_file", "Read a previously uploaded document.", 1, 30.0, true),
        ToolSpec("write_file", "Write a text file into the private file area.", 2),
        ToolSpec("delete_file", "Delete an uploaded/private file.", 3),
        ToolSpec("web_search", "Search the public web using the model's web search tool.", 1, 30.0, true),
        ToolSpec("device_action", "Control a configured server-side device after approval.", 3),
    )

    val tools: List<ToolSpec> get() = definitions

    fun find(name: String): ToolSpec? = definitions.firstOrNull { it.name == name }
}
