<?php

namespace yii\base;

class Object {}

class ClassNeedsProperties extends Object {
    private $_author;

    public function getAuthor()        {}
    public function setAuthor($author) {}
}

class ClassNeedsNoProperties extends Object {
}