<?php

namespace yii\base;

class Object {}

class ClassNeedsProperties extends Object {
    private $_author;

    public function getAuthor()        {}
    public function setAuthor($author) {}
}

class ClassNeedsNoPropertiesHasStatic extends Object {
    public static function getAuthor()        {}
    public static function setAuthor($author) {}
}

class ClassNeedsNoPropertiesHasOnlyGet extends Object {
    public function getAuthor() {}
}
