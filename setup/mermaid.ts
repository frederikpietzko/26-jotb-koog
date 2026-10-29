import { defineMermaidSetup } from '@slidev/types'

// Slidev renders Mermaid diagrams inside a shadow root, so rules in
// styles.css never reach them. Mermaid's default theme draws lavender boxes
// in a thin system font that look foreign next to the deck's CSS flow cards.
// `themeCSS` is injected into the diagram itself; custom properties inherit
// through the shadow boundary, so the card tint, outline and gradient colors
// follow the slide (and light/dark mode) exactly like the HTML diagrams do.
export default defineMermaidSetup(() => ({
  themeCSS: `
    .node rect, .node circle, .node polygon, .node path {
      fill: var(--graphic-card-bg, transparent) !important;
      stroke: var(--graphic-card-border, currentColor) !important;
      stroke-width: 1px !important;
    }
    .nodeLabel, .edgeLabel, .edgeLabel p, .label {
      color: currentColor !important;
      font-family: var(--slidev-code-font-family, monospace) !important;
      font-weight: 700 !important;
    }
    .edgeLabel, .edgeLabel p, .edgeLabel rect, .labelBkg {
      background: var(--diagram-surface, transparent) !important;
      background-color: var(--diagram-surface, transparent) !important;
      fill: var(--diagram-surface, transparent) !important;
    }
    .flowchart-link {
      stroke: var(--fundamentals-purple, currentColor) !important;
      stroke-width: 2px !important;
    }
    marker path, .marker {
      fill: var(--fundamentals-purple, currentColor) !important;
      stroke: var(--fundamentals-purple, currentColor) !important;
    }
  `,
}))
