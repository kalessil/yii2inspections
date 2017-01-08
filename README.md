Yii2 Inspections for PhpStorm/IdeaUltimate
---
Supports Yii 2+ and Craft CMS 3+, see list of inspections below.

Installation
---

The plugin is not yet published at plugins.jetbrains.com, though installation process include 2 steps:
- Download **yii2inspections.jar** from the repository
- In IDE: File -> Settings -> Plugins, below plugins list click *Install plugin from disk* and select the file

IDE will suggest restarting, do so. Now inspections and Quick-Fixes are available.

Inspections
---
- **Missing @property annotations**, checks property feature annotations (*ready*, *has QF*)
- **Message doesn't have any translations**, checks if messages have translations or contains non-ASCII characters (*ready*)
- **Missing translations**, checks translation files for missing translations (*ready*, *has QF*)
- **Unused translations**, checks translation files for unused translations (*ready*, *has QF*)