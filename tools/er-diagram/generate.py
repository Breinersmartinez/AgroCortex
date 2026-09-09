#!/usr/bin/env python3
"""Genera el diagrama entidad-relación (draw.io) y el bloque Mermaid
a partir de la fuente única de verdad docs/02-data/er/model.yaml.

Estándar enterprise de diagramas (diagram-as-code):
  - El modelo se edita SOLO en docs/02-data/er/model.yaml.
  - Los artefactos generados (data-model-er.drawio y el bloque erDiagram
    de data-model-mvp.md) se vuelcan al repo versionado.
  - `--check` regenera en un directorio temporal y compara con lo
    versionado: si hay deriva, el job `diagram` de CI falla.
  - La salida es determinista (sin timestamps ni ubicaciones de la máquina):
    versionado y reproducible.

Uso:
  python tools/er-diagram/generate.py            # regenera los artefactos
  python tools/er-diagram/generate.py --check    # valida sin escribir
"""
import argparse
import pathlib
import sys
import xml.etree.ElementTree as ET

import yaml

ROOT = pathlib.Path(__file__).resolve().parents[2]
MODEL = ROOT / "docs" / "02-data" / "er" / "model.yaml"
DRAWIO_OUT = ROOT / "docs" / "02-data" / "data-model-er.drawio"
MVP_MD = ROOT / "docs" / "02-data" / "data-model-mvp.md"

# Coordenadas del lienzo (3 columnas fijas; verande en layout unit tests)
X0, COL_W, COL_GAP = 80, 320, 160
ROW0_Y, ROW_GAP = 100, 420
HEADER_H, ROW_H = 30, 26
PAGE_W, PAGE_H = 1500, 1800

HEADER = """<?xml version="1.0" encoding="UTF-8"?>
<mxfile host="app.diagrams.net" agent="agrocortex-tools" version="24.0.0">
  <diagram id="data-model-er" name="Modelo de datos MVP">
    <mxGraphModel dx="1200" dy="800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{pw}" pageHeight="{ph}" math="0" shadow="0">
      <root>
        <mxCell id="0" />
        <mxCell id="1" parent="0" />
"""

FOOTER = """      </root>
    </mxGraphModel>
  </diagram>
</mxfile>
"""

M_BEGIN = "<!-- BEGIN er-diagram (generated from docs/02-data/er/model.yaml by tools/er-diagram/generate.py -- do not edit) -->"
M_END = "<!-- END er-diagram -->"

MERMAID_TYPE = {
    "uuid": "uuid",
    "fk": "uuid",
    "text": "string",
    "numeric": "float",
    "enum": "string",
    "timestamp": "string",
    "date": "string",
    "jsonb": "string",
    "int": "int",
    "bool": "bool",
}

POS = {"AGRICULTOR": (0, 0), "PARCELA": (1, 0), "CULTIVO": (2, 0),
       "CONSULTA": (0, 1), "SESION": (1, 1), "MENSAJE": (2, 1),
       "EVIDENCIA": (0, 2), "DIAGNOSTICO": (1, 2), "HIPOTESIS": (2, 2),
       "RECOMENDACION": (1, 3)}


def _esc_xml(s):
    return (s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
             .replace('"', "&quot;").replace("'", "&apos;"))


def entity_box(eid, cfg):
    n = len(cfg["attrs"])
    x = X0 + POS[eid][0] * (COL_W + COL_GAP)
    y = ROW0_Y + POS[eid][1] * ROW_GAP
    h = HEADER_H + ROW_H * n
    name = _esc_xml(eid.capitalize())
    style = (
        "swimlane;fontStyle=1;childLayout=stackLayout;horizontal=1;"
        "startSize={hs};horizontalStack=0;resizeParent=1;resizeParentMax=1;"
        "resizeLast=0;collapsible=1;marginBottom=0;rounded=0;"
        "fillColor={c};strokeColor={s};fontColor=#ffffff;fontSize=13;".format(
            hs=HEADER_H, c=cfg["color"], s=cfg["stroke"])
    )
    lines = [
        f'<mxCell id="{eid}" value="{name}" style="{style}" vertex="1" parent="1">',
        f'  <mxGeometry x="{x}" y="{y}" width="{COL_W}" height="{h}" as="geometry"/>',
        "</mxCell>",
    ]
    for i, (attr, atype, flags) in enumerate(cfg["attrs"]):
        badge = ""
        if "PK" in flags:
            badge = "<b>PK</b>&nbsp;"
        elif "FK" in flags:
            badge = "<b>FK</b>&nbsp;"
        key = "UQ" in flags or flags.startswith("FK") or attr == "id"
        suffix = ""
        extra = [f for f in flags.split("·") if f.strip() not in ("PK", "FK", "")]
        if extra:
            suffix = "&nbsp;<font color=\"#78909C\">· " + " · ".join(x.strip() for x in extra) + "</font>"
        bold = "<b>" if key else ""
        boldc = "</b>" if key else ""
        value = f"{badge}{bold}{_esc_xml(attr)}{boldc}{suffix}"
        row_id = f"{eid}-r{i}"
        lines.append(
            f'<mxCell id="{row_id}" value="{_esc_xml(value)}" '
            'style="text;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;'
            'spacingLeft=6;spacingRight=6;overflow=hidden;points=[[0,0.5],[1,0.5]];'
            'portConstraint=eastwest;rotatable=0;html=1;'
            'fontFamily=Menlo,Consolas,monospace;fontSize=11;fontColor=#37474F;" '
            f'vertex="1" parent="{eid}">'
        )
        lines.append(f'  <mxGeometry y="{HEADER_H + ROW_H * i}" width="{COL_W}" height="{ROW_H}" as="geometry"/>')
        lines.append("</mxCell>")
    return lines


def edge_cell(idx, rel):
    eid = f"e{idx}"
    value = f"{_esc_xml(rel['label'])} ({rel['srcCard']} — {rel['tgtCard']})"
    style = (
        "edgeStyle=orthogonalEdgeStyle;rounded=0;html=1;"
        "exitX=1;exitY=0.5;exitDx=0;exitDy=0;"
        "entryX=0;entryY=0.5;entryDx=0;entryDy=0;"
        "startArrow=none;endArrow=none;strokeColor=#90A4AE;"
        "fontColor=#37474F;fontSize=11;fontStyle=2;"
    )
    return (
        f'<mxCell id="{eid}" value="{_esc_xml(value)}" style="{style}" '
        f'edge="1" parent="1" source="{rel["src"]}" target="{rel["tgt"]}">\n'
        '  <mxGeometry relative="1" as="geometry"/>\n'
        "</mxCell>"
    )


def legend_cell(model):
    name = _esc_xml(model["name"])
    version = _esc_xml(model["version"])
    lines_text = [
        f"<b>{name} — Modelo de datos MVP</b>",
        f"Versión: <b>{version}</b> &nbsp;·&nbsp; Notación: crow's foot",
        "<b>PK</b> clave primaria &nbsp;·&nbsp; <b>FK</b> clave foránea",
        "Relaciones: <b>(1 — N)</b> / <b>(1 — 1)</b>",
        "<font color=\"#78909C\">Generado desde </font><font color=\"#78909C\" face=\"Menlo,Consolas,monospace\">docs/02-data/er/model.yaml</font>",
    ]
    value = "<br>".join(lines_text)
    eid = "LEGEND"
    style = (
        "rounded=0;dashed=1;dashPattern=5 3;whiteSpace=wrap;html=1;"
        "align=left;verticalAlign=top;spacingLeft=10;spacingTop=8;"
        "fillColor=none;strokeColor=#90A4AE;fontColor=#455A64;fontSize=11;"
    )
    x = X0 + 2 * (COL_W + COL_GAP)
    return [
        f'<mxCell id="{eid}" value="{_esc_xml(value)}" style="{style}" vertex="1" parent="1">',
        f'  <mxGeometry x="{x}" y="{ROW0_Y + 3 * ROW_GAP}" width="{COL_W}" height="150" as="geometry"/>',
        "</mxCell>",
    ]


def render_drawio(model):
    body = []
    for i, eid in enumerate(model["entities"]):
        body.extend(entity_box(eid, model["entities"][eid]))
    for i, rel in enumerate(model["relations"]):
        body.append(edge_cell(i, rel))
    body.extend(legend_cell(model))
    page = HEADER.format(pw=PAGE_W, ph=PAGE_H)
    return page + "\n".join(body) + "\n" + FOOTER


def mermaid_card(card):
    return "||" if card == "1" else "o{"


def render_mermaid(model):
    lines = ["erDiagram"]
    for rel in model["relations"]:
        lines.append(
            f'    {rel["src"]} {mermaid_card(rel["srcCard"])}--{mermaid_card(rel["tgtCard"])} '
            f'{rel["tgt"]} : "{rel["label"]}"'
        )
    for eid, cfg in model["entities"].items():
        lines.append("")
        lines.append(f"    {eid} {{")
        for attr, atype, flags in cfg["attrs"]:
            key = ""
            if "PK" in flags:
                key = " PK"
            elif "FK" in flags:
                key = " FK"
            elif "UQ" in flags:
                key = " UK"
            py_type = MERMAID_TYPE.get(atype, "string")
            lines.append(f"        {py_type} {attr}{key}")
        lines.append("    }")
    return "\n".join(lines)


def replace_mermaid_block(md_text, mermaid):
    begin = md_text.find(M_BEGIN)
    end = md_text.find(M_END)
    if begin == -1 or end == -1:
        raise SystemExit("no se encontraron las marcas de bloque erDiagram en data-model-mvp.md")
    block = f"{M_BEGIN}\n```mermaid\n{mermaid}\n```\n{M_END}"
    return md_text[:begin] + block + md_text[end + len(M_END):]


def _validate_drawio(xml_text):
    try:
        ET.fromstring(xml_text)
    except ET.ParseError as exc:
        raise SystemExit(f"XML inválido: {exc}")
    # 1) ids únicos, 2) sin mxCell anidado en otro mxCell, 3) edges referencian existentes
    root = ET.fromstring(xml_text)
    cells = {}
    for el in root.iter("mxCell"):
        cid = el.get("id")
        if cid in cells:
            raise SystemExit(f"id duplicado: {cid}")
        cells[cid] = el
        for child in el:
            if child.tag == "mxCell":
                raise SystemExit(f"mxCell anidado: {cid}")
    for el in root.iter("mxCell"):
        if el.get("edge") == "1":
            for ref in ("source", "target"):
                if el.get(ref) not in cells:
                    raise SystemExit(f"edge {el.get('id')} apunta a célula inexistente: {el.get(ref)}")
    return len(cells)


def generate():
    model = yaml.safe_load(MODEL.read_text())
    drawio = render_drawio(model)
    mermaid = render_mermaid(model)
    md = MVP_MD.read_text()
    md_new = replace_mermaid_block(md, mermaid)
    n_cells = _validate_drawio(drawio)
    return model, drawio, md_new, mermaid, n_cells


def write_artifacts(drawio, md_new):
    DRAWIO_OUT.write_text(drawio)
    MVP_MD.write_text(md_new)


def check():
    _, drawio, md_new, _, n_cells = generate()
    drift = []
    if DRAWIO_OUT.read_text() != drawio:
        drift.append(str(DRAWIO_OUT.relative_to(ROOT)))
    if MVP_MD.read_text() != md_new:
        drift.append(str(MVP_MD.relative_to(ROOT)))
    if drift:
        print("ERROR: deriva entre modelo y artefactos versionados. Ejecuta `python "
              "tools/er-diagram/generate.py` y comitea los cambios:")
        for f in drift:
            print(f"  - {f}")
        return 1
    print(f"OK: {n_cells} celdas XML válidas; artefactos al día con model.yaml")
    return 0


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--check", action="store_true",
                    help="verifica sin escribir que los artefactos versionados están al día")
    args = ap.parse_args()
    model, drawio, md_new, _, _ = generate()
    if args.check:
        sys.exit(check())
    write_artifacts(drawio, md_new)
    print(f"regenerado: {DRAWIO_OUT.relative_to(ROOT)} ({len(model['entities'])} entidades, "
          f"{len(model['relations'])} relaciones)")
    print(f"actualizado: {MVP_MD.relative_to(ROOT)} (bloque erDiagram)")


if __name__ == "__main__":
    main()