<?php

function stringsDiscoveryWrapper($view) {
    $nonExistingMessage        = 'Non-existing message';
    $nonAsciiCharactersMessage = 'Message with non-ASCII characters (Властелин колец)';
    $messageWithInjectedToken  = "Message with injected $token";

    Craft::t('app', <weak_warning descr="[Yii2] The message doesn't have any translations or doesn't belong to the category">$nonExistingMessage</weak_warning>);

    Yii::t('app', <weak_warning descr="[Yii2] The message doesn't have any translations or doesn't belong to the category">$nonExistingMessage</weak_warning>);
    Yii::t('app', <weak_warning descr="[Yii2] Usage of any characters out of ASCII range is not recommended.">$nonAsciiCharactersMessage</weak_warning>);
    Yii::t('app', <weak_warning descr="[Yii2] Parametrized message should be used instead, e.g.: Yii::t('app', 'Token is: {token}', ['token' => 'value'])">$messageWithInjectedToken</weak_warning>);

    Translation::prep('app', <weak_warning descr="[Yii2] The message doesn't have any translations or doesn't belong to the category">$nonExistingMessage</weak_warning>);
    Translation::prep('app', <weak_warning descr="[Yii2] Usage of any characters out of ASCII range is not recommended.">$nonAsciiCharactersMessage</weak_warning>);
    Translation::prep('app', <weak_warning descr="[Yii2] Parametrized message should be used instead, e.g.: Yii::t('app', 'Token is: {token}', ['token' => 'value'])">$messageWithInjectedToken</weak_warning>);

    $view->registerTranslations('app', [
        <weak_warning descr="[Yii2] The message doesn't have any translations or doesn't belong to the category">$nonExistingMessage</weak_warning>,
        <weak_warning descr="[Yii2] Usage of any characters out of ASCII range is not recommended.">$nonAsciiCharactersMessage</weak_warning>,
        <weak_warning descr="[Yii2] Parametrized message should be used instead, e.g.: Yii::t('app', 'Token is: {token}', ['token' => 'value'])">$messageWithInjectedToken</weak_warning>,
    ]);

    /* false-positives */
    Yii::t('app',             'Existing translation');
    Yii::t('com/company/app', 'Existing translation');
    Yii::t('app', 'With "');
    Yii::t('app', "With \"");
    Yii::t('app', "With '");
    Yii::t('app', 'With \'');
    $view->registerTranslations('app', ['Existing translation']);
}
