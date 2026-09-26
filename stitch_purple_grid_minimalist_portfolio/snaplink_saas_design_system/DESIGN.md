---
name: SnapLink SaaS Design System
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#464555'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#777587'
  outline-variant: '#c7c4d8'
  surface-tint: '#4d44e3'
  primary: '#3525cd'
  on-primary: '#ffffff'
  primary-container: '#4f46e5'
  on-primary-container: '#dad7ff'
  inverse-primary: '#c3c0ff'
  secondary: '#712ae2'
  on-secondary: '#ffffff'
  secondary-container: '#8a4cfc'
  on-secondary-container: '#fffbff'
  tertiary: '#005338'
  on-tertiary: '#ffffff'
  tertiary-container: '#006e4b'
  on-tertiary-container: '#67f4b7'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e2dfff'
  primary-fixed-dim: '#c3c0ff'
  on-primary-fixed: '#0f0069'
  on-primary-fixed-variant: '#3323cc'
  secondary-fixed: '#eaddff'
  secondary-fixed-dim: '#d2bbff'
  on-secondary-fixed: '#25005a'
  on-secondary-fixed-variant: '#5a00c6'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
  brand-indigo: '#4F46E5'
  brand-violet: '#7C3AED'
  brand-purple-light: '#C49BFF'
  brand-purple-glow: '#9855FF'
  status-emerald: '#10B981'
  status-amber: '#F59E0B'
  status-rose: '#F43F5E'
  canvas-bg: '#F8FAFC'
  surface-card: '#FFFFFF'
  surface-card-subtle: '#F1F5F9'
  border-subtle: '#E2E8F0'
  border-hover: '#CBD5E1'
  text-primary: '#0F172A'
  text-secondary: '#64748B'
  text-muted: '#94A3B8'
typography:
  display-hero:
    fontFamily: Outfit
    fontSize: 52px
    fontWeight: '700'
    lineHeight: 60px
    letterSpacing: -0.03em
  display-hero-mobile:
    fontFamily: Outfit
    fontSize: 34px
    fontWeight: '700'
    lineHeight: 42px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Outfit
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Outfit
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Outfit
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
    letterSpacing: -0.005em
  body-md:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  code-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: -0.01em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

### Personality & Values
The brand conveys precision, engineering excellence, velocity, and uncompromised clarity. It treats short links not as simple utilities, but as high-velocity telemetry channels. The visual atmosphere merges hyper-clean SaaS ergonomics with a luminous, high-tech presence inspired by developer-first design studios like kiro.dev.

### Visual Architecture
The design style integrates **Clean Modern SaaS** with **Delicate Ambient Glassmorphism**:
- **Backgrounds**: Ultra-clean, luminescent `#F8FAFC` (Slate-50) canvas textured with micro-dot grid matrices (`radial-gradient(circle, #CBD5E1 1px, transparent 1px)` with 24px spacing) and soft violet-indigo ambient radial glows anchored at header corners.
- **Surfaces**: Crisp white cards (`#FFFFFF`) framed by micro-hairline borders (`#E2E8F0` / `rgba(226, 232, 240, 0.8)`).
- **Accents**: Chromatic intensity focuses on a vibrant Indigo-to-Violet gradient range (`#4F46E5` to `#7C3AED`) balanced by an energetic Emerald (`#10B981`) accent for active metrics, live status toggles, and copy confirmations.
- **Interaction Feel**: Effortless, fast, precise, and reassuringly tactile.

## Colors

### Hierarchy & Role Allocation
- **Primary (`#4F46E5`) & Secondary (`#7C3AED`)**: Form the signature brand gradient (`linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)`). Applied strictly to key calls-to-action (e.g., Shorten URL CTA), active navigation highlights, metric trajectory charts, and branded hero badges.
- **Tertiary / Success (`#10B981`)**: Dedicated to positive telemetry signals — link active pills, clipboard copy confirmation states, positive delta metrics (`+12.4%`), and verified destination indicators.
- **Warning (`#F59E0B`) & Danger (`#F43F5E`)**: Reserved for link expiration notices, irreversible link destruction modals, and real-time form validation alerts.
- **Neutrals**:
  - Main Canvas: `#F8FAFC`
  - High-Elevation Card Fill: `#FFFFFF`
  - Subtle Secondary Container: `#F1F5F9`
  - Primary Typography: `#0F172A` (Slate-900)
  - Secondary / Supporting Typography: `#64748B` (Slate-500)
  - Structural Border: `#E2E8F0` (Slate-200)

### Ambient Accents & Glow Mesh
Use delicate radial backdrop gradients to produce a subtle halo behind key action hubs:
`radial-gradient(circle at 50% -20%, rgba(124, 58, 237, 0.08) 0%, rgba(79, 70, 229, 0.03) 40%, transparent 70%)`.

## Typography

### Structural Pairings
- **Display & Headlines (`Outfit`)**: Geometric yet sculpted with subtle organic warmth, delivering high authority in marketing hero bands, card title headers, and metric KPI summaries. Tight tracking (`-0.02em` to `-0.03em`) creates a modern SaaS presence.
- **Body, UI, & Data Columns (`Inter`)**: Engineered for screen clarity and tabular stability. Used across long URL truncate strings, form labels, tooltips, analytics tables, and system badges.

### Short Link Typography Rule
Short URLs (e.g., `snaplink.io/ngan-cv`) use `code-sm` or `body-md` set to Medium weight (`500` or `600`) with high-contrast text (`#0F172A`) and subtle hover tints to signal immediate clickability.

## Layout & Spacing

### Layout Philosophy & Grid System
The design uses a responsive 12-column grid capped at a maximum width of `1280px` (`max-w-7xl`) for dashboards and `960px` (`max-w-4xl`) for the core URL conversion focus zone.
- **Desktop (>= 1024px)**: 12 columns, `1.5rem` (`24px`) gutters, `2rem` outer page canvas margin.
- **Tablet (768px - 1023px)**: 8 columns, `1.25rem` (`20px`) gutters, `1.5rem` canvas margin.
- **Mobile (< 768px)**: 4 columns, `1rem` (`16px`) gutters, `1rem` outer canvas padding. Stack horizontal toolbars into vertical action rows.

### Spacing Rhythm
- `space-xs` (4px): Icon-to-text gaps inside compact buttons and status badges.
- `space-sm` (8px): Form input inner paddings, table cell vertical paddings, dropdown menu items.
- `space-md` (16px): Standard card inner padding, input field height offsets, accordion headers.
- `space-lg` (24px): Card boundaries, dashboard KPI card padding, metric chart modules.
- `space-xl` (40px): Section vertical cadence and dashboard layout block separation.

## Elevation & Depth

### Ambient Illumination Strategy
Elevation in this system avoids heavy, mud-like drop shadows. Instead, it relies on crisp white planar surfaces resting slightly above a patterned background, separated by hair-thin outlines and diffused, low-opacity ambient drop shadows tinted with faint indigo.

### Surface Tiers
- **Tier 0 (Canvas)**: `#F8FAFC` base surface enriched by a faint dot pattern (`rgba(148, 163, 184, 0.25)` dots spaced 24px).
- **Tier 1 (Cards & Data Tables)**: Solid `#FFFFFF`, border `1px solid #E2E8F0`, shadow `0 1px 3px 0 rgba(15, 23, 42, 0.04), 0 1px 2px -1px rgba(15, 23, 42, 0.02)`.
- **Tier 2 (Hero URL Card & Active Modals)**: Translucent glass or elevated solid card with `backdrop-filter: blur(12px)`, background `rgba(255, 255, 255, 0.95)`, border `1px solid rgba(226, 232, 240, 0.9)`, shadow `0 10px 25px -5px rgba(79, 70, 229, 0.06), 0 8px 10px -6px rgba(15, 23, 42, 0.03)`.
- **Tier 3 (Floating Popovers, Dropdowns, QR Tooltips)**: `#FFFFFF`, border `1px solid #E2E8F0`, shadow `0 20px 25px -5px rgba(15, 23, 42, 0.08), 0 10px 10px -5px rgba(15, 23, 42, 0.03)`.

## Shapes

### Corner Radii
The geometry utilizes standard **Rounded (Level 2)** geometry to balance professional software precision with welcoming consumer SaaS usability:
- **Base (0.5rem / 8px)**: Standard inputs, data table row selections, compact action buttons (`Copy`, `QR`, `Delete`), dropdown menus.
- **Large (1rem / 16px)**: Metric overview cards, Hero URL conversion card, QR Code showcase modal containers.
- **Extra Large (1.5rem / 24px)**: Outer marketing hero wrapper segments and modal sheet dialogues.
- **Full Pill (`9999px`)**: Status badges (`Active`, `Inactive`), avatar frame containers, and filter chips.

## Components

### 1. Primary Action Button
- **Structure**: High-contrast, interactive gradient (`linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)`).
- **Typography & Icon**: White text, `label-lg` (14px/600), inline Lucide React icon spaced by 8px.
- **States**:
  - Hover: Opacity brightness increased (`filter: brightness(1.06)`), subtle upward lift (`transform: translateY(-1px)`), glow shadow `0 4px 14px 0 rgba(124, 58, 237, 0.35)`.
  - Active: Scale micro-press (`transform: scale(0.98)`).
  - Loading: Label concealed, centered 16px spinner rendered in crisp white.

### 2. URL Input Bar (Hero Component)
- **Structure**: Single-row merged input capsule or clean multi-element card with a subtle `1px solid #E2E8F0` border.
- **Interior**: Leading Lucide `Link2` icon in `#94A3B8`, high-legibility auto-focus text input field, and direct inline Submit button.
- **States**: Focused state triggers an indigo outer ring: `ring-2 ring-indigo-500/20 border-indigo-600`.

### 3. Result & Copy Card
- **Structure**: High-tier elevated white card (`#FFFFFF`) with a dynamic emerald highlight border on generation.
- **Layout**: Left section highlights the newly generated short link in bold `Outfit` (`#0F172A`) above the truncated original URL in `#64748B`. Right section houses quick action buttons:
  - Copy Button: Triggers a state swap to `Emerald-500` background or icon, switching `Copy` to `Check` with a 2-second persistence toast.
  - QR Button: Triggers a clean modal preview.
  - Analytics Button: Direct deep-link redirect to `/analytics/:id`.

### 4. Input Fields & Form Controls
- **Structure**: Clean `#FFFFFF` fill, `0.5rem` radius, `1px solid #E2E8F0` border, `14px` font size.
- **Affixes**: Custom Alias displays a fixed neutral prefix pill (`snaplink.io/`) locked to the left side with a divider line.

### 5. Status Badges & Chips
- **Active / Success**: `bg-emerald-50 text-emerald-700 border border-emerald-200/60`, with a pulsing 6px emerald status dot.
- **Warning / Expiring**: `bg-amber-50 text-amber-700 border border-amber-200/60`.
- **Danger / Inactive**: `bg-rose-50 text-rose-700 border border-rose-200/60`.

### 6. Data Table (UrlTable)
- **Structure**: Flat container with zero-margin inner borders.
- **Header**: `#F8FAFC`, uppercase `11px` bold text in `#64748B`, height 40px.
- **Rows**: `#FFFFFF` background with `1px solid #F1F5F9` bottom divider, transitioning smoothly to `#F8FAFC` on hover. Original URLs truncate with a max width and reveal an instant tooltip on hover.

### 7. Metric KPI Cards
- **Structure**: Pure white background, `16px` padding, `1rem` corner radius, micro-border in `#E2E8F0`.
- **Content**: Subtle title in `#64748B` with a companion icon in a tinted purple pill container (`#EDE9FE`), metric value in 28px bold `Outfit`, and trend indicator in `12px` emerald/rose badge typography.