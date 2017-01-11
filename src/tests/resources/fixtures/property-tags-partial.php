<?php

namespace yii\base;

class Object {}

class ClassNeedsNoProperties extends Object { // <-- all false-positives
    static public function setAuthor()        {}
    public function setPrice($price)          {}
    public function getBook($default)         {}
    static public function getQuantity()      {}
}

class <weak_warning descr="'author', 'book': properties needs to be annotated">ClassNeedsProperties1</weak_warning>
    extends Object
{
    public function setAuthor($author)        {}
    public function getBook()                 {}
}
class <weak_warning descr="'author1', 'author2': properties needs to be annotated">ClassNeedsProperties2</weak_warning>
    extends Object
{
    public function setAuthor1($author)        {}
    static public function getAuthor1()        {} // <-- static

    public function setAuthor2($author)        {}
    public function getAuthor2($default)       {} // <- extra arguments
}
class <weak_warning descr="'author1', 'author2': properties needs to be annotated">ClassNeedsProperties3</weak_warning>
    extends Object
{
    public function getAuthor1()               {}
    static public function setAuthor1($author) {} // <-- static

    public function getAuthor2()               {}
    public function setAuthor2()               {} // <- no arguments
}