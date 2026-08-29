<?php

    CraftCms\Cms\t('Used message from PHP (t, regular category)', [], 'unused');
    CraftCms\Cms\t('Used message from PHP (t, slashed category)', [], 'com/company/unused');
    Yii::t('unused',             'Used message from PHP (t, regular category)');
    Yii::t('com/company/unused', 'Used message from PHP (t, slashed category)');
    $view->registerTranslations('unused', ['Used message from PHP (registerTranslations)']);