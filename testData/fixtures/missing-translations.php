<?php

    Yii::t('missing', 'Used message in PHP');

    CraftCms\Cms\t('Missing translation from PHP (t)', [], 'missing');
    Yii::t('missing', 'Missing translation from PHP (t)');
    $view->registerTranslations('missing', ['Missing translation from PHP (registerTranslations)', '']);