[![Version](http://phpstorm.espend.de/badge/9400/version)](https://plugins.jetbrains.com/plugin/9400)
[![Downloads](http://phpstorm.espend.de/badge/9400/downloads)](https://plugins.jetbrains.com/plugin/9400)
[![Downloads last month](http://phpstorm.espend.de/badge/9400/last-month)](https://plugins.jetbrains.com/plugin/9400)


Yii2 Inspections for PhpStorm/IdeaUltimate
---
Supports Yii 2+ and Craft CMS 3+, see list of inspections below.

Installation
---

The plugin is not yet published at plugins.jetbrains.com, though installation process include 4 steps:
- Install the *Twig support* plugin (to get twig files indexing)
- Download **yii2inspections.jar** from the repository
- In IDE: File -> Settings -> Plugins, below plugins list click *Install plugin from disk* and select the file
- In IDE: File -> Invalidate Caches / Restart. Click the *Invalidate and restart* button.

Inspections
---
- **Missing @property annotations**, checks property feature annotations (*has QF*, *has Settings*)
- **Translation message correctness**, checks if messages have translations and follows best practices (*has Settings*)
- **Missing translations**, checks translation files for missing translations (*has QF*)
- **Unused translations**, checks translation files for unused translations (*has QF*)