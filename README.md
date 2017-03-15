[![Version](http://phpstorm.espend.de/badge/9400/version)](https://plugins.jetbrains.com/plugin/9400)
[![Downloads](http://phpstorm.espend.de/badge/9400/downloads)](https://plugins.jetbrains.com/plugin/9400)
[![Downloads last month](http://phpstorm.espend.de/badge/9400/last-month)](https://plugins.jetbrains.com/plugin/9400)


Yii2 Inspections for PhpStorm/IdeaUltimate
---
Supports Yii 2+ and Craft CMS 3+, see list of inspections below.
The plugin has been funded by the [Pixel and Tonic](https://github.com/pixelandtonic), the company behind Craft CMS. Our special thank  you to [Brandon Kelly](https://github.com/brandonkelly) and [Alexander Makarov](https://github.com/samdark), who gave us valuable feedback on early development stages =)

Installation
---

Regular installation process includes 3 steps:
- Navigate to *File -> Settings -> Plugins* and click *Browse Repositories*. New window will popup listing available plugins. 
- Type *Yii2 Inspections* into the top search field and install the plugin. Same for *Twig support* plugin (required dependency).  
- Click *OK* buttons on both open windows, restart as IDE suggests. That's it.

Inspections
---
- **Missing @property annotations**, checks property feature annotations (*has QF*, *has Settings*)
- **Translation message correctness**, checks if messages have translations and follows best practices (*has Settings*)
- **Missing translations**, checks translation files for missing translations (*has QF*)
- **Unused translations**, checks translation files for unused translations (*has QF*)
