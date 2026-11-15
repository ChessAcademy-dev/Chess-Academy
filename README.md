# Chess Academy – Ultimate Android Chess Ecosystem

**Chess Academy** ist eine moderne, hochmoderne und umfassende Android-Schachplattform, entwickelt von Grund auf mit Kotlin, Jetpack Compose und Material 3. Die Anwendung verbindet Schachlernen, Training, freies Spielen, tiefgehende Analyse und vollständige visuelle Individualisierung in einem eleganten, performanten Ökosystem.

---

## Hauptfunktionen

- **Learn (Academy):** Umfassender Lehrplan in sieben Bereichen (Grundlagen, Taktik, Strategie, Eröffnungen, Mittelspiel, Endspiele und ein interaktiver Gedankenbaum zur Variantenberechnung).
- **Train (Training & Coach):** Puzzle Rush, Blindschachtraining, Endspielsimulatoren, ein adaptiver KI-Coach mit automatischer Schwächenerkennung und intelligentes Spaced Repetition.
- **Play (Spielmodi & Bot-Persönlichkeiten):** Lokales 2-Spieler-Spiel, Computergegner mit 7 kalibrierten Spielstilen (von aggressiv bis positionell), Schachuhr und integrierter Stellungseditor.
- **Analyze (Analyse & Datenbank):** Partienanalyse mit Stockfish (UCI), Zugklassifizierung (Hervorragend, Fehler etc.), interaktiver Variantenbaum, Schachbrett-Fehler-Heatmap und PGN-Import/Export.
- **Customize (Design & Figurenbibliothek):** 30+ Figurenfamilien (mit speziellem Kontrastsystem für weiße Figuren auf hellen Feldern), anpassbare Brettthemes, Bretteditor und selektives Glass-Design.

---

## Technische Architektur

Das Projekt ist als **Multi-Modul-Architektur** aufgebaut, um maximale Testbarkeit, Übersichtlichkeit und Wartbarkeit zu gewährleisten:

```text
ChessAcademy/
├── app/                         # Einstiegspunkt, Hilt DI & Navigation
├── core/                        # Core-Module (Common, DesignSystem, DataStore, Database, Audio, Network)
├── chess/                       # Schach-Kern (Rules, PGN, Engine/Stockfish, Board)
└── feature/                     # Feature-Module (Onboarding, Home, Play, Academy, Training, Coach, Analyze, Profile, Settings)
