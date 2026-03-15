#!/usr/bin/env python3
"""Convert PRD markdown to styled HTML (open in browser → Print to PDF)."""

import re
import html as html_lib

INPUT = "/home/naevis/.claude/plans/abundant-watching-milner.md"
OUTPUT = "/home/naevis/P.A.R.A/01-Projects/couplebase/Couplebase-PRD.html"

def md_to_html(md: str) -> str:
    lines = md.split("\n")
    result = []
    in_code = False
    in_table = False
    in_list = False
    table_rows = []

    def flush_table():
        nonlocal in_table, table_rows
        if not table_rows:
            return ""
        out = '<table>\n'
        for i, row in enumerate(table_rows):
            cells = [c.strip() for c in row.strip("|").split("|")]
            if i == 0:
                out += '<thead><tr>' + ''.join(f'<th>{c}</th>' for c in cells) + '</tr></thead>\n<tbody>\n'
            elif i == 1 and all(set(c.strip()) <= set("-: ") for c in cells):
                continue
            else:
                out += '<tr>' + ''.join(f'<td>{inline(c)}</td>' for c in cells) + '</tr>\n'
        out += '</tbody></table>\n'
        in_table = False
        table_rows = []
        return out

    def inline(text):
        text = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', text)
        text = re.sub(r'`(.+?)`', r'<code>\1</code>', text)
        text = re.sub(r'\[(.+?)\]\((.+?)\)', r'<a href="\2">\1</a>', text)
        text = re.sub(r'(?<!\*)\*(?!\*)(.+?)(?<!\*)\*(?!\*)', r'<em>\1</em>', text)
        return text

    i = 0
    while i < len(lines):
        line = lines[i]

        # Code blocks
        if line.strip().startswith("```"):
            if in_table:
                result.append(flush_table())
            if in_code:
                result.append("</code></pre>\n")
                in_code = False
            else:
                lang = line.strip()[3:].strip()
                cls = f' class="language-{lang}"' if lang else ''
                result.append(f"<pre><code{cls}>")
                in_code = True
            i += 1
            continue

        if in_code:
            result.append(html_lib.escape(line) + "\n")
            i += 1
            continue

        # Empty line
        if not line.strip():
            if in_table:
                result.append(flush_table())
            if in_list:
                result.append("</ul>\n")
                in_list = False
            result.append("")
            i += 1
            continue

        # Headings
        hm = re.match(r'^(#{1,6})\s+(.+)', line)
        if hm:
            if in_table:
                result.append(flush_table())
            level = len(hm.group(1))
            text = inline(hm.group(2))
            cls = ' class="page-break"' if level <= 2 else ''
            result.append(f"<h{level}{cls}>{text}</h{level}>")
            i += 1
            continue

        # Tables
        if "|" in line and line.strip().startswith("|"):
            if not in_table:
                in_table = True
                table_rows = []
            table_rows.append(line)
            i += 1
            continue
        elif in_table:
            result.append(flush_table())

        # Horizontal rule
        if re.match(r'^---+$', line.strip()):
            result.append("<hr>")
            i += 1
            continue

        # Unordered list
        lm = re.match(r'^(\s*)[-*]\s+(.+)', line)
        if lm:
            if not in_list:
                result.append("<ul>")
                in_list = True
            result.append(f"<li>{inline(lm.group(2))}</li>")
            i += 1
            continue

        # Ordered list
        om = re.match(r'^(\s*)\d+\.\s+(.+)', line)
        if om:
            if not in_list:
                result.append("<ul>")
                in_list = True
            result.append(f"<li>{inline(om.group(2))}</li>")
            i += 1
            continue

        # Regular paragraph
        if in_list:
            result.append("</ul>")
            in_list = False
        result.append(f"<p>{inline(line)}</p>")
        i += 1

    if in_table:
        result.append(flush_table())
    if in_list:
        result.append("</ul>")
    if in_code:
        result.append("</code></pre>")

    return "\n".join(result)


CSS = """
@media print {
    .page-break { page-break-before: always; }
    body { font-size: 10pt; }
    pre { font-size: 8pt; }
}
* { box-sizing: border-box; }
body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    max-width: 900px;
    margin: 0 auto;
    padding: 40px 32px;
    color: #1a1a1a;
    line-height: 1.6;
    background: #fff;
}
h1 { font-size: 28px; border-bottom: 3px solid #C2185B; padding-bottom: 12px; margin-top: 48px; }
h2 { font-size: 22px; color: #C2185B; margin-top: 40px; border-bottom: 1px solid #e0e0e0; padding-bottom: 8px; }
h3 { font-size: 18px; margin-top: 28px; color: #333; }
h4 { font-size: 15px; margin-top: 24px; color: #555; }
p { margin: 8px 0; }
a { color: #C2185B; }
code {
    background: #f5f5f5;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 13px;
    font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', monospace;
}
pre {
    background: #1e1e2e;
    color: #cdd6f4;
    padding: 16px 20px;
    border-radius: 8px;
    overflow-x: auto;
    font-size: 12px;
    line-height: 1.5;
}
pre code {
    background: none;
    padding: 0;
    color: inherit;
    font-size: inherit;
}
table {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;
    font-size: 14px;
}
th {
    background: #C2185B;
    color: white;
    padding: 10px 12px;
    text-align: left;
    font-weight: 600;
}
td {
    padding: 8px 12px;
    border-bottom: 1px solid #e0e0e0;
}
tr:nth-child(even) { background: #fafafa; }
tr:hover { background: #fff0f3; }
ul { padding-left: 24px; }
li { margin: 4px 0; }
hr { border: none; border-top: 2px solid #e0e0e0; margin: 32px 0; }
strong { color: #C2185B; }
.header-meta {
    color: #666;
    font-size: 14px;
    margin-bottom: 32px;
}
"""

with open(INPUT, "r") as f:
    md = f.read()

body = md_to_html(md)

html_doc = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Couplebase — Product Requirements Document</title>
<style>{CSS}</style>
</head>
<body>
{body}
<footer style="margin-top: 48px; padding-top: 16px; border-top: 1px solid #e0e0e0; color: #999; font-size: 12px; text-align: center;">
Couplebase PRD v1.0 — Generated March 10, 2026
</footer>
</body>
</html>"""

with open(OUTPUT, "w") as f:
    f.write(html_doc)

print(f"✓ HTML generated: {OUTPUT}")
print(f"  Open in browser → File → Print → Save as PDF")
print(f"  (or Ctrl+P → Destination: Save as PDF)")