[![Version](http://phpstorm.espend.de/badge/9400/version)](https://plugins.jetbrains.com/plugin/9400)
[![Downloads](http://phpstorm.espend.de/badge/9400/downloads)](https://plugins.jetbrains.com/plugin/9400)
[![Downloads last month](http://phpstorm.espend.de/badge/9400/last-month)](https://plugins.jetbrains.com/plugin/9400)


Yii2 Inspections for PhpStorm/IdeaUltimate
---
Supports Yii 2+ and Craft CMS 3+, see list of inspections below.

Installation
---

Regular installation process includes 3 steps:
- Install the *Twig support* plugin
- Navigate to *File -> Settings -> Plugins* and click *Browse Repositories*. New window will popup listing available plugins. 
- Type *Yii2 Inspections* into the top search field and install the plugin. Click *OK* buttons on both open windows.

Inspections
---
- **Missing @property annotations**, checks property feature annotations (*has QF*, *has Settings*)
- **Translation message correctness**, checks if messages have translations and follows best practices (*has Settings*)
- **Missing translations**, checks translation files for missing translations (*has QF*)
- **Unused translations**, checks translation files for unused translations (*has QF*)