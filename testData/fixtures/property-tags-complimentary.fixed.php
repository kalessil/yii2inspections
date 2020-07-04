<?php

namespace yii\base;

class Object {}

/**
 * @property void $author
 * @property $book
 */
class ClassNeedsProperties extends Object {
    private $_author;
    private $_book;

    public function getAuthor()        {}
    public function setAuthor($author) {}

    public function getBook()          {}
    public function setBook()          {}
}

class ClassNeedsNoPropertiesHasStatic1 extends Object {
    public function getAuthor()               {}
    public static function setAuthor($author) {}
}
class ClassNeedsNoPropertiesHasStatic2 extends Object {
    public static function getAuthor()        {}
    public function setAuthor($author)        {}
}

class ClassNeedsNoPropertiesHasOnlyGet extends Object {
    public function getAuthor()               {}
}
class ClassNeedsNoPropertiesHasOnlySet extends Object {
    public function setAuthor($author)        {}
}
