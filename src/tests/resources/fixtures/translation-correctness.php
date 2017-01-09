<?php

function stringsDiscoveryWrapper() {
    $nonExistingMessage        = 'Non-existing message';
    $nonAsciiCharactersMessage = 'Message with non-ASCII characters (Властелин колец)';
    $messageWithInjectedToken  = "Message with injected $token";

    Craft::t('app', <weak_warning descr="The message doesn't have any translations or doesn't belong to the category">$nonExistingMessage</weak_warning>);
    Yii::t('app', <weak_warning descr="The message doesn't have any translations or doesn't belong to the category">$nonExistingMessage</weak_warning>);
    Yii::t('app', <weak_warning descr="Usage of any characters out of ASCII range is not recommended.">$nonAsciiCharactersMessage</weak_warning>);
    Yii::t('app', <weak_warning descr="Parametrized message should be used instead, e.g.: Yii::t('app', 'Token is: {token}', ['token' => 'value'])">$messageWithInjectedToken</weak_warning>);

    /* false-positives */
    Yii::t('app', 'Existing translation');
    Yii::t('app', 'With "');
    Yii::t('app', "With '");
}
