## Shared AI Context (read first, every session)

Before doing anything else — before exploring code, planning, or editing — load the organization baseline from the `teni-shared-ai` MCP server:

1. Call `get_shared_ai_context` (use `get_shared_ai_help` first if unsure what the server exposes). This project resolves the `java`, `react`, `nosql`, and `documentation` packs (see `.mcp.json`).
2. Read the returned baseline resources — `AGENTS.md`, `RULES.md`, `settings.json` — plus any pack-specific rules/skills/agents relevant to the current task.
3. Treat these as mandatory guardrails: everything below in this file may extend them, but must never override or weaken them.

This applies to every agent, on every task, no matter how small.

---
