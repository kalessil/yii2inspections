<?php

namespace yii\base;

class Object {}

class ClassNeedsNoProperties extends Object { // <-- all false-positives
    static public function setAuthor()        {} // <- static
    public function setPrice()                {} // <- missing argument
    public function getBook($default)         {} // <- extra argument
    static public function getQuantity()      {} // <- static
}

/**
 *
 * @property-write mixed $author
 * @property-read void $book
 */
class ClassNeedsProperties1
    extends Object
{
    public function setAuthor($author)        {}
    public function getBook()                 {}
}

/**
 *
 * @property-write mixed $author2
 * @property-write mixed $author1
 */
class ClassNeedsProperties2
    extends Object
{
    public function setAuthor1($author)        {}
    static public function getAuthor1()        {} // <-- static

    public function setAuthor2($author)        {}
    public function getAuthor2($default)       {} // <- extra arguments
}

/**
 *
 * @property-read void $author2
 * @property-read void $author1
 */
class ClassNeedsProperties3
    extends Object
{
    public function getAuthor1()               {}
    static public function setAuthor1($author) {} // <-- static

    public function getAuthor2()               {}
    public function setAuthor2()               {} // <- no arguments
}